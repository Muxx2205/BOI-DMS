package com.boi.headless.rest.dms.client.resource.v1_0;

import com.boi.headless.rest.dms.client.dto.v1_0.BOIHeadlessResponse;
import com.boi.headless.rest.dms.client.dto.v1_0.Base64ConsentUploadRequest;
import com.boi.headless.rest.dms.client.dto.v1_0.ConsentUploadResponse;
import com.boi.headless.rest.dms.client.http.HttpInvoker;
import com.boi.headless.rest.dms.client.pagination.Pagination;
import com.boi.headless.rest.dms.client.problem.Problem;
import com.boi.headless.rest.dms.client.serdes.v1_0.BOIHeadlessResponseSerDes;
import com.boi.headless.rest.dms.client.serdes.v1_0.ConsentUploadResponseSerDes;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Generated;

/**
 * @author shehn
 * @generated
 */
@Generated("")
public interface ConsentResource {

	public static Builder builder() {
		return new Builder();
	}

	public ConsentUploadResponse uploadConsentDocument(
			Base64ConsentUploadRequest base64ConsentUploadRequest)
		throws Exception;

	public HttpInvoker.HttpResponse uploadConsentDocumentHttpResponse(
			Base64ConsentUploadRequest base64ConsentUploadRequest)
		throws Exception;

	public BOIHeadlessResponse getConsentRecords(
			String search, String branchCode, String insuranceType,
			String policyYear, String mobileNumber, String bankAccountNo,
			String urn, java.util.Date fromDate, java.util.Date toDate,
			String filterString, Pagination pagination, String sortString)
		throws Exception;

	public HttpInvoker.HttpResponse getConsentRecordsHttpResponse(
			String search, String branchCode, String insuranceType,
			String policyYear, String mobileNumber, String bankAccountNo,
			String urn, java.util.Date fromDate, java.util.Date toDate,
			String filterString, Pagination pagination, String sortString)
		throws Exception;

	public BOIHeadlessResponse getConsentRecord(Integer id) throws Exception;

	public HttpInvoker.HttpResponse getConsentRecordHttpResponse(Integer id)
		throws Exception;

	public static class Builder {

		public Builder authentication(String login, String password) {
			_login = login;
			_password = password;

			return this;
		}

		public ConsentResource build() {
			return new ConsentResourceImpl(this);
		}

		public Builder endpoint(String host, int port, String scheme) {
			_host = host;
			_port = port;
			_scheme = scheme;

			return this;
		}

		public Builder header(String key, String value) {
			_headers.put(key, value);

			return this;
		}

		public Builder locale(Locale locale) {
			_locale = locale;

			return this;
		}

		public Builder parameter(String key, String value) {
			_parameters.put(key, value);

			return this;
		}

		public Builder parameters(String... parameters) {
			if ((parameters.length % 2) != 0) {
				throw new IllegalArgumentException(
					"Parameters length is not an even number");
			}

			for (int i = 0; i < parameters.length; i += 2) {
				String parameterName = String.valueOf(parameters[i]);
				String parameterValue = String.valueOf(parameters[i + 1]);

				_parameters.put(parameterName, parameterValue);
			}

			return this;
		}

		private Builder() {
		}

		private Map<String, String> _headers = new LinkedHashMap<>();
		private String _host = "localhost";
		private Locale _locale;
		private String _login = "";
		private String _password = "";
		private Map<String, String> _parameters = new LinkedHashMap<>();
		private int _port = 8080;
		private String _scheme = "http";

	}

	public static class ConsentResourceImpl implements ConsentResource {

		public ConsentUploadResponse uploadConsentDocument(
				Base64ConsentUploadRequest base64ConsentUploadRequest)
			throws Exception {

			HttpInvoker.HttpResponse httpResponse =
				uploadConsentDocumentHttpResponse(base64ConsentUploadRequest);

			String content = httpResponse.getContent();

			if ((httpResponse.getStatusCode() / 100) != 2) {
				_logger.log(
					Level.WARNING,
					"Unable to process HTTP response content: " + content);
				_logger.log(
					Level.WARNING,
					"HTTP response message: " + httpResponse.getMessage());
				_logger.log(
					Level.WARNING,
					"HTTP response status code: " +
						httpResponse.getStatusCode());

				throw new Problem.ProblemException(Problem.toDTO(content));
			}
			else {
				_logger.fine("HTTP response content: " + content);
				_logger.fine(
					"HTTP response message: " + httpResponse.getMessage());
				_logger.fine(
					"HTTP response status code: " +
						httpResponse.getStatusCode());
			}

			try {
				return ConsentUploadResponseSerDes.toDTO(content);
			}
			catch (Exception e) {
				_logger.log(
					Level.WARNING,
					"Unable to process HTTP response: " + content, e);

				throw new Problem.ProblemException(Problem.toDTO(content));
			}
		}

