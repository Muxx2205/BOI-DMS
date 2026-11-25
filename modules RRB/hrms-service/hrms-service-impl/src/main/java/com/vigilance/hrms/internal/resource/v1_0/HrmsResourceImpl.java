/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.vigilance.hrms.internal.resource.v1_0;

import com.vigilance.hrms.resource.v1_0.HrmsResource;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;

/**
 * @author shehn
 */
@Component(
	properties = "OSGI-INF/liferay/rest/v1_0/hrms.properties",
	scope = ServiceScope.PROTOTYPE, service = HrmsResource.class
)
public class HrmsResourceImpl extends BaseHrmsResourceImpl {
}