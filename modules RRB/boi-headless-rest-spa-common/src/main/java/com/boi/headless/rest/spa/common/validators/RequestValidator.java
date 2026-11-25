package com.boi.headless.rest.spa.common.validators;

import com.boi.headless.rest.spa.common.constants.BOIDMSConstants;

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

	private static final Log _log = LogFactoryUtil.getLog(RequestValidator.class);
	private static final long _MAX_FILE_SIZE_BYTES = 10L * 1024 * 1024;
	private static final Set<String> _ALLOWED_EXTENSIONS;

	static {
		Set<String> exts = new HashSet<>();
		exts.add("jpg"); exts.add("jpeg"); exts.add("png"); exts.add("tif");
		exts.add("pdf"); exts.add("doc"); exts.add("docx");
		exts.add("xls"); exts.add("xlsx"); exts.add("csv");
		exts.add("gif"); exts.add("txt");
		_ALLOWED_EXTENSIONS = Collections.unmodifiableSet(exts);
	}

	private final MultipartBody _multipartBody;
	private final String _className;

	public RequestValidator(MultipartBody multipartBody, String className) {
		_multipartBody = multipartBody;
		_className = className;
	}

	public void validateRequestFiles() throws Exception {
		Map<String, String> fileUploadMap = BOIDMSConstants.getObjectFileNames(_className);
		_validateBinaryFile("attachments");
		for (String fieldName : fileUploadMap.keySet()) {
			_validateBinaryFile(fieldName);
		}
	}

	private void _validateBinaryFile(String fieldName) throws Exception {
		BinaryFile binaryFile = _multipartBody.getBinaryFile(fieldName);
		if ((binaryFile == null) || Validator.isNull(binaryFile.getFileName())) return;
		_validateFileExtension(binaryFile);
		_validateFileSize(binaryFile);
	}

	private void _validateFileSize(BinaryFile binaryFile) throws Exception {
		if (binaryFile.getSize() > _MAX_FILE_SIZE_BYTES) {
			throw new Exception("File '" + binaryFile.getFileName() + "' exceeds maximum allowed size of 10 MB");
		}
	}

	private void _validateFileExtension(BinaryFile binaryFile) throws Exception {
		String fileName = binaryFile.getFileName();
		String extension = _extractExtension(fileName);
		if (!_ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
			throw new Exception("File extension '" + extension + "' is not allowed");
		}
	}

	private String _extractExtension(String fileName) throws Exception {
		int lastDotIndex = fileName.lastIndexOf('.');
		if ((lastDotIndex <= 0) || (lastDotIndex == (fileName.length() - 1))) {
			throw new Exception("Invalid file name: " + fileName);
		}
		return fileName.substring(lastDotIndex + 1);
	}
}