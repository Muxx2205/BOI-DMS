package boi.headless.delivery.memorandumapi.internal.jaxrs.application;

import javax.ws.rs.core.Application;
import org.osgi.service.component.annotations.Component;

@Component(
   property = {"liferay.jackson=false", "osgi.jaxrs.application.base=/boi-headless-delivery-memorandumapi", "osgi.jaxrs.extension.select=(osgi.jaxrs.name=Liferay.Vulcan)", "osgi.jaxrs.name=BoiHeadlessDeliveryMemorandumapi"},
   service = {Application.class}
)
public class BoiHeadlessDeliveryMemorandumapiApplication extends Application {
}
