package boi.headless.delivery.memorandum.util;

import boi.headless.delivery.memorandum.configuration.ConfigurationUtil;
import boi.headless.delivery.memorandum.configuration.PAIMSConfiguration;
import com.liferay.document.library.kernel.service.DLAppService;
import com.liferay.document.library.util.DLURLHelperUtil;
import com.liferay.object.model.ObjectEntry;
import com.liferay.object.model.ObjectField;
import com.liferay.object.service.ObjectEntryLocalServiceUtil;
import com.liferay.object.service.ObjectFieldLocalServiceUtil;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Organization;
import com.liferay.portal.kernel.model.Role;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.repository.model.FileEntry;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.MapUtil;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.vulcan.multipart.BinaryFile;
import com.liferay.portal.vulcan.multipart.MultipartBody;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;

public class PAIMSCommon {
   private static final String PARAM_FROM_DATE = "fromDate";
   private static final String PARAM_TO_DATE = "toDate";
   private static final String PARAM_STATUS = "status";
   private HttpServletRequest contextHttpServletRequest;
   private PAIMSConfiguration paimsConfiguration;
   private User user;
   public boolean isBranchMaker;
   public boolean isBranchChecker;
   public boolean isHOChecker;
   public boolean isPAIMS;
   private String branchName = "";
   private String zoneName = "";
   private String nbgName = "";
   private String hoName = "";
   Log _log = LogFactoryUtil.getLog(this.getClass());

   public PAIMSCommon(HttpServletRequest contextHttpServletRequest, User user, PAIMSConfiguration configuration) {
      this.contextHttpServletRequest = contextHttpServletRequest;
      this.user = user;
      this.loadOrganizationHierarchy(user);
      this.loadUserRoles(user);
      this.paimsConfiguration = configuration;
   }

   public PAIMSCommon(HttpServletRequest contextHttpServletRequest, User user, PAIMSConfiguration configuration, String def) {
      this.contextHttpServletRequest = contextHttpServletRequest;
      this.user = user;
      this.loadUserRoles(user);
      this.paimsConfiguration = configuration;
   }

   public PAIMSCommon() {
   }

   public String getUserFilterQuery() {
      List<String> queryList = new LinkedList();
      SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
      Date toDate = ParamUtil.getDate(this.contextHttpServletRequest, "toDate", sdf, (Date)null);
      Date fromDate = ParamUtil.getDate(this.contextHttpServletRequest, "fromDate", sdf, (Date)null);
      String status = ParamUtil.getString(this.contextHttpServletRequest, "status", (String)null);
      this.addHierarchyFilter(queryList);
      this.addStatusFilter(queryList, status);
      this.addDateFilter(queryList, fromDate, toDate, sdf);
      String queryResult = (String)queryList.stream().filter(Validator::isNotNull).filter((x) -> !Validator.isBlank(x)).collect(Collectors.joining(" and "));
      if (this.containsHierarchyFilters(queryResult)) {
         this._log.debug("Final queryResult:  " + queryResult);
         return queryResult;
      } else {
         this._log.debug("Final queryResult:  Blank");
         return "";
      }
   }

   private void addStatusFilter(List<String> queryList, String status) {
      if (Validator.isNotNull(status)) {
         int var10001 = this.getStatusValue(status);
         queryList.add("currST eq " + var10001);
      }

      if (this.isBranchChecker) {
         queryList.add("currST ne 0");
      }

   }

   private int getStatusValue(String status) {
      switch (status.trim().toLowerCase()) {
         case "draft":
            return 0;
         case "pending with branch maker":
            return -1;
         case "pending at branch checker":
            return 1;
         case "approved":
            return 2;
         default:
            return 10;
      }
   }

   private boolean containsHierarchyFilters(String queryResult) {
      return queryResult.contains("branch") || queryResult.contains("nbg") || queryResult.contains("zone");
   }

   private void addHierarchyFilter(List<String> queryList) {
      if (!Validator.isBlank(this.branchName)) {
         queryList.add("branch eq '" + this.branchName + "'");
      } else if (!Validator.isBlank(this.zoneName)) {
         queryList.add("zone eq '" + this.zoneName + "'");
      } else {
         if (!Validator.isBlank(this.nbgName)) {
            queryList.add("nbg eq '" + this.nbgName + "'");
         }

      }
   }

