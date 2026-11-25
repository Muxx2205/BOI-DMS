package com.boi.headless.rest.common.spa.application;

import com.liferay.object.model.ObjectField;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.Validator;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import javax.servlet.http.HttpServletRequest;

public class ObjectConverter {
   public static String convertToString(HttpServletRequest httpServletRequest, String fieldType, String fieldName, boolean isRequired) {
      if (httpServletRequest != null && fieldType != null) {
         switch (fieldType) {
            case "Date":
               SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
               Date value = ParamUtil.getDate(httpServletRequest, fieldName, (DateFormat)null);
               if (value == null && isRequired) {
                  return "";
               }

               return sdf.format(value);
            case "LongInteger":
               long valuew = ParamUtil.getLong(httpServletRequest, fieldName, 0L);
               if (valuew == 0L && isRequired) {
                  return "";
               }

               return String.valueOf(valuew);
            case "Text":
               String value = ParamUtil.getString(httpServletRequest, fieldName, "");
               if (Validator.isBlank(value) && isRequired) {
                  return "";
               }

               return value;
            default:
               return "";
         }
      } else {
         return "";
      }
   }

   public static Map<String, Serializable> getDefaultData(HttpServletRequest httpServletRequest, List<ObjectField> fields, Map<String, Serializable> values) {
      for(ObjectField objectField : fields) {
         if (objectField.getDBType().equalsIgnoreCase("String") || objectField.getDBType().equalsIgnoreCase("Clob")) {
            String value = ParamUtil.getString(httpServletRequest, objectField.getName(), "");
            if (value.equalsIgnoreCase("")) {
               value = (String)getDefaultData((String)objectField.getName(), values, "");
            }

            values.put(objectField.getName(), value);
         }

         if (objectField.getDBType().equalsIgnoreCase("Date")) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date defaultValue = new Date();
            Date value = ParamUtil.getDate(httpServletRequest, objectField.getName(), sdf, defaultValue);
            if (value.compareTo(defaultValue) == 0) {
               value = (Date)getDefaultData((String)objectField.getName(), values, (Serializable)null);
            }

            values.put(objectField.getName(), value);
         }

         if (objectField.getDBType().equalsIgnoreCase("Long")) {
            long value = ParamUtil.getLong(httpServletRequest, objectField.getName(), 0L);
            if (value == 0L) {
               value = (Long)getDefaultData((String)objectField.getName(), values, 0L);
            }

            values.put(objectField.getName(), value);
         }

         if (objectField.getDBType().equalsIgnoreCase("Integer")) {
            int value = ParamUtil.getInteger(httpServletRequest, objectField.getName(), 0);
            if (value == 0) {
               value = (Integer)getDefaultData((String)objectField.getName(), values, 0);
            }

            values.put(objectField.getName(), value);
         }
      }

      return values;
   }

   private static Serializable getDefaultData(String fieldName, Map<String, Serializable> values, Serializable serializable) {
      return values.containsKey(fieldName) ? (Serializable)values.get(fieldName) : serializable;
   }
}
