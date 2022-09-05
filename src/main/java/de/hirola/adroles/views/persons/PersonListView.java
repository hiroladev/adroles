package de.hirola.adroles.views.persons;

import com.google.common.eventbus.Subscribe;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.contextmenu.GridContextMenu;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.textfield.TextArea;
import de.hirola.adroles.Global;
import de.hirola.adroles.data.entity.Person;
import de.hirola.adroles.service.IdentityService;
import de.hirola.adroles.util.ServiceResult;
import de.hirola.adroles.util.ServiceEvent;
import de.hirola.adroles.views.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.PageTitle;
import de.hirola.adroles.views.NotificationPopUp;
import de.hirola.adroles.views.ProgressModalDialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.annotation.security.PermitAll;
import java.util.ArrayList;
import java.util.List;

@Route(value="persons", layout = MainLayout.class)
@PageTitle("Persons | AD-Roles")
@PermitAll
public class PersonListView extends VerticalLayout {

    private final Logger logger = LoggerFactory.getLogger(PersonListView.class);
    private final IdentityService identityService;
    private ProgressModalDialog progressModalDialog;
    private final List<Person> selectedPersons = new ArrayList<>();
    private PersonForm personForm;
    private PersonAssignADUserForm assignADUserForm;
    private PersonAssignRoleForm assignRoleForm;
    private final Grid<Person> grid = new Grid<>(Person.class, false);
    private TextField filterTextField;
    private Button addPersonButton, updateButton, assignToRolesButton, deletePersonsButton;

