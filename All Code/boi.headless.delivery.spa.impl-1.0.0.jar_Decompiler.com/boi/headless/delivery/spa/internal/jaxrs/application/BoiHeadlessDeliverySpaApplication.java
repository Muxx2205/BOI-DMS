package boi.headless.delivery.spa.internal.jaxrs.application;

import javax.ws.rs.core.Application;
import org.osgi.service.component.annotations.Component;

@Component(
   property = {"liferay.jackson=false", "osgi.jaxrs.application.base=/boi-headless-delivery-spa", "osgi.jaxrs.extension.select=(osgi.jaxrs.name=Liferay.Vulcan)", "osgi.jaxrs.name=BoiHeadlessDeliverySpa"},
   service = {Application.class}
)
public class BoiHeadlessDeliverySpaApplication extends Application {
}
