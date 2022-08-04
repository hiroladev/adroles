package de.hirola.adroles.views.employees;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import de.hirola.adroles.Global;
import de.hirola.adroles.data.entity.Person;
import de.hirola.adroles.service.IdentityService;
import de.hirola.adroles.views.MainLayout;

import javax.annotation.security.PermitAll;
import java.util.List;

@Route(value="employees", layout = MainLayout.class)
@PageTitle("Employee | AD-Roles")
@PermitAll
public class EmployeeListView extends VerticalLayout {
    private final IdentityService identityService;
    private EmpoyeeForm empoyeeForm;
    private final Grid<Person> grid = new Grid<>(Person.class, false);
    private TextField filterTextField;

    public EmployeeListView(IdentityService identityService) {
        this.identityService = identityService;
        addClassName("employee-list-view");
        setSizeFull();
        addComponents();
        updateList();
        closeEmployeeForm();
    }

    private void addComponents() {

        filterTextField = new TextField();
        filterTextField.setPlaceholder(getTranslation("searchFilter"));
        filterTextField.setClearButtonVisible(true);
        filterTextField.setValueChangeMode(ValueChangeMode.LAZY);
        filterTextField.addValueChangeListener(event -> updateList());

        HorizontalLayout toolbar = new HorizontalLayout(filterTextField);
        toolbar.addClassName("toolbar");

        grid.addClassNames("employee-grid");
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
        grid.getColumns().forEach(col -> col.setAutoWidth(true));
        grid.setSelectionMode(Grid.SelectionMode.SINGLE);
        grid.addItemClickListener(event -> editEmployee(event.getItem()));

        empoyeeForm = new EmpoyeeForm();
        empoyeeForm.setWidthFull();
        empoyeeForm.addListener(EmpoyeeForm.SaveEvent.class, this::saveEmployee);
        empoyeeForm.addListener(EmpoyeeForm.CloseEvent.class, event -> closeEmployeeForm());

        FlexLayout content = new FlexLayout(grid, empoyeeForm);
        content.setFlexGrow(2, grid);
        content.setFlexGrow(1, empoyeeForm);
        content.setFlexShrink(0, empoyeeForm);
        content.addClassNames("content", "gap-m");
        content.setSizeFull();

        add(toolbar, content);
    }

    private void saveEmployee(EmpoyeeForm.SaveEvent event) {
        identityService.savePerson(event.getPerson());
        closeEmployeeForm();
        updateList();
    }

    public void editEmployee(Person person) {
        if (person == null) {
            closeEmployeeForm();
        } else {
            enableComponents(false);
            empoyeeForm.setPerson(person);
            empoyeeForm.setVisible(true);
            addClassName("editing");
        }
    }

    private void updateList() {
        List<Person> filteredPersons = identityService.findAllEmployees(filterTextField.getValue());
        grid.setItems(filteredPersons);
        grid.deselectAll();
        grid.getColumnByKey(Global.Component.FOOTER_COLUMN_KEY)
                .setFooter(String.format(getTranslation("persons.sum") + ": %s", filteredPersons.size()));
    }

    private void closeEmployeeForm() {
        empoyeeForm.setPerson(null);
        enableComponents(true);
        empoyeeForm.setVisible(false);
        removeClassName("editing");
    }

    private void enableComponents(boolean enabled) {
        filterTextField.setEnabled(enabled);
    }
}
