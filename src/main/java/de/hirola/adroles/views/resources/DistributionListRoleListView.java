/*
 * Copyright 2022 by Michael Schmidt, Hirola Consulting
 * This software us licensed under the AGPL-3.0 or later.
 *
 * @author Michael Schmidt (Hirola)
 * @since v0.1
 */

package de.hirola.adroles.views.resources;

import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import de.hirola.adroles.Global;
import de.hirola.adroles.service.IdentityService;
import de.hirola.adroles.views.MainLayout;

import javax.annotation.security.PermitAll;

@Route(value = "distribution-list", layout = MainLayout.class)
@PageTitle("Distribution Lists | AD-Roles")
@PermitAll
public class DistributionListRoleListView extends ResourceListView {

    public DistributionListRoleListView(IdentityService identityService) throws InstantiationException {
        super(identityService, Global.ROLE_RESOURCE.DISTRIBUTION_LIST_ROLE);
    }
}
