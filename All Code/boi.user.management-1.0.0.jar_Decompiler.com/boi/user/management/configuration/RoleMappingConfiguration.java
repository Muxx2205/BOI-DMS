package boi.user.management.configuration;

import aQute.bnd.annotation.metatype.Meta.AD;
import aQute.bnd.annotation.metatype.Meta.OCD;
import com.liferay.portal.configuration.metatype.annotations.ExtendedObjectClassDefinition;
import com.liferay.portal.configuration.metatype.annotations.ExtendedObjectClassDefinition.Scope;

@ExtendedObjectClassDefinition(
   category = "USER-MANAGEMENT-MAPPING",
   scope = Scope.SYSTEM
)
@OCD(
   id = "boi.user.management.configuration.RoleMappingConfiguration"
)
public interface RoleMappingConfiguration {
   @AD(
      name = "role-mapping",
      description = "Define role mappings in format: source=target1,target2",
      required = false,
      deflt = "FINANCE PLOE=Finance Ploe  \nUPI-CPD Insurance=UPI Insurance,CPD Insurance"
   )
   String[] roleMappings();

   @AD(
      name = "department-mapping",
      required = false
   )
   String departmentMappings();

   @AD(
      name = "organization-Id",
      required = false
   )
   long organizationId();

   @AD(
      name = "folder-Id",
      required = false
   )
   long folderId();
}
