package com.boi.headless.web.customeraccount.internal.jaxrs.application;

import javax.ws.rs.core.Application;
import org.osgi.service.component.annotations.Component;

@Component(
   property = {"liferay.jackson=false", "osgi.jaxrs.application.base=/customer-account-headless-delivery", "osgi.jaxrs.extension.select=(osgi.jaxrs.name=Liferay.Vulcan)", "osgi.jaxrs.name=CustomerAccountHeadlessDelivery"},
   service = {Application.class}
)
public class CustomerAccountHeadlessDeliveryApplication extends Application {
}
