package de.hirola.adroles.views.organizations;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import de.hirola.adroles.data.service.IdentityService;
import de.hirola.adroles.views.MainLayout;

import javax.annotation.security.PermitAll;

@Route(value = "organizations", layout = MainLayout.class)
@PageTitle("Organizations | AD-Roles")
@PermitAll
public class OrganizationsView extends VerticalLayout {
    private final IdentityService service;

    public OrganizationsView(IdentityService service) {
        this.service = service;
        addClassName("dashboard-view");
        setDefaultHorizontalComponentAlignment(Alignment.CENTER);
        //add(getContactStats(), getCompaniesChart());
    }
}