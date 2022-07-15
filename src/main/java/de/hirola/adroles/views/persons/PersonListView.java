package de.hirola.adroles.views.persons;

import de.hirola.adroles.data.entity.Person;
import de.hirola.adroles.data.service.ADRolesService;
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
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.security.PermitAll;


@Component
@Scope("prototype")
@Route(value="", layout = MainLayout.class)
@PageTitle("Persons | AD-Roles")
@PermitAll
public class PersonListView extends VerticalLayout {
    Grid<Person> grid = new Grid<>(Person.class);
    TextField filterText = new TextField();
    PersonForm form;
    ADRolesService service;

    public PersonListView(ADRolesService service) {
        this.service = service;
        addClassName("list-view");
        setSizeFull();
        configureGrid();

        form = new PersonForm(service.findAllCompanies(), service.findAllStatuses());
        form.setWidth("25em");
        form.addListener(PersonForm.SaveEvent.class, this::saveContact);
        form.addListener(PersonForm.DeleteEvent.class, this::deleteContact);
        form.addListener(PersonForm.CloseEvent.class, e -> closeEditor());

        FlexLayout content = new FlexLayout(grid, form);
        content.setFlexGrow(2, grid);
        content.setFlexGrow(1, form);
        content.setFlexShrink(0, form);
        content.addClassNames("content", "gap-m");
        content.setSizeFull();

        add(getToolbar(), content);
        updateList();
        closeEditor();
        grid.asSingleSelect().addValueChangeListener(event ->
            editContact(event.getValue()));
    }

    private void configureGrid() {
        grid.addClassNames("person-grid");
        grid.setSizeFull();
        grid.setColumns("firstName", "lastName", "emailAddress");
        //grid.addColumn(person -> person.getStatus().getName()).setHeader("Status");
        //grid.addColumn(person -> person.getCompany().getName()).setHeader("Company");
        grid.getColumns().forEach(col -> col.setAutoWidth(true));
    }

    private HorizontalLayout getToolbar() {
        filterText.setPlaceholder("Filter by name...");
        filterText.setClearButtonVisible(true);
        filterText.setValueChangeMode(ValueChangeMode.LAZY);
        filterText.addValueChangeListener(e -> updateList());

        Button addPersonButton = new Button(getTranslation("listview.addPerson"));
        addPersonButton.addClickListener(click -> addContact());

        HorizontalLayout toolbar = new HorizontalLayout(filterText, addPersonButton);
        toolbar.addClassName("toolbar");
        return toolbar;
    }

    private void saveContact(PersonForm.SaveEvent event) {
        service.savePerson(event.getContact());
        updateList();
        closeEditor();
    }

    private void deleteContact(PersonForm.DeleteEvent event) {
        service.deletePerson(event.getContact());
        updateList();
        closeEditor();
    }

    public void editContact(Person contact) {
        if (contact == null) {
            closeEditor();
        } else {
            form.setContact(contact);
            form.setVisible(true);
            addClassName("editing");
        }
    }

    void addContact() {
        grid.asSingleSelect().clear();
        editContact(new Person());
    }

    private void closeEditor() {
        form.setContact(null);
        form.setVisible(false);
        removeClassName("editing");
    }

    private void updateList() {
        grid.setItems(service.findAllPersons(filterText.getValue()));
    }


}
