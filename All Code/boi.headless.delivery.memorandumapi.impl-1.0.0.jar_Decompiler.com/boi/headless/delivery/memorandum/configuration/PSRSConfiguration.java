package boi.headless.delivery.memorandum.configuration;

import aQute.bnd.annotation.metatype.Meta.AD;
import aQute.bnd.annotation.metatype.Meta.OCD;
import com.liferay.portal.configuration.metatype.annotations.ExtendedObjectClassDefinition;
import com.liferay.portal.configuration.metatype.annotations.ExtendedObjectClassDefinition.Scope;

@ExtendedObjectClassDefinition(
   category = "MEMOConfiguration",
   scope = Scope.SYSTEM
)
@OCD(
   id = "boi.headless.delivery.memorandum.configuration.PSRSConfiguration"
)
public interface PSRSConfiguration {
   @AD(
      required = false
   )
   long M1Id();

   @AD(
      required = false
   )
   long M2Id();

   @AD(
      required = false
   )
   long M3Id();

   @AD(
      required = false
   )
   long M4Id();

   @AD(
      required = false
   )
   long M5Id();

   @AD(
      required = false
   )
   long M6Id();

   @AD(
      required = false
   )
   long M7Id();

   @AD(
      required = false
   )
   long M8Id();

   @AD(
      required = false
   )
   long MonthlyCheckListId();

   @AD(
      required = false
   )
   String M1ObjectColumns();

   @AD(
      required = false
   )
   String M2ObjectColumns();

   @AD(
      required = false
   )
   String M3ObjectColumns();

   @AD(
      required = false
   )
   String M4ObjectColumns();

   @AD(
      required = false
   )
   String M5ObjectColumns();

   @AD(
      required = false
   )
   String M6ObjectColumns();

   @AD(
      required = false
   )
   String M7ObjectColumns();

   @AD(
      required = false
   )
   String M8ObjectColumns();

   @AD(
      required = false
   )
   String M1OrderColumns();

   @AD(
      required = false
   )
   String M2OrderColumns();

   @AD(
      required = false
   )
   String M3OrderColumns();

   @AD(
      required = false
   )
   String M4OrderColumns();

   @AD(
      required = false
   )
   String M5OrderColumns();

   @AD(
      required = false
   )
   String M6OrderColumns();

   @AD(
      required = false
   )
   String M7OrderColumns();

   @AD(
      required = false
   )
   String M8OrderColumns();

   @AD(
      required = false
   )
   String MonthlyCheckListOrderColumns();

   @AD(
      required = false
   )
   String CommonHideColumns();

   @AD(
      required = false
   )
   String PSRSDateColumns();

   @AD(
      required = false,
      deflt = "0"
   )
   long headOfficeFolderId();

   @AD(
      required = false,
      deflt = "0"
   )
   long PSRSFolderId();

   @AD(
      required = false
   )
   boolean isExportToExcel();
}
