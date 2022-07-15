package de.hirola.adroles.views.adusers;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import de.hirola.adroles.data.service.ADRolesService;
import de.hirola.adroles.views.MainLayout;

import javax.annotation.security.PermitAll;

@Route(value = "adusers", layout = MainLayout.class)
@PageTitle("AD Users | AD-Roles")
@PermitAll
public class ADUsersView extends VerticalLayout {
    private final ADRolesService service;

    public ADUsersView(ADRolesService service) {
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