   private void addDateFilter(List<String> queryList, Date fromDate, Date toDate, SimpleDateFormat sdf) {
      if (Validator.isNotNull(fromDate) && Validator.isNotNull(toDate)) {
         Map<String, String> isoRange = this.convertToISO8601Range(sdf.format(fromDate), sdf.format(toDate));
         String var10000 = (String)isoRange.get("fromDate");
         String dateFilter = "dateModified gt " + var10000 + " and dateModified lt " + (String)isoRange.get("toDate");
         this._log.debug("Request required dateFilter  " + dateFilter);
         queryList.add(dateFilter);
      }

   }

   public JSONObject getPAIMSDetailWithActions(long entryId) throws Exception {
      JSONObject response = JSONFactoryUtil.createJSONObject();

      try {
         ObjectEntry entry = ObjectEntryLocalServiceUtil.fetchObjectEntry(entryId);
         if (entry == null) {
            response.put("errors", "PAIMS entry not found for ID: " + entryId);
            return response;
         }

         if (!this.validateId(entry)) {
            response.put("errors", "Access denied. This record belongs to a different branch.");
            return response;
         }

         JSONObject paimsJson = JSONFactoryUtil.createJSONObject();
         Map<String, Serializable> entryValues = entry.getValues();
         entryValues.forEach((key, val) -> {
            if (!"c_paimsConsentId".equals(key) && !"currST".equals(key) && !"paimsStatus".equals(key)) {
               paimsJson.put(key, val != null ? String.valueOf(val) : "");
            }
         });
         paimsJson.put("entryId", entryId);
         this.addCustomActions(paimsJson, entry);
         response.put("paimsData", paimsJson);
      } catch (Exception e) {
         response.put("errors", e.getMessage());
      }

      return response;
   }

   private boolean validateId(ObjectEntry entry) {
      String branch = (String)entry.getValues().get("branch");
      if (!this.isBranchMaker && !this.isBranchChecker) {
         if (this.isHOChecker) {
            return true;
         }
      } else if (this.branchName.equalsIgnoreCase(branch)) {
         return true;
      }

      return false;
   }

