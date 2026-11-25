package boi.headless.delivery.dms.common.background.task;

import aQute.bnd.annotation.metatype.Meta.AD;
import aQute.bnd.annotation.metatype.Meta.OCD;
import com.liferay.portal.configuration.metatype.annotations.ExtendedObjectClassDefinition;
import com.liferay.portal.configuration.metatype.annotations.ExtendedObjectClassDefinition.Scope;

@ExtendedObjectClassDefinition(
   category = "DMSCommonAPIConfiguation",
   scope = Scope.SYSTEM
)
@OCD(
   id = "boi.headless.delivery.dms.common.background.task.ArchivalDashboard1Configuration"
)
public interface ArchivalDashboard1Configuration {
   @AD(
      required = false
   )
   String ArchiveBasicAuth();

   @AD(
      required = false
   )
   String ArchiveEndPoint();

   @AD(
      required = false
   )
   String ArchiveTemplateNames();
}
