package de.hirola.adroles.views.settings;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import de.hirola.adroles.data.service.IdentityService;
import de.hirola.adroles.views.MainLayout;

import javax.annotation.security.PermitAll;

@Route(value = "roles-settings", layout = MainLayout.class)
@PageTitle("Settings - Roles | AD-Roles")
@PermitAll
public class RoleSettingsTabView extends VerticalLayout {
    private final IdentityService service;

    public RoleSettingsTabView(IdentityService service) {
        this.service = service;
        addClassName("dashboard-view");
        setDefaultHorizontalComponentAlignment(Alignment.START);
        add(SettingsTabBar.getTabs(2));
    }

}