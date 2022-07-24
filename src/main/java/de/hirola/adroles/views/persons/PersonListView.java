package de.hirola.adroles.views.persons;

import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.textfield.TextArea;
import de.hirola.adroles.Global;
import de.hirola.adroles.data.entity.Person;
import de.hirola.adroles.data.service.IdentityService;
import de.hirola.adroles.data.service.RolesService;
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

import javax.annotation.security.PermitAll;
import java.util.ArrayList;
import java.util.List;

@Route(value="persons", layout = MainLayout.class)
@PageTitle("Persons | AD-Roles")
@PermitAll
public class PersonListView extends VerticalLayout {
    private final IdentityService identityService;
    private final RolesService rolesService;
    private final List<Person> selectedPersons = new ArrayList<>();
    private PersonForm personForm;
    private PersonAssignRoleForm assignRoleForm;
    private final Grid<Person> grid = new Grid<>(Person.class, false);
    private TextField filterTextField;
    private Button deletePersonsButton;

    public PersonListView(IdentityService identityService, RolesService rolesService) {
        this.identityService = identityService;
        this.rolesService = rolesService;
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
        filterTextField.addValueChangeListener(e -> updateList());

        Button addPersonButton = new Button(getTranslation("addPerson"));
        addPersonButton.setWidth(Global.DEFAULT_BUTTON_WIDTH, Unit.PIXELS);
        addPersonButton.setWidth(Global.DEFAULT_BUTTON_WIDTH, Unit.PIXELS);
        addPersonButton.addClickListener(click -> addPerson());

        deletePersonsButton = new Button(getTranslation("deletePersons"));
        deletePersonsButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);
        deletePersonsButton.setWidth(Global.DEFAULT_BUTTON_WIDTH, Unit.PIXELS);
        deletePersonsButton.setWidth(Global.DEFAULT_BUTTON_WIDTH, Unit.PIXELS);
        deletePersonsButton.addClickListener(click -> deletePersons());
        deletePersonsButton.setEnabled(false);

        //TODO: enable / disable import by config
        Button importButton = new Button(getTranslation("persons.importFromActiveDirectory"));
        importButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        importButton.addClickListener(click -> importPersons());

        HorizontalLayout toolbar = new HorizontalLayout(filterTextField, addPersonButton, deletePersonsButton, importButton);
        toolbar.addClassName("toolbar");

        grid.addClassNames("person-grid");
        grid.setSizeFull();
        grid.addColumn(Person::getLastName).setHeader(getTranslation("lastname"))
                .setSortable(true)
                .setFooter(String.format(getTranslation("persons.sum") + ": %s", identityService.countPersons()));
        grid.addColumn(Person::getFirstName).setHeader(getTranslation("firstname"))
                .setSortable(true);
        grid.addColumn(Person::getCentralAccountName)
                .setHeader(getTranslation("centralAccountName"))
                .setSortable(true);
        grid.addColumn(Person::getDepartment).setHeader(getTranslation("department"))
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

    private void savePerson(PersonForm.SaveEvent event) {
        identityService.savePerson(event.getPerson());
        updateList();
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

    private void deletePerson(PersonForm.DeleteEvent event) {
        identityService.deletePerson(event.getPerson());
        updateList();
        closePersonForm();
    }

    public void editPerson(Person person) {
        if (person == null) {
            closePersonForm();
        } else {
            personForm.setPerson(person);
            personForm.setVisible(true);
            addClassName("editing");
        }
    }

    private void addPerson() {
        editPerson(new Person());
    }

    private void importPersons() {
        if (identityService.countPersons() > 0) {
            // data can be override
            Dialog dialog = new Dialog();
            dialog.setHeaderTitle(getTranslation("persons.importFromActiveDirectory.dialog.title"));

            TextArea messageArea = new TextArea();
            messageArea.setWidthFull();
            messageArea.setValue(getTranslation("persons.importFromActiveDirectory.dialog.message"));

            Button okButton = new Button("Ok", clickEvent -> {
                identityService.importPersonsFromAD(false);
                updateList();
                dialog.close();
            });
            okButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);
            okButton.getStyle().set("margin-right", "auto");

            Button partiallyButton = new Button(getTranslation("persons.importFromActiveDirectory.missing"), clickEvent -> {
                identityService.importPersonsFromAD(true);
                updateList();
                dialog.close();
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
            identityService.importPersonsFromAD(false);
            updateList();
        }

    }

    private void addRoles(PersonForm.AssignRolesEvent event) {
        closePersonForm();
        assignRoleForm.setVisible(true);
        assignRoleForm.setData(event.getPerson(), rolesService.findAllRoles(null));
        addClassName("editing");
    }

    private void saveAssignedRoles(PersonAssignRoleForm.SaveEvent event) {
        identityService.savePerson(event.getPerson());
        updateList();
    }

    private void updateList() {
        grid.setItems(identityService.findAllPersons(filterTextField.getValue()));
    }

    private void closePersonForm() {
        personForm.setPerson(null);
        personForm.setVisible(false);
        removeClassName("editing");
    }

    private void closeAssignRolesForm() {
        assignRoleForm.setData(null, null);
        assignRoleForm.setVisible(false);
        removeClassName("editing");
    }

}
