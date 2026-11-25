package com.boi.headless.rest.consent.resource.v1_0.test;

import com.boi.headless.rest.consent.client.dto.v1_0.BOIHeadlessResponse;
import com.boi.headless.rest.consent.client.dto.v1_0.ConsentResponse;
import com.boi.headless.rest.consent.client.dto.v1_0.StatusCheckResponse;
import com.boi.headless.rest.consent.client.http.HttpInvoker;
import com.boi.headless.rest.consent.client.pagination.Page;
import com.boi.headless.rest.consent.client.resource.v1_0.ConsentResource;

import com.liferay.petra.reflect.ReflectionUtil;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Company;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.service.CompanyLocalServiceUtil;
import com.liferay.portal.kernel.test.util.GroupTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.util.ArrayUtil;
import com.liferay.portal.kernel.util.DateFormatFactoryUtil;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.odata.entity.EntityField;
import com.liferay.portal.odata.entity.EntityModel;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.vulcan.resource.EntityModelResource;

import java.lang.reflect.InvocationTargetException;

import java.text.DateFormat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Generated;

import javax.ws.rs.core.MultivaluedHashMap;

import org.apache.commons.beanutils.BeanUtilsBean;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author shehn
 * @generated
 */
@Generated("")
public abstract class BaseConsentResourceTestCase {

	@ClassRule
	@Rule
	public static final LiferayIntegrationTestRule liferayIntegrationTestRule =
		new LiferayIntegrationTestRule();

	@BeforeClass
	public static void setUpClass() throws Exception {
		_dateFormat = DateFormatFactoryUtil.getSimpleDateFormat(
			"yyyy-MM-dd'T'HH:mm:ss'Z'");
	}

	@Before
	public void setUp() throws Exception {
		irrelevantGroup = GroupTestUtil.addGroup();
		testGroup = GroupTestUtil.addGroup();

		testCompany = CompanyLocalServiceUtil.getCompany(
			testGroup.getCompanyId());

		_consentResource.setContextCompany(testCompany);

		ConsentResource.Builder builder = ConsentResource.builder();

		consentResource = builder.authentication(
			"test@liferay.com", "test"
		).locale(
			LocaleUtil.getDefault()
		).build();
	}

	@After
	public void tearDown() throws Exception {
		GroupTestUtil.deleteGroup(irrelevantGroup);
		GroupTestUtil.deleteGroup(testGroup);
	}

	@Test
	public void testPushConsentMetadata() throws Exception {
		Assert.assertTrue(true);
	}

	@Test
	public void testUpdateURN() throws Exception {
		Assert.assertTrue(true);
	}

	@Test
	public void testGetConsentRecords() throws Exception {
		Consent postConsent = testGetConsent_addConsent();

		BOIHeadlessResponse postBOIHeadlessResponse =
			testGetConsentRecords_addBOIHeadlessResponse(
				postConsent.getId(), randomBOIHeadlessResponse());

		BOIHeadlessResponse getBOIHeadlessResponse =
			consentResource.getConsentRecords(postConsent.getId());

		assertEquals(postBOIHeadlessResponse, getBOIHeadlessResponse);
		assertValid(getBOIHeadlessResponse);
	}

	protected BOIHeadlessResponse testGetConsentRecords_addBOIHeadlessResponse(
			long consentId, BOIHeadlessResponse boiHeadlessResponse)
		throws Exception {

		throw new UnsupportedOperationException(
			"This method needs to be implemented");
	}

	@Test
	public void testGetConsentRecord() throws Exception {
		Consent postConsent = testGetConsent_addConsent();

		BOIHeadlessResponse postBOIHeadlessResponse =
			testGetConsentRecord_addBOIHeadlessResponse(
				postConsent.getId(), randomBOIHeadlessResponse());

		BOIHeadlessResponse getBOIHeadlessResponse =
			consentResource.getConsentRecord(postConsent.getId());

		assertEquals(postBOIHeadlessResponse, getBOIHeadlessResponse);
		assertValid(getBOIHeadlessResponse);
	}

	protected BOIHeadlessResponse testGetConsentRecord_addBOIHeadlessResponse(
			long consentId, BOIHeadlessResponse boiHeadlessResponse)
		throws Exception {

		throw new UnsupportedOperationException(
			"This method needs to be implemented");
	}

