package com.boi.headless.rest.jansuraksha.client.resource.v1_0;

import com.boi.headless.rest.jansuraksha.client.dto.v1_0.JanSurakshaResponse;
import com.boi.headless.rest.jansuraksha.client.dto.v1_0.JanSurakshaUploadRequest;
import com.boi.headless.rest.jansuraksha.client.dto.v1_0.JanSurakshaUploadResponse;
import com.boi.headless.rest.jansuraksha.client.http.HttpInvoker;
import com.boi.headless.rest.jansuraksha.client.pagination.Pagination;
import com.boi.headless.rest.jansuraksha.client.problem.Problem;
import com.boi.headless.rest.jansuraksha.client.serdes.v1_0.JanSurakshaResponseSerDes;
import com.boi.headless.rest.jansuraksha.client.serdes.v1_0.JanSurakshaUploadResponseSerDes;

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
public interface JanSurakshaResource {

	public static Builder builder() {
		return new Builder();
	}

	public JanSurakshaUploadResponse uploadConsentDocument(
			JanSurakshaUploadRequest janSurakshaUploadRequest)
		throws Exception;

	public HttpInvoker.HttpResponse uploadConsentDocumentHttpResponse(
			JanSurakshaUploadRequest janSurakshaUploadRequest)
		throws Exception;

	public JanSurakshaResponse getJanSurakshaRecords(
			String accountNumber, String branchCode, String customerName,
			String typeOfInsurance, String urn, String year,
			String documentFileName, String documentFileType,
			java.util.Date fromDate, java.util.Date toDate,
			Pagination pagination)
		throws Exception;

	public HttpInvoker.HttpResponse getJanSurakshaRecordsHttpResponse(
			String accountNumber, String branchCode, String customerName,
			String typeOfInsurance, String urn, String year,
			String documentFileName, String documentFileType,
			java.util.Date fromDate, java.util.Date toDate,
			Pagination pagination)
		throws Exception;

	public JanSurakshaResponse getJanSurakshaRecord(Integer id)
		throws Exception;

	public HttpInvoker.HttpResponse getJanSurakshaRecordHttpResponse(Integer id)
		throws Exception;

	public static class Builder {

		public Builder authentication(String login, String password) {
			_login = login;
			_password = password;

			return this;
		}

		public JanSurakshaResource build() {
			return new JanSurakshaResourceImpl(this);
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

	public static class JanSurakshaResourceImpl implements JanSurakshaResource {

		public JanSurakshaUploadResponse uploadConsentDocument(
				JanSurakshaUploadRequest janSurakshaUploadRequest)
			throws Exception {

			HttpInvoker.HttpResponse httpResponse =
				uploadConsentDocumentHttpResponse(janSurakshaUploadRequest);

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
				return JanSurakshaUploadResponseSerDes.toDTO(content);
			}
			catch (Exception e) {
				_logger.log(
					Level.WARNING,
					"Unable to process HTTP response: " + content, e);

				throw new Problem.ProblemException(Problem.toDTO(content));
			}
		}

		public HttpInvoker.HttpResponse uploadConsentDocumentHttpResponse(
				JanSurakshaUploadRequest janSurakshaUploadRequest)
			throws Exception {

			HttpInvoker httpInvoker = HttpInvoker.newHttpInvoker();

			httpInvoker.body(
				janSurakshaUploadRequest.toString(), "application/json");

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
					_builder._port +
						"/o/jansuraksha-headless-rest/v1.0/jansuraksha/consent/upload");

			httpInvoker.userNameAndPassword(
				_builder._login + ":" + _builder._password);

			return httpInvoker.invoke();
		}

		public JanSurakshaResponse getJanSurakshaRecords(
				String accountNumber, String branchCode, String customerName,
				String typeOfInsurance, String urn, String year,
				String documentFileName, String documentFileType,
				java.util.Date fromDate, java.util.Date toDate,
				Pagination pagination)
			throws Exception {

			HttpInvoker.HttpResponse httpResponse =
				getJanSurakshaRecordsHttpResponse(
					accountNumber, branchCode, customerName, typeOfInsurance,
					urn, year, documentFileName, documentFileType, fromDate,
					toDate, pagination);

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
				return JanSurakshaResponseSerDes.toDTO(content);
			}
			catch (Exception e) {
				_logger.log(
					Level.WARNING,
					"Unable to process HTTP response: " + content, e);

				throw new Problem.ProblemException(Problem.toDTO(content));
			}
		}

		public HttpInvoker.HttpResponse getJanSurakshaRecordsHttpResponse(
				String accountNumber, String branchCode, String customerName,
				String typeOfInsurance, String urn, String year,
				String documentFileName, String documentFileType,
				java.util.Date fromDate, java.util.Date toDate,
				Pagination pagination)
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

			if (accountNumber != null) {
				httpInvoker.parameter(
					"accountNumber", String.valueOf(accountNumber));
			}

			if (branchCode != null) {
				httpInvoker.parameter("branchCode", String.valueOf(branchCode));
			}

			if (customerName != null) {
				httpInvoker.parameter(
					"customerName", String.valueOf(customerName));
			}

			if (typeOfInsurance != null) {
				httpInvoker.parameter(
					"typeOfInsurance", String.valueOf(typeOfInsurance));
			}

			if (urn != null) {
				httpInvoker.parameter("urn", String.valueOf(urn));
			}

			if (year != null) {
				httpInvoker.parameter("year", String.valueOf(year));
			}

			if (documentFileName != null) {
				httpInvoker.parameter(
					"documentFileName", String.valueOf(documentFileName));
			}

			if (documentFileType != null) {
				httpInvoker.parameter(
					"documentFileType", String.valueOf(documentFileType));
			}

			if (fromDate != null) {
				httpInvoker.parameter(
					"fromDate", liferayToJSONDateFormat.format(fromDate));
			}

			if (toDate != null) {
				httpInvoker.parameter(
					"toDate", liferayToJSONDateFormat.format(toDate));
			}

			if (pagination != null) {
				httpInvoker.parameter(
					"page", String.valueOf(pagination.getPage()));
				httpInvoker.parameter(
					"pageSize", String.valueOf(pagination.getPageSize()));
			}

			httpInvoker.path(
				_builder._scheme + "://" + _builder._host + ":" +
					_builder._port +
						"/o/jansuraksha-headless-rest/v1.0/jansuraksha/consent/records");

			httpInvoker.userNameAndPassword(
				_builder._login + ":" + _builder._password);

			return httpInvoker.invoke();
		}

		public JanSurakshaResponse getJanSurakshaRecord(Integer id)
			throws Exception {

			HttpInvoker.HttpResponse httpResponse =
				getJanSurakshaRecordHttpResponse(id);

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
				return JanSurakshaResponseSerDes.toDTO(content);
			}
			catch (Exception e) {
				_logger.log(
					Level.WARNING,
					"Unable to process HTTP response: " + content, e);

				throw new Problem.ProblemException(Problem.toDTO(content));
			}
		}

		public HttpInvoker.HttpResponse getJanSurakshaRecordHttpResponse(
				Integer id)
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
						"/o/jansuraksha-headless-rest/v1.0/jansuraksha/consent/record/{id}");

			httpInvoker.path("id", id);

			httpInvoker.userNameAndPassword(
				_builder._login + ":" + _builder._password);

			return httpInvoker.invoke();
		}

		private JanSurakshaResourceImpl(Builder builder) {
			_builder = builder;
		}

		private static final Logger _logger = Logger.getLogger(
			JanSurakshaResource.class.getName());

		private Builder _builder;

	}

}