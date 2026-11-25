package com.boi.headless.rest.common.spa.application;

import aQute.bnd.annotation.metatype.Meta.AD;
import aQute.bnd.annotation.metatype.Meta.OCD;
import com.liferay.portal.configuration.metatype.annotations.ExtendedObjectClassDefinition;
import com.liferay.portal.configuration.metatype.annotations.ExtendedObjectClassDefinition.Scope;

@ExtendedObjectClassDefinition(
   category = "BOISPAConfiguation",
   scope = Scope.SYSTEM
)
@OCD(
   id = "com.boi.headless.rest.common.spa.application.BOISPAConfiguation"
)
public interface BOISPAConfiguation {
   @AD(
      required = false,
      deflt = "0"
   )
   long SPADefualtFolderId();

   @AD(
      required = false,
      deflt = "0"
   )
   String ArchiveUrl();

   @AD(
      required = false,
      deflt = "0"
   )
   String DMSUrl();

   @AD(
      required = false,
      deflt = "0"
   )
   long EplatFormDefaultFolderId();

   @AD(
      required = false,
      deflt = "false"
   )
   boolean isEplatFormArchival();

   @AD(
      required = false
   )
   long EPlateFormObjectDefinitionId();

   @AD(
      required = false,
      deflt = "0"
   )
   long KCCJansmarthDefaultFolderId();

   @AD(
      required = false,
      deflt = "false"
   )
   boolean isKCCJansmarthArchival();

   @AD(
      required = false
   )
   long KCCJansamarthFormObjectDefinitionId();

   @AD(
      required = false,
      deflt = "0"
   )
   long utopiaDefaultFolderId();

   @AD(
      required = false,
      deflt = "false"
   )
   boolean isUtopiaArchival();

   @AD(
      required = false
   )
   long UTOPIAFormObjectDefinitionId();

   @AD(
      required = false,
      deflt = "false"
   )
   boolean isSSYArchival();

   @AD(
      required = false
   )
   long SSYObjectDefinitionId();

   @AD(
      required = false
   )
   long SSYDefaultFolderId();

   @AD(
      required = false,
      deflt = "0"
   )
   long CRMNextDefaultFolderId();

   @AD(
      required = false,
      deflt = "false"
   )
   boolean isCRMNextArchival();

   @AD(
      required = false
   )
   long ECRMFormObjectDefinitionId();

   @AD(
      required = false
   )
   long BranchDigitationObjectDefinitionId();

   @AD(
      required = false,
      deflt = "0"
   )
   long BranchDigitizationDefaultFolderId();

   @AD(
      required = false
   )
   long BoardLevelMeetingObjectDefinitionId();

   @AD(
      required = false
   )
   long CDADObjectDefinitionId();

   @AD(
      required = false
   )
   long InspectionAuditACEObjectDefinitionId();

   @AD(
      required = false
   )
   long DataWareHouseObjectDefinitionId();

   @AD(
      required = false
   )
   long ElectricalWiringObjectDefinitionId();

   @AD(
      required = false
   )
   long FinastraObjectDefinitionId();

   @AD(
      required = false
   )
   long FinastraOtherObjectDefinitionId();

   @AD(
      required = false
   )
   long HORDObjectDefinitionId();

   @AD(
      required = false
   )
   long HOSecurityDepartmentObjectDefinitionId();

   @AD(
      required = false
   )
   long InspectionAuditACBObjectDefinitionId();

   @AD(
      required = false
   )
   long InspectionAuditCAAppointmentObjectDefinitionId();

   @AD(
      required = false
   )
   long InspectionAuditMiscObjectDefinitionId();

   @AD(
      required = false
   )
   long InspectionAuditOthReportObjectDefinitionId();

   @AD(
      required = false
   )
   long InspectionAuditRepotsObjectDefinitionId();

   @AD(
      required = false
   )
   long InspectionForeignAuditObjectDefinitionId();

   @AD(
      required = false
   )
   long InternalLayoutMapObjectDefinitionId();

   @AD(
      required = false
   )
   long InternationalDepObjectDefinitionId();

   @AD(
      required = false
   )
   long ProjectManagementListObjectDefinitionId();

   @AD(
      required = false
   )
   long AdminUserId();

   @AD(
      required = false
   )
   String BoardLevelMeetingObjectColumns();

   @AD(
      required = false
   )
   String CDADObjectColumns();

   @AD(
      required = false
   )
   String HORecruitmentDivObjectColumns();

   @AD(
      required = false
   )
   String IAORObjectColumns();

   @AD(
      required = false
   )
   String InsceptionAuditACBObjectColumns();

   @AD(
      required = false
   )
   String InsceptionAuditACEObjectColumns();

   @AD(
      required = false
   )
   String InsceptionAuditMISCObjectColumns();

   @AD(
      required = false
   )
   String FinancePloeObjectColumns();

   @AD(
      required = false
   )
   String InternalLayoutMapObjectColumns();

   @AD(
      required = false
   )
   String InternationalDepartmentObjectColumns();

   @AD(
      required = false
   )
   String ProjectManageListObjectColumns();

   @AD(
      required = false
   )
   String InspectionForeignAuditObjectColumns();

   @AD(
      required = false
   )
   String DataWarehouseObjectColumns();

   @AD(
      required = false
   )
   String ElectricalWiringDiagramObjectColumns();

   @AD(
      required = false
   )
   String HOSecurityDepObjectColumns();

   @AD(
      required = false
   )
   String InspectionAuditCAAppointObjectColumns();

   @AD(
      required = false
   )
   String InspectionAuditMiscObjectColumns();

   @AD(
      required = false
   )
   String InspectionAuditReportsObjectColumns();

   @AD(
      required = false
   )
   long UserDepartmentObjectDefinitionId();
}
