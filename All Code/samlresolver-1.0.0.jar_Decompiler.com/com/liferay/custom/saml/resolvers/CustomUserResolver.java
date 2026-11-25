package com.liferay.custom.saml.resolvers;

import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.exception.UserEmailAddressException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Company;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.security.auth.CompanyThreadLocal;
import com.liferay.portal.kernel.service.CompanyLocalService;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.UserLocalService;
import com.liferay.portal.kernel.util.ArrayUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.ListUtil;
import com.liferay.portal.kernel.util.MapUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.saml.opensaml.integration.field.expression.handler.UserFieldExpressionHandler;
import com.liferay.saml.opensaml.integration.field.expression.handler.registry.UserFieldExpressionHandlerRegistry;
import com.liferay.saml.opensaml.integration.field.expression.resolver.UserFieldExpressionResolver;
import com.liferay.saml.opensaml.integration.field.expression.resolver.registry.UserFieldExpressionResolverRegistry;
import com.liferay.saml.opensaml.integration.processor.UserProcessor;
import com.liferay.saml.opensaml.integration.processor.factory.UserProcessorFactory;
import com.liferay.saml.opensaml.integration.resolver.UserResolver;
import com.liferay.saml.persistence.model.SamlPeerBinding;
import com.liferay.saml.persistence.model.SamlSpIdpConnection;
import com.liferay.saml.persistence.service.SamlPeerBindingLocalService;
import com.liferay.saml.persistence.service.SamlSpIdpConnectionLocalService;
import com.liferay.saml.runtime.configuration.SamlProviderConfigurationHelper;
import com.liferay.saml.runtime.exception.SubjectException;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(
   immediate = true,
   property = {"serviceranking:Integer=-2147482648"},
   service = {UserResolver.class}
)
public class CustomUserResolver implements UserResolver {
   private static Log _log = null;
   @Reference
   private CompanyLocalService _companyLocalService;
   @Reference
   private SamlPeerBindingLocalService _samlPeerBindingLocalService;
   @Reference
   private SamlProviderConfigurationHelper _samlProviderConfigurationHelper;
   @Reference
   private SamlSpIdpConnectionLocalService _samlSpIdpConnectionLocalService;
   @Reference
   private UserFieldExpressionHandlerRegistry _userFieldExpressionHandlerRegistry;
   @Reference
   private UserFieldExpressionResolverRegistry _userFieldExpressionResolverRegistry;
   @Reference
   private UserLocalService _userLocalService;
   @Reference
   private UserProcessorFactory _userProcessorFactory;

   static {
      _log = LogFactoryUtil.getLog(CustomUserResolver.class);
      _log.info("CustomUserResolver class Loaded..");
      System.out.println("CustomUserResolver class Loaded..");
   }

   public User resolveUser(UserResolver.UserResolverSAMLContext userResolverSAMLContext, ServiceContext serviceContext) throws Exception {
      String subjectNameFormat = userResolverSAMLContext.resolveSubjectNameFormat();
      if (_log.isDebugEnabled()) {
         String subjectNameIdentifier = userResolverSAMLContext.resolveSubjectNameIdentifier();
         _log.debug(StringBundler.concat(new String[]{"Resolving user with name ID format ", subjectNameFormat, " and value ", subjectNameIdentifier}));
      }

      _log.info("resolveUser method of CustomUserResolver class start..");
      System.out.println("resolveUser method of CustomUserResolver class start..");
      long companyId = CompanyThreadLocal.getCompanyId();
      SamlSpIdpConnection samlSpIdpConnection = this._samlSpIdpConnectionLocalService.getSamlSpIdpConnection(companyId, userResolverSAMLContext.resolvePeerEntityId());
      subjectNameFormat = this._getNameIdFormat(userResolverSAMLContext, samlSpIdpConnection.getNameIdFormat());
      return this._importUser(companyId, samlSpIdpConnection, userResolverSAMLContext.resolveSubjectNameIdentifier(), subjectNameFormat, userResolverSAMLContext, serviceContext);
   }

