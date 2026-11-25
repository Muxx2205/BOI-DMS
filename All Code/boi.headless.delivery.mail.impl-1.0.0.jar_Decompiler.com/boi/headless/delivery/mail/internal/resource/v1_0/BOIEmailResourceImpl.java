package boi.headless.delivery.mail.internal.resource.v1_0;

import boi.headless.delivery.mail.configuration.MailConfiguration;
import boi.headless.delivery.mail.dto.v1_0.BOIHeadlessResponse;
import boi.headless.delivery.mail.resource.v1_0.BOIEmailResource;
import com.liferay.mail.kernel.model.MailMessage;
import com.liferay.mail.kernel.service.MailServiceUtil;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.rest.dto.v1_0.ObjectEntry;
import com.liferay.object.rest.manager.v1_0.ObjectEntryManager;
import com.liferay.object.rest.manager.v1_0.ObjectEntryManagerRegistry;
import com.liferay.object.service.ObjectDefinitionLocalServiceUtil;
import com.liferay.portal.configuration.metatype.bnd.util.ConfigurableUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.search.Sort;
import com.liferay.portal.kernel.service.UserLocalServiceUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.vulcan.aggregation.Aggregation;
import com.liferay.portal.vulcan.dto.converter.DTOConverterRegistry;
import com.liferay.portal.vulcan.dto.converter.DefaultDTOConverterContext;
import com.liferay.portal.vulcan.pagination.Page;
import com.liferay.portal.vulcan.pagination.Pagination;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import javax.mail.internet.InternetAddress;
import javax.validation.constraints.NotNull;
import javax.ws.rs.core.UriInfo;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;

@Component(
   configurationPid = {"boi.headless.delivery.mail.configuration.MailConfiguration"},
   properties = {"OSGI-INF/liferay/rest/v1_0/boi-email.properties"},
   scope = ServiceScope.PROTOTYPE,
   service = {BOIEmailResource.class}
)
public class BOIEmailResourceImpl extends BaseBOIEmailResourceImpl {
   private static final Log _log = LogFactoryUtil.getLog(BOIEmailResourceImpl.class);
   private MailConfiguration _mailConfiguration;
   @Reference
   private DTOConverterRegistry _dtoConverterRegistry;
   @Reference
   private ObjectEntryManagerRegistry _objectEntryManagerRegistry;

   public BOIHeadlessResponse sendBOIMail(@NotNull Integer set, Object object) throws Exception {
      BOIHeadlessResponse sendMail = this.sendMail(set, object);
      return sendMail;
   }

