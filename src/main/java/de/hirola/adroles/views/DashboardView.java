package de.hirola.adroles.views;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import de.hirola.adroles.data.service.IdentityService;

import javax.annotation.security.PermitAll;

@Route(value = "", layout = MainLayout.class) // value = "" -> start page
@PageTitle("Dashboard | AD-Roles")
@PermitAll
public class DashboardView extends VerticalLayout {
    private final IdentityService service;

    public DashboardView(IdentityService service) {
        this.service = service;
        addClassName("dashboard-view");
        setDefaultHorizontalComponentAlignment(Alignment.CENTER);
        //add(getContactStats(), getCompaniesChart());
    }
}