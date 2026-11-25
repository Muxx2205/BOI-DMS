package boi.headless.delivery.mail.configuration;

import aQute.bnd.annotation.metatype.Meta.AD;
import aQute.bnd.annotation.metatype.Meta.OCD;
import com.liferay.portal.configuration.metatype.annotations.ExtendedObjectClassDefinition;
import com.liferay.portal.configuration.metatype.annotations.ExtendedObjectClassDefinition.Scope;

@ExtendedObjectClassDefinition(
   category = "BOIMail",
   scope = Scope.SYSTEM
)
@OCD(
   id = "boi.headless.delivery.mail.configuration.MailConfiguration"
)
public interface MailConfiguration {
   @AD(
      required = false,
      name = "From Email Address (Set 1)",
      description = "Sender email address for Set 1"
   )
   String fromEmail1();

   @AD(
      required = false,
      name = "To Email Addresses (Set 1)",
      description = "Comma-separated recipients for Set 1"
   )
   String toEmails1();

   @AD(
      required = false,
      name = "CC Email Addresses (Set 1)",
      description = "Comma-separated CC for Set 1"
   )
   String ccEmails1();

   @AD(
      required = false,
      deflt = "false",
      name = "Enable Branch Mail Mapping (Set 1)",
      description = "Enable Branch Mail Mapping for Set 1"
   )
   boolean branchMailAddInCC1();

   @AD(
      required = false,
      name = "BCC Email Addresses (Set 1)",
      description = "Comma-separated BCC for Set 1"
   )
   String bccEmails1();

   @AD(
      required = false,
      name = "Reply-To Addresses (Set 1)",
      description = "Comma-separated Reply-To for Set 1"
   )
   String replyToEmails1();

   @AD(
      required = false,
      name = "In-Reply-To Message ID (Set 1)",
      description = "In-Reply-To ID for Set 1"
   )
   String inReplyTo1();

   @AD(
      required = false,
      name = "Email Subject (Set 1)",
      description = "Subject line for email template 1"
   )
   String emailSubject1();

   @AD(
      required = false,
      name = "Email Body (Set 1)",
      description = "Body content for email template 1"
   )
   String emailBody1();

   @AD(
      required = false,
      name = "From Email Address (Set 2)",
      description = "Sender email address for Set 2"
   )
   String fromEmail2();

   @AD(
      required = false,
      name = "To Email Addresses (Set 2)",
      description = "Comma-separated recipients for Set 2"
   )
   String toEmails2();

   @AD(
      required = false,
      name = "CC Email Addresses (Set 2)",
      description = "Comma-separated CC for Set 2"
   )
   String ccEmails2();

   @AD(
      required = false,
      deflt = "false",
      name = "Enable Branch Mail Mapping (Set 2)",
      description = "Enable Branch Mail Mapping for Set 2"
   )
   boolean branchMailAddInCC2();

   @AD(
      required = false,
      name = "BCC Email Addresses (Set 2)",
      description = "Comma-separated BCC for Set 2"
   )
   String bccEmails2();

   @AD(
      required = false,
      name = "Reply-To Addresses (Set 2)",
      description = "Comma-separated Reply-To for Set 2"
   )
   String replyToEmails2();

   @AD(
      required = false,
      name = "In-Reply-To Message ID (Set 2)",
      description = "In-Reply-To ID for Set 2"
   )
   String inReplyTo2();

   @AD(
      required = false,
      name = "Email Subject (Set 2)",
      description = "Subject line for email template 2"
   )
   String emailSubject2();

   @AD(
      required = false,
      name = "Email Body (Set 2)",
      description = "Body content for email template 2"
   )
   String emailBody2();

   @AD(
      required = false,
      name = "From Email Address (Set 3)",
      description = "Sender email address for Set 3"
   )
   String fromEmail3();

   @AD(
      required = false,
      name = "To Email Addresses (Set 3)",
      description = "Comma-separated recipients for Set 3"
   )
   String toEmails3();

   @AD(
      required = false,
      name = "CC Email Addresses (Set 3)",
      description = "Comma-separated CC for Set 3"
   )
   String ccEmails3();

   @AD(
      required = false,
      deflt = "false",
      name = "Enable Branch Mail Mapping (Set 3)",
      description = "Enable Branch Mail Mapping for Set 3"
   )
   boolean branchMailAddInCC3();

   @AD(
      required = false,
      name = "BCC Email Addresses (Set 3)",
      description = "Comma-separated BCC for Set 3"
   )
   String bccEmails3();

   @AD(
      required = false,
      name = "Reply-To Addresses (Set 3)",
      description = "Comma-separated Reply-To for Set 3"
   )
   String replyToEmails3();

   @AD(
      required = false,
      name = "In-Reply-To Message ID (Set 3)",
      description = "In-Reply-To ID for Set 3"
   )
   String inReplyTo3();

   @AD(
      required = false,
      name = "Email Subject (Set 3)",
      description = "Subject line for email template 3"
   )
   String emailSubject3();

   @AD(
      required = false,
      name = "Email Body (Set 3)",
      description = "Body content for email template 3"
   )
   String emailBody3();

   @AD(
      required = false,
      deflt = "false",
      name = "HTML Format",
      description = "Send the email in HTML format if checked"
   )
   boolean htmlFormat();

   @AD(
      required = false,
      name = "SMTP Account",
      description = "Optional SMTP account name or ID to use for sending emails"
   )
   String smtpAccount();

   @AD(
      required = false,
      name = "Branch Mail Mapping",
      description = "Branch Mail Mapping Folder Id"
   )
   Long branchMailMappingObjectId();
}
