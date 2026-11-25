package com.liferay.boi.ua.headless.web.internal.jaxrs.application;

import javax.ws.rs.core.Application;
import org.osgi.service.component.annotations.Component;

@Component(
   property = {"liferay.jackson=false", "osgi.jaxrs.application.base=/boi-headless-ua-web", "osgi.jaxrs.extension.select=(osgi.jaxrs.name=Liferay.Vulcan)", "osgi.jaxrs.name=BoiHeadlessUaWeb"},
   service = {Application.class}
)
public class BoiHeadlessUaWebApplication extends Application {
}
