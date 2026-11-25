package boi.headless.delivery.mail.internal.jaxrs.application;

import javax.ws.rs.core.Application;
import org.osgi.service.component.annotations.Component;

@Component(
   property = {"liferay.jackson=false", "osgi.jaxrs.application.base=/boi-headless-delivery-mail", "osgi.jaxrs.extension.select=(osgi.jaxrs.name=Liferay.Vulcan)", "osgi.jaxrs.name=BoiHeadlessDeliveryMail"},
   service = {Application.class}
)
public class BoiHeadlessDeliveryMailApplication extends Application {
}