	@Test
	public void testAddConsentAsDraft() throws Exception {
		Assert.assertTrue(true);
	}

	@Test
	public void testAddConsentAsExport() throws Exception {
		Assert.assertTrue(true);
	}

	@Test
	public void testCheckDocumentStatus() throws Exception {
		Consent postConsent = testGetConsent_addConsent();

		StatusCheckResponse postStatusCheckResponse =
			testCheckDocumentStatus_addStatusCheckResponse(
				postConsent.getId(), randomStatusCheckResponse());

		StatusCheckResponse getStatusCheckResponse =
			consentResource.checkDocumentStatus(postConsent.getId());

		assertEquals(postStatusCheckResponse, getStatusCheckResponse);
		assertValid(getStatusCheckResponse);
	}

	protected StatusCheckResponse
			testCheckDocumentStatus_addStatusCheckResponse(
				long consentId, StatusCheckResponse statusCheckResponse)
		throws Exception {

		return consentResource.checkDocumentStatus(
			consentId, statusCheckResponse);
	}

	protected void assertContains(Object consent, List<Object> consents) {
		boolean contains = false;

		for (Object item : consents) {
			if (equals(consent, item)) {
				contains = true;

				break;
			}
		}

		Assert.assertTrue(consents + " does not contain " + consent, contains);
	}

	protected void assertHttpResponseStatusCode(
		int expectedHttpResponseStatusCode,
		HttpInvoker.HttpResponse actualHttpResponse) {

		Assert.assertEquals(
			expectedHttpResponseStatusCode, actualHttpResponse.getStatusCode());
	}

	protected void assertEquals(Object consent1, Object consent2) {
		Assert.assertTrue(
			consent1 + " does not equal " + consent2,
			equals(consent1, consent2));
	}

	protected void assertEquals(
		List<Object> consents1, List<Object> consents2) {

		Assert.assertEquals(consents1.size(), consents2.size());

		for (int i = 0; i < consents1.size(); i++) {
			Object consent1 = consents1.get(i);
			Object consent2 = consents2.get(i);

			assertEquals(consent1, consent2);
		}
	}

	protected void assertEquals(
		ConsentResponse consentResponse1, ConsentResponse consentResponse2) {

		Assert.assertTrue(
			consentResponse1 + " does not equal " + consentResponse2,
			equals(consentResponse1, consentResponse2));
	}

	protected void assertEquals(
		BOIHeadlessResponse boiHeadlessResponse1,
		BOIHeadlessResponse boiHeadlessResponse2) {

		Assert.assertTrue(
			boiHeadlessResponse1 + " does not equal " + boiHeadlessResponse2,
			equals(boiHeadlessResponse1, boiHeadlessResponse2));
	}

	protected void assertEquals(
		StatusCheckResponse statusCheckResponse1,
		StatusCheckResponse statusCheckResponse2) {

		Assert.assertTrue(
			statusCheckResponse1 + " does not equal " + statusCheckResponse2,
			equals(statusCheckResponse1, statusCheckResponse2));
	}

	protected void assertEqualsIgnoringOrder(
		List<Object> consents1, List<Object> consents2) {

		Assert.assertEquals(consents1.size(), consents2.size());

		for (Object consent1 : consents1) {
			boolean contains = false;

			for (Object consent2 : consents2) {
				if (equals(consent1, consent2)) {
					contains = true;

					break;
				}
			}

			Assert.assertTrue(
				consents2 + " does not contain " + consent1, contains);
		}
	}

	protected void assertValid(Object consent) throws Exception {
		boolean valid = true;

		for (String additionalAssertFieldName :
				getAdditionalAssertFieldNames()) {

			throw new IllegalArgumentException(
				"Invalid additional assert field name " +
					additionalAssertFieldName);
		}

		Assert.assertTrue(valid);
	}

	protected void assertValid(Page<Object> page) {
		boolean valid = false;

		java.util.Collection<Object> consents = page.getItems();

		int size = consents.size();

		if ((page.getLastPage() > 0) && (page.getPage() > 0) &&
			(page.getPageSize() > 0) && (page.getTotalCount() > 0) &&
			(size > 0)) {

			valid = true;
		}

		Assert.assertTrue(valid);
	}