    public PersonListView(IdentityService identityService) {
        this.identityService = identityService;
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            identityService.setSessionValues(this, authentication.getName());
        } catch (RuntimeException exception) {
            logger.debug("Could not determine currently user.", exception);
        }
        addClassName("persons-list-view");
        setSizeFull();
        addComponents();
        enableComponents(true);
        updateList();
    }

    @Subscribe
    public void onServiceEvent(ServiceEvent event) {
        if (getUI().isPresent()) {
            getUI().get().access(() -> {
                if (progressModalDialog != null) {
                    progressModalDialog.close();
                    ServiceResult serviceResult = event.getServiceResult();
                    if (serviceResult.operationSuccessful) {
                        NotificationPopUp.show(NotificationPopUp.INFO,
                                getTranslation("import.successful"), serviceResult.resultMessage);
                    } else {
                        NotificationPopUp.show(NotificationPopUp.ERROR,
                                getTranslation("error.import"), serviceResult.resultMessage);
                    }
                    updateList();
                }
            });
        }
    }

    private void addComponents() {

        filterTextField = new TextField();
        filterTextField.setPlaceholder(getTranslation("searchFilter"));
        filterTextField.setClearButtonVisible(true);
        filterTextField.setValueChangeMode(ValueChangeMode.LAZY);
        filterTextField.addValueChangeListener(event -> updateList());

        addPersonButton = new Button(new Icon(VaadinIcon.PLUS));
        addPersonButton.addThemeVariants(ButtonVariant.LUMO_ICON);
        addPersonButton.getElement().setAttribute("aria-label", getTranslation("addPerson"));
        addPersonButton.setWidth(Global.Component.DEFAULT_ICON_BUTTON_WIDTH);
        addPersonButton.addClickListener(click -> addPerson());

        deletePersonsButton = new Button(new Icon(VaadinIcon.MINUS));
        deletePersonsButton.addThemeVariants(ButtonVariant.LUMO_ICON);
        deletePersonsButton.getElement().setAttribute("aria-label", getTranslation("deletePersons"));
        deletePersonsButton.setWidth(Global.Component.DEFAULT_ICON_BUTTON_WIDTH);
        deletePersonsButton.addClickListener(click -> deletePersons());

        //TODO: enable / disable import by config
        updateButton = new Button(getTranslation("updateFromActiveDirectory"));
        updateButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        updateButton.setWidth(Global.Component.DEFAULT_BUTTON_WIDTH);
        updateButton.addClickListener(click -> importPersons());

        assignToRolesButton = new Button(getTranslation("assignAutomatically"));
        assignToRolesButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        assignToRolesButton.setWidth(Global.Component.DEFAULT_BUTTON_WIDTH);
        assignToRolesButton.addClickListener(click -> assignRoles());

        HorizontalLayout toolbar = new HorizontalLayout(filterTextField, addPersonButton, deletePersonsButton,
                updateButton, assignToRolesButton);
        toolbar.addClassName("toolbar");

        grid.addClassNames("person-grid");
        grid.setSizeFull();
        grid.addColumn(Person::getLastName).setHeader(getTranslation("lastname"))
                .setKey(Global.Component.FOOTER_COLUMN_KEY)
                .setSortable(true)
                .setFooter(String.format(getTranslation("persons.sum") + ": %s", identityService.countPersons()));
        grid.addColumn(Person::getFirstName).setHeader(getTranslation("firstname"))
                .setSortable(true);
        grid.addColumn(Person::getCentralAccountName)
                .setHeader(getTranslation("centralAccountName"))
                .setSortable(true);
        grid.addColumn(Person::getDepartmentName).setHeader(getTranslation("department"))
                .setSortable(true);
        grid.addColumn(Person::getDescription).setHeader(getTranslation("description"))
                .setSortable(true);
        grid.addColumn(person -> person.isEmployee() ? getTranslation("yes") : getTranslation("no"))
                .setHeader(getTranslation("employee"))
                .setSortable(true);
        grid.getColumns().forEach(col -> col.setAutoWidth(true));
        grid.setSelectionMode(Grid.SelectionMode.MULTI);
        grid.addItemClickListener(event -> editPerson(event.getItem()));
        grid.addSelectionListener(selection -> {
            selectedPersons.clear();
            if (selection.getAllSelectedItems().size() == 0) {
                deletePersonsButton.setEnabled(false);
                assignToRolesButton.setEnabled(false);
            } else {
                selectedPersons.addAll(selection.getAllSelectedItems());
                deletePersonsButton.setEnabled(true);
                assignToRolesButton.setEnabled(true);
            }
        });

        personForm = new PersonForm();
        personForm.setWidthFull();
        personForm.addListener(PersonForm.SaveEvent.class, this::savePerson);
        personForm.addListener(PersonForm.AssignADUsersEvent.class, this::assignADUsers);
        personForm.addListener(PersonForm.AssignRolesEvent.class, this::assignRoles);
        personForm.addListener(PersonForm.AssignOrgEvent.class, this::assignOrgRoles);
        personForm.addListener(PersonAssignRoleForm.CloseEvent.class, this::closeAssignRolesForm);
        personForm.addListener(PersonForm.CloseEvent.class, event -> closePersonForm());
        personForm.setVisible(false);

        assignADUserForm = new PersonAssignADUserForm(identityService);
        assignADUserForm.setWidthFull();
        assignADUserForm.addListener(PersonAssignADUserForm.SaveEvent.class, this::saveAssignedADUsers);
        assignADUserForm.addListener(PersonAssignADUserForm.CloseEvent.class, event -> closeAssignADUsersForm(event.getPerson()));
        assignADUserForm.setVisible(false);

        assignRoleForm = new PersonAssignRoleForm();
        assignRoleForm.setWidthFull();
        assignRoleForm.addListener(PersonAssignRoleForm.SaveEvent.class, this::saveAssignedRoles);
        assignRoleForm.addListener(PersonAssignRoleForm.CloseEvent.class, this::closeAssignRolesForm);
        assignRoleForm.setVisible(false);

        // context menu to change the role resource of selected roles
        PersonContextMenu contextMenu = new PersonContextMenu(grid, this);

        FlexLayout content = new FlexLayout(grid, contextMenu, personForm, assignADUserForm, assignRoleForm);
        content.setFlexGrow(2, grid);
        content.setFlexGrow(1, personForm, assignADUserForm, assignRoleForm);
        content.setFlexShrink(0, personForm, assignADUserForm, assignRoleForm);
        content.addClassNames("content", "gap-m");
        content.setSizeFull();

        add(toolbar, content);
    }
    
    private void importPersons() { //TODO: show progress bar
        Dialog dialog = new Dialog();
        dialog.setWidth(Global.Component.DEFAULT_DIALOG_WIDTH);
        if (identityService.countPersons() > 0) {
            updateButton.setEnabled(false);
            // data can be override
            dialog.setHeaderTitle(getTranslation("question.updateData"));

            TextArea messageArea = new TextArea();
            messageArea.setWidthFull();
            messageArea.setValue(getTranslation("updatePersonsFromActiveDirectory.dialog.message"));

            Button okButton = new Button("Ok", clickEvent -> {
                dialog.close();
                if (!identityService.updatePersonsFromAD()) {
                    NotificationPopUp.show(NotificationPopUp.ERROR, getTranslation("error.import"));
                }
                updateList();
            });
            okButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);
            okButton.getStyle().set("margin-right", "auto");

            Button cancelButton = new Button(getTranslation("cancel"), (clickEvent) -> dialog.close());
            cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

            dialog.add(messageArea);
            dialog.getFooter().add(okButton);
            dialog.getFooter().add(cancelButton);
            dialog.open();
        } else {
            dialog.close();
            if (!identityService.updatePersonsFromAD()) {
                NotificationPopUp.show(NotificationPopUp.ERROR, getTranslation("error.import"));
            }
            updateList();
        }
    }

    private void assignADUsers(PersonForm.AssignADUsersEvent event) {
        closePersonForm();
        enableComponents(false);
        assignADUserForm.setData(event.getPerson(), identityService.findAllADUsers(null));
        assignADUserForm.setVisible(true);
        addClassName("editing-assign-ad-users-form");
    }

    private void saveAssignedADUsers(PersonAssignADUserForm.SaveEvent event) {
        Person person = event.getPerson();
        if (identityService.savePerson(person)) {
            closeAssignADUsersForm(person);
            updateList();
        } else {
            NotificationPopUp.show(NotificationPopUp.ERROR, getTranslation("error.save"));
        }
    }

    private void assignRoles() {
        Dialog dialog = new Dialog();
        dialog.setWidth(Global.Component.DEFAULT_DIALOG_WIDTH);
        if (selectedPersons.size() > 0) {
            // data can be override
            dialog.setHeaderTitle(getTranslation("question.updateData"));
            TextArea messageArea = new TextArea();
            messageArea.setWidthFull();
            messageArea.setValue(getTranslation("assignPersonsToRoles.dialog.message"));

            Button okButton = new Button("Ok", clickEvent -> {
                progressModalDialog = new ProgressModalDialog(
                        "update",
                        "import.running.message",
                        "import.running.subMessage");
                progressModalDialog.open();
                new Thread(() -> {
                    identityService.assignPersonsToRoles(selectedPersons);
                }).start();
                dialog.close();
            });
            okButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);
            okButton.getStyle().set("margin-right", "auto");

            Button cancelButton = new Button(getTranslation("cancel"), (clickEvent) -> dialog.close());
            cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

            dialog.add(messageArea);
            dialog.getFooter().add(okButton);
            dialog.getFooter().add(cancelButton);
            dialog.open();
        }
    }

    private void updateEmployeeStatusOfSelectedPersons(boolean isEmployee) {
        for (Person person: selectedPersons) {
            person.setEmployee(isEmployee);
            if (!identityService.savePerson(person)) {
                NotificationPopUp.show(NotificationPopUp.ERROR, "error.save");
            }
        }
        updateList();
    }

    private void addPerson() {
        editPerson(new Person());
    }

    private void savePerson(PersonForm.SaveEvent event) {
        identityService.savePerson(event.getPerson());
        closePersonForm();
        updateList();
    }

    private void editPerson(Person person) {
        if (person == null) {
            closePersonForm();
        } else {
            enableComponents(false);
            personForm.setPerson(person);
            personForm.setVisible(true);
            addClassName("editing");
        }
    }

    private void deletePersons() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(getTranslation("question.delete"));

        Button okButton = new Button("Ok", clickEvent -> {
            identityService.deletePersons(selectedPersons);
            updateList();
            selectedPersons.clear();
            deletePersonsButton.setEnabled(false);
            dialog.close();
        });
        okButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);
        okButton.getStyle().set("margin-right", "auto");

        Button cancelButton = new Button(getTranslation("cancel"), clickEvent -> {
            grid.deselectAll();
            dialog.close();
        });
        cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        dialog.getFooter().add(okButton);
        dialog.getFooter().add(cancelButton);

        dialog.open();

    }

    private void assignRoles(PersonForm.AssignRolesEvent event) {
        closePersonForm();
        enableComponents(false);
        assignRoleForm.setVisible(true);
        assignRoleForm.setData(event.getPerson(), identityService.findAllRoles(null, null),
                identityService.getRoleResource(Global.ROLE_RESOURCE.DEFAULT_ROLE));
        addClassName("editing");
    }

    private void assignOrgRoles(PersonForm.AssignOrgEvent event) {
        closePersonForm();
        enableComponents(false);
        assignRoleForm.setVisible(true);
        assignRoleForm.setData(event.getPerson(), identityService.findAllRoles(null, null),
                identityService.getRoleResource(Global.ROLE_RESOURCE.ORG_ROLE));
        addClassName("editing");
    }

    private void saveAssignedRoles(PersonAssignRoleForm.SaveEvent event) {
        identityService.savePerson(event.getPerson());
        updateList();
    }

    private void updateList() {
        List<Person> filteredPersons = identityService.findAllPersons(filterTextField.getValue());
        grid.setItems(filteredPersons);
        grid.deselectAll();
        grid.getColumnByKey(Global.Component.FOOTER_COLUMN_KEY)
                .setFooter(String.format(getTranslation("persons.sum") + ": %s", filteredPersons.size()));
    }

    private void closePersonForm() {
        personForm.setPerson(null);
        enableComponents(true);
        personForm.setVisible(false);
        removeClassName("editing");
    }

    private void closeAssignADUsersForm(Person person) {
        assignADUserForm.setData(null, null);
        assignADUserForm.setVisible(false);
        personForm.setPerson(person);
        personForm.setVisible(true);
        removeClassName("editing-assign-ad-users-form");
    }

    private void closeAssignRolesForm(PersonAssignRoleForm.CloseEvent event) {
        assignRoleForm.setData(null, null, null);
        assignRoleForm.setVisible(false);
        personForm.setPerson(event.getPerson());
        personForm.setVisible(true);
        enableComponents(true);
        removeClassName("editing");
    }

    private boolean matchesTerm(String value, String searchTerm) {
        return value.toLowerCase().contains(searchTerm.toLowerCase());
    }

    private void enableComponents(boolean enabled) {
        filterTextField.setEnabled(enabled);
        addPersonButton.setEnabled(enabled);
        deletePersonsButton.setEnabled(enabled);
        updateButton.setEnabled(enabled);
        assignToRolesButton.setEnabled(enabled);

        if (enabled) {
            if (selectedPersons.size() == 0) {
                deletePersonsButton.setEnabled(false);
                assignToRolesButton.setEnabled(false);
            }
            if (!identityService.isConnected()) {
                updateButton.setEnabled(false);
            }
        }
    }

    private static class PersonContextMenu extends GridContextMenu<Person> {

        private final PersonListView listView;
        public PersonContextMenu(Grid<Person> target, PersonListView listView) {
            super(target);
            this.listView = listView;

            addItem(getTranslation("setEmployeeStatus"), event -> event.getItem().ifPresent(person -> showDialog(true)));

            addItem(getTranslation("unsetEmployeeStatus"), event -> event.getItem().ifPresent(person -> showDialog(false)));

        }

        private void showDialog(boolean isEmployee) {
            Dialog dialog = new Dialog();
            dialog.setHeaderTitle(getTranslation("question.employeeStatusChange"));

            Button okButton = new Button("Ok", clickEvent -> {
                listView.updateEmployeeStatusOfSelectedPersons(isEmployee);
                dialog.close();
            });
            okButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);
            okButton.getStyle().set("margin-right", "auto");

            Button cancelButton = new Button(getTranslation("cancel"), clickEvent -> dialog.close());
            cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

            dialog.getFooter().add(okButton);
            dialog.getFooter().add(cancelButton);

            dialog.open();
        }
    }
}
