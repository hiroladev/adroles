/*
 * *
 *  * Copyright 2022 by Michael Schmidt, Hirola Consulting
 *  * This software us licensed under the AGPL-3.0 or later.
 *  *
 *  *
 *  * @author Michael Schmidt (Hirola)
 *  * @since v0.1
 *
 */

package de.hirola.adroles.views.roles;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.router.RouterLink;
import de.hirola.adroles.views.settings.BasicSettingsView;
import de.hirola.adroles.views.settings.ConnectionSettingsTabView;
import de.hirola.adroles.views.settings.DatabaseSettingsTabView;
import de.hirola.adroles.views.settings.RoleSettingsTabView;

/**
 * Copyright 2022 by Michael Schmidt, Hirola Consulting
 * This software us licensed under the AGPL-3.0 or later.
 *
 * The tabs for all roles dialogs.
 *
 * @author Michael Schmidt (Hirola)
 * @since v0.1
 */
public final class RolesTabBar {

    public static Tabs getTabs(int selectedIndex) {
        Tabs tabs = new Tabs();
        tabs.add(
                createTab(VaadinIcon.TABLE, RolesListView.class, UI.getCurrent().getTranslation("overview")),
                createTab(VaadinIcon.SERVER, ConnectionSettingsTabView.class, UI.getCurrent().getTranslation("settings.connection")),
                createTab(VaadinIcon.AUTOMATION, RoleSettingsTabView.class, UI.getCurrent().getTranslation("settings.roles")),
                createTab(VaadinIcon.DATABASE, DatabaseSettingsTabView.class, UI.getCurrent().getTranslation("settings.database"))
        );
        tabs.setOrientation(Tabs.Orientation.HORIZONTAL);
        tabs.setSelectedIndex(selectedIndex);
        return tabs;
    }

    private static Tab createTab(VaadinIcon viewIcon, Class<? extends Component> viewClass, String menuText) {
        Icon icon = viewIcon.create();
        icon.getStyle()
                .set("box-sizing", "border-box")
                .set("margin-inline-end", "var(--lumo-space-m)")
                .set("margin-inline-start", "var(--lumo-space-xs)")
                .set("padding", "var(--lumo-space-xs)");

        RouterLink link = new RouterLink();
        link.add(icon, new Span(menuText));
        link.setRoute(viewClass);
        link.setTabIndex(-1);

        return new Tab(link);
    }
}
