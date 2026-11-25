package com.boi.web.headless.delivery.internal.jaxrs.application;

import javax.ws.rs.core.Application;
import org.osgi.service.component.annotations.Component;

@Component(
   property = {"liferay.jackson=false", "osgi.jaxrs.application.base=/boi-headless-delivery-web", "osgi.jaxrs.extension.select=(osgi.jaxrs.name=Liferay.Vulcan)", "osgi.jaxrs.name=BoiHeadlessDeliveryWeb"},
   service = {Application.class}
)
public class BoiHeadlessDeliveryWebApplication extends Application {
}
