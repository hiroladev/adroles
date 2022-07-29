package de.hirola.adroles.views.persons;

import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.textfield.TextArea;
import de.hirola.adroles.Global;
import de.hirola.adroles.data.entity.Person;
import de.hirola.adroles.service.IdentityService;
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

import javax.annotation.security.PermitAll;
import java.util.ArrayList;
import java.util.List;

@Route(value="persons", layout = MainLayout.class)
@PageTitle("Persons | AD-Roles")
@PermitAll
public class PersonListView extends VerticalLayout {
    private final IdentityService identityService;
    private final List<Person> selectedPersons = new ArrayList<>();
    private PersonForm personForm;
    private PersonAssignRoleForm assignRoleForm;
    private final Grid<Person> grid = new Grid<>(Person.class, false);
    private TextField filterTextField;
    private Button addPersonButton, importButton, deletePersonsButton;

    public PersonListView(IdentityService identityService) {
        this.identityService = identityService;
        addClassName("persons-list-view");
        setSizeFull();
        addComponents();
        updateList();
        closePersonForm();
        closeAssignRolesForm();
    }

    private void addComponents() {

        filterTextField = new TextField();
        filterTextField.setPlaceholder(getTranslation("searchFilter"));
        filterTextField.setClearButtonVisible(true);
        filterTextField.setValueChangeMode(ValueChangeMode.LAZY);
        filterTextField.addValueChangeListener(event -> updateList());

        addPersonButton = new Button(getTranslation("addPerson"));
        addPersonButton.setWidth(Global.Component.DEFAULT_BUTTON_WIDTH);
        addPersonButton.addClickListener(click -> addPerson());

        deletePersonsButton = new Button(getTranslation("deletePersons"));
        deletePersonsButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);
        deletePersonsButton.setWidth(Global.Component.DEFAULT_BUTTON_WIDTH);
        deletePersonsButton.addClickListener(click -> deletePersons());

        //TODO: enable / disable import by config
        importButton = new Button(getTranslation("importFromActiveDirectory"));
        importButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        importButton.setWidth(Global.Component.DEFAULT_BUTTON_WIDTH);
        importButton.addClickListener(click -> importPersons());

        HorizontalLayout toolbar = new HorizontalLayout(filterTextField, addPersonButton, deletePersonsButton, importButton);
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
        grid.getColumns().forEach(col -> col.setAutoWidth(true));
        grid.setSelectionMode(Grid.SelectionMode.MULTI);
        grid.addItemClickListener(event -> editPerson(event.getItem()));
        grid.addSelectionListener(selection -> {
            selectedPersons.clear();
            if (selection.getAllSelectedItems().size() == 0) {
                deletePersonsButton.setEnabled(false);
            } else {
                deletePersonsButton.setEnabled(true);
                selectedPersons.addAll(selection.getAllSelectedItems());
            }
        });

        personForm = new PersonForm();
        personForm.setWidthFull();
        personForm.addListener(PersonForm.SaveEvent.class, this::savePerson);
        personForm.addListener(PersonForm.AssignRolesEvent.class, this::addRoles);
        personForm.addListener(PersonForm.DeleteEvent.class, this::deletePerson);
        personForm.addListener(PersonForm.CloseEvent.class, event -> closePersonForm());

        assignRoleForm = new PersonAssignRoleForm();
        assignRoleForm.setWidthFull();
        assignRoleForm.addListener(PersonAssignRoleForm.SaveEvent.class, this::saveAssignedRoles);
        assignRoleForm.addListener(PersonAssignRoleForm.CloseEvent.class, event -> closeAssignRolesForm());

        FlexLayout content = new FlexLayout(grid, personForm, assignRoleForm);
        content.setFlexGrow(2, grid);
        content.setFlexGrow(1, personForm, assignRoleForm);
        content.setFlexShrink(0, personForm, assignRoleForm);
        content.addClassNames("content", "gap-m");
        content.setSizeFull();

        add(toolbar, content);
    }

    private void importPersons() {
        Dialog dialog = new Dialog();
        if (identityService.countPersons() > 0) {
            // data can be override
            dialog.setHeaderTitle(getTranslation("question.updateData"));

            TextArea messageArea = new TextArea();
            messageArea.setWidthFull();
            messageArea.setValue(getTranslation("persons.importFromActiveDirectory.dialog.message"));

            Button okButton = new Button("Ok", clickEvent -> {
                dialog.close();
                if (!identityService.importPersonsFromAD(false)) {
                    NotificationPopUp.show(NotificationPopUp.ERROR, getTranslation("error.import"));
                }
                updateList();
            });
            okButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);
            okButton.getStyle().set("margin-right", "auto");

            Button partiallyButton = new Button(getTranslation("question.missing"), clickEvent -> {
                dialog.close();
                if (!identityService.importPersonsFromAD(true)) {
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
            dialog.getFooter().add(partiallyButton);
            dialog.getFooter().add(cancelButton);

            dialog.open();
        } else {
            dialog.close();
            if (!identityService.importPersonsFromAD(false)) {
                NotificationPopUp.show(NotificationPopUp.ERROR, getTranslation("error.import"));
            }
            updateList();
        }
    }

    private void addPerson() {
        editPerson(new Person());
    }

    private void savePerson(PersonForm.SaveEvent event) {
        identityService.savePerson(event.getPerson());
        closePersonForm();
        updateList();
    }

    public void editPerson(Person person) {
        if (person == null) {
            closePersonForm();
        } else {
            enableComponents(false);
            personForm.setPerson(person);
            personForm.setVisible(true);
            addClassName("editing");
        }
    }

    private void deletePerson(PersonForm.DeleteEvent event) {
        identityService.deletePerson(event.getPerson());
        updateList();
        closePersonForm();
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

    private void addRoles(PersonForm.AssignRolesEvent event) {
        closePersonForm();
        enableComponents(false);
        assignRoleForm.setVisible(true);
        assignRoleForm.setData(event.getPerson(), identityService.findAllRoles(null, null));
        addClassName("editing");
    }

    private void saveAssignedRoles(PersonAssignRoleForm.SaveEvent event) {
        identityService.savePerson(event.getPerson());
        updateList();
    }

    private void updateList() {
        List<Person> filteredPersons = identityService.findAllPersons(filterTextField.getValue());
        grid.setItems(filteredPersons);
        grid.getColumnByKey(Global.Component.FOOTER_COLUMN_KEY)
                .setFooter(String.format(getTranslation("persons.sum") + ": %s", filteredPersons.size()));
    }

    private void closePersonForm() {
        personForm.setPerson(null);
        enableComponents(true);
        personForm.setVisible(false);
        removeClassName("editing");
    }

    private void closeAssignRolesForm() {
        assignRoleForm.setData(null, null);
        assignRoleForm.setVisible(false);
        enableComponents(true);
        removeClassName("editing");
    }

    private void enableComponents(boolean enabled) {
        filterTextField.setEnabled(enabled);
        addPersonButton.setEnabled(enabled);
        if (selectedPersons.size() == 0) {
            deletePersonsButton.setEnabled(false);
        } else {
            deletePersonsButton.setEnabled(enabled);
        }
        if (!identityService.isConnected()) {
            importButton.setEnabled(false);
        } else {
            importButton.setEnabled(enabled);
        }
    }

    private boolean matchesTerm(String value, String searchTerm) {
        return value.toLowerCase().contains(searchTerm.toLowerCase());
    }
}
