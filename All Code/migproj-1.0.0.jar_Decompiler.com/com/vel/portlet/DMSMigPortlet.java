package com.vel.portlet;

import com.data.mig.util.AppDataUtil;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCPortlet;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.ServiceContextFactory;
import com.liferay.portal.kernel.util.ParamUtil;
import com.vel.dto.DBRequest;
import com.vel.util.CleanDocumentUtil;
import com.vel.util.CleanMetaDataUtil;
import com.vel.util.DocMetaDataMigrationUtil;
import com.vel.util.FileDumpUtil;
import com.vel.util.FileMigrationUtil;
import com.vel.util.FolderPermissionUtill;
import com.vel.util.MscFileMigrationUtil;
import com.vel.util.OrgMigrationUtil;
import com.vel.util.UserMigrationUtil;
import java.io.IOException;
import java.util.Arrays;
import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.Portlet;
import javax.portlet.PortletException;
import javax.portlet.ProcessAction;
import org.osgi.service.component.annotations.Component;

@Component(
   property = {"com.liferay.portlet.display-category=category.sample", "com.liferay.portlet.header-portlet-css=/css/main.css", "com.liferay.portlet.instanceable=true", "javax.portlet.display-name=DMSMig", "javax.portlet.init-param.template-path=/", "javax.portlet.init-param.view-template=/view.jsp", "javax.portlet.name=com_vel_DMSMigPortlet", "javax.portlet.resource-bundle=content.Language", "javax.portlet.security-role-ref=power-user,user"},
   service = {Portlet.class}
)
public class DMSMigPortlet extends MVCPortlet {
   @ProcessAction(
      name = "opendbmigform"
   )
   public void opendbmigform(ActionRequest actionRequest, ActionResponse actionResponse) throws IOException, PortletException {
      actionResponse.setRenderParameter("mvcPath", "/dbmig.jsp");
   }

   @ProcessAction(
      name = "openFldmigform"
   )
   public void openFldmigform(ActionRequest actionRequest, ActionResponse actionResponse) throws IOException, PortletException {
      actionResponse.setRenderParameter("mvcPath", "/fldmig.jsp");
   }

   @ProcessAction(
      name = "openDocmigform"
   )
   public void openDocmigform(ActionRequest actionRequest, ActionResponse actionResponse) throws IOException, PortletException {
      actionResponse.setRenderParameter("mvcPath", "/docmig.jsp");
   }

   @ProcessAction(
      name = "openArchDocmigform"
   )
   public void openArchDocmigform(ActionRequest actionRequest, ActionResponse actionResponse) throws IOException, PortletException {
      actionResponse.setRenderParameter("mvcPath", "/archdocmig.jsp");
   }

   @ProcessAction(
      name = "openMscDocmigform"
   )
   public void openMscDocmigform(ActionRequest actionRequest, ActionResponse actionResponse) throws IOException, PortletException {
      actionResponse.setRenderParameter("mvcPath", "/mscdocmig.jsp");
   }

   @ProcessAction(
      name = "openArchMscDocmigform"
   )
   public void openArchMscDocmigform(ActionRequest actionRequest, ActionResponse actionResponse) throws IOException, PortletException {
      actionResponse.setRenderParameter("mvcPath", "/archmscdocmig.jsp");
   }

   @ProcessAction(
      name = "orgmigform"
   )
   public void orgmigform(ActionRequest actionRequest, ActionResponse actionResponse) throws IOException, PortletException {
      actionResponse.setRenderParameter("mvcPath", "/orgmig.jsp");
   }

   @ProcessAction(
      name = "usermigform"
   )
   public void usermigform(ActionRequest actionRequest, ActionResponse actionResponse) throws IOException, PortletException {
      actionResponse.setRenderParameter("mvcPath", "/usermig.jsp");
   }

   @ProcessAction(
      name = "openMetamigform"
   )
   public void openDocMetamigform(ActionRequest actionRequest, ActionResponse actionResponse) throws IOException, PortletException {
      actionResponse.setRenderParameter("mvcPath", "/docmetadatamig.jsp");
   }