		public HttpInvoker.HttpResponse uploadConsentDocumentHttpResponse(
				Base64ConsentUploadRequest base64ConsentUploadRequest)
			throws Exception {

			HttpInvoker httpInvoker = HttpInvoker.newHttpInvoker();

			httpInvoker.body(
				base64ConsentUploadRequest.toString(), "application/json");

			if (_builder._locale != null) {
				httpInvoker.header(
					"Accept-Language", _builder._locale.toLanguageTag());
			}

			for (Map.Entry<String, String> entry :
					_builder._headers.entrySet()) {

				httpInvoker.header(entry.getKey(), entry.getValue());
			}

			for (Map.Entry<String, String> entry :
					_builder._parameters.entrySet()) {

				httpInvoker.parameter(entry.getKey(), entry.getValue());
			}

			httpInvoker.httpMethod(HttpInvoker.HttpMethod.POST);

			httpInvoker.path(
				_builder._scheme + "://" + _builder._host + ":" +
					_builder._port + "/o/dms-consent-rest/v1.0/consent/upload");

			httpInvoker.userNameAndPassword(
				_builder._login + ":" + _builder._password);

			return httpInvoker.invoke();
		}

		public BOIHeadlessResponse getConsentRecords(
				String search, String branchCode, String insuranceType,
				String policyYear, String mobileNumber, String bankAccountNo,
				String urn, java.util.Date fromDate, java.util.Date toDate,
				String filterString, Pagination pagination, String sortString)
			throws Exception {

			HttpInvoker.HttpResponse httpResponse =
				getConsentRecordsHttpResponse(
					search, branchCode, insuranceType, policyYear, mobileNumber,
					bankAccountNo, urn, fromDate, toDate, filterString,
					pagination, sortString);

			String content = httpResponse.getContent();

			if ((httpResponse.getStatusCode() / 100) != 2) {
				_logger.log(
					Level.WARNING,
					"Unable to process HTTP response content: " + content);
				_logger.log(
					Level.WARNING,
					"HTTP response message: " + httpResponse.getMessage());
				_logger.log(
					Level.WARNING,
					"HTTP response status code: " +
						httpResponse.getStatusCode());

				throw new Problem.ProblemException(Problem.toDTO(content));
			}
			else {
				_logger.fine("HTTP response content: " + content);
				_logger.fine(
					"HTTP response message: " + httpResponse.getMessage());
				_logger.fine(
					"HTTP response status code: " +
						httpResponse.getStatusCode());
			}

			try {
				return BOIHeadlessResponseSerDes.toDTO(content);
			}
			catch (Exception e) {
				_logger.log(
					Level.WARNING,
					"Unable to process HTTP response: " + content, e);

				throw new Problem.ProblemException(Problem.toDTO(content));
			}
		}

