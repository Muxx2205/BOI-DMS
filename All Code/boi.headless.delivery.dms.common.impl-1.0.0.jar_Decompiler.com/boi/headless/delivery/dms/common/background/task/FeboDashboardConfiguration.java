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
   id = "boi.headless.delivery.dms.common.background.task.FeboDashboardConfiguration"
)
public interface FeboDashboardConfiguration {
   @AD(
      required = false
   )
   long FeboObjectDefinitionId();

   @AD(
      required = false
   )
   String BluePrintTypeOfRemittance();

   @AD(
      required = false
   )
   String TypeOfRemittanceFieldName();

   @AD(
      required = false
   )
   String TypeOfRemittanceNames();

   @AD(
      required = false
   )
   String BluePrintStage();

   @AD(
      required = false
   )
   String StageFieldName();

   @AD(
      required = false
   )
   String StageValue();

   @AD(
      required = false
   )
   boolean hideMigratedCount();

   @AD(
      required = false
   )
   long parentOrgId();
}
