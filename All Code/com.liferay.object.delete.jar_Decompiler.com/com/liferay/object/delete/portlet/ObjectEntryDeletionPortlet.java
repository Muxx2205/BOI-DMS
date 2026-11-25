package com.liferay.object.delete.portlet;

import com.liferay.portal.kernel.portlet.bridges.mvc.MVCPortlet;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;
import javax.portlet.Portlet;
import org.osgi.service.component.annotations.Component;

@Component(
   immediate = true,
   property = {"com.liferay.portlet.display-category=category.sample", "com.liferay.portlet.header-portlet-css=/css/main.css", "com.liferay.portlet.instanceable=true", "javax.portlet.display-name=ObjectEntryDeletion", "javax.portlet.init-param.template-path=/", "javax.portlet.init-param.view-template=/view.jsp", "javax.portlet.name=com_liferay_object_delete_ObjectEntryDeletionPortlet", "javax.portlet.resource-bundle=content.Language", "javax.portlet.security-role-ref=power-user,user"},
   service = {Portlet.class}
)
public class ObjectEntryDeletionPortlet extends MVCPortlet {
   public void fetchFileFromServer() {
      String fileUrl = "http://localhost:8080/documents/20118/0/on_home.png/51e42bff-2919-4d85-45a7-e5f09bc3381c?version=1.0&t=1751128037633&download=true";
      String username = "test123@liferay.com";
      String password = "test123";
      String outputPath = "D:\\work\\liferay-server\\q1.1\\liferay-dxp\\cdsc.jpg";

      try {
         URL url = new URL(fileUrl);
         HttpURLConnection conn = (HttpURLConnection)url.openConnection();
         conn.setRequestMethod("GET");
         String encoded = Base64.getEncoder().encodeToString((username + ":" + password).getBytes());
         conn.setRequestProperty("Authorization", "Basic " + encoded);
         InputStream in = conn.getInputStream();

         try {
            FileOutputStream fos = new FileOutputStream(outputPath);

            try {
               byte[] buffer = new byte[8192];

               int bytesRead;
               while((bytesRead = in.read(buffer)) != -1) {
                  fos.write(buffer, 0, bytesRead);
               }
            } catch (Throwable var14) {
               try {
                  fos.close();
               } catch (Throwable var13) {
                  var14.addSuppressed(var13);
               }

               throw var14;
            }

            fos.close();
         } catch (Throwable var15) {
            if (in != null) {
               try {
                  in.close();
               } catch (Throwable var12) {
                  var15.addSuppressed(var12);
               }
            }

            throw var15;
         }

         if (in != null) {
            in.close();
         }

         System.out.println("File downloaded successfully to " + outputPath);
      } catch (Exception e) {
         e.printStackTrace();
      }

   }
}
