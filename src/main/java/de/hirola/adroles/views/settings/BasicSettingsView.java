package de.hirola.adroles.views.settings;

import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import de.hirola.adroles.Global;
import de.hirola.adroles.service.IdentityService;
import de.hirola.adroles.views.MainLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.logging.LoggersEndpoint;
import org.springframework.boot.logging.LogLevel;

import javax.annotation.security.PermitAll;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

@Route(value = "settings", layout = MainLayout.class)
@PageTitle("Settings | AD-Roles")
@PermitAll
public class BasicSettingsView extends VerticalLayout {

    private final Logger logger = LoggerFactory.getLogger(BasicSettingsView.class);
    private final LoggersEndpoint loggersEndpoint;

    public BasicSettingsView(LoggersEndpoint loggersEndpoint) {
        this.loggersEndpoint = loggersEndpoint;
        addClassName("dashboard-view");
        setDefaultHorizontalComponentAlignment(Alignment.START);
        add(SettingsTabBar.getTabs(0));
        addComponents();
    }

    private void addComponents() {
        Checkbox importDeactivateObjectsCB = new Checkbox(getTranslation("importDeactivateObjects"));
        importDeactivateObjectsCB.addValueChangeListener(event -> {
            if (importDeactivateObjectsCB.getValue()) {
                System.setProperty("", "");
            } else {
                System.setProperty("", "");
            }
        });
        add(importDeactivateObjectsCB);

        ComboBox<String> logLevelComboBox = new ComboBox<>(getTranslation("logLevel"));
        logLevelComboBox.setHelperText(getTranslation("logLevelHelperText"));
        String[] logLevelStrings = new String[]{"ERROR", "WARN", "INFO", "DEBUG", "TRACE"};
        String actualLogLevelString = loggersEndpoint.loggerLevels("de.hirola").getConfiguredLevel();
        logLevelComboBox.setItems(Arrays.stream(logLevelStrings).toList());
        logLevelComboBox.setValue(actualLogLevelString);
        logLevelComboBox.addValueChangeListener(event -> {
            String logLevel = logLevelComboBox.getValue();
            if (!logLevel.isEmpty()) {
                try {
                    loggersEndpoint.configureLogLevel("de.hirola", LogLevel.valueOf(logLevel));
                } catch(IllegalArgumentException exception) {
                    loggersEndpoint.configureLogLevel("de.hirola", LogLevel.DEBUG);
                    logger.debug("Log level \"" + logLevel + "\" is unknown. Using debug level.");
                }
            }
        });
        add(logLevelComboBox);
    }
}