	protected void assertValid(ConsentResponse consentResponse) {
		boolean valid = true;

		for (String additionalAssertFieldName :
				getAdditionalConsentResponseAssertFieldNames()) {

			if (Objects.equals(
					"cbsReferenceNumber", additionalAssertFieldName)) {

				if (consentResponse.getCbsReferenceNumber() == null) {
					valid = false;
				}

				continue;
			}

			if (Objects.equals(
					"documentReferenceNo", additionalAssertFieldName)) {

				if (consentResponse.getDocumentReferenceNo() == null) {
					valid = false;
				}

				continue;
			}

			if (Objects.equals("entryId", additionalAssertFieldName)) {
				if (consentResponse.getEntryId() == null) {
					valid = false;
				}

				continue;
			}

			if (Objects.equals("message", additionalAssertFieldName)) {
				if (consentResponse.getMessage() == null) {
					valid = false;
				}

				continue;
			}

			if (Objects.equals("stage", additionalAssertFieldName)) {
				if (consentResponse.getStage() == null) {
					valid = false;
				}

				continue;
			}

			if (Objects.equals("success", additionalAssertFieldName)) {
				if (consentResponse.getSuccess() == null) {
					valid = false;
				}

				continue;
			}

			throw new IllegalArgumentException(
				"Invalid additional assert field name " +
					additionalAssertFieldName);
		}

		Assert.assertTrue(valid);
	}

	protected void assertValid(BOIHeadlessResponse boiHeadlessResponse) {
		boolean valid = true;

		for (String additionalAssertFieldName :
				getAdditionalBOIHeadlessResponseAssertFieldNames()) {

			if (Objects.equals("responseId", additionalAssertFieldName)) {
				if (boiHeadlessResponse.getResponseId() == null) {
					valid = false;
				}

				continue;
			}

			if (Objects.equals("responseStatus", additionalAssertFieldName)) {
				if (boiHeadlessResponse.getResponseStatus() == null) {
					valid = false;
				}

				continue;
			}

			throw new IllegalArgumentException(
				"Invalid additional assert field name " +
					additionalAssertFieldName);
		}

		Assert.assertTrue(valid);
	}

	protected void assertValid(StatusCheckResponse statusCheckResponse) {
		boolean valid = true;

		for (String additionalAssertFieldName :
				getAdditionalStatusCheckResponseAssertFieldNames()) {

			if (Objects.equals(
					"documentReferenceNo", additionalAssertFieldName)) {

				if (statusCheckResponse.getDocumentReferenceNo() == null) {
					valid = false;
				}

				continue;
			}

			if (Objects.equals("documentVerified", additionalAssertFieldName)) {
				if (statusCheckResponse.getDocumentVerified() == null) {
					valid = false;
				}

				continue;
			}

			if (Objects.equals("message", additionalAssertFieldName)) {
				if (statusCheckResponse.getMessage() == null) {
					valid = false;
				}

				continue;
			}

			if (Objects.equals("stage", additionalAssertFieldName)) {
				if (statusCheckResponse.getStage() == null) {
					valid = false;
				}

				continue;
			}

			throw new IllegalArgumentException(
				"Invalid additional assert field name " +
					additionalAssertFieldName);
		}

		Assert.assertTrue(valid);
	}

	protected String[] getAdditionalAssertFieldNames() {
		return new String[0];
	}

	protected String[] getAdditionalConsentResponseAssertFieldNames() {
		return new String[0];
	}

	protected String[] getAdditionalBOIHeadlessResponseAssertFieldNames() {
		return new String[0];
	}

	protected String[] getAdditionalStatusCheckResponseAssertFieldNames() {
		return new String[0];
	}

	protected List<GraphQLField> getGraphQLFields() throws Exception {
		List<GraphQLField> graphQLFields = new ArrayList<>();

		return graphQLFields;
	}