   private BOIHeadlessResponse sendMail(int templateNumber, Object object) {
      BOIHeadlessResponse response = new BOIHeadlessResponse();

      try {
         String fromAddress = this.getFromBySet(templateNumber);
         Set<String> toSet = this.splitEmails(this.getToBySet(templateNumber));
         Set<String> ccSet = this.splitEmails(this.getCCBySet(templateNumber));
         Set<String> bccSet = this.splitEmails(this.getBCCBySet(templateNumber));
         Set<String> replyToSet = this.splitEmails(this.getReplyToBySet(templateNumber));
         String inReplyTo = this.getInReplyToBySet(templateNumber);
         boolean isBranchMailEnabled = this.isBranchMailEnabled(templateNumber);
         String subject = this.getSubjectByTemplate(templateNumber);
         String body = this.getBodyByTemplate(templateNumber);
         boolean html = this._mailConfiguration.htmlFormat();
         String smtp = this._mailConfiguration.smtpAccount();
         _log.info("Object Comes  " + object);
         if (object instanceof Map) {
            Map<?, ?> map = (Map)object;
            long userId = 0L;
            String branchCode = "";
            Object userIdObj = map.get("USERID");
            Object branchCodeObj = map.get("BRANCHCODE");
            if (userIdObj != null) {
               userId = Long.parseLong(String.valueOf(userIdObj));
            }

            if (branchCodeObj != null) {
               branchCode = String.valueOf(branchCodeObj);
            }

            if (userId > 0L) {
               User user = UserLocalServiceUtil.fetchUser(userId);
               if (user != null) {
                  toSet.add(user.getEmailAddress());
               }
            }

            if (isBranchMailEnabled && Validator.isNotNull(branchCode)) {
               BranchMailDTO branchMail = this.getBranchMail("branchCode eq '" + branchCode + "'", this.contextUser, this._mailConfiguration.branchMailMappingObjectId());
               if (branchMail != null && Validator.isNotNull(branchMail.getEmail())) {
                  ccSet.add(branchMail.getEmail());
               }
            }

            body = this.replacePlaceholders(body, map);
            subject = this.replacePlaceholders(subject, map);
         }

         if (toSet.isEmpty()) {
            _log.info("No valid recipients found. Mail not sent for set " + templateNumber);
            response.setResponseId(400);
            response.setResponseStatus("No recipients configured. Mail not sent.");
            return response;
         } else {
            InternetAddress from = new InternetAddress(fromAddress);
            InternetAddress[] toAddresses = this.convertToInternetAddresses(new ArrayList(toSet));
            InternetAddress[] ccAddresses = this.convertToInternetAddresses(new ArrayList(ccSet));
            InternetAddress[] bccAddresses = this.convertToInternetAddresses(new ArrayList(bccSet));
            InternetAddress[] replyToAddresses = this.convertToInternetAddresses(new ArrayList(replyToSet));
            MailMessage mailMessage = new MailMessage();
            mailMessage.setFrom(from);
            mailMessage.setTo(toAddresses);
            if (!ccSet.isEmpty()) {
               mailMessage.setCC(ccAddresses);
            }

            if (!bccSet.isEmpty()) {
               mailMessage.setBCC(bccAddresses);
            }

            if (!replyToSet.isEmpty()) {
               mailMessage.setReplyTo(replyToAddresses);
            }

            if (inReplyTo != null && !inReplyTo.isEmpty()) {
               mailMessage.setInReplyTo(inReplyTo);
            }

            mailMessage.setSubject(subject != null ? subject : "(No Subject)");
            mailMessage.setBody(body != null ? body : "");
            mailMessage.setHTMLFormat(html);
            MailServiceUtil.sendEmail(mailMessage);
            Log var10000 = _log;
            String var10001 = String.join(", ", toSet);
            var10000.info("Mail sent successfully to: " + var10001 + " using set " + templateNumber);
            response.setResponseId(200);
            var10001 = String.join(", ", toSet);
            response.setResponseStatus("Mail sent successfully to " + var10001 + " recipient(s) and cc " + String.join(", ", ccSet) + " for set " + templateNumber);
            return response;
         }
      } catch (Exception e) {
         _log.error("Failed to send mail for set " + templateNumber, e);
         response.setResponseId(500);
         response.setResponseStatus("Mail sending failed for set " + templateNumber + ": " + e.getMessage());
         return response;
      }
   }

   private String getFromBySet(int set) {
      switch (set) {
         case 1:
            return this._mailConfiguration.fromEmail1();
         case 2:
            return this._mailConfiguration.fromEmail2();
         case 3:
            return this._mailConfiguration.fromEmail3();
         default:
            return this._mailConfiguration.fromEmail1();
      }
   }

   private String getToBySet(int set) {
      switch (set) {
         case 1:
            return this._mailConfiguration.toEmails1();
         case 2:
            return this._mailConfiguration.toEmails2();
         case 3:
            return this._mailConfiguration.toEmails3();
         default:
            return this._mailConfiguration.toEmails1();
      }
   }

   private String getCCBySet(int set) {
      switch (set) {
         case 1:
            return this._mailConfiguration.ccEmails1();
         case 2:
            return this._mailConfiguration.ccEmails2();
         case 3:
            return this._mailConfiguration.ccEmails3();
         default:
            return this._mailConfiguration.ccEmails1();
      }
   }

   private boolean isBranchMailEnabled(int set) {
      switch (set) {
         case 1:
            return this._mailConfiguration.branchMailAddInCC1();
         case 2:
            return this._mailConfiguration.branchMailAddInCC2();
         case 3:
            return this._mailConfiguration.branchMailAddInCC3();
         default:
            return false;
      }
   }

   private String getBCCBySet(int set) {
      switch (set) {
         case 1:
            return this._mailConfiguration.bccEmails1();
         case 2:
            return this._mailConfiguration.bccEmails2();
         case 3:
            return this._mailConfiguration.bccEmails3();
         default:
            return this._mailConfiguration.bccEmails1();
      }
   }