   @ProcessAction(
      name = "orgmigformSubmit"
   )
   public void orgmigformSubmit(ActionRequest actionRequest, ActionResponse actionResponse) throws IOException, PortletException {
      try {
         long orgId = ParamUtil.getLong(actionRequest, "orgId");
         ServiceContext ctx = ServiceContextFactory.getInstance(actionRequest);
         OrgMigrationUtil util = new OrgMigrationUtil(orgId, ctx);
         util.setDaemon(true);
         util.start();
         actionResponse.setRenderParameter("pvalue", "Success! Migration Started..");
      } catch (Exception e) {
         e.printStackTrace();
         actionResponse.setRenderParameter("pvalue", "Exception ! " + String.valueOf(e));
      }

      actionResponse.setRenderParameter("mvcPath", "/orgmig.jsp");
   }

   @ProcessAction(
      name = "usermigformSubmit"
   )
   public void usermigformSubmit(ActionRequest actionRequest, ActionResponse actionResponse) throws IOException, PortletException {
      try {
         long companyId = ParamUtil.getLong(actionRequest, "companyId");
         long groupId = ParamUtil.getLong(actionRequest, "groupId");
         long creatorId = ParamUtil.getLong(actionRequest, "creatorId");
         long userRoleId = ParamUtil.getLong(actionRequest, "userRoleId");
         ServiceContext ctx = ServiceContextFactory.getInstance(actionRequest);
         UserMigrationUtil util = new UserMigrationUtil(companyId, creatorId, userRoleId, groupId, ctx);
         util.setDaemon(true);
         util.run();
         actionResponse.setRenderParameter("pvalue", "Success! Migration Started..");
      } catch (Exception e) {
         e.printStackTrace();
         actionResponse.setRenderParameter("pvalue", "Exception ! " + String.valueOf(e));
      }

      actionResponse.setRenderParameter("mvcPath", "/usermig.jsp");
   }

   @ProcessAction(
      name = "metamigformSubmit"
   )
   public void metamigformSubmit(ActionRequest actionRequest, ActionResponse actionResponse) throws IOException, PortletException {
      try {
         long dataClassId = ParamUtil.getLong(actionRequest, "dataClassId");
         long liferayObjectId = ParamUtil.getLong(actionRequest, "liferayObjectId");
         long companyId = ParamUtil.getLong(actionRequest, "companyId");
         long adminUserId = ParamUtil.getLong(actionRequest, "adminUserId");
         String fldMapping = ParamUtil.getString(actionRequest, "fldMapping");
         String mdate = ParamUtil.getString(actionRequest, "mdate");
         String startStop = ParamUtil.getString(actionRequest, "startStop");
         System.out.println("adminUserId  >> " + adminUserId + "           companyId >>" + companyId);
         if (startStop.equals("Start")) {
            ServiceContext ctx = ServiceContextFactory.getInstance(actionRequest);
            DocMetaDataMigrationUtil util = new DocMetaDataMigrationUtil(dataClassId, liferayObjectId, companyId, adminUserId, fldMapping, mdate, ctx);
            util.start();
         } else if (startStop.equals("StartDirect")) {
            ServiceContext ctx = ServiceContextFactory.getInstance(actionRequest);
            DocMetaDataMigrationUtil util = new DocMetaDataMigrationUtil(dataClassId, liferayObjectId, companyId, adminUserId, fldMapping, mdate, startStop, ctx);
            util.start();
         } else if (startStop.equals("StartDel")) {
            ServiceContext ctx = ServiceContextFactory.getInstance(actionRequest);
            CleanMetaDataUtil util = new CleanMetaDataUtil("" + dataClassId);
            util.start();
            System.out.println("Records Cleaning start for " + dataClassId);
         }

         actionResponse.setRenderParameter("pvalue", "Success! Migration Started..");
      } catch (Exception e) {
         e.printStackTrace();
         actionResponse.setRenderParameter("pvalue", "Exception ! " + String.valueOf(e));
      }

      actionResponse.setRenderParameter("mvcPath", "/docmetadatamig.jsp");
   }

