package com.boi.headless.rest.common.application;

import com.liferay.object.constants.ObjectFieldConstants;
import com.liferay.object.model.ObjectField;
import com.liferay.object.service.ObjectFieldLocalService;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.Validator;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

public class ObjectConverter {
	 
	 
	 public static String convertToString(HttpServletRequest httpServletRequest, String fieldType,String fieldName,boolean isRequired) {
	        if (httpServletRequest == null || fieldType == null) {
	            return "";
	        }

	        switch (fieldType) {
	            case "Date":{
	            	 
	                    SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
	                    Date value=ParamUtil.getDate(httpServletRequest, fieldName,null);
	                    if(value==null && isRequired)
	                    {return StringPool.BLANK;}
	                    return sdf.format((Date) value);
	            }
			case "LongInteger":{
	                long valuew=ParamUtil.getLong(httpServletRequest, fieldName,0l);
	                if(valuew==0l && isRequired)
                	{return StringPool.BLANK;}
	                    return String.valueOf(valuew);
			}
	                 
			case "Text":{
	                String value= ParamUtil.getString(httpServletRequest, fieldName,StringPool.BLANK);
	                if(Validator.isBlank(value) && isRequired)
	                {return StringPool.BLANK;}
	                return value;
			}
	            default:
	            {return StringPool.BLANK;}
	        }

	        
	    }
	 
	 
	 public static Map<String, Serializable>  getDefaultData(HttpServletRequest httpServletRequest,Map<String, Serializable> values,long objectDefinitionId,ObjectFieldLocalService _objectFieldLocalService
			 ,List<String> onlyWithTheseFields) {
		 
		 List<ObjectField> fields = _objectFieldLocalService.getObjectFields(objectDefinitionId);
		 if(Validator.isNotNull(onlyWithTheseFields) && (!onlyWithTheseFields.isEmpty())) {
		  fields = fields.stream()
				    .filter(field -> onlyWithTheseFields.contains(field.getName()))
				    .collect(Collectors.toList());
		 }
		 for (ObjectField objectField : fields) {
			 String fieldName=objectField.getName();
			 if(objectField.getDBType().equalsIgnoreCase(ObjectFieldConstants.DB_TYPE_STRING) || objectField.getDBType().equalsIgnoreCase(ObjectFieldConstants.DB_TYPE_CLOB)) {
				 String value=ParamUtil.getString(httpServletRequest,fieldName,StringPool.BLANK);
				 value= (String) getRestrictedValues(fieldName, values, value);
				 if(value.equalsIgnoreCase(StringPool.BLANK)) {
					 value=(String) getDefaultData(fieldName, values, StringPool.BLANK);
				 }
				 values.put(fieldName, value);
			 }
			 if(objectField.getDBType().equalsIgnoreCase(ObjectFieldConstants.DB_TYPE_DATE)) {
				// log.info("date converter..");
				 SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
				 sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
				 Date defaultValue=new Date();
				 Date value=ParamUtil.getDate(httpServletRequest,fieldName,sdf,defaultValue);
				// log.info("value converter.."+value);
				 if(value.compareTo(defaultValue)==0) {
					 value= (Date) getDefaultData(fieldName, values, null);
				 }
				// log.info("value converted.."+value);
				 values.put(fieldName, value);
				
			 }
			 if(objectField.getDBType().equalsIgnoreCase(ObjectFieldConstants.DB_TYPE_LONG)) {
				 long value=ParamUtil.getLong(httpServletRequest,fieldName,0l);
				 if(value==0l) {
					 value=(long) getDefaultData(fieldName, values, 0l);
				 }
				 values.put(fieldName, value);
			 }
			 if(objectField.getDBType().equalsIgnoreCase(ObjectFieldConstants.DB_TYPE_INTEGER)) {
				 int value=ParamUtil.getInteger(httpServletRequest,fieldName,0);
				 if(value==0) {
					 value=(int) getDefaultData(fieldName, values, 0);
				 }
				 values.put(fieldName, value);
			 } 
		}
		 return values;
	 }
	 
	 
	 private static Serializable getRestrictedValues(String fieldName,Map<String, Serializable> values,Serializable serializable) {
		 
		 if(fieldName.equalsIgnoreCase("branchCode") || fieldName.equalsIgnoreCase("zcode") 
				 || fieldName.equalsIgnoreCase("nbgCode") 
				 || fieldName.equalsIgnoreCase("zone") || fieldName.equalsIgnoreCase("urn")) {
			 Serializable value=values.get(fieldName);
			 return Validator.isNotNull(value)?value:serializable;
		 }
		 return serializable;
		 
	 }
	 
	 
	 
	 
	 
	 private static Serializable getDefaultData(String fieldName,Map<String, Serializable> values,Serializable serializable) {
		 
		 if(values.containsKey(fieldName)) {
			 return values.get(fieldName);
		 }
		 return serializable;
	 }
	 
	 
	 private static final Log log = LogFactoryUtil.getLog(ObjectConverter.class);
	 
	 
}
