package in.co.boi.branchadmin.web.action;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.portlet.bridges.mvc.BaseMVCActionCommand;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCActionCommand;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.ServiceContextFactory;
import com.liferay.portal.kernel.service.UserLocalServiceUtil;
import com.liferay.portal.kernel.servlet.SessionMessages;
import com.liferay.portal.kernel.util.ParamUtil;
import java.util.Locale;
import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import org.osgi.service.component.annotations.Component;

@Component(
   immediate = true,
   property = {"javax.portlet.name=in_co_boi_branchadmin_web_BranchAdminWebPortlet", "mvc.command.name=/addUser"},
   service = {MVCActionCommand.class}
)
public class AddUserMVCActionCommand extends BaseMVCActionCommand {
   private static final Log _log = LogFactoryUtil.getLog(AddUserMVCActionCommand.class);

   protected void doProcessAction(ActionRequest actionRequest, ActionResponse actionResponse) throws Exception {
      ServiceContext serviceContext = ServiceContextFactory.getInstance(User.class.getName(), actionRequest);
      long companyId = serviceContext.getCompanyId();
      long userId = ParamUtil.getLong(actionRequest, "userId");
      String firstName = ParamUtil.getString(actionRequest, "firstName");
      String lastName = ParamUtil.getString(actionRequest, "lastName");
      String emailAddress = ParamUtil.getString(actionRequest, "emailAddress");
      String screenName = ParamUtil.getString(actionRequest, "screenName");

      try {
         if (userId > 0L) {
            this.updateExistingUser(userId, firstName, lastName, emailAddress, screenName, actionRequest);
         } else {
            this.createNewUser(serviceContext, companyId, firstName, lastName, emailAddress, screenName, actionRequest);
         }

         actionResponse.getRenderParameters().setValue("mvcRenderCommandName", "/view-users");
      } catch (Exception e) {
         _log.error("Error while adding/updating user: " + e.getMessage(), e);
         SessionMessages.add(actionRequest, "userError");
      }

   }

   private void updateExistingUser(long userId, String firstName, String lastName, String emailAddress, String screenName, ActionRequest actionRequest) throws PortalException {
      User user = UserLocalServiceUtil.getUser(userId);
      user.setFirstName(firstName);
      user.setLastName(lastName);
      user.setEmailAddress(emailAddress);
      user.setScreenName(screenName);
      UserLocalServiceUtil.updateUser(user);
      Log var10000 = _log;
      String var10001 = user.getFullName();
      var10000.info("Updated existing user: " + var10001 + " (" + user.getUserId() + ")");
      SessionMessages.add(actionRequest, "userUpdatedSuccessfully");
   }

   private void createNewUser(ServiceContext serviceContext, long companyId, String firstName, String lastName, String emailAddress, String screenName, ActionRequest actionRequest) throws PortalException {
      boolean autoPassword = false;
      String password1 = "root";
      String password2 = "root";
      boolean autoScreenName = false;
      User newUser = UserLocalServiceUtil.addUser(serviceContext.getUserId(), companyId, autoPassword, password1, password2, autoScreenName, screenName, emailAddress, Locale.getDefault(), firstName, "", lastName, 0L, 0L, true, 1, 1, 1970, "", 1, (long[])null, (long[])null, (long[])null, (long[])null, false, serviceContext);
      newUser.setPasswordEncrypted(true);
      UserLocalServiceUtil.updateUser(newUser);
      Log var10000 = _log;
      String var10001 = newUser.getPassword();
      var10000.info("Created new user: " + var10001 + newUser.getFullName() + " (" + newUser.getUserId() + ")");
      SessionMessages.add(actionRequest, "userAddedSuccessfully");
   }
}
