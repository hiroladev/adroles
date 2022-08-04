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

@Route(value = "mail-resources", layout = MainLayout.class)
@PageTitle("E-Mail resources | AD-Roles")
@PermitAll
public class MailResourceRoleListView extends ResourceListView {

    public MailResourceRoleListView(IdentityService identityService) throws InstantiationException {
        super(identityService, Global.ROLE_RESOURCE.EMAIL_RESOURCE_ROLE);
    }
}