		public HttpInvoker.HttpResponse getConsentRecordsHttpResponse(
				String search, String branchCode, String insuranceType,
				String policyYear, String mobileNumber, String bankAccountNo,
				String urn, java.util.Date fromDate, java.util.Date toDate,
				String filterString, Pagination pagination, String sortString)
			throws Exception {

			HttpInvoker httpInvoker = HttpInvoker.newHttpInvoker();

			if (_builder._locale != null) {
				httpInvoker.header(
					"Accept-Language", _builder._locale.toLanguageTag());
			}

			for (Map.Entry<String, String> entry :
					_builder._headers.entrySet()) {

				httpInvoker.header(entry.getKey(), entry.getValue());
			}

			for (Map.Entry<String, String> entry :
					_builder._parameters.entrySet()) {

				httpInvoker.parameter(entry.getKey(), entry.getValue());
			}

			httpInvoker.httpMethod(HttpInvoker.HttpMethod.GET);

			DateFormat liferayToJSONDateFormat = new SimpleDateFormat(
				"yyyy-MM-dd'T'HH:mm:ss'Z'");

			if (search != null) {
				httpInvoker.parameter("search", String.valueOf(search));
			}

			if (branchCode != null) {
				httpInvoker.parameter("branchCode", String.valueOf(branchCode));
			}

			if (insuranceType != null) {
				httpInvoker.parameter(
					"insuranceType", String.valueOf(insuranceType));
			}

			if (policyYear != null) {
				httpInvoker.parameter("policyYear", String.valueOf(policyYear));
			}

			if (mobileNumber != null) {
				httpInvoker.parameter(
					"mobileNumber", String.valueOf(mobileNumber));
			}

			if (bankAccountNo != null) {
				httpInvoker.parameter(
					"bankAccountNo", String.valueOf(bankAccountNo));
			}

			if (urn != null) {
				httpInvoker.parameter("urn", String.valueOf(urn));
			}

			if (fromDate != null) {
				httpInvoker.parameter(
					"fromDate", liferayToJSONDateFormat.format(fromDate));
			}

			if (toDate != null) {
				httpInvoker.parameter(
					"toDate", liferayToJSONDateFormat.format(toDate));
			}

			if (filterString != null) {
				httpInvoker.parameter("filter", filterString);
			}

			if (pagination != null) {
				httpInvoker.parameter(
					"page", String.valueOf(pagination.getPage()));
				httpInvoker.parameter(
					"pageSize", String.valueOf(pagination.getPageSize()));
			}

			if (sortString != null) {
				httpInvoker.parameter("sort", sortString);
			}

			httpInvoker.path(
				_builder._scheme + "://" + _builder._host + ":" +
					_builder._port +
						"/o/dms-consent-rest/v1.0/consent/records");

			httpInvoker.userNameAndPassword(
				_builder._login + ":" + _builder._password);

			return httpInvoker.invoke();
		}

		public BOIHeadlessResponse getConsentRecord(Integer id)
			throws Exception {

			HttpInvoker.HttpResponse httpResponse =
				getConsentRecordHttpResponse(id);

			String content = httpResponse.getContent();

			if ((httpResponse.getStatusCode() / 100) != 2) {
				_logger.log(
					Level.WARNING,
					"Unable to process HTTP response content: " + content);
				_logger.log(
					Level.WARNING,
					"HTTP response message: " + httpResponse.getMessage());
				_logger.log(
					Level.WARNING,
					"HTTP response status code: " +
						httpResponse.getStatusCode());

				throw new Problem.ProblemException(Problem.toDTO(content));
			}
			else {
				_logger.fine("HTTP response content: " + content);
				_logger.fine(
					"HTTP response message: " + httpResponse.getMessage());
				_logger.fine(
					"HTTP response status code: " +
						httpResponse.getStatusCode());
			}

			try {
				return BOIHeadlessResponseSerDes.toDTO(content);
			}
			catch (Exception e) {
				_logger.log(
					Level.WARNING,
					"Unable to process HTTP response: " + content, e);

				throw new Problem.ProblemException(Problem.toDTO(content));
			}
		}

		public HttpInvoker.HttpResponse getConsentRecordHttpResponse(Integer id)
			throws Exception {

			HttpInvoker httpInvoker = HttpInvoker.newHttpInvoker();

			if (_builder._locale != null) {
				httpInvoker.header(
					"Accept-Language", _builder._locale.toLanguageTag());
			}

			for (Map.Entry<String, String> entry :
					_builder._headers.entrySet()) {

				httpInvoker.header(entry.getKey(), entry.getValue());
			}

			for (Map.Entry<String, String> entry :
					_builder._parameters.entrySet()) {

				httpInvoker.parameter(entry.getKey(), entry.getValue());
			}

			httpInvoker.httpMethod(HttpInvoker.HttpMethod.GET);

			httpInvoker.path(
				_builder._scheme + "://" + _builder._host + ":" +
					_builder._port +
						"/o/dms-consent-rest/v1.0/consent/record/{id}");

			httpInvoker.path("id", id);

			httpInvoker.userNameAndPassword(
				_builder._login + ":" + _builder._password);

			return httpInvoker.invoke();
		}

		private ConsentResourceImpl(Builder builder) {
			_builder = builder;
		}

		private static final Logger _logger = Logger.getLogger(
			ConsentResource.class.getName());

		private Builder _builder;

	}

}