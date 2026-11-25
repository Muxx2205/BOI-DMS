package com.boi.headless.rest.common.mail;

import com.liferay.mail.kernel.model.MailMessage;
import com.liferay.mail.kernel.service.MailServiceUtil;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.rest.manager.v1_0.ObjectEntryManager;
import com.liferay.object.rest.manager.v1_0.ObjectEntryManagerRegistry;
import com.liferay.object.service.ObjectDefinitionLocalServiceUtil;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.service.UserLocalServiceUtil;
import com.liferay.portal.kernel.util.Validator;
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


public class BOIEmail {

	public BOIEmail(MailConfiguration _mailConfiguration, DTOConverterRegistry _dtoConverterRegistry,
			ObjectEntryManagerRegistry _objectEntryManagerRegistry) {
		this._mailConfiguration = _mailConfiguration;
		this._dtoConverterRegistry = _dtoConverterRegistry;
		this._objectEntryManagerRegistry = _objectEntryManagerRegistry;
	}

	
 

	public void sendMail(int templateNumber, Object object) {
		try {
			String fromAddress = getFromBySet(templateNumber);

			Set<String> toSet = splitEmails(getToBySet(templateNumber));
			Set<String> ccSet = splitEmails(getCCBySet(templateNumber));
			Set<String> bccSet = splitEmails(getBCCBySet(templateNumber));
			Set<String> replyToSet = splitEmails(getReplyToBySet(templateNumber));
			String inReplyTo = getInReplyToBySet(templateNumber);

			boolean isBranchMailEnabled = isBranchMailEnabled(templateNumber);

			String subject = getSubjectByTemplate(templateNumber);
			String body = getBodyByTemplate(templateNumber);
			boolean html = _mailConfiguration.htmlFormat();
			String smtp = _mailConfiguration.smtpAccount();

			_log.info("Object Comes  " + object);

			if (object instanceof Map) {
				Map<String, Object> map = (Map<String, Object>) object;

				long userId = 0L;
				User user = null;
				String branchCode = StringPool.BLANK;

				Object userIdObj = map.get("USERID");
				Object branchCodeObj = map.get("BRANCHCODE");

				if (userIdObj != null) {
					userId = Long.parseLong(String.valueOf(userIdObj));
				}

				if (branchCodeObj != null) {
					branchCode = String.valueOf(branchCodeObj);
				}

				if (userId > 0) {
					 user = UserLocalServiceUtil.fetchUser(userId);
					if (user != null) {
						toSet.add(user.getEmailAddress());
					}
				}

				if (isBranchMailEnabled && Validator.isNotNull(branchCode)) {
					BranchMailDTO branchMail = getBranchMail("branchCode eq '" + branchCode + "'", user,
							_mailConfiguration.branchMailMappingObjectId());
					if (branchMail != null && Validator.isNotNull(branchMail.getEmail())) {
						ccSet.add(branchMail.getEmail());
					}

				}

				body = replacePlaceholders(body, (Map<String, Object>) map);
				subject = replacePlaceholders(subject, (Map<String, Object>) map);
			}

			if (toSet.isEmpty()) {
				_log.info("No valid recipients found. Mail not sent for set " + templateNumber);
			//	response.setResponseId(400);
			//	response.setResponseStatus("No recipients configured. Mail not sent.");
			//	return response;
			}

			InternetAddress from = new InternetAddress(fromAddress);
			InternetAddress[] toAddresses = convertToInternetAddresses(new ArrayList<>(toSet));
			InternetAddress[] ccAddresses = convertToInternetAddresses(new ArrayList<>(ccSet));
			InternetAddress[] bccAddresses = convertToInternetAddresses(new ArrayList<>(bccSet));
			InternetAddress[] replyToAddresses = convertToInternetAddresses(new ArrayList<>(replyToSet));

			MailMessage mailMessage = new MailMessage();
			mailMessage.setFrom(from);
			mailMessage.setTo(toAddresses);

			if (!ccSet.isEmpty())
				mailMessage.setCC(ccAddresses);
			if (!bccSet.isEmpty())
				mailMessage.setBCC(bccAddresses);
			if (!replyToSet.isEmpty())
				mailMessage.setReplyTo(replyToAddresses);
			if (inReplyTo != null && !inReplyTo.isEmpty())
				mailMessage.setInReplyTo(inReplyTo);

			mailMessage.setSubject(subject != null ? subject : "(No Subject)");
			mailMessage.setBody(body != null ? body : "");
			mailMessage.setHTMLFormat(html);

			MailServiceUtil.sendEmail(mailMessage);

			_log.info("Mail sent successfully to: " + String.join(", ", toSet) + " using set " + templateNumber);
			/*
			 * response.setResponseId(200);
			 * response.setResponseStatus("Mail sent successfully to " + String.join(", ",
			 * toSet) + " recipient(s) and cc " + String.join(", ", ccSet) + " for set " +
			 * templateNumber);
			 * 
			 * return response;
			 */

		} catch (Exception e) {
			_log.error("Failed to send mail for set " + templateNumber, e);
			/*
			 * response.setResponseId(500);
			 * response.setResponseStatus("Mail sending failed for set " + templateNumber +
			 * ": " + e.getMessage()); return response;
			 */
		}
	}

	private String getFromBySet(int set) {
		switch (set) {
		case 1:
			return _mailConfiguration.fromEmail1();
		case 2:
			return _mailConfiguration.fromEmail2();
		case 3:
			return _mailConfiguration.fromEmail3();
		default:
			return _mailConfiguration.fromEmail1();
		}
	}