	protected List<GraphQLField> getGraphQLFields(
			java.lang.reflect.Field... fields)
		throws Exception {

		List<GraphQLField> graphQLFields = new ArrayList<>();

		for (java.lang.reflect.Field field : fields) {
			com.liferay.portal.vulcan.graphql.annotation.GraphQLField
				vulcanGraphQLField = field.getAnnotation(
					com.liferay.portal.vulcan.graphql.annotation.GraphQLField.
						class);

			if (vulcanGraphQLField != null) {
				Class<?> clazz = field.getType();

				if (clazz.isArray()) {
					clazz = clazz.getComponentType();
				}

				List<GraphQLField> childrenGraphQLFields = getGraphQLFields(
					getDeclaredFields(clazz));

				graphQLFields.add(
					new GraphQLField(field.getName(), childrenGraphQLFields));
			}
		}

		return graphQLFields;
	}

	protected String[] getIgnoredEntityFieldNames() {
		return new String[0];
	}

	protected boolean equals(Object consent1, Object consent2) {
		if (consent1 == consent2) {
			return true;
		}

		for (String additionalAssertFieldName :
				getAdditionalAssertFieldNames()) {

			throw new IllegalArgumentException(
				"Invalid additional assert field name " +
					additionalAssertFieldName);
		}

		return true;
	}

	protected boolean equals(
		Map<String, Object> map1, Map<String, Object> map2) {

		if (Objects.equals(map1.keySet(), map2.keySet())) {
			for (Map.Entry<String, Object> entry : map1.entrySet()) {
				if (entry.getValue() instanceof Map) {
					if (!equals(
							(Map)entry.getValue(),
							(Map)map2.get(entry.getKey()))) {

						return false;
					}
				}
				else if (!Objects.deepEquals(
							entry.getValue(), map2.get(entry.getKey()))) {

					return false;
				}
			}

			return true;
		}

		return false;
	}

	protected boolean equals(
		ConsentResponse consentResponse1, ConsentResponse consentResponse2) {

		if (consentResponse1 == consentResponse2) {
			return true;
		}

		for (String additionalAssertFieldName :
				getAdditionalConsentResponseAssertFieldNames()) {

			if (Objects.equals(
					"cbsReferenceNumber", additionalAssertFieldName)) {

				if (!Objects.deepEquals(
						consentResponse1.getCbsReferenceNumber(),
						consentResponse2.getCbsReferenceNumber())) {

					return false;
				}

				continue;
			}

			if (Objects.equals(
					"documentReferenceNo", additionalAssertFieldName)) {

				if (!Objects.deepEquals(
						consentResponse1.getDocumentReferenceNo(),
						consentResponse2.getDocumentReferenceNo())) {

					return false;
				}

				continue;
			}

			if (Objects.equals("entryId", additionalAssertFieldName)) {
				if (!Objects.deepEquals(
						consentResponse1.getEntryId(),
						consentResponse2.getEntryId())) {

					return false;
				}

				continue;
			}

			if (Objects.equals("message", additionalAssertFieldName)) {
				if (!Objects.deepEquals(
						consentResponse1.getMessage(),
						consentResponse2.getMessage())) {

					return false;
				}

				continue;
			}

			if (Objects.equals("stage", additionalAssertFieldName)) {
				if (!Objects.deepEquals(
						consentResponse1.getStage(),
						consentResponse2.getStage())) {

					return false;
				}

				continue;
			}

			if (Objects.equals("success", additionalAssertFieldName)) {
				if (!Objects.deepEquals(
						consentResponse1.getSuccess(),
						consentResponse2.getSuccess())) {

					return false;
				}

				continue;
			}

			throw new IllegalArgumentException(
				"Invalid additional assert field name " +
					additionalAssertFieldName);
		}

		return true;
	}

	protected boolean equals(
		BOIHeadlessResponse boiHeadlessResponse1,
		BOIHeadlessResponse boiHeadlessResponse2) {

		if (boiHeadlessResponse1 == boiHeadlessResponse2) {
			return true;
		}

		for (String additionalAssertFieldName :
				getAdditionalBOIHeadlessResponseAssertFieldNames()) {

			if (Objects.equals("responseId", additionalAssertFieldName)) {
				if (!Objects.deepEquals(
						boiHeadlessResponse1.getResponseId(),
						boiHeadlessResponse2.getResponseId())) {

					return false;
				}

				continue;
			}

			if (Objects.equals("responseStatus", additionalAssertFieldName)) {
				if (!Objects.deepEquals(
						boiHeadlessResponse1.getResponseStatus(),
						boiHeadlessResponse2.getResponseStatus())) {

					return false;
				}

				continue;
			}

			throw new IllegalArgumentException(
				"Invalid additional assert field name " +
					additionalAssertFieldName);
		}

		return true;
	}

