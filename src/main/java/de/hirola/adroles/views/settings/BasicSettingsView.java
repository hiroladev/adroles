package de.hirola.adroles.views.settings;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import de.hirola.adroles.data.service.IdentityService;
import de.hirola.adroles.views.MainLayout;

import javax.annotation.security.PermitAll;

@Route(value = "settings", layout = MainLayout.class)
@PageTitle("Settings | AD-Roles")
@PermitAll
public class BasicSettingsView extends VerticalLayout {
    private final IdentityService service;

    public BasicSettingsView(IdentityService service) {
        this.service = service;
        addClassName("dashboard-view");
        setDefaultHorizontalComponentAlignment(Alignment.START);
        add(SettingsTabBar.getTabs(0));
    }
}