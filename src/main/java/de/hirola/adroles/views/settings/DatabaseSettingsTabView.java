package de.hirola.adroles.views.settings;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import de.hirola.adroles.data.DataSourceFactory;
import de.hirola.adroles.views.MainLayout;
import de.hirola.adroles.views.NotificationPopUp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.security.PermitAll;

@Route(value = "database-settings", layout = MainLayout.class)
@PageTitle("Settings - ApplicationConfig | AD-Roles")
@PermitAll
public class DatabaseSettingsTabView extends VerticalLayout implements ComponentEventListener<ClickEvent<Button>> {
    private final Logger logger = LoggerFactory.getLogger(DatabaseSettingsTabView.class);
    private Button saveButton;
    private Button verifyButton;

    public DatabaseSettingsTabView() {

        addClassName("dashboard-view");
        setDefaultHorizontalComponentAlignment(Alignment.START);
        add(SettingsTabBar.getTabs(3));
        addComponents();
    }

    private void addComponents() {

    }

    @Override
    public void onComponentEvent(ClickEvent<Button> buttonClickEvent) {
    }
}