	protected boolean equals(
		StatusCheckResponse statusCheckResponse1,
		StatusCheckResponse statusCheckResponse2) {

		if (statusCheckResponse1 == statusCheckResponse2) {
			return true;
		}

		for (String additionalAssertFieldName :
				getAdditionalStatusCheckResponseAssertFieldNames()) {

			if (Objects.equals(
					"documentReferenceNo", additionalAssertFieldName)) {

				if (!Objects.deepEquals(
						statusCheckResponse1.getDocumentReferenceNo(),
						statusCheckResponse2.getDocumentReferenceNo())) {

					return false;
				}

				continue;
			}

			if (Objects.equals("documentVerified", additionalAssertFieldName)) {
				if (!Objects.deepEquals(
						statusCheckResponse1.getDocumentVerified(),
						statusCheckResponse2.getDocumentVerified())) {

					return false;
				}

				continue;
			}

			if (Objects.equals("message", additionalAssertFieldName)) {
				if (!Objects.deepEquals(
						statusCheckResponse1.getMessage(),
						statusCheckResponse2.getMessage())) {

					return false;
				}

				continue;
			}

			if (Objects.equals("stage", additionalAssertFieldName)) {
				if (!Objects.deepEquals(
						statusCheckResponse1.getStage(),
						statusCheckResponse2.getStage())) {

					return false;
				}

				continue;
			}

			throw new IllegalArgumentException(
				"Invalid additional assert field name " +
					additionalAssertFieldName);
		}

		return true;
	}

	protected java.lang.reflect.Field[] getDeclaredFields(Class clazz)
		throws Exception {

		Stream<java.lang.reflect.Field> stream = Stream.of(
			ReflectionUtil.getDeclaredFields(clazz));

		return stream.filter(
			field -> !field.isSynthetic()
		).toArray(
			java.lang.reflect.Field[]::new
		);
	}

	protected java.util.Collection<EntityField> getEntityFields()
		throws Exception {

		if (!(_consentResource instanceof EntityModelResource)) {
			throw new UnsupportedOperationException(
				"Resource is not an instance of EntityModelResource");
		}

		EntityModelResource entityModelResource =
			(EntityModelResource)_consentResource;

		EntityModel entityModel = entityModelResource.getEntityModel(
			new MultivaluedHashMap());

		Map<String, EntityField> entityFieldsMap =
			entityModel.getEntityFieldsMap();

		return entityFieldsMap.values();
	}

	protected List<EntityField> getEntityFields(EntityField.Type type)
		throws Exception {

		java.util.Collection<EntityField> entityFields = getEntityFields();

		Stream<EntityField> stream = entityFields.stream();

		return stream.filter(
			entityField ->
				Objects.equals(entityField.getType(), type) &&
				!ArrayUtil.contains(
					getIgnoredEntityFieldNames(), entityField.getName())
		).collect(
			Collectors.toList()
		);
	}

	protected String getFilterString(
		EntityField entityField, String operator, Object consent) {

		StringBundler sb = new StringBundler();

		String entityFieldName = entityField.getName();

		sb.append(entityFieldName);

		sb.append(" ");
		sb.append(operator);
		sb.append(" ");

		throw new IllegalArgumentException(
			"Invalid entity field " + entityFieldName);
	}

	protected String invoke(String query) throws Exception {
		HttpInvoker httpInvoker = HttpInvoker.newHttpInvoker();

		httpInvoker.body(
			JSONUtil.put(
				"query", query
			).toString(),
			"application/json");
		httpInvoker.httpMethod(HttpInvoker.HttpMethod.POST);
		httpInvoker.path("http://localhost:8080/o/graphql");
		httpInvoker.userNameAndPassword("test@liferay.com:test");

		HttpInvoker.HttpResponse httpResponse = httpInvoker.invoke();

		return httpResponse.getContent();
	}

