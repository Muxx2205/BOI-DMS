package com.boi.liferay.dms.assignment.portlet;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCActionCommand;
import com.liferay.portal.kernel.util.ParamUtil;
import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;
import org.osgi.service.component.annotations.Component;

@Component(
   immediate = true,
   property = {"javax.portlet.name=com_boi_liferay_dms_assignment_AssignmentUsersPortlet", "mvc.command.name=submitAssignments"},
   service = {MVCActionCommand.class}
)
public class SubmitAssignments implements MVCActionCommand {
   private static final Log log = LogFactoryUtil.getLog(SubmitAssignments.class);

   public boolean processAction(ActionRequest actionRequest, ActionResponse actionResponse) throws PortletException {
      String keyword = ParamUtil.getString(actionRequest, "organizationData");
      long[] selectedKeys = ParamUtil.getLongValues(actionRequest, "selectedKeys");
      long[] removeOrganition = ParamUtil.getLongValues(actionRequest, "removeOrganition");
      return false;
   }
}
