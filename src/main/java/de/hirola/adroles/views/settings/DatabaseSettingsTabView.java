package de.hirola.adroles.views.settings;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import de.hirola.adroles.data.service.ADRolesService;
import de.hirola.adroles.views.MainLayout;

import javax.annotation.security.PermitAll;

@Route(value = "database-settings", layout = MainLayout.class)
@PageTitle("Settings - Database | AD-Roles")
@PermitAll
public class DatabaseSettingsTabView extends VerticalLayout {
    private final ADRolesService service;

    public DatabaseSettingsTabView(ADRolesService service) {
        this.service = service;
        addClassName("dashboard-view");
        setDefaultHorizontalComponentAlignment(Alignment.START);
        add(SettingsTabBar.getTabs(2));
    }
}