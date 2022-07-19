package de.hirola.adroles.views.persons;

import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.textfield.TextArea;
import de.hirola.adroles.Global;
import de.hirola.adroles.data.entity.Person;
import de.hirola.adroles.data.service.IdentityService;
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

@Route(value="persons", layout = MainLayout.class)
@PageTitle("Persons | AD-Roles")
@PermitAll
public class PersonListView extends VerticalLayout {
    private PersonForm form;
    private final Grid<Person> grid = new Grid<>(Person.class);
    private TextField filterTextField;
    private final IdentityService service;

    public PersonListView(IdentityService service) {
        this.service = service;
        addClassName("list-view");
        setSizeFull();
        addComponents();
        updateList();
        closeForm();
    }

    private void addComponents() {

        grid.addClassNames("person-grid");
        grid.setSizeFull();
        grid.setColumns("firstName", "lastName", "emailAddress");
        //grid.addColumn(person -> person.getStatus().getName()).setHeader("Status");
        //grid.addColumn(person -> person.getCompany().getName()).setHeader("Company");
        grid.getColumns().forEach(col -> col.setAutoWidth(true));
        grid.asSingleSelect().addValueChangeListener(event -> editPerson(event.getValue()));

        form = new PersonForm();
        form.setWidth("25em");
        form.addListener(PersonForm.SaveEvent.class, this::savePerson);
        form.addListener(PersonForm.DeleteEvent.class, this::deletePerson);
        form.addListener(PersonForm.CloseEvent.class, event -> closeForm());

        FlexLayout content = new FlexLayout(grid, form);
        content.setFlexGrow(2, grid);
        content.setFlexGrow(1, form);
        content.setFlexShrink(0, form);
        content.addClassNames("content", "gap-m");
        content.setSizeFull();

        filterTextField = new TextField();
        filterTextField.setPlaceholder(getTranslation("personListView.searchFilter"));
        filterTextField.setClearButtonVisible(true);
        filterTextField.setValueChangeMode(ValueChangeMode.LAZY);
        filterTextField.addValueChangeListener(e -> updateList());

        Button addPersonButton = new Button(getTranslation("personListView.addPerson"));
        addPersonButton.setWidth(Global.DEFAULT_BUTTON_WIDTH, Unit.PIXELS);
        addPersonButton.setWidth(Global.DEFAULT_BUTTON_WIDTH, Unit.PIXELS);
        addPersonButton.addClickListener(click -> addPerson());

        //TODO: enable / disable import by config
        Button importButton = new Button(getTranslation("personListView.importFromActiveDirectory"));
        importButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        importButton.addClickListener(click -> {
            if (service.countPersons() > 0) {
                // data can be override
                Dialog dialog = new Dialog();
                dialog.setHeaderTitle(getTranslation("personListView.importFromActiveDirectory.dialog.title"));

                TextArea messageArea =
                        new TextArea(getTranslation("personListView.importFromActiveDirectory.dialog.message"));

                Button okButton = new Button("Ok", (clickEvent) -> importPersons());
                okButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);
                okButton.getStyle().set("margin-right", "auto");

                Button cancelButton = new Button(getTranslation("cancel"), (clickEvent) -> dialog.close());
                cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

                dialog.add(messageArea);
                dialog.getFooter().add(okButton);
                dialog.getFooter().add(cancelButton);

                dialog.open();
            } else {
                importPersons();
            }
        });

        HorizontalLayout toolbar = new HorizontalLayout(filterTextField, addPersonButton, importButton);
        toolbar.addClassName("toolbar");

        add(toolbar, content);
    }

    private void savePerson(PersonForm.SaveEvent event) {
        service.savePerson(event.getPerson());
        updateList();
        closeForm();
    }

    private void deletePerson(PersonForm.DeleteEvent event) {
        service.deletePerson(event.getPerson());
        updateList();
        closeForm();
    }

    public void editPerson(Person contact) {
        if (contact == null) {
            closeForm();
        } else {
            form.setPerson(contact);
            form.setVisible(true);
            addClassName("editing");
        }
    }

    private void addPerson() {
        grid.asSingleSelect().clear();
        editPerson(new Person());
    }

    private void importPersons() {
        service.importPersonsFromAD();
        updateList();
    }

    private void updateList() {
        grid.setItems(service.findAllPersons(filterTextField.getValue()));
    }

    private void closeForm() {
        form.setPerson(null);
        form.setVisible(false);
        removeClassName("editing");
    }
}
