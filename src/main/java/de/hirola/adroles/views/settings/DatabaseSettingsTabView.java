package de.hirola.adroles.views.settings;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import de.hirola.adroles.Global;
import de.hirola.adroles.service.ConfigurationService;
import de.hirola.adroles.views.MainLayout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

import javax.annotation.PostConstruct;
import javax.annotation.security.PermitAll;

@Route(value = "database-settings", layout = MainLayout.class)
@PageTitle("Settings - ApplicationConfig | AD-Roles")
@PermitAll
public class DatabaseSettingsTabView extends VerticalLayout {

    @Autowired
    private Environment env;
    private final ConfigurationService configurationService;

    public DatabaseSettingsTabView(ConfigurationService configurationService) {
        this.configurationService = configurationService;
        addClassName("database-settings-tabview");
        setDefaultHorizontalComponentAlignment(Alignment.START);
        add(SettingsTabBar.getTabs(2));
    }

    @PostConstruct // Autowiring happens later than load() is called (for some reason) -> env is null
    private void addComponents() {
        TextField typeTextField = new TextField(getTranslation("database.configuration.type"));
        typeTextField.setValue(env.getProperty(Global.CONFIG.DATASOURCE_TYPE));
        typeTextField.setReadOnly(true);
        typeTextField.setWidth(Global.Component.DEFAULT_TEXT_FIELD_WIDTH);
        add(typeTextField);

        TextField configurationNameTextField = new TextField(getTranslation("database.configuration.name"));
        configurationNameTextField.setValue(env.getProperty(Global.CONFIG.DATASOURCE_NAME));
        configurationNameTextField.setReadOnly(true);
        configurationNameTextField.setWidth(Global.Component.DEFAULT_TEXT_FIELD_WIDTH);
        add(configurationNameTextField);

        TextField jdbcDriverTextField = new TextField(getTranslation("database.configuration.jdbcDriver"));
        jdbcDriverTextField.setReadOnly(true);
        jdbcDriverTextField.setWidth(Global.Component.DEFAULT_TEXT_FIELD_WIDTH);
        String jdbcDriverName = env.getProperty(Global.CONFIG.DATASOURCE_DRIVER_NAME, "");
        if (jdbcDriverName.isEmpty()) {
            jdbcDriverTextField.setValue(getTranslation("database.configuration.jdbcDriverDefaultValue"));
        } else {
            jdbcDriverTextField.setValue(jdbcDriverName);
        }
        add(jdbcDriverTextField);

        TextField jdbcURLTextField = new TextField(getTranslation("database.configuration.jdbcURL"));
        jdbcURLTextField.setReadOnly(true);
        jdbcURLTextField.setWidth(Global.Component.DEFAULT_TEXT_FIELD_WIDTH);
        add(jdbcURLTextField);
    }
}