	protected JSONObject invokeGraphQLMutation(GraphQLField graphQLField)
		throws Exception {

		GraphQLField mutationGraphQLField = new GraphQLField(
			"mutation", graphQLField);

		return JSONFactoryUtil.createJSONObject(
			invoke(mutationGraphQLField.toString()));
	}

	protected JSONObject invokeGraphQLQuery(GraphQLField graphQLField)
		throws Exception {

		GraphQLField queryGraphQLField = new GraphQLField(
			"query", graphQLField);

		return JSONFactoryUtil.createJSONObject(
			invoke(queryGraphQLField.toString()));
	}

	protected ConsentResponse randomConsentResponse() throws Exception {
		return new ConsentResponse() {
			{
				cbsReferenceNumber = RandomTestUtil.randomString();
				documentReferenceNo = RandomTestUtil.randomString();
				entryId = RandomTestUtil.randomLong();
				message = RandomTestUtil.randomString();
				stage = RandomTestUtil.randomInteger();
				success = RandomTestUtil.randomBoolean();
			}
		};
	}

	protected BOIHeadlessResponse randomBOIHeadlessResponse() throws Exception {
		return new BOIHeadlessResponse() {
			{
				responseId = RandomTestUtil.randomInteger();
				responseStatus = RandomTestUtil.randomString();
			}
		};
	}

	protected StatusCheckResponse randomStatusCheckResponse() throws Exception {
		return new StatusCheckResponse() {
			{
				documentReferenceNo = RandomTestUtil.randomString();
				documentVerified = RandomTestUtil.randomBoolean();
				message = RandomTestUtil.randomString();
				stage = RandomTestUtil.randomInteger();
			}
		};
	}

	protected ConsentResource consentResource;
	protected Group irrelevantGroup;
	protected Company testCompany;
	protected Group testGroup;

	protected class GraphQLField {

		public GraphQLField(String key, GraphQLField... graphQLFields) {
			this(key, new HashMap<>(), graphQLFields);
		}

		public GraphQLField(String key, List<GraphQLField> graphQLFields) {
			this(key, new HashMap<>(), graphQLFields);
		}

		public GraphQLField(
			String key, Map<String, Object> parameterMap,
			GraphQLField... graphQLFields) {

			_key = key;
			_parameterMap = parameterMap;
			_graphQLFields = Arrays.asList(graphQLFields);
		}

		public GraphQLField(
			String key, Map<String, Object> parameterMap,
			List<GraphQLField> graphQLFields) {

			_key = key;
			_parameterMap = parameterMap;
			_graphQLFields = graphQLFields;
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder(_key);

			if (!_parameterMap.isEmpty()) {
				sb.append("(");

				for (Map.Entry<String, Object> entry :
						_parameterMap.entrySet()) {

					sb.append(entry.getKey());
					sb.append(": ");
					sb.append(entry.getValue());
					sb.append(", ");
				}

				sb.setLength(sb.length() - 2);

				sb.append(")");
			}

			if (!_graphQLFields.isEmpty()) {
				sb.append("{");

				for (GraphQLField graphQLField : _graphQLFields) {
					sb.append(graphQLField.toString());
					sb.append(", ");
				}

				sb.setLength(sb.length() - 2);

				sb.append("}");
			}

			return sb.toString();
		}

		private final List<GraphQLField> _graphQLFields;
		private final String _key;
		private final Map<String, Object> _parameterMap;

	}

	private static final com.liferay.portal.kernel.log.Log _log =
		LogFactoryUtil.getLog(BaseConsentResourceTestCase.class);

	private static BeanUtilsBean _beanUtilsBean = new BeanUtilsBean() {

		@Override
		public void copyProperty(Object bean, String name, Object value)
			throws IllegalAccessException, InvocationTargetException {

			if (value != null) {
				super.copyProperty(bean, name, value);
			}
		}

	};
	private static DateFormat _dateFormat;

	@Inject
	private com.boi.headless.rest.consent.resource.v1_0.ConsentResource
		_consentResource;

}