   @ProcessAction(
      name = "dbmigformSubmit"
   )
   public void dbmigformSubmit(ActionRequest actionRequest, ActionResponse actionResponse) throws Exception {
      DBRequest req = new DBRequest();
      req.setSourceDb(ParamUtil.getString(actionRequest, "sourceDB"));
      req.setSourceSchema(ParamUtil.getString(actionRequest, "sourceSchema"));
      req.setSourceHost(ParamUtil.getString(actionRequest, "sourceHost"));
      req.setSourcePort(ParamUtil.getString(actionRequest, "sourcePort"));
      req.setSourceUser(ParamUtil.getString(actionRequest, "sourceUserName"));
      req.setSourcePassword(ParamUtil.getString(actionRequest, "sourcePassword"));
      req.setTargetDb(ParamUtil.getString(actionRequest, "targetDB"));
      req.setTargetSchema(ParamUtil.getString(actionRequest, "targetSchema"));
      req.setTargetHost(ParamUtil.getString(actionRequest, "targetHost"));
      req.setTargetPort(ParamUtil.getString(actionRequest, "targetPort"));
      req.setTargetUser(ParamUtil.getString(actionRequest, "targetUserName"));
      req.setTargetPassword(ParamUtil.getString(actionRequest, "targetPassword"));
      String tlist = ParamUtil.getString(actionRequest, "tableNames");
      String prefix = ParamUtil.getString(actionRequest, "prefix");
      req.setPrefix(prefix);
      if (tlist != null) {
         req.setTableNames(Arrays.asList(tlist.split(",")));
      }

      AppDataUtil appdtata = new AppDataUtil(req);
      appdtata.start();
      actionResponse.setRenderParameter("mvcPath", "/dbmig.jsp");
   }

   @ProcessAction(
      name = "fldmigformSubmit"
   )
   public void fldmigformSubmit(ActionRequest actionRequest, ActionResponse actionResponse) throws IOException, PortletException {
      try {
         long oldId = ParamUtil.getLong(actionRequest, "newGenFolderId");
         long fldId = ParamUtil.getLong(actionRequest, "liferayFolderId");
         String startStop = ParamUtil.getString(actionRequest, "startStop");
         System.out.println("fldmigformSubmit >>  " + startStop + "   " + fldId);
         if (!startStop.equals("Start") && !startStop.equals("Start1") && !startStop.equals("Start2")) {
            FolderPermissionUtill.startFlag = false;
         } else {
            FolderPermissionUtill.startFlag = true;
            ServiceContext ctx = ServiceContextFactory.getInstance(actionRequest);
            FolderPermissionUtill util = new FolderPermissionUtill(oldId, fldId, startStop, ctx);
            util.setDaemon(true);
            util.start();
         }

         actionResponse.setRenderParameter("pvalue", "Success! Migration Started..");
      } catch (Exception e) {
         e.printStackTrace();
         actionResponse.setRenderParameter("pvalue", "Exception ! " + String.valueOf(e));
      }

      actionResponse.setRenderParameter("mvcPath", "/fldmig.jsp");
   }

   @ProcessAction(
      name = "docmigformSubmit"
   )
   public void docmigformSubmit(ActionRequest actionRequest, ActionResponse actionResponse) throws IOException, PortletException {
      try {
         int oldId = ParamUtil.getInteger(actionRequest, "dataclassId");
         String fromIndex = ParamUtil.getString(actionRequest, "fromindex");
         String toIndex = ParamUtil.getString(actionRequest, "toindex");
         Integer userNumber = ParamUtil.getInteger(actionRequest, "userNumber");
         String startStop = ParamUtil.getString(actionRequest, "startStop");
         String rootpath = ParamUtil.getString(actionRequest, "rootpath");
         if (startStop.equals("Start")) {
            FolderPermissionUtill.startFlag = true;
            ServiceContext ctx = ServiceContextFactory.getInstance(actionRequest);
            FileMigrationUtil util = new FileMigrationUtil(oldId, fromIndex, toIndex, userNumber, rootpath, ctx);
            util.start();
         } else if (startStop.equals("StartDirect")) {
            FolderPermissionUtill.startFlag = true;
            ServiceContext ctx = ServiceContextFactory.getInstance(actionRequest);
            FileMigrationUtil util = new FileMigrationUtil(oldId, fromIndex, toIndex, startStop, userNumber, rootpath, ctx);
            util.start();
         } else if (startStop.equals("StartDump")) {
            FolderPermissionUtill.startFlag = true;
            ServiceContext ctx = ServiceContextFactory.getInstance(actionRequest);
            FileDumpUtil util = new FileDumpUtil(oldId, fromIndex, toIndex, userNumber, rootpath, ctx);
            util.start();
         } else if (startStop.equals("StartDel")) {
            ServiceContext ctx = ServiceContextFactory.getInstance(actionRequest);
            CleanDocumentUtil util = new CleanDocumentUtil("" + oldId);
            util.start();
            System.out.println("Start Cleaning for " + oldId);
         } else {
            FolderPermissionUtill.startFlag = false;
         }

         actionResponse.setRenderParameter("pvalue", "Success! Migration Started..");
      } catch (Exception e) {
         e.printStackTrace();
         actionResponse.setRenderParameter("pvalue", "Exception ! " + String.valueOf(e));
      }

      actionResponse.setRenderParameter("mvcPath", "/docmig.jsp");
   }

