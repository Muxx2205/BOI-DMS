package boi.headless.delivery.dms.common.internal.resource.v1_0;

import boi.headless.delivery.dms.common.background.task.DMSCommonAPIConfiguation;
import boi.headless.delivery.dms.common.background.task.ObjectExportBackgroundTaskExecutor;
import boi.headless.delivery.dms.common.dto.v1_0.BOIHeadlessResponse;
import boi.headless.delivery.dms.common.resource.v1_0.DashboardGraphResource;
import com.liferay.document.library.kernel.service.DLAppService;
import com.liferay.object.rest.manager.v1_0.ObjectEntryManagerRegistry;
import com.liferay.object.service.ObjectDefinitionLocalService;
import com.liferay.object.service.ObjectEntryLocalService;
import com.liferay.object.service.ObjectFieldLocalService;
import com.liferay.portal.background.task.model.BackgroundTask;
import com.liferay.portal.background.task.service.BackgroundTaskLocalServiceUtil;
import com.liferay.portal.configuration.metatype.bnd.util.ConfigurableUtil;
import com.liferay.portal.kernel.backgroundtask.BackgroundTaskManagerUtil;
import com.liferay.portal.kernel.dao.orm.DynamicQuery;
import com.liferay.portal.kernel.dao.orm.Order;
import com.liferay.portal.kernel.dao.orm.OrderFactoryUtil;
import com.liferay.portal.kernel.dao.orm.RestrictionsFactoryUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Role;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.vulcan.dto.converter.DTOConverterRegistry;
import com.liferay.portal.vulcan.dto.converter.DefaultDTOConverterContext;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;

@Component(
   configurationPid = {"boi.headless.delivery.dms.common.background.task.DMSCommonAPIConfiguation"},
   properties = {"OSGI-INF/liferay/rest/v1_0/dashboard-graph.properties"},
   scope = ServiceScope.PROTOTYPE,
   service = {DashboardGraphResource.class}
)
public class DashboardGraphResourceImpl extends BaseDashboardGraphResourceImpl {
   String url = "not-available";
   private DMSCommonAPIConfiguation dmsCommonAPIConfiguation;
   @Reference
   private ObjectFieldLocalService objectFieldLocalService;
   @Reference
   private DLAppService dlAppService;
   private static final Log log = LogFactoryUtil.getLog(DashboardGraphResourceImpl.class);
   @Reference
   private DTOConverterRegistry dtoConverterRegistry;
   @Reference
   private ObjectEntryManagerRegistry objectEntryManagerRegistry;
   @Reference
   private ObjectDefinitionLocalService objectDefinitionLocalService;
   @Reference
   private ObjectEntryLocalService objectEntryLocalService;

