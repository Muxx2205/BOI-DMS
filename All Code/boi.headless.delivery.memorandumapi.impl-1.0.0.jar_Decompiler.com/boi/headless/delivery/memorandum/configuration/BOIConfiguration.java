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
   id = "boi.headless.delivery.memorandum.configuration.BOIConfiguration"
)
public interface BOIConfiguration {
   @AD(
      required = false,
      deflt = "0"
   )
   long DefaultFolderId();

   @AD(
      required = false,
      deflt = "0"
   )
   long MEMORANDUMObjectDefinitionId();

   @AD(
      required = false,
      deflt = "0"
   )
   long MEMOUserMappingObjectDefinitionId();

   @AD(
      required = false,
      deflt = "0"
   )
   long MEMOWorkflowLogsObjectDefinitionId();

   @AD(
      required = false
   )
   long AdminUserId();

   @AD(
      required = false
   )
   String MEMORANDUMObjectColumns();
}
