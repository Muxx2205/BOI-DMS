package in.co.boi.branchadmin.web.config;

import aQute.bnd.annotation.metatype.Meta.AD;
import aQute.bnd.annotation.metatype.Meta.OCD;

@OCD(
   id = "in.co.boi.branchadmin.web.config.ParentSetupConfiguration",
   localization = "content/Language",
   name = "parent-setup-configuration-name"
)
public interface ParentSetupConfiguration {
   @AD(
      required = false,
      deflt = "BOI Document",
      name = "parent-folder-name",
      description = "select-the-parent-folder-from-documents-and-media"
   )
   String parentFolderName();

   @AD(
      required = false,
      deflt = "BOI HO",
      name = "parent-organization-name",
      description = "select-the-parent-organization"
   )
   String parentOrganizationName();
}
