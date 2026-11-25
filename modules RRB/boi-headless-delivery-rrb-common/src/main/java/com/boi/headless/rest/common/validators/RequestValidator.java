package com.boi.headless.rest.common.validators;

import com.boi.headless.rest.common.constants.BOIDMSConstants;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.vulcan.multipart.BinaryFile;
import com.liferay.portal.vulcan.multipart.MultipartBody;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class RequestValidator {

	private static final Log log = LogFactoryUtil.getLog(RequestValidator.class);

	private static final long MAX_FILE_SIZE_BYTES = 10L * 1024 * 1024;

	private static final Set<String> ALLOWED_EXTENSIONS =
			Collections.unmodifiableSet(new HashSet<String>() {{
				add("jpg"); add("jpeg"); add("png"); add("tif");
				add("pdf"); add("doc"); add("docx");
				add("xls"); add("xlsx"); add("csv");
				add("gif"); add("txt");
			}});

	private final MultipartBody multipartBody;
	private final String className;

	public RequestValidator(MultipartBody multipartBody, String className) {
		this.multipartBody = multipartBody;
		this.className = className;
	}

	public void validateRequestFiles() throws Exception {

		Map<String, String> fileUploadMap =
				BOIDMSConstants.getObjectFileNames(className);

		log.debug("File validation matrix loaded for {} "+ className);

		validateBinaryFile("attachments");

		for (String fieldName : fileUploadMap.keySet()) {
			validateBinaryFile(fieldName);
		}
	}

	private void validateBinaryFile(String fieldName) throws Exception {

		BinaryFile binaryFile = multipartBody.getBinaryFile(fieldName);

		if (!isFilePresent(binaryFile)) {
			log.debug("No file found for field: {}"+ fieldName);
			return;
		}

		log.debug("Validating file '{}' for field '{}'"+
				binaryFile.getFileName()+ fieldName);

		validateFileExtension(binaryFile);
		validateFileSize(binaryFile);
	}

	private boolean isFilePresent(BinaryFile binaryFile) {
		return Validator.isNotNull(binaryFile) &&
			   Validator.isNotNull(binaryFile.getFileName());
	}

	private void validateFileSize(BinaryFile binaryFile) throws Exception {

		long fileSize = binaryFile.getSize();

		log.debug("File '{}' size: {} bytes (max allowed: {})"+
				binaryFile.getFileName()+" "+ fileSize+" "+ MAX_FILE_SIZE_BYTES);

		if (fileSize > MAX_FILE_SIZE_BYTES) {
			throw new Exception(
					"File '" + binaryFile.getFileName() +
					"' exceeds maximum allowed size of 10MB"
			);
		}
	}

	private void validateFileExtension(BinaryFile binaryFile) throws Exception {

		String fileName = binaryFile.getFileName();
		String extension = extractExtension(fileName);

		log.debug("Extracted extension '{}' from file '{}'"+ extension+" "+ fileName);

		if (!ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
			throw new Exception("File extension '" + extension + "' is not allowed");
		}
	}

	private String extractExtension(String fileName) throws Exception {

		int lastDotIndex = fileName.lastIndexOf('.');

		if (lastDotIndex <= 0 || lastDotIndex == fileName.length() - 1) {
			throw new Exception("Invalid file name: " + fileName);
		}

		return fileName.substring(lastDotIndex + 1);
	}
}