	private String getToBySet(int set) {
		switch (set) {
		case 1:
			return _mailConfiguration.toEmails1();
		case 2:
			return _mailConfiguration.toEmails2();
		case 3:
			return _mailConfiguration.toEmails3();
		default:
			return _mailConfiguration.toEmails1();
		}
	}

	private String getCCBySet(int set) {
		switch (set) {
		case 1:
			return _mailConfiguration.ccEmails1();
		case 2:
			return _mailConfiguration.ccEmails2();
		case 3:
			return _mailConfiguration.ccEmails3();
		default:
			return _mailConfiguration.ccEmails1();
		}
	}

	private boolean isBranchMailEnabled(int set) {
		switch (set) {
		case 1:
			return _mailConfiguration.branchMailAddInCC1();
		case 2:
			return _mailConfiguration.branchMailAddInCC2();
		case 3:
			return _mailConfiguration.branchMailAddInCC3();
		default:
			return false;
		}
	}

	private String getBCCBySet(int set) {
		switch (set) {
		case 1:
			return _mailConfiguration.bccEmails1();
		case 2:
			return _mailConfiguration.bccEmails2();
		case 3:
			return _mailConfiguration.bccEmails3();
		default:
			return _mailConfiguration.bccEmails1();
		}
	}

	private String getReplyToBySet(int set) {
		switch (set) {
		case 1:
			return _mailConfiguration.replyToEmails1();
		case 2:
			return _mailConfiguration.replyToEmails2();
		case 3:
			return _mailConfiguration.replyToEmails3();
		default:
			return _mailConfiguration.replyToEmails1();
		}
	}

	private String getInReplyToBySet(int set) {
		switch (set) {
		case 1:
			return _mailConfiguration.inReplyTo1();
		case 2:
			return _mailConfiguration.inReplyTo2();
		case 3:
			return _mailConfiguration.inReplyTo3();
		default:
			return _mailConfiguration.inReplyTo1();
		}
	}

	private String getSubjectByTemplate(int templateNumber) {
		switch (templateNumber) {
		case 1:
			return _mailConfiguration.emailSubject1();
		case 2:
			return _mailConfiguration.emailSubject2();
		case 3:
			return _mailConfiguration.emailSubject3();
		default:
			return _mailConfiguration.emailSubject1();
		}
	}

	private String getBodyByTemplate(int templateNumber) {
		switch (templateNumber) {
		case 1:
			return _mailConfiguration.emailBody1();
		case 2:
			return _mailConfiguration.emailBody2();
		case 3:
			return _mailConfiguration.emailBody3();
		default:
			return _mailConfiguration.emailBody1();
		}
	}

	private String replacePlaceholders(String template, Map<String, Object> data) {
		String result = template;
		for (Map.Entry<String, Object> entry : data.entrySet()) {
			String placeholder = "${" + entry.getKey() + "}";
			if (result.contains(placeholder)) {
				result = result.replace(placeholder, String.valueOf(entry.getValue()));
			}
		}
		return result;
	}

	private Set<String> splitEmails(String emails) {
		if (emails == null || emails.trim().isEmpty())
			return new HashSet<String>();
		return Arrays.stream(emails.split(",")).map(String::trim).filter(s -> !s.isEmpty()).collect(Collectors.toSet());
	}

	private InternetAddress[] convertToInternetAddresses(List<String> emails) {
		if (emails == null || emails.isEmpty()) {
			return new InternetAddress[0];
		}

		return emails.stream().map(email -> {
			try {
				return new InternetAddress(email.trim());
			} catch (Exception e) {
				_log.error("Invalid email address skipped: " + email, e);
				return null;
			}
		}).filter(Objects::nonNull).toArray(InternetAddress[]::new);
	}

	private BranchMailDTO getBranchMail(String query, User user, long objectDefinitionId) {
		try {
			ObjectDefinition branchMailMapping = ObjectDefinitionLocalServiceUtil
					.getObjectDefinition(objectDefinitionId);

			DefaultDTOConverterContext dtoConverterContext = new DefaultDTOConverterContext(_dtoConverterRegistry,
					branchMailMapping.getObjectDefinitionId(), Locale.getDefault(), null, user);

			ObjectEntryManager objectEntryManager = _objectEntryManagerRegistry
					.getObjectEntryManager(branchMailMapping.getStorageType());

			Page<com.liferay.object.rest.dto.v1_0.ObjectEntry> page = objectEntryManager.getObjectEntries(
					branchMailMapping.getCompanyId(), branchMailMapping, branchMailMapping.getScope(), null,
					dtoConverterContext, query, Pagination.of(-1, -1), null, null);

			for (com.liferay.object.rest.dto.v1_0.ObjectEntry entry : page.getItems()) {
				Map<String, Object> map = entry.getProperties();

				if (map.containsKey("branchCode") && map.containsKey("branchName") && map.containsKey("zone")
						&& map.containsKey("email")) {

					return new BranchMailDTO((String) map.get("branchCode"), (String) map.get("branchName"),
							(String) map.get("sOLID"), (String) map.get("zone"), (String) map.get("email"));
				}
			}
		} catch (Exception e) {
			_log.error("Error fetching user departments", e);
		}

		return null;
	}

	private static final Log _log = LogFactoryUtil.getLog(BOIEmail.class);


	private MailConfiguration _mailConfiguration;

	
	private DTOConverterRegistry _dtoConverterRegistry;

	
	private ObjectEntryManagerRegistry _objectEntryManagerRegistry;

}