package de.hirola.adroles.views.settings;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import de.hirola.adroles.Global;
import de.hirola.adroles.data.entity.DatabaseConfiguration;
import de.hirola.adroles.service.DatabaseConfigurationService;
import de.hirola.adroles.views.MainLayout;
import de.hirola.adroles.views.NotificationPopUp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.security.PermitAll;

@Route(value = "database-settings", layout = MainLayout.class)
@PageTitle("Settings - DatabaseConfiguration | AD-Roles")
@PermitAll
public class DatabaseSettingsTabView extends VerticalLayout implements ComponentEventListener<ClickEvent<Button>> {
    private final Logger logger = LoggerFactory.getLogger(DatabaseConfiguration.class);
    private final DatabaseConfiguration databaseConfiguration;
    private final DatabaseConfigurationService service;
    private Button saveButton;
    private Button verifyButton;
    private final Binder<DatabaseConfiguration> databaseConfigurationBinder = new Binder<>(DatabaseConfiguration.class);

    public DatabaseSettingsTabView(DatabaseConfigurationService service) {
        this.service = service;
        databaseConfiguration = new DatabaseConfiguration(); //TODO: load
        addClassName("dashboard-view");
        setDefaultHorizontalComponentAlignment(Alignment.START);
        add(SettingsTabBar.getTabs(3));
        addComponents();
    }

    private void addComponents() {
        ComboBox<DatabaseConfiguration> comboBox = new ComboBox<>(getTranslation("database.configuration"));
        comboBox.setItems(service.getDatabases());
        comboBox.setItemLabelGenerator(DatabaseConfiguration::getName);
        add(comboBox);

        TextField configurationNameTextField = new TextField();
        configurationNameTextField.setLabel(getTranslation("database.configuration.name"));
        configurationNameTextField.setWidth(Global.Component.DEFAULT_TEXT_FIELD_WIDTH);
        databaseConfigurationBinder
                .forField(configurationNameTextField)
                .withValidator(name -> name.length() > 0, getTranslation("error.input.name.empty"))
                .bind(DatabaseConfiguration::getName, DatabaseConfiguration::setName);
        add(configurationNameTextField);

        TextField jdbcDriverTextField = new TextField();
        jdbcDriverTextField.setLabel(getTranslation("database.configuration.jdbcDriver"));
        jdbcDriverTextField.setPlaceholder(getTranslation("database.configuration.jdbcDriver.placeholder"));
        jdbcDriverTextField.setWidth(Global.Component.DEFAULT_TEXT_FIELD_WIDTH);
        databaseConfigurationBinder
                .forField(jdbcDriverTextField)
                .withValidator(jdbcDriverString -> jdbcDriverString.length() > 0, getTranslation("error.input.jdbcDriver.empty"))
                .bind(DatabaseConfiguration::getJdbcDriver, DatabaseConfiguration::setJdbcDriver);
        add(jdbcDriverTextField);

        TextField jdbcURLTextField = new TextField();
        jdbcURLTextField.setLabel(getTranslation("database.configuration.jdbcURL"));
        jdbcURLTextField.setPlaceholder(getTranslation("database.configuration.jdbcURL.placeholder"));
        jdbcURLTextField.setWidth(Global.Component.DEFAULT_TEXT_FIELD_WIDTH);
        databaseConfigurationBinder
                .forField(jdbcURLTextField)
                .withValidator(jdbcDriverString -> jdbcDriverString.length() > 0, getTranslation("error.input.jdbcDriver.empty"))
                .bind(DatabaseConfiguration::getJdbcUrl, DatabaseConfiguration::setJdbcUrl);
        add(jdbcURLTextField);

        TextField usernameTextField = new TextField();
        usernameTextField.setLabel(getTranslation("database.configuration.username"));
        usernameTextField.setWidth(Global.Component.DEFAULT_TEXT_FIELD_WIDTH);
        databaseConfigurationBinder
                .forField(usernameTextField)
                .bind(DatabaseConfiguration::getUsername, DatabaseConfiguration::setUsername);
        add(usernameTextField);

        PasswordField passwordField = new PasswordField();
        passwordField.setLabel(getTranslation("database.configuration.password"));
        passwordField.setWidth(Global.Component.DEFAULT_TEXT_FIELD_WIDTH);
        databaseConfigurationBinder
                .forField(passwordField)
                .bind(DatabaseConfiguration::getPassword, DatabaseConfiguration::setPassword);
        add(passwordField);

        saveButton = new Button(getTranslation("save"));
        saveButton.setWidth(Global.Component.DEFAULT_BUTTON_WIDTH);
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveButton.addClickListener(this);
        add(saveButton);

        verifyButton = new Button(getTranslation("verify"));
        verifyButton.setWidth(Global.Component.DEFAULT_BUTTON_WIDTH);
        verifyButton.addClickListener(this);
        add(verifyButton);

    }

    @Override
    public void onComponentEvent(ClickEvent<Button> buttonClickEvent) {
        if (buttonClickEvent.getSource().equals(saveButton)) {
            try {
                // update form object from component values
                databaseConfigurationBinder.writeBean(databaseConfiguration);
                NotificationPopUp.show(NotificationPopUp.INFO, getTranslation("data.saved"));
            } catch (ValidationException exception) {
                //TODO: inform the user
                logger.debug(exception.getLocalizedMessage());
            }
        }
        if (buttonClickEvent.getSource().equals(verifyButton)) {
            try {
                // update form object from component values
                databaseConfigurationBinder.writeBean(databaseConfiguration);
                NotificationPopUp.show(NotificationPopUp.INFO, getTranslation("domain.connected"));
            } catch (ValidationException exception) {
                NotificationPopUp.show(NotificationPopUp.ERROR, getTranslation("error.domain.connection"));
                logger.debug(exception.getMessage());
            }
        }
    }
}