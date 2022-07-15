package de.hirola.adroles.views.adgroups;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import de.hirola.adroles.data.service.ADRolesService;
import de.hirola.adroles.views.MainLayout;

import javax.annotation.security.PermitAll;

@Route(value = "adgroups", layout = MainLayout.class)
@PageTitle("AD Groups | AD-Roles")
@PermitAll
public class ADGroupsView extends VerticalLayout {
    private final ADRolesService service;

    public ADGroupsView(ADRolesService service) {
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