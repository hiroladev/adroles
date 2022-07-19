package de.hirola.adroles.views.roles;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import de.hirola.adroles.data.service.IdentityService;
import de.hirola.adroles.views.MainLayout;

import javax.annotation.security.PermitAll;

@Route(value = "roles", layout = MainLayout.class)
@PageTitle("Roles | AD-Roles")
@PermitAll
public class RolesView extends VerticalLayout {
    private final IdentityService service;

    public RolesView(IdentityService service) {
        this.service = service;
        addClassName("dashboard-view");
        setDefaultHorizontalComponentAlignment(Alignment.CENTER);
        //add(getContactStats(), getCompaniesChart());
    }

}