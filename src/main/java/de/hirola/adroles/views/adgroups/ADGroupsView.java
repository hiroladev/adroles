package de.hirola.adroles.views.adgroups;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import de.hirola.adroles.data.service.IdentityService;
import de.hirola.adroles.views.MainLayout;

import javax.annotation.security.PermitAll;

@Route(value = "adgroups", layout = MainLayout.class)
@PageTitle("AD Groups | AD-Roles")
@PermitAll
public class ADGroupsView extends VerticalLayout {
    private final IdentityService service;

    public ADGroupsView(IdentityService service) {
        this.service = service;
        addClassName("dashboard-view");
        setDefaultHorizontalComponentAlignment(Alignment.CENTER);
        //add(getContactStats(), getCompaniesChart());
    }

}