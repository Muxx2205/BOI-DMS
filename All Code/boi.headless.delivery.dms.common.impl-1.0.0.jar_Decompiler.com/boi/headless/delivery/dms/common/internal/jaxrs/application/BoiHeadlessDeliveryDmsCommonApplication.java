package boi.headless.delivery.dms.common.internal.jaxrs.application;

import javax.ws.rs.core.Application;
import org.osgi.service.component.annotations.Component;

@Component(
   property = {"liferay.jackson=false", "osgi.jaxrs.application.base=/boi-headless-delivery-dms-common", "osgi.jaxrs.extension.select=(osgi.jaxrs.name=Liferay.Vulcan)", "osgi.jaxrs.name=BoiHeadlessDeliveryDmsCommon"},
   service = {Application.class}
)
public class BoiHeadlessDeliveryDmsCommonApplication extends Application {
}