   public JSONObject approveOrRejectPAIMS(DLAppService dlAppService, MultipartBody multipartBody) {
      JSONObject response = JSONFactoryUtil.createJSONObject();

      try {
         long objectEntryId = ParamUtil.getLong(this.contextHttpServletRequest, "entryId", 0L);
         String action = ParamUtil.getString(this.contextHttpServletRequest, "action", "");
         String description = ParamUtil.getString(this.contextHttpServletRequest, "description", "");
         ObjectEntry entry = ObjectEntryLocalServiceUtil.fetchObjectEntry(objectEntryId);
         if (entry == null) {
            return response.put("status", "error").put("message", "Object entry not found");
         }

         int currST = (Integer)entry.getValues().get("currST");
         String paimsStatus = (String)entry.getValues().get("paimsStatus");
         String currentBranch = (String)entry.getValues().get("branch");
         if (!currentBranch.equalsIgnoreCase(this.branchName)) {
            return response.put("status", "error").put("message", "User not authorized");
         }

         String fileURL = "";
         BinaryFile binaryFile = multipartBody.getBinaryFile("file");
         if (binaryFile != null) {
            JSONObject uploadErrors = JSONFactoryUtil.createJSONObject();
            FileUtil fileUtil = new FileUtil(multipartBody, "PAIMS");
            long uploadFolderId = this.paimsConfiguration.PAIMSDefaultFolderId();
            Map<String, BinaryFile> validateFile = fileUtil.validateFile(uploadErrors);
            if (MapUtil.isEmpty(validateFile)) {
               return uploadErrors;
            }

            List<FileEntry> workflowFileEntries = fileUtil.uploadFile(uploadFolderId, uploadErrors, (String)null, validateFile);
            FileEntry workflowFileEntry = workflowFileEntries != null && !workflowFileEntries.isEmpty() ? (FileEntry)workflowFileEntries.get(0) : null;
            if (uploadErrors.has("errors")) {
               return uploadErrors;
            }

            if (workflowFileEntry != null) {
               ThemeDisplay themeDisplay = (ThemeDisplay)this.contextHttpServletRequest.getAttribute("LIFERAY_SHARED_THEME_DISPLAY");
               fileURL = DLURLHelperUtil.getDownloadURL(workflowFileEntry, workflowFileEntry.getFileVersion(), themeDisplay, "");
            }
         }

         String remarks = ParamUtil.getString(this.contextHttpServletRequest, "remarks", "");
         Map<String, Serializable> values = entry.getValues();
         if (action.equalsIgnoreCase("Approve") && this.isBranchChecker) {
            values.put("currST", 2);
            values.put("paimsSTatus", "approved");
         } else if (action.equalsIgnoreCase("Reject") && this.isBranchChecker) {
            values.put("currST", -1);
            values.put("paimsSTatus", "pending with branch maker");
         } else {
            if (!action.equalsIgnoreCase("Resubmit") || !this.isBranchMaker) {
               return response.put("status", "error").put("message", "Invalid action (approve/reject required)");
            }

            values.put("currST", 1);
            values.put("paimsSTatus", "pending at branch checker");
         }

         values.put("description", description);
         entry.setValues(values);
         ServiceContext serviceContext = new ServiceContext();
         serviceContext.setCompanyId(this.user.getCompanyId());
         serviceContext.setUserId(entry.getUserId());
         serviceContext.setWorkflowAction(1);
         ObjectEntryLocalServiceUtil.addOrUpdateObjectEntry(entry.getExternalReferenceCode(), entry.getUserId(), 0L, entry.getObjectDefinitionId(), values, serviceContext);
         this.createWorkflowEntry(entry.getObjectEntryId(), action, remarks, fileURL);
         response.put("status", "success");
         response.put("action", action);
         response.put("remarks", remarks);
         response.put("entryId", objectEntryId);
      } catch (Exception e) {
         response.put("status", "error").put("message", e.getMessage());
         e.printStackTrace();
      }

      return response;
   }

   private ObjectEntry createWorkflowEntry(long entryId, String logStatus, String remarks, String fileURL) {
      try {
         ServiceContext serviceContext = new ServiceContext();
         serviceContext.setCompanyId(this.user.getCompanyId());
         serviceContext.setUserId(this.user.getUserId());
         Map<String, Serializable> workflowLogs = new HashMap();
         workflowLogs.put("paimsId", entryId);
         workflowLogs.put("customAction", logStatus);
         workflowLogs.put("actionBy", this.user.getScreenName());
         workflowLogs.put("createdDate", new Date());
         workflowLogs.put("remarks", remarks);
         if (Validator.isNotNull(fileURL)) {
            workflowLogs.put("file", fileURL);
         }

         ObjectEntry workflowEntry = ObjectEntryLocalServiceUtil.addOrUpdateObjectEntry((String)null, this.user.getUserId(), 0L, ConfigurationUtil.getMemoWorkflowObjectDefinitionId(), workflowLogs, serviceContext);
         return workflowEntry;
      } catch (Exception var9) {
         return null;
      }
   }

   private void addCustomActions(JSONObject jsonObject, ObjectEntry entry) {
      int curStatus = (Integer)entry.getValues().get("currST");
      String paimsStatus = (String)entry.getValues().get("paimsStatus");
      if (this.isBranchMaker && curStatus == -1 && "pending with branch maker".equalsIgnoreCase(paimsStatus)) {
         JSONArray buttons = JSONFactoryUtil.createJSONArray();
         buttons.put("Resubmit");
         jsonObject.put("actions", buttons);
      } else if (this.isBranchChecker && curStatus == 1 && "pending at branch checker".equalsIgnoreCase(paimsStatus)) {
         JSONArray buttons = JSONFactoryUtil.createJSONArray();
         buttons.put("Approve");
         buttons.put("Reject");
         jsonObject.put("actions", buttons);
      }

   }