   @ProcessAction(
      name = "archdocmigformSubmit"
   )
   public void archdocmigformSubmit(ActionRequest actionRequest, ActionResponse actionResponse) throws IOException, PortletException {
      try {
         System.out.println();
         int oldId = ParamUtil.getInteger(actionRequest, "dataclassId");
         long folderId = ParamUtil.getLong(actionRequest, "folderId");
         Long fromIndex = ParamUtil.getLong(actionRequest, "fromindex");
         Long toIndex = ParamUtil.getLong(actionRequest, "toindex");
         String startStop = ParamUtil.getString(actionRequest, "startStop");
         if (startStop.equals("Start")) {
            FolderPermissionUtill.startFlag = true;
            ServiceContext var9 = ServiceContextFactory.getInstance(actionRequest);
         } else {
            FolderPermissionUtill.startFlag = false;
         }

         actionResponse.setRenderParameter("pvalue", "Success! Migration Started..");
      } catch (Exception e) {
         e.printStackTrace();
         actionResponse.setRenderParameter("pvalue", "Exception ! " + String.valueOf(e));
      }

      actionResponse.setRenderParameter("mvcPath", "/archdocmig.jsp");
   }

   @ProcessAction(
      name = "mscdocmigformSubmit"
   )
   public void mscdocmigformSubmit(ActionRequest actionRequest, ActionResponse actionResponse) throws IOException, PortletException {
      try {
         System.out.println();
         String fromIndex = ParamUtil.getString(actionRequest, "fromindex");
         String toIndex = ParamUtil.getString(actionRequest, "toindex");
         int userNumber = ParamUtil.getInteger(actionRequest, "userNumber");
         String startStop = ParamUtil.getString(actionRequest, "startStop");
         if (startStop.equals("Start")) {
            FolderPermissionUtill.startFlag = true;
            ServiceContext ctx = ServiceContextFactory.getInstance(actionRequest);
            MscFileMigrationUtil util = new MscFileMigrationUtil(fromIndex, toIndex, userNumber, ctx);
            util.start();
         } else {
            FolderPermissionUtill.startFlag = false;
         }

         actionResponse.setRenderParameter("pvalue", "Success! Migration Started..");
      } catch (Exception e) {
         e.printStackTrace();
         actionResponse.setRenderParameter("pvalue", "Exception ! " + String.valueOf(e));
      }

      actionResponse.setRenderParameter("mvcPath", "/mscdocmig.jsp");
   }

   @ProcessAction(
      name = "archmscdocmigformSubmit"
   )
   public void archmscdocmigformSubmit(ActionRequest actionRequest, ActionResponse actionResponse) throws IOException, PortletException {
      try {
         System.out.println();
         Long folderId = ParamUtil.getLong(actionRequest, "folderId");
         Long fromIndex = ParamUtil.getLong(actionRequest, "fromindex");
         Long toIndex = ParamUtil.getLong(actionRequest, "toindex");
         String startStop = ParamUtil.getString(actionRequest, "startStop");
         if (startStop.equals("Start")) {
            FolderPermissionUtill.startFlag = true;
            ServiceContext var7 = ServiceContextFactory.getInstance(actionRequest);
         } else {
            FolderPermissionUtill.startFlag = false;
         }

         actionResponse.setRenderParameter("pvalue", "Success! Migration Started..");
      } catch (Exception e) {
         e.printStackTrace();
         actionResponse.setRenderParameter("pvalue", "Exception ! " + String.valueOf(e));
      }

      actionResponse.setRenderParameter("mvcPath", "/archmscdocmig.jsp");
   }
}
