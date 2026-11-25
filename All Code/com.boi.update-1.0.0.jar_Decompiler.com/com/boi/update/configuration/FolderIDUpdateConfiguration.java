package com.boi.update.configuration;

import aQute.bnd.annotation.metatype.Meta.AD;
import aQute.bnd.annotation.metatype.Meta.OCD;
import com.liferay.portal.configuration.metatype.annotations.ExtendedObjectClassDefinition;
import com.liferay.portal.configuration.metatype.annotations.ExtendedObjectClassDefinition.Scope;

@ExtendedObjectClassDefinition(
   category = "FOLDER-MAPPING",
   scope = Scope.SYSTEM
)
@OCD(
   id = "com.boi.update.configuration.FolderIDUpdateConfiguration"
)
public interface FolderIDUpdateConfiguration {
   @AD(
      name = "department-Name",
      required = false
   )
   String departmentName();
}
