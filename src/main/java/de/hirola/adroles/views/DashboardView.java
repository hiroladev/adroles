package de.hirola.adroles.views;

import de.hirola.adroles.data.service.ADRolesService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import javax.annotation.security.PermitAll;

@Route(value = "dashboard", layout = MainLayout.class)
@PageTitle("Dashboard | AD Roles")
@PermitAll
public class DashboardView extends VerticalLayout {
    private final ADRolesService service;

    public DashboardView(ADRolesService service) {
        this.service = service;
        addClassName("dashboard-view");
        setDefaultHorizontalComponentAlignment(Alignment.CENTER);
        //add(getContactStats(), getCompaniesChart());
    }

    private Component getContactStats() {
        Span stats = new Span(service.countPersons() + " contacts");
        stats.addClassNames("text-xl", "mt-m");
        return stats;
    }
}