   private String getReplyToBySet(int set) {
      switch (set) {
         case 1:
            return this._mailConfiguration.replyToEmails1();
         case 2:
            return this._mailConfiguration.replyToEmails2();
         case 3:
            return this._mailConfiguration.replyToEmails3();
         default:
            return this._mailConfiguration.replyToEmails1();
      }
   }

   private String getInReplyToBySet(int set) {
      switch (set) {
         case 1:
            return this._mailConfiguration.inReplyTo1();
         case 2:
            return this._mailConfiguration.inReplyTo2();
         case 3:
            return this._mailConfiguration.inReplyTo3();
         default:
            return this._mailConfiguration.inReplyTo1();
      }
   }

   private String getSubjectByTemplate(int templateNumber) {
      switch (templateNumber) {
         case 1:
            return this._mailConfiguration.emailSubject1();
         case 2:
            return this._mailConfiguration.emailSubject2();
         case 3:
            return this._mailConfiguration.emailSubject3();
         default:
            return this._mailConfiguration.emailSubject1();
      }
   }

   private String getBodyByTemplate(int templateNumber) {
      switch (templateNumber) {
         case 1:
            return this._mailConfiguration.emailBody1();
         case 2:
            return this._mailConfiguration.emailBody2();
         case 3:
            return this._mailConfiguration.emailBody3();
         default:
            return this._mailConfiguration.emailBody1();
      }
   }

   private String replacePlaceholders(String template, Map<String, Object> data) {
      String result = template;

      for(Map.Entry<String, Object> entry : data.entrySet()) {
         String placeholder = "${" + (String)entry.getKey() + "}";
         if (result.contains(placeholder)) {
            result = result.replace(placeholder, String.valueOf(entry.getValue()));
         }
      }

      return result;
   }

   private Set<String> splitEmails(String emails) {
      return (Set<String>)(emails != null && !emails.trim().isEmpty() ? (Set)Arrays.stream(emails.split(",")).map(String::trim).filter((s) -> !s.isEmpty()).collect(Collectors.toSet()) : new HashSet());
   }

   private InternetAddress[] convertToInternetAddresses(List<String> emails) {
      return emails != null && !emails.isEmpty() ? (InternetAddress[])emails.stream().map((email) -> {
         try {
            return new InternetAddress(email.trim());
         } catch (Exception e) {
            _log.error("Invalid email address skipped: " + email, e);
            return null;
         }
      }).filter(Objects::nonNull).toArray((x$0) -> new InternetAddress[x$0]) : new InternetAddress[0];
   }

   private BranchMailDTO getBranchMail(String query, User user, long objectDefinitionId) {
      try {
         ObjectDefinition branchMailMapping = ObjectDefinitionLocalServiceUtil.getObjectDefinition(objectDefinitionId);
         DefaultDTOConverterContext dtoConverterContext = new DefaultDTOConverterContext(this._dtoConverterRegistry, branchMailMapping.getObjectDefinitionId(), Locale.getDefault(), (UriInfo)null, user);
         ObjectEntryManager objectEntryManager = this._objectEntryManagerRegistry.getObjectEntryManager(branchMailMapping.getStorageType());
         Page<ObjectEntry> page = objectEntryManager.getObjectEntries(branchMailMapping.getCompanyId(), branchMailMapping, branchMailMapping.getScope(), (Aggregation)null, dtoConverterContext, query, Pagination.of(-1, -1), (String)null, (Sort[])null);

         for(ObjectEntry entry : page.getItems()) {
            Map<String, Object> map = entry.getProperties();
            if (map.containsKey("branchCode") && map.containsKey("branchName") && map.containsKey("zone") && map.containsKey("email")) {
               return new BranchMailDTO((String)map.get("branchCode"), (String)map.get("branchName"), (String)map.get("sOLID"), (String)map.get("zone"), (String)map.get("email"));
            }
         }
      } catch (Exception e) {
         _log.error("Error fetching user departments", e);
      }

      return null;
   }

   @Activate
   @Modified
   protected void activate(Map<String, Object> properties) {
      this._mailConfiguration = (MailConfiguration)ConfigurableUtil.createConfigurable(MailConfiguration.class, properties);
   }
}
