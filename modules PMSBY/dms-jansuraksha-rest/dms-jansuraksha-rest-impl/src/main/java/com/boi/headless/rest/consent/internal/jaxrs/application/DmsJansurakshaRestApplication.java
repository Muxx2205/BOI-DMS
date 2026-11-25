package com.boi.headless.rest.consent.internal.jaxrs.application;

import javax.annotation.Generated;

import javax.ws.rs.core.Application;

import org.osgi.service.component.annotations.Component;

/**
 * @author shehn
 * @generated
 */
@Component(
	property = {
		"liferay.jackson=false",
		"osgi.jaxrs.application.base=/dms-jansuraksha-rest",
		"osgi.jaxrs.extension.select=(osgi.jaxrs.name=Liferay.Vulcan)",
		"osgi.jaxrs.name=DmsJansurakshaRest"
	},
	service = Application.class
)
@Generated("")
public class DmsJansurakshaRestApplication extends Application {
}