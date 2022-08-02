package de.hirola.adroles.views;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import de.hirola.adroles.security.SecurityService;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.RouterLink;
import de.hirola.adroles.views.adgroups.ADGroupListView;
import de.hirola.adroles.views.adusers.ADUserListView;
import de.hirola.adroles.views.employees.EmployeeListView;
import de.hirola.adroles.views.resources.DistributionListRoleListView;
import de.hirola.adroles.views.resources.FileShareRoleListView;
import de.hirola.adroles.views.resources.OrgRoleListView;
import de.hirola.adroles.views.persons.PersonListView;
import de.hirola.adroles.views.resources.ProjectRoleListView;
import de.hirola.adroles.views.roles.RolesListView;
import de.hirola.adroles.views.settings.BasicSettingsView;

public class MainLayout extends AppLayout {
    private final SecurityService securityService;

    public MainLayout(SecurityService securityService) {
        this.securityService = securityService;
        createHeader();
        createDrawer();
    }

    private void createHeader() {
        H1 logo = new H1(getTranslation("app.name"));
        logo.addClassNames("text-l", "m-m");

        Button logout = new Button(getTranslation("logout"), event -> securityService.logout());

        HorizontalLayout header = new HorizontalLayout(new DrawerToggle(), logo, logout);

        header.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        header.expand(logo);
        header.setWidth("100%");
        header.addClassNames("py-0", "px-m");

        addToNavbar(header);

    }

    private void createDrawer() {
        Tabs tabs = getTabs();
        addToDrawer(tabs);
    }

    private Tabs getTabs() {
        Tabs tabs = new Tabs();
        tabs.add(
                createTab(VaadinIcon.DASHBOARD, DashboardView.class, getTranslation("dashboard")),
                createTab(VaadinIcon.SPECIALIST, EmployeeListView.class, getTranslation("employees")),
                createTab(VaadinIcon.USERS, PersonListView.class, getTranslation("persons")),
                createTab(VaadinIcon.OFFICE, OrgRoleListView.class, getTranslation("organizations")),
                createTab(VaadinIcon.CALENDAR_BRIEFCASE, ProjectRoleListView.class, getTranslation("projects")),
                createTab(VaadinIcon.FOLDER, FileShareRoleListView.class, getTranslation("fileShares")),
                createTab(VaadinIcon.FOLDER, DistributionListRoleListView.class, getTranslation("distributionLists")),
                createTab(VaadinIcon.CONNECT, RolesListView.class, getTranslation("roles")),
                createTab(VaadinIcon.USER_STAR, ADUserListView.class, getTranslation("aduser")),
                createTab(VaadinIcon.GROUP, ADGroupListView.class, getTranslation("adgroup")),
                createTab(VaadinIcon.CONTROLLER, BasicSettingsView.class, getTranslation("settings"))
        );
        tabs.setOrientation(Tabs.Orientation.VERTICAL);
        return tabs;
    }

    private Tab createTab(VaadinIcon viewIcon, Class<? extends Component> viewClass, String menuText) {
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