   private User _addUser(long companyId, SamlSpIdpConnection samlSpIdpConnection, Map<String, List<Serializable>> attributesMap, ServiceContext serviceContext) throws Exception {
      if (_log.isDebugEnabled()) {
         _log.debug("Adding user with attributes map " + MapUtil.toString(attributesMap));
      }

      Company company = this._companyLocalService.getCompany(companyId);
      String emailAddress = this._getValueAsString("emailAddress", attributesMap);
      if (samlSpIdpConnection.isUnknownUsersAreStrangers()) {
         if (!company.isStrangers()) {
            throw new SubjectException("User is a stranger and company " + companyId + " does not allow strangers to create accounts");
         }

         if (Validator.isNotNull(emailAddress) && !company.isStrangersWithMx() && company.hasCompanyMx(emailAddress)) {
            throw new UserEmailAddressException.MustNotUseCompanyMx(emailAddress);
         }
      }

      User user = this._userLocalService.createUser(0L);
      user.setCompanyId(companyId);
      user = this._processUser(user, attributesMap, serviceContext);
      if (_log.isDebugEnabled()) {
         _log.debug("Added user " + user.toString());
      }

      return user;
   }

   private Map<String, List<Serializable>> _getAttributesMap(SamlSpIdpConnection samlSpIdpConnection, UserResolver.UserResolverSAMLContext userResolverSAMLContext) {
      try {
         return userResolverSAMLContext.resolveBearerAssertionAttributesWithMapping(samlSpIdpConnection.getNormalizedUserAttributeMappings());
      } catch (Exception var4) {
         if (_log.isWarnEnabled()) {
            _log.warn(var4);
         }

         return Collections.emptyMap();
      }
   }

   private String _getNameIdFormat(UserResolver.UserResolverSAMLContext userResolverSAMLContext, String defaultNameIdFormat) {
      String format = userResolverSAMLContext.resolveSubjectNameFormat();
      if (Validator.isNull(format)) {
         format = defaultNameIdFormat;
      }

      return format;
   }

   private String _getPrefix(String userFieldExpression) {
      if (userFieldExpression == null) {
         return null;
      } else {
         int prefixEndIndex = userFieldExpression.indexOf(58);
         return prefixEndIndex == -1 ? "" : userFieldExpression.substring(0, prefixEndIndex);
      }
   }

   private UserFieldExpressionResolver _getUserFieldExpressionResolver(String userIdentifierExpression) {
      String userFieldExpressionResolverKey = this._getPrefix(userIdentifierExpression);
      if (Validator.isBlank(userFieldExpressionResolverKey)) {
         userFieldExpressionResolverKey = userIdentifierExpression;
      }

      return this._userFieldExpressionResolverRegistry.getUserFieldExpressionResolver(userFieldExpressionResolverKey);
   }

   private String _getValueAsString(String key, Map<String, List<Serializable>> attributesMap) {
      List<Serializable> values = (List)attributesMap.get(key);
      return ListUtil.isEmpty(values) ? null : String.valueOf(values.get(0));
   }

   private String[] _getValuesAsString(String key, Map<String, List<Serializable>> attributesMap) {
      List<Serializable> values = (List)attributesMap.get(key);
      return ListUtil.isEmpty(values) ? null : ArrayUtil.toStringArray(values);
   }

