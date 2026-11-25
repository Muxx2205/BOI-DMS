package com.boi.headless.rest.pmsby.internal.jaxrs.application;

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
		"osgi.jaxrs.application.base=/pmsby-headless-delivery",
		"osgi.jaxrs.extension.select=(osgi.jaxrs.name=Liferay.Vulcan)",
		"osgi.jaxrs.name=PmsbyHeadlessDelivery"
	},
	service = Application.class
)
@Generated("")
public class PmsbyHeadlessDeliveryApplication extends Application {
}