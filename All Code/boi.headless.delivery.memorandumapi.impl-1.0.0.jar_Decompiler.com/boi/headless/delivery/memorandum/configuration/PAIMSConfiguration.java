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
   id = "boi.headless.delivery.memorandum.configuration.PAIMSConfiguration"
)
public interface PAIMSConfiguration {
   @AD(
      required = false
   )
   long PAIMSObjectDefinitionId();

   @AD(
      required = false
   )
   long PAIMSWorkflowLogsDefinitionId();

   @AD(
      required = false
   )
   long PAIMSDefaultFolderId();
}