   public BOIHeadlessResponse ExportToExcel(Object object) throws Exception {
      if (object == null) {
         throw new IllegalArgumentException("Input object is null");
      } else {
         JSONObject jsonObject;
         if (object instanceof JSONObject) {
            jsonObject = (JSONObject)object;
         } else if (object instanceof String) {
            jsonObject = JSONFactoryUtil.createJSONObject((String)object);
         } else {
            if (!(object instanceof Map)) {
               throw new IllegalArgumentException("Unsupported object type: " + object.getClass());
            }

            jsonObject = JSONFactoryUtil.createJSONObject((Map)object);
         }

         BOIHeadlessResponse response = new BOIHeadlessResponse();
         List<Role> roles = this.contextUser.getRoles();
         if (roles != null && !roles.isEmpty()) {
            boolean hasDashboardReportRole = roles.stream().anyMatch((role) -> "Dashboard Report".equalsIgnoreCase(role.getName()) || "FBD FE BO DASHBOARD".equalsIgnoreCase(role.getName()));
            if (!hasDashboardReportRole) {
               jsonObject.put("status", "ERROR");
               jsonObject.put("message", "Permission denied. Please contact the dms support team.");
               response.setData(jsonObject.toMap());
               response.setResponseId(402);
               response.setResponseStatus("ERROR");
               return response;
            } else {
               this.validateExportInput(jsonObject, response);
               String templateName = jsonObject.getString("templateName", "DefaultTemplate");
               String fromDate = jsonObject.getString("fromDate", "");
               String toDate = jsonObject.getString("toDate", "");
               List<String> queryList = new LinkedList();
               SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
               Date from = sdf.parse(fromDate);
               Date to = sdf.parse(toDate);
               this.addDateFilter(queryList, from, to, sdf);
               String queryResult = (String)queryList.stream().filter(Validator::isNotNull).filter((x) -> !Validator.isBlank(x)).collect(Collectors.joining(" and "));
               String fileName = this.generateFileName(fromDate, toDate);
               this.exportToExcelData(queryResult, templateName, fileName);
               jsonObject.put("status", "SUCCESS");
               jsonObject.put("message", "Excel export task triggered");
               response.setData(jsonObject.toMap());
               response.setResponseId(200);
               response.setResponseStatus("SUCCESS");
               return response;
            }
         } else {
            jsonObject.put("status", "ERROR");
            jsonObject.put("message", "Permission denied. Please contact the dms support team.");
            response.setData(jsonObject.toMap());
            response.setResponseId(402);
            response.setResponseStatus("ERROR");
            return response;
         }
      }
   }

   private String generateFileName(String fromDate, String toDate) {
      SimpleDateFormat sdf = new SimpleDateFormat("HHmmss");
      String time = sdf.format(new Date());
      return "Report_" + fromDate + "_to_" + toDate + "_(" + time + ").xlsx";
   }

   private void addDateFilter(List<String> queryList, Date fromDate, Date toDate, SimpleDateFormat sdf) {
      if (Validator.isNotNull(fromDate) && Validator.isNotNull(toDate)) {
         Map<String, String> isoRange = this.convertToISO8601Range(sdf.format(fromDate), sdf.format(toDate));
         String var10000 = (String)isoRange.get("fromDate");
         String dateFilter = "dateCreated ge " + var10000 + " and dateCreated le " + (String)isoRange.get("toDate");
         log.info("Request required dateFilter  " + dateFilter);
         queryList.add(dateFilter);
      }

   }

   private BOIHeadlessResponse error(JSONObject jsonObject, String message, BOIHeadlessResponse response) {
      jsonObject.put("message", message);
      response.setData(jsonObject.toMap());
      response.setResponseId(402);
      response.setResponseStatus("ERROR");
      return response;
   }

   private BOIHeadlessResponse validateExportInput(JSONObject jsonObject, BOIHeadlessResponse response) {
      String templateName = jsonObject.getString("templateName", "").trim();
      String fromDateStr = jsonObject.getString("fromDate", "").trim();
      String toDateStr = jsonObject.getString("toDate", "").trim();
      if (Validator.isBlank(templateName)) {
         return this.error(jsonObject, "Template name is required.", response);
      } else if (!Validator.isBlank(fromDateStr) && !Validator.isBlank(toDateStr)) {
         LocalDate fromDate;
         LocalDate toDate;
         try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            fromDate = LocalDate.parse(fromDateStr, formatter);
            toDate = LocalDate.parse(toDateStr, formatter);
         } catch (Exception var10) {
            return this.error(jsonObject, "Invalid date format. Expected format: yyyy-MM-dd.", response);
         }

         if (toDate.isBefore(fromDate)) {
            return this.error(jsonObject, "To Date cannot be before From Date.", response);
         } else {
            long daysBetween = ChronoUnit.DAYS.between(fromDate, toDate);
            return daysBetween > 90L ? this.error(jsonObject, "Date range cannot exceed 90 days.", response) : null;
         }
      } else {
         return this.error(jsonObject, "From Date and To Date are required.", response);
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
      Log var10000 = log;
      String var10001 = isoFormatter.format(fromDateTime);
      var10000.info("fromDate" + var10001);
      var10000 = log;
      var10001 = isoFormatter.format(toDateTime);
      var10000.info("toDate" + var10001);
      return result;
   }

