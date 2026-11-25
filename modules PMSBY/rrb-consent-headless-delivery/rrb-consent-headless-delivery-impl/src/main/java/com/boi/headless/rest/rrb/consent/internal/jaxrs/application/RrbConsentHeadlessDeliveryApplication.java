package com.boi.headless.rest.rrb.consent.internal.jaxrs.application;

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
		"osgi.jaxrs.application.base=/rrb-consent-headless-delivery",
		"osgi.jaxrs.extension.select=(osgi.jaxrs.name=Liferay.Vulcan)",
		"osgi.jaxrs.name=RrbConsentHeadlessDelivery"
	},
	service = Application.class
)
@Generated("")
public class RrbConsentHeadlessDeliveryApplication extends Application {
}