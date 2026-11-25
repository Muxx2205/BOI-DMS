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
   id = "boi.headless.delivery.dms.common.background.task.DMSCommonAPIConfiguation"
)
public interface DMSCommonAPIConfiguation {
   @AD(
      required = false,
      deflt = "0"
   )
   long DMSDefualtFolderId();

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
   long EPlateFormObjectDefinitionId();

   @AD(
      required = false
   )
   long ECRMFormObjectDefinitionId();

   @AD(
      required = false
   )
   long KCCJansamarthFormObjectDefinitionId();

   @AD(
      required = false
   )
   long UTOPIAFormObjectDefinitionId();

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
   long BranchDigitationObjectDefinitionId();

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
   long CustomerAccountObjectDefinitionId();

   @AD(
      required = false
   )
   long UPIInsuranceObjectDefinitionId();

   @AD(
      required = false
   )
   long CPDInsuranceObjectDefinitionId();

   @AD(
      required = false
   )
   long CustomWorkflowLogsObjectDefinitionId();

   @AD(
      required = false
   )
   long DBDepartmentObjectDefinitionId();

   @AD(
      required = false
   )
   long DBDMandateObjectDefinitionId();

   @AD(
      required = false
   )
   long FBDObjectDefinitionId();

   @AD(
      required = false
   )
   long FEBONONFinancialObjectDefinitionId();

   @AD(
      required = false
   )
   long FinancePloeObjectDefinitionId();

   @AD(
      required = false
   )
   long FBDFRBOObjectDefinitionId();

   @AD(
      required = false
   )
   String CustomerAccountObjectColumns();

   @AD(
      required = false
   )
   String UPIInsuranceObjectColumns();

   @AD(
      required = false
   )
   String CPDInsuranceObjectColumns();

   @AD(
      required = false
   )
   String DBDepartmentObjectColumns();

   @AD(
      required = false
   )
   String DBDMandateObjectColumns();

   @AD(
      required = false
   )
   String FBDObjectColumns();

   @AD(
      required = false
   )
   String FEBONONFinancialObjectColumns();

   @AD(
      required = false
   )
   String FinancePloeObjectColumns();

   @AD(
      required = false
   )
   String FBDFOBOObjectColumns();
}