   private void exportToExcelData(String statusQuery, String templateName, String fileName) {
      Map<String, Serializable> taskContextMap = new HashMap();
      ServiceContext serviceContext = new ServiceContext();
      taskContextMap.put("companyId", this.contextUser.getCompanyId());
      taskContextMap.put("statusQuery", statusQuery);
      taskContextMap.put("userId", this.contextUser.getUserId());
      taskContextMap.put("screenName", this.contextUser.getScreenName());
      taskContextMap.put("serviceContext", serviceContext);
      taskContextMap.put("templateName", templateName);
      taskContextMap.put("fileName", fileName);

      try {
         log.info("invoking  MyBackgroundTaskExecutor");
         BackgroundTask backgroundTask = BackgroundTaskLocalServiceUtil.addBackgroundTask(this.contextUser.getUserId(), this.contextUser.getGroupId(), templateName, (String[])null, ObjectExportBackgroundTaskExecutor.class, taskContextMap, serviceContext);
         BackgroundTaskManagerUtil.triggerBackgroundTask(backgroundTask.getBackgroundTaskId());
         log.info("End  MyBackgroundTaskExecutor" + backgroundTask.getBackgroundTaskId());
         this.url = "in-progress";
      } catch (PortalException e1) {
         e1.printStackTrace();
      }

   }

   public String getTaskCompletedURL(long objectDefinitionId, String name) {
      try {
         List<BackgroundTask> backgroundTasks = this.getBackgroundTasks(objectDefinitionId, name);
         if (Validator.isNotNull(backgroundTasks) && backgroundTasks.size() > 0) {
            BackgroundTask backgroundTask = (BackgroundTask)backgroundTasks.get(0);
            if (backgroundTask.isCompleted()) {
               String fileType = (String)backgroundTask.getTaskContextMap().get("fileURL");
               return Validator.isNotNull(fileType) ? fileType : "";
            } else {
               return backgroundTask.getStatusLabel();
            }
         } else {
            return "";
         }
      } catch (Exception w) {
         w.printStackTrace();
         return "";
      }
   }

   private List<BackgroundTask> getBackgroundTasks(long objectDefinitionId, String name) {
      DynamicQuery dynamicQuery = BackgroundTaskLocalServiceUtil.dynamicQuery();
      dynamicQuery.add(RestrictionsFactoryUtil.eq("userId", this.contextUser.getUserId()));
      dynamicQuery.add(RestrictionsFactoryUtil.eq("groupId", this.contextUser.getGroupId()));
      dynamicQuery.add(RestrictionsFactoryUtil.eq("name", name));
      dynamicQuery.add(RestrictionsFactoryUtil.eq("completed", true));
      Order order = OrderFactoryUtil.desc("modifiedDate");
      dynamicQuery.addOrder(order);
      return BackgroundTaskLocalServiceUtil.dynamicQuery(dynamicQuery);
   }