   public Map<String, String> convertToISO8601Range(String fromDateStr, String toDateStr) {
      DateTimeFormatter isoFormatter = DateTimeFormatter.ISO_INSTANT;
      LocalDate fromDate = LocalDate.parse(fromDateStr);
      LocalDate toDate = LocalDate.parse(toDateStr);
      ZonedDateTime fromDateTime = fromDate.atStartOfDay(ZoneOffset.UTC);
      ZonedDateTime toDateTime = toDate.atTime(23, 59, 59).atZone(ZoneOffset.UTC);
      Map<String, String> result = new HashMap();
      result.put("fromDate", isoFormatter.format(fromDateTime));
      result.put("toDate", isoFormatter.format(toDateTime));
      return result;
   }

   public ObjectEntry saveAsDraft(HttpServletRequest request, Map<String, Serializable> values, long objectDefinitionId, String referenceCode) {
      try {
         Map<String, Serializable> objectData = this.getObjectData(request, values, objectDefinitionId);
         return this.updateData(objectData, 2, objectDefinitionId, "Save as Draft", referenceCode);
      } catch (Exception e) {
         e.printStackTrace();
         return null;
      }
   }

   public ObjectEntry saveAsExport(HttpServletRequest request, Map<String, Serializable> values, long objectDefinitionId, String referenceCode) {
      try {
         Map<String, Serializable> objectData = this.getObjectData(request, values, objectDefinitionId);
         return this.updateData(objectData, 1, objectDefinitionId, "Save as Export", referenceCode);
      } catch (Exception e) {
         e.printStackTrace();
         return null;
      }
   }

   public Map<String, Serializable> getObjectData(HttpServletRequest request, Map<String, Serializable> values, long objectDefinitionId) {
      try {
         for(ObjectField field : ObjectFieldLocalServiceUtil.getObjectFields(objectDefinitionId)) {
            String fieldName = field.getName();
            String dbType = field.getDBType();

            try {
               if (!"String".equalsIgnoreCase(dbType) && !"Clob".equalsIgnoreCase(dbType) && !"RichText".equalsIgnoreCase(dbType)) {
                  if ("Date".equalsIgnoreCase(dbType)) {
                     SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                     sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                     Date defaultValue = new Date();
                     Date value = ParamUtil.getDate(request, fieldName, sdf, defaultValue);
                     if (value.compareTo(defaultValue) == 0) {
                        value = (Date)this.getDefaultData(fieldName, values, (Serializable)null);
                     }

                     values.put(fieldName, value);
                  } else if ("Long".equalsIgnoreCase(dbType)) {
                     long value = ParamUtil.getLong(request, fieldName, 0L);
                     if (value == 0L) {
                        value = (Long)this.getDefaultData(fieldName, values, 0L);
                     }

                     values.put(fieldName, value);
                  } else if ("Integer".equalsIgnoreCase(dbType)) {
                     int value = ParamUtil.getInteger(request, fieldName, 0);
                     if (value == 0) {
                        value = (Integer)this.getDefaultData(fieldName, values, 0);
                     }

                     values.put(fieldName, value);
                  } else {
                     this._log.warn("Unsupported DBType for field " + fieldName + ": " + dbType);
                  }
               } else {
                  String value = ParamUtil.getString(request, fieldName, "");
                  if ("".equalsIgnoreCase(value)) {
                     value = (String)this.getDefaultData(fieldName, values, "");
                  }

                  values.put(fieldName, value);
               }
            } catch (Exception e) {
               this._log.error("Error processing field " + fieldName, e);
            }
         }
      } catch (Exception e) {
         this._log.error("Error retrieving object fields for objectDefinitionId " + objectDefinitionId, e);
      }

      return values;
   }

   private Serializable getDefaultData(String fieldName, Map<String, Serializable> values, Serializable serializable) {
      return values.containsKey(fieldName) ? (Serializable)values.get(fieldName) : serializable;
   }

