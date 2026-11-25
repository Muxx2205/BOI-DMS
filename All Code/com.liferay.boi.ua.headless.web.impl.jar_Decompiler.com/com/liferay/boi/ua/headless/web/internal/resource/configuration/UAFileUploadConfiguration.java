package com.liferay.boi.ua.headless.web.internal.resource.configuration;

import aQute.bnd.annotation.metatype.Meta.AD;
import aQute.bnd.annotation.metatype.Meta.OCD;
import com.liferay.portal.configuration.metatype.annotations.ExtendedObjectClassDefinition;
import com.liferay.portal.configuration.metatype.annotations.ExtendedObjectClassDefinition.Scope;

@ExtendedObjectClassDefinition(
   category = "UAFileUploadConfiguration",
   scope = Scope.SYSTEM
)
@OCD(
   id = "com.liferay.boi.ua.headless.web.internal.resource.configuration.UAFileUploadConfiguration"
)
public interface UAFileUploadConfiguration {
   @AD(
      required = false,
      deflt = "false"
   )
   boolean isActive();

   @AD(
      required = false
   )
   int parentFolderId();

   @AD(
      required = false
   )
   int file1Size();

   @AD(
      required = false
   )
   int file2Size();

   @AD(
      required = false
   )
   int file3Size();

   @AD(
      required = false
   )
   int file4Size();

   @AD(
      required = false
   )
   int file5Size();

   @AD(
      required = false
   )
   int file6Size();

   @AD(
      required = false
   )
   int file7Size();

   @AD(
      required = false
   )
   int file8Size();

   @AD(
      required = false
   )
   int file9Size();

   @AD(
      required = false
   )
   int file10Size();
}