   private long getObjectDefinitionIdByTemplateName(String templateName) {
      if (Validator.isNull(templateName)) {
         return 0L;
      } else {
         switch (templateName) {
            case "BoardLevelMeeting":
               return this.dmsCommonAPIConfiguation.BoardLevelMeetingObjectDefinitionId();
            case "CDAD":
               return this.dmsCommonAPIConfiguation.CDADObjectDefinitionId();
            case "InspectionAuditACE":
               return this.dmsCommonAPIConfiguation.InspectionAuditACEObjectDefinitionId();
            case "DataWarehouse":
               return this.dmsCommonAPIConfiguation.DataWareHouseObjectDefinitionId();
            case "EPlatform":
               return this.dmsCommonAPIConfiguation.EPlateFormObjectDefinitionId();
            case "ECRM":
               return this.dmsCommonAPIConfiguation.ECRMFormObjectDefinitionId();
            case "KCCJansamarth":
               return this.dmsCommonAPIConfiguation.KCCJansamarthFormObjectDefinitionId();
            case "UTOPIA":
               return this.dmsCommonAPIConfiguation.UTOPIAFormObjectDefinitionId();
            case "ElectricalWiring":
               return this.dmsCommonAPIConfiguation.ElectricalWiringObjectDefinitionId();
            case "Finastra":
               return this.dmsCommonAPIConfiguation.FinastraObjectDefinitionId();
            case "FinastraOther":
               return this.dmsCommonAPIConfiguation.FinastraOtherObjectDefinitionId();
            case "HORD":
               return this.dmsCommonAPIConfiguation.HORDObjectDefinitionId();
            case "HOSecurityDepartment":
               return this.dmsCommonAPIConfiguation.HOSecurityDepartmentObjectDefinitionId();
            case "InspectionAuditACB":
               return this.dmsCommonAPIConfiguation.InspectionAuditACBObjectDefinitionId();
            case "InspectionAuditCAAppointment":
               return this.dmsCommonAPIConfiguation.InspectionAuditCAAppointmentObjectDefinitionId();
            case "InspectionAuditMisc":
               return this.dmsCommonAPIConfiguation.InspectionAuditMiscObjectDefinitionId();
            case "InspectionAuditOtherReport":
               return this.dmsCommonAPIConfiguation.InspectionAuditOthReportObjectDefinitionId();
            case "InspectionAuditReports":
               return this.dmsCommonAPIConfiguation.InspectionAuditRepotsObjectDefinitionId();
            case "InspectionForeignAudit":
               return this.dmsCommonAPIConfiguation.InspectionForeignAuditObjectDefinitionId();
            case "InternalLayoutMap":
               return this.dmsCommonAPIConfiguation.InternalLayoutMapObjectDefinitionId();
            case "InternationalDepartment":
               return this.dmsCommonAPIConfiguation.InternationalDepObjectDefinitionId();
            case "BranchDigitation":
               return this.dmsCommonAPIConfiguation.BranchDigitationObjectDefinitionId();
            case "ProjectManagementList":
               return this.dmsCommonAPIConfiguation.ProjectManagementListObjectDefinitionId();
            case "CustomerAccount":
               return this.dmsCommonAPIConfiguation.CustomerAccountObjectDefinitionId();
            case "UPIInsurance":
               return this.dmsCommonAPIConfiguation.UPIInsuranceObjectDefinitionId();
            case "CPDInsurance":
               return this.dmsCommonAPIConfiguation.CPDInsuranceObjectDefinitionId();
            case "DBDepartment":
               return this.dmsCommonAPIConfiguation.DBDepartmentObjectDefinitionId();
            case "DBDMandate":
               return this.dmsCommonAPIConfiguation.DBDMandateObjectDefinitionId();
            case "FBD":
               return this.dmsCommonAPIConfiguation.FBDObjectDefinitionId();
            case "FEBONONFinancial":
               return this.dmsCommonAPIConfiguation.FEBONONFinancialObjectDefinitionId();
            case "FinancePloe":
               return this.dmsCommonAPIConfiguation.FinancePloeObjectDefinitionId();
            case "FBDFRBO":
               return this.dmsCommonAPIConfiguation.FBDFRBOObjectDefinitionId();
            default:
               return 0L;
         }
      }
   }

   public DefaultDTOConverterContext getDTOConverterContext(Long objectEntryId) {
      return new DefaultDTOConverterContext(this.contextAcceptLanguage.isAcceptAllLanguages(), (Map)null, this.dtoConverterRegistry, this.contextHttpServletRequest, objectEntryId, this.contextAcceptLanguage.getPreferredLocale(), this.contextUriInfo, this.contextUser);
   }

   @Activate
   @Modified
   protected void activate(Map<String, Object> properties) {
      this.dmsCommonAPIConfiguation = (DMSCommonAPIConfiguation)ConfigurableUtil.createConfigurable(DMSCommonAPIConfiguation.class, properties);
   }
}