   public ObjectEntry updateData(Map<String, Serializable> values, int status, long objectDefinitionId, String logStatus, String referenceCode) throws Exception {
      ServiceContext serviceContext = new ServiceContext();
      serviceContext.setCompanyId(this.user.getCompanyId());
      serviceContext.setUserId(this.user.getUserId());
      serviceContext.setWorkflowAction(status);
      if ("Save as Draft".equalsIgnoreCase(logStatus)) {
         values.put("currST", 0);
         values.put("paimsStatus", "draft");
      } else {
         values.put("currST", 1);
         values.put("paimsStatus", "pending at branch checker");
      }

      ObjectEntry objectEntry = ObjectEntryLocalServiceUtil.addOrUpdateObjectEntry(referenceCode, this.user.getUserId(), 0L, objectDefinitionId, values, serviceContext);
      if (Validator.isNotNull(objectEntry)) {
         this.createWorkflowEntry(objectEntry, logStatus, logStatus, (String)null);
      }

      return objectEntry;
   }

   private ObjectEntry createWorkflowEntry(ObjectEntry objectEntry, String logStatus, String remarks, String fileURL) {
      try {
         ServiceContext serviceContext = new ServiceContext();
         serviceContext.setCompanyId(this.user.getCompanyId());
         serviceContext.setUserId(this.user.getUserId());
         Map<String, Serializable> workflowLogs = new HashMap();
         workflowLogs.put("paimsId", objectEntry.getObjectEntryId());
         workflowLogs.put("customAction", logStatus);
         workflowLogs.put("actionBy", this.user.getScreenName());
         workflowLogs.put("createdDate", new Date());
         workflowLogs.put("remarks", remarks);
         if (Validator.isNotNull(fileURL)) {
            workflowLogs.put("paimsAttachment", fileURL);
         }

         ObjectEntry workflowEntry = ObjectEntryLocalServiceUtil.addOrUpdateObjectEntry((String)null, this.user.getUserId(), 0L, ConfigurationUtil.getMemoWorkflowObjectDefinitionId(), workflowLogs, serviceContext);
         Object var10001 = workflowEntry != null ? workflowEntry.getObjectEntryId() : "null";
         this._log.info("Workflow entry created/updated: " + var10001);
         return workflowEntry;
      } catch (Exception var8) {
         return null;
      }
   }

   private void loadOrganizationHierarchy(User user) {
      try {
         this.branchName = this.getOrganization(user.getOrganizations(), "BRANCH");
         this.zoneName = this.getOrganization(user.getOrganizations(), "ZONE");
         this.nbgName = this.getOrganization(user.getOrganizations(), "NBG");
         this.hoName = this.getOrganization(user.getOrganizations(), "Organization");
      } catch (Exception e) {
         e.printStackTrace();
      }

   }

   private Organization getOrganization(String type, Organization organization) {
      if (Validator.isNotNull(organization) && type.equalsIgnoreCase(organization.getType())) {
         return organization;
      } else {
         if (organization != null && organization.getParentOrganizationId() > 0L) {
            try {
               return this.getOrganization(type, organization.getParentOrganization());
            } catch (Exception e) {
               e.printStackTrace();
            }
         }

         return null;
      }
   }

   private String getOrganization(List<Organization> organizations, String type) {
      if (organizations == null) {
         return null;
      } else {
         String orgName = null;

         for(Organization org : organizations) {
            Organization match = this.getOrganization(type, org);
            if (match != null) {
               orgName = match.getName();
               break;
            }
         }

         return orgName;
      }
   }

   public long getObjectDefinitionId() {
      return this.paimsConfiguration.PAIMSObjectDefinitionId();
   }

   public void loadUserRoles(User user) {
      for(Role role : user.getRoles()) {
         String roleName = role.getName().trim();
         if ("Branch Maker".equalsIgnoreCase(roleName)) {
            this.isBranchMaker = true;
         } else if ("Branch Checker".equalsIgnoreCase(roleName)) {
            this.isBranchChecker = true;
         } else if ("HO Checker".equalsIgnoreCase(roleName)) {
            this.isHOChecker = true;
         } else if ("PAIMS Consent".equalsIgnoreCase(roleName)) {
            this.isPAIMS = true;
         }
      }

   }

   public String toString() {
      return "PAIMSCommon [isBranchMaker=" + this.isBranchMaker + ", isBranchChecker=" + this.isBranchChecker + ", isHOChecker=" + this.isHOChecker + ", branchName=" + this.branchName + ", zoneName=" + this.zoneName + ", nbgName=" + this.nbgName + ", hoName=" + this.hoName + "]";
   }
}
