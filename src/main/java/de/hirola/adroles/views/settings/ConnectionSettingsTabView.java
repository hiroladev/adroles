package de.hirola.adroles.views.settings;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import de.hirola.adroles.data.entity.ActiveDirectory;
import de.hirola.adroles.data.entity.DomainController;
import de.hirola.adroles.data.service.ConnectionSettingsService;
import de.hirola.adroles.views.MainLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.security.PermitAll;
import java.net.ConnectException;
import java.security.GeneralSecurityException;
import java.util.List;

@Route(value = "connection-setting", layout = MainLayout.class)
@PageTitle("Settings - Connection | AD-Roles")
@PermitAll
public class ConnectionSettingsTabView extends VerticalLayout implements ComponentEventListener<ClickEvent<Button>> {

    private final Logger logger = LoggerFactory.getLogger(ConnectionSettingsTabView.class);
    private final ActiveDirectory activeDirectory;
    private final DomainController domainController; //TODO: in v.0.1 only 1 DC is possible
    private final ConnectionSettingsService service;
    private Button saveButton;
    private Button verifyButton;
    private final Binder<ActiveDirectory> activeDirectoryBinder = new Binder<>(ActiveDirectory.class);
    private final Binder<DomainController> domainControllerBinder = new Binder<>(DomainController.class);

    public ConnectionSettingsTabView(ConnectionSettingsService service) {
        this.service = service;
        // load the objects from backend
        activeDirectory = service.getActiveDirectory();
        activeDirectoryBinder.readBean(activeDirectory);
        List<DomainController> domainControllers = activeDirectory.getServers(); //TODO: in v.0.1 only 1 DC is possible
        if (domainControllers.isEmpty()) {
            domainController = new DomainController();
        } else {
            domainController = domainControllers.get(0);
        }
        domainControllerBinder.readBean(domainController);
        addClassName("dashboard-view");
        setDefaultHorizontalComponentAlignment(Alignment.START);
        add(SettingsTabBar.getTabs(0));
        addComponents();
    }

    private void addComponents() {
        TextField domainNameTextField = new TextField();
        domainNameTextField.setLabel(getTranslation("domain.name"));
        domainNameTextField.setPlaceholder(getTranslation("domain.name.placeholder"));
        activeDirectoryBinder
                .forField(domainNameTextField)
                .withValidator(domainName -> domainName.length() > 0, getTranslation("error.input.all.empty"))
                .bind(ActiveDirectory::getDomainName, ActiveDirectory::setDomainName);
        add(domainNameTextField);

        TextField serverTextField = new TextField();
        serverTextField.setLabel(getTranslation("domain.server.ip"));
        serverTextField.setPlaceholder(getTranslation("domain.server.ip.placeholder"));
        domainControllerBinder
                .forField(serverTextField)
                .withValidator(server -> server.length() > 0, getTranslation("error.input.all.empty"))
                .bind(DomainController::getIPAddress, DomainController::setIPAddress);
        add(serverTextField);

        NumberField serverPortTextField = new NumberField();
        serverPortTextField.setLabel(getTranslation("domain.server.port"));
        serverPortTextField.setPlaceholder(getTranslation("domain.server.port.placeholder"));
        domainControllerBinder
                .forField(serverPortTextField)
                .withValidator(port -> port > 0, getTranslation("error.input.all.empty"))
                .bind(DomainController::getPort, DomainController::setPort);
        add(serverPortTextField);

        Checkbox secureConnectionCheckBox = new Checkbox(getTranslation("domain.secure"));
        domainControllerBinder
                .forField(secureConnectionCheckBox)
                .bind(DomainController::useSecureConnection, DomainController::setUseSecureConnection);
        add(secureConnectionCheckBox);

        TextField usernameTextField = new TextField();
        usernameTextField.setLabel(getTranslation("username"));
        usernameTextField.setPlaceholder("CN=AD-Roles,CN=Users,DC=example,DC=com");
        activeDirectoryBinder
                .forField(usernameTextField)
                .withValidator(username -> username.length() > 0, getTranslation("error.input.all.empty"))
                .bind(ActiveDirectory::getConnectionUserName, ActiveDirectory::setConnectionUserName);
        add(usernameTextField);

        PasswordField passwordField = new PasswordField();
        passwordField.setLabel(getTranslation("password"));
        activeDirectoryBinder
                .forField(passwordField)
                .withValidator(password -> password.length() > 0, getTranslation("error.input.all.empty"))
                .bind(ActiveDirectory::getEncryptedConnectionPassword, ActiveDirectory::setEncryptedConnectionPassword);
        add(passwordField);

        saveButton = new Button(getTranslation("save"));
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveButton.addClickListener(this);
        add(saveButton);

        verifyButton = new Button(getTranslation("verify"));
        verifyButton.addClickListener(this);
        add(verifyButton);

    }

    @Override
    public void onComponentEvent(ClickEvent<Button> buttonClickEvent) {
        if (buttonClickEvent.getSource().equals(saveButton)) {
            try {
                // update form object from component values
                activeDirectoryBinder.writeBean(activeDirectory);
                domainControllerBinder.writeBean(domainController);
                activeDirectory.addServer(domainController);
                service.saveActiveDirectory(activeDirectory);
                Dialog dialog = new Dialog();
                dialog.add(getTranslation("data.saved"));
                dialog.open();
            } catch (ValidationException exception) {
                //TODO: inform the user
                logger.debug(exception.getLocalizedMessage());
            }
        }
        if (buttonClickEvent.getSource().equals(verifyButton)) {
            try {
                // update form object from component values
                activeDirectoryBinder.writeBean(activeDirectory);
                domainControllerBinder.writeBean(domainController);
                // test the connection
                service.verifyConnection(activeDirectory, domainController);
                Dialog dialog = new Dialog();
                dialog.add(getTranslation("domain.connected"));
                dialog.open();
            } catch (ValidationException | ConnectException | GeneralSecurityException exception) {
                Dialog dialog = new Dialog();
                dialog.setHeaderTitle(getTranslation("error.domain.connection"));
                dialog.add(exception.getMessage());
                dialog.open();
                logger.debug(exception.getMessage());
            }
        }
    }
}