   private User _importUser(long companyId, SamlSpIdpConnection samlSpIdpConnection, String subjectNameIdentifier, String nameIdFormat, UserResolver.UserResolverSAMLContext userResolverSAMLContext, ServiceContext serviceContext) throws Exception {
      UserFieldExpressionResolver userFieldExpressionResolver = this._getUserFieldExpressionResolver(samlSpIdpConnection.getUserIdentifierExpression());
      Map<String, List<Serializable>> attributesMap = this._getAttributesMap(samlSpIdpConnection, userResolverSAMLContext);
      String userFieldExpression = this._removePrefix("", GetterUtil.getString(userFieldExpressionResolver.resolveUserFieldExpression(attributesMap, userResolverSAMLContext)));
      if (Validator.isBlank(userFieldExpression)) {
         if (_log.isDebugEnabled()) {
            _log.debug("User field expression is null");
         }

         return this._resolveByNameId(companyId, nameIdFormat, userResolverSAMLContext.resolveSubjectNameQualifier(), subjectNameIdentifier, userResolverSAMLContext.resolvePeerEntityId());
      } else {
         String searchFieldValue = subjectNameIdentifier;
         if (attributesMap.containsKey(userFieldExpression)) {
            searchFieldValue = this._getValueAsString(userFieldExpression, attributesMap);
            if (_log.isDebugEnabled()) {
               _log.debug(StringBundler.concat(new String[]{"User identifier expression is mapped to SAML ", "attribute value \"", searchFieldValue, "\""}));
            }
         } else if (_log.isDebugEnabled()) {
            _log.debug(StringBundler.concat(new String[]{"Resolving user using subject naming identifier ", subjectNameIdentifier, " and name ID format ", nameIdFormat}));
         }

         String prefix = this._getPrefix(userFieldExpression);
         UserFieldExpressionHandler userFieldExpressionHandler = this._userFieldExpressionHandlerRegistry.getFieldExpressionHandler(prefix);
         if (Validator.isNotNull(userFieldExpression) && this._samlProviderConfigurationHelper.isLDAPImportEnabled()) {
            User user = userFieldExpressionHandler.getLdapUser(companyId, searchFieldValue, this._removePrefix(prefix, userFieldExpression));
            if (user != null) {
               if (_log.isDebugEnabled()) {
                  _log.debug("Matched and imported LDAP user " + user.toString());
               }

               return user;
            }
         }

         User user = this._resolveByNameId(companyId, nameIdFormat, userResolverSAMLContext.resolveSubjectNameQualifier(), subjectNameIdentifier, userResolverSAMLContext.resolvePeerEntityId());
         if (user == null) {
            user = userFieldExpressionHandler.getUser(companyId, searchFieldValue, this._removePrefix(prefix, userFieldExpression));
         }

         if (user == null) {
            this._addUser(companyId, samlSpIdpConnection, attributesMap, serviceContext);
         }

         return user;
      }
   }

   private User _processUser(User user, Map<String, List<Serializable>> attributesMap, ServiceContext serviceContext) throws Exception {
      UserProcessor userProcessor = this._userProcessorFactory.create(user, this._userFieldExpressionHandlerRegistry);

      for(String key : attributesMap.keySet()) {
         userProcessor.setValueArray(key, this._getValuesAsString(key, attributesMap));
      }

      return (User)userProcessor.process(serviceContext);
   }

   private String _removePrefix(String prefix, String prefixedUserFieldExpression) {
      return prefixedUserFieldExpression.length() > prefix.length() && prefixedUserFieldExpression.charAt(prefix.length()) == ':' && prefixedUserFieldExpression.startsWith(prefix) ? prefixedUserFieldExpression.substring(prefix.length() + 1) : prefixedUserFieldExpression;
   }

   private User _resolveByNameId(long companyId, String subjectNameFormat, String subjectNameQualifier, String subjectNameIdentifier, String samlIdpEntityId) {
      SamlPeerBinding samlPeerBinding = this._samlPeerBindingLocalService.fetchSamlPeerBinding(companyId, false, subjectNameFormat, subjectNameQualifier, subjectNameIdentifier, samlIdpEntityId);
      if (samlPeerBinding != null) {
         if (_log.isDebugEnabled()) {
            _log.debug(StringBundler.concat(new String[]{"Matched known subject name identifier ", subjectNameIdentifier, " of subject name format ", subjectNameFormat, " with subject name qualifier \"", subjectNameQualifier, "\" for SAML IDP entity ID ", samlIdpEntityId}));
         }

         return this._userLocalService.fetchUserById(samlPeerBinding.getUserId());
      } else {
         return null;
      }
   }

   private User _updateUserDel(User user, Map<String, List<Serializable>> attributesMap, ServiceContext serviceContext) throws Exception {
      if (_log.isDebugEnabled()) {
         _log.debug(StringBundler.concat(new Object[]{"Updating user ", user.getUserId(), " with attributes map ", MapUtil.toString(attributesMap)}));
      }

      return this._processUser(user, attributesMap, serviceContext);
   }

   @Activate
   protected void activate(Map<String, Object> properties) {
      if (_log.isDebugEnabled()) {
         _log.debug("Activated");
         _log.debug("Default resolver: " + String.valueOf(this.getClass()));
      }

      _log.info("Default resolver: " + String.valueOf(this.getClass()));
   }
}
