package com.boi.liferay.dms.assignment.portlet;

import aQute.bnd.annotation.metatype.Meta.AD;
import aQute.bnd.annotation.metatype.Meta.OCD;
import com.liferay.portal.configuration.metatype.annotations.ExtendedObjectClassDefinition;
import com.liferay.portal.configuration.metatype.annotations.ExtendedObjectClassDefinition.Scope;

@ExtendedObjectClassDefinition(
   category = "RolesAllowToAssignment",
   scope = Scope.SYSTEM
)
@OCD(
   id = "com.boi.liferay.dms.assignment.portlet.RolesAllowToAssignment"
)
public interface RolesAllowToAssignment {
   @AD(
      required = false,
      deflt = ""
   )
   String roles();

   @AD(
      required = false,
      deflt = "false"
   )
   boolean isSyncProdStatus();
}
