package com.boi.headless.rest.common.mail;

import com.liferay.portal.configuration.metatype.annotations.ExtendedObjectClassDefinition;

import aQute.bnd.annotation.metatype.Meta;

@ExtendedObjectClassDefinition(category = "BOIMail", scope = ExtendedObjectClassDefinition.Scope.SYSTEM)
@Meta.OCD(id = "com.boi.headless.rest.common.mail.MailConfiguration")

public interface MailConfiguration {

	// ---------------- SET 1 ----------------
		@Meta.AD(required = false, name = "From Email Address (Set 1)", description = "Sender email address for Set 1")
		String fromEmail1();

		@Meta.AD(required = false, name = "To Email Addresses (Set 1)", description = "Comma-separated recipients for Set 1")
		String toEmails1();

		@Meta.AD(required = false, name = "CC Email Addresses (Set 1)", description = "Comma-separated CC for Set 1")
		String ccEmails1();

		@Meta.AD(required = false, deflt = "false", name = "Enable Branch Mail Mapping (Set 1)", description = "Enable Branch Mail Mapping for Set 1")
		boolean branchMailAddInCC1();
		
		@Meta.AD(required = false, name = "BCC Email Addresses (Set 1)", description = "Comma-separated BCC for Set 1")
		String bccEmails1();

		@Meta.AD(required = false, name = "Reply-To Addresses (Set 1)", description = "Comma-separated Reply-To for Set 1")
		String replyToEmails1();

		@Meta.AD(required = false, name = "In-Reply-To Message ID (Set 1)", description = "In-Reply-To ID for Set 1")
		String inReplyTo1();

		@Meta.AD(required = false, name = "Email Subject (Set 1)", description = "Subject line for email template 1")
		String emailSubject1();

		@Meta.AD(required = false, name = "Email Body (Set 1)", description = "Body content for email template 1")
		String emailBody1();

		// ---------------- SET 2 ----------------
		@Meta.AD(required = false, name = "From Email Address (Set 2)", description = "Sender email address for Set 2")
		String fromEmail2();

		@Meta.AD(required = false, name = "To Email Addresses (Set 2)", description = "Comma-separated recipients for Set 2")
		String toEmails2();

		@Meta.AD(required = false, name = "CC Email Addresses (Set 2)", description = "Comma-separated CC for Set 2")
		String ccEmails2();
		
		@Meta.AD(required = false, deflt = "false", name = "Enable Branch Mail Mapping (Set 2)", description = "Enable Branch Mail Mapping for Set 2")
		boolean branchMailAddInCC2();

		@Meta.AD(required = false, name = "BCC Email Addresses (Set 2)", description = "Comma-separated BCC for Set 2")
		String bccEmails2();

		@Meta.AD(required = false, name = "Reply-To Addresses (Set 2)", description = "Comma-separated Reply-To for Set 2")
		String replyToEmails2();

		@Meta.AD(required = false, name = "In-Reply-To Message ID (Set 2)", description = "In-Reply-To ID for Set 2")
		String inReplyTo2();

		@Meta.AD(required = false, name = "Email Subject (Set 2)", description = "Subject line for email template 2")
		String emailSubject2();

		@Meta.AD(required = false, name = "Email Body (Set 2)", description = "Body content for email template 2")
		String emailBody2();

		// ---------------- SET 3 ----------------
		@Meta.AD(required = false, name = "From Email Address (Set 3)", description = "Sender email address for Set 3")
		String fromEmail3();

		@Meta.AD(required = false, name = "To Email Addresses (Set 3)", description = "Comma-separated recipients for Set 3")
		String toEmails3();

		@Meta.AD(required = false, name = "CC Email Addresses (Set 3)", description = "Comma-separated CC for Set 3")
		String ccEmails3();

		@Meta.AD(required = false, deflt = "false", name = "Enable Branch Mail Mapping (Set 3)", description = "Enable Branch Mail Mapping for Set 3")
		boolean branchMailAddInCC3();
		
		@Meta.AD(required = false, name = "BCC Email Addresses (Set 3)", description = "Comma-separated BCC for Set 3")
		String bccEmails3();

		@Meta.AD(required = false, name = "Reply-To Addresses (Set 3)", description = "Comma-separated Reply-To for Set 3")
		String replyToEmails3();

		@Meta.AD(required = false, name = "In-Reply-To Message ID (Set 3)", description = "In-Reply-To ID for Set 3")
		String inReplyTo3();

		@Meta.AD(required = false, name = "Email Subject (Set 3)", description = "Subject line for email template 3")
		String emailSubject3();

		@Meta.AD(required = false, name = "Email Body (Set 3)", description = "Body content for email template 3")
		String emailBody3();

		// ---------------- COMMON ----------------
		@Meta.AD(required = false, deflt = "false", name = "HTML Format", description = "Send the email in HTML format if checked")
		boolean htmlFormat();

		@Meta.AD(required = false, name = "SMTP Account", description = "Optional SMTP account name or ID to use for sending emails")
		String smtpAccount();
		
		@Meta.AD(required = false, name = "Branch Mail Mapping", description = "Branch Mail Mapping Folder Id")
		Long branchMailMappingObjectId();
	

}
