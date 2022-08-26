package de.hirola.adroles.views.settings;

import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import de.hirola.adroles.service.IdentityService;
import de.hirola.adroles.views.MainLayout;

import javax.annotation.security.PermitAll;
import java.util.Properties;

@Route(value = "settings", layout = MainLayout.class)
@PageTitle("Settings | AD-Roles")
@PermitAll
public class BasicSettingsView extends VerticalLayout {

    private Properties systemProperties = System.getProperties();
    private Checkbox importDeactivateObjectsCB;

    public BasicSettingsView(IdentityService service) {
        addClassName("dashboard-view");
        setDefaultHorizontalComponentAlignment(Alignment.START);
        add(SettingsTabBar.getTabs(0));
        addComponents();
    }

    private void addComponents() {
        importDeactivateObjectsCB = new Checkbox(getTranslation("importDeactivateObjects"));
        importDeactivateObjectsCB.addValueChangeListener(event -> {
            if (importDeactivateObjectsCB.getValue()) {
                systemProperties.setProperty("", "");
            } else {
                systemProperties.setProperty("", "");
            }
        });
        add(importDeactivateObjectsCB);
    }
}