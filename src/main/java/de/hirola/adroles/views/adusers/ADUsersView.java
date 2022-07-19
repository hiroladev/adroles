package de.hirola.adroles.views.adusers;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import de.hirola.adroles.data.service.IdentityService;
import de.hirola.adroles.views.MainLayout;

import javax.annotation.security.PermitAll;

@Route(value = "adusers", layout = MainLayout.class)
@PageTitle("AD Users | AD-Roles")
@PermitAll
public class ADUsersView extends VerticalLayout {
    private final IdentityService service;

    public ADUsersView(IdentityService service) {
        this.service = service;
        addClassName("dashboard-view");
        setDefaultHorizontalComponentAlignment(Alignment.CENTER);
        //add(getContactStats(), getCompaniesChart());
    }

}