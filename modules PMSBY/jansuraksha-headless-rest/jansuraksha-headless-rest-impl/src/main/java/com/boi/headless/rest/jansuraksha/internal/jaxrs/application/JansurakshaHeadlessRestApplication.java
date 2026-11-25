package com.boi.headless.rest.jansuraksha.internal.jaxrs.application;

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
		"osgi.jaxrs.application.base=/jansuraksha-headless-rest",
		"osgi.jaxrs.extension.select=(osgi.jaxrs.name=Liferay.Vulcan)",
		"osgi.jaxrs.name=JansurakshaHeadlessRest"
	},
	service = Application.class
)
@Generated("")
public class JansurakshaHeadlessRestApplication extends Application {
}