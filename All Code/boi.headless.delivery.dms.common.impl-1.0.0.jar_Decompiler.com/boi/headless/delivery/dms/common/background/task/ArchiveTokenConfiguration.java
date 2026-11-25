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
   id = "boi.headless.delivery.dms.common.background.task.ArchiveTokenConfiguration"
)
public interface ArchiveTokenConfiguration {
   @AD(
      required = false
   )
   String TokenURL();

   @AD(
      required = false
   )
   String grantType();

   @AD(
      required = false
   )
   String boiDMSClientId();

   @AD(
      required = false
   )
   String boiDMSSecret();

   @AD(
      required = false
   )
   String archiveDashboard1ClientId();

   @AD(
      required = false
   )
   String archiveDashboard1Secret();

   @AD(
      required = false
   )
   String branchDigitilizationClientId();

   @AD(
      required = false
   )
   String branchDigitilizationSecret();
}
