package boi.document.management.system.configuration;

import aQute.bnd.annotation.metatype.Meta.AD;
import aQute.bnd.annotation.metatype.Meta.OCD;
import com.liferay.portal.configuration.metatype.annotations.ExtendedObjectClassDefinition;
import com.liferay.portal.configuration.metatype.annotations.ExtendedObjectClassDefinition.Scope;

@ExtendedObjectClassDefinition(
   category = "BOIDMS",
   scope = Scope.SYSTEM
)
@OCD(
   id = "boi.document.management.system.configuration.DMSConfiguration"
)
public interface DMSConfiguration {
   @AD(
      required = false,
      name = "API 3 LEVEl HIDE FOLDER",
      description = " Add API 3 LEVEL HIDE Folders in Comma-separated"
   )
   String api3LevelHideFolder();

   @AD(
      required = false,
      name = "SPA Roles",
      description = " Add Roles in Comma-separated"
   )
   String spaRoles();

   @AD(
      required = false,
      name = "WorkFlow Folders Mapping",
      description = "Add Mapping in Comma-Separated"
   )
   String workflowFolderNameMapping();

   @AD(
      required = false,
      name = "Workflow Role Name",
      description = "Add Workflow Role Name in Comma-Separated"
   )
   String workflowRoleName();

   @AD(
      required = false,
      name = "SPA Folder Ids with Template",
      description = "Add SPA Folder Ids with Template in Comma-Separated"
   )
   String spaFolderIdsWithTemplate();

   @AD(
      required = false,
      name = "Organization Master Object Id",
      description = "Add Organization Master Object Id"
   )
   Long organizationMasterObjectId();

   @AD(
      required = false,
      name = "User Folder Object Id",
      description = "Add User Folder Object Id"
   )
   Long userFolderObjectId();

   @AD(
      required = false,
      name = "NBG Level Folder Names",
      description = "Add NBG Level Folder Names in Comma-Separated"
   )
   String nbgLevelFolderNames();

   @AD(
      deflt = "10",
      required = false,
      description = "Maximum file size in MB"
   )
   long maxFileSize();

   @AD(
      deflt = "10",
      required = false,
      description = "Maximum number of files allowed per upload"
   )
   long maxFiles();

   @AD(
      required = false,
      description = "Filters files and folders by Department"
   )
   String filterFileFolderByDepartment();

   @AD(
      deflt = "pdf,doc,docx,xls,xlsx,txt,jpg,jpeg,png,gif,tif,tiff",
      required = false,
      description = "Comma-separated list of allowed file extensions"
   )
   String allowedExtensions();

   @AD(
      deflt = "exe,bat,cmd,sh,js,vbs,ps1,jar,war,ear,dll,msi",
      required = false,
      description = "Comma-separated list of blocked file extensions"
   )
   String blockedExtensions();

   @AD(
      deflt = "CON,PRN,AUX,NUL,COM1,COM2,COM3,COM4,COM5,COM6,COM7,COM8,COM9,LPT1,LPT2,LPT3,LPT4,LPT5,LPT6,LPT7,LPT8,LPT9",
      required = false,
      description = "Comma-separated list of reserved filenames"
   )
   String[] reservedNames();

   @AD(
      required = false,
      name = "Archive Endpoint",
      description = "Archive Endpoint"
   )
   String archiveEndPoints();

   @AD(
      required = false,
      name = "DMS Endpoint",
      description = "DMS Endpoint"
   )
   String dmsEndPoints();

   @AD(
      required = false,
      name = "Archive ClientId",
      description = "Archive ClientId"
   )
   String archiveClientId();

   @AD(
      required = false,
      name = "Archive SecretId",
      description = "Archive SecretId"
   )
   String archiveSecretId();

   @AD(
      required = false,
      name = "Board Secretariat File Rename FolderIds",
      description = "Board Secretariat File Rename FolderIds"
   )
   String boardSecretariatFileRenameFolderIds();

   @AD(
      required = false,
      name = "Board Secretariat FolderId",
      description = "Board Secretariat FolderId"
   )
   String boardSecretariatFolderId();
}
