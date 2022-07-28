package de.hirola.adroles.views.organizations;

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.QuerySortOrder;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.function.ValueProvider;
import com.vaadin.flow.shared.Registration;
import de.hirola.adroles.Global;
import de.hirola.adroles.data.entity.Person;
import de.hirola.adroles.data.entity.Role;
import de.hirola.adroles.service.IdentityService;

import java.util.*;
import java.util.stream.Stream;

public class OrgUnitAssignPersonForm extends VerticalLayout {
  private final IdentityService identityService;
  private Role orgUnit;
  private final Set<Person> selectedPersons = new LinkedHashSet<>();
  private TextField orgUnitTexField, searchField;
  private Button assignFromPersonsButton;
  private final Grid<Person> grid = new Grid<>(Person.class, false);

  private GridListDataView<Person> dataView;
  public OrgUnitAssignPersonForm(IdentityService identityService) {
    this.identityService = identityService;
    addClassName("org-assign-person-form");
    addComponents();
  }

  private void addComponents() {

    orgUnitTexField = new TextField(getTranslation("orgUnit"));
    orgUnitTexField.setWidth(Global.Component.DEFAULT_TEXT_FIELD_WIDTH);
    orgUnitTexField.setReadOnly(true);
    add(orgUnitTexField);

    searchField = new TextField();
    searchField.setWidth(Global.Component.DEFAULT_TEXT_FIELD_WIDTH);
    searchField.setPlaceholder(getTranslation("search"));
    searchField.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
    searchField.setValueChangeMode(ValueChangeMode.EAGER);
    searchField.addValueChangeListener(event -> {
      if (dataView != null) {
        dataView.refreshAll();
      }
    });
    add(searchField);

    assignFromPersonsButton = new Button(getTranslation("orgUnits.assignFromPersons"), new Icon(VaadinIcon.DOWNLOAD));
    assignFromPersonsButton.addThemeVariants(ButtonVariant.LUMO_ICON);
    assignFromPersonsButton.setWidth(Global.Component.DEFAULT_BUTTON_WIDTH);
    assignFromPersonsButton.addClickListener(click -> assignFromPersons());
    add(assignFromPersonsButton);

    grid.setSizeFull();
    grid.addClassNames("person-grid");
    grid.addColumn(person -> selectedPersons.contains(person) ? getTranslation("assigned") : getTranslation("notAssigned"), "status")
            .setHeader(getTranslation("status"))
            .setKey(Global.Component.FOOTER_COLUMN_KEY)
            .setSortOrderProvider(direction -> Stream.of(new QuerySortOrder("status", direction)))
            .setComparator((person1, person2) -> {
              if ((selectedPersons.contains(person1) && selectedPersons.contains(person2)) ||
                      (!selectedPersons.contains(person1) && !selectedPersons.contains(person2)) ) {
                return 0;
              }
              if (selectedPersons.contains(person1) && !selectedPersons.contains(person2)) {
                return 1;
              }
              return -1;
            });
    grid.addColumn(Person::getLastName).setHeader(getTranslation("lastname"))
            .setSortable(true);
    grid.addColumn(Person::getFirstName).setHeader(getTranslation("firstname"))
            .setSortable(true);
    grid.addColumn(Person::getCentralAccountName)
            .setHeader(getTranslation("centralAccountName"))
            .setSortable(true);
    grid.addColumn(Person::getDepartmentName).setHeader(getTranslation("department"))
            .setSortable(true);
    grid.getColumns().forEach(col -> col.setAutoWidth(true));
    grid.setSelectionMode(Grid.SelectionMode.MULTI);
    grid.addSelectionListener(selection -> {
      selectedPersons.clear();
      selectedPersons.addAll(selection.getAllSelectedItems());
    });

    add(grid);

    Button saveButton = new Button(getTranslation("save"));
    saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
    saveButton.addClickShortcut(Key.ENTER);
    saveButton.addClickListener(event -> validateAndSave());

    Button closeButton = new Button(getTranslation("cancel"));
    closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
    closeButton.addClickShortcut(Key.ESCAPE);
    closeButton.addClickListener(event -> this.setVisible(false));
    closeButton.addClickListener(event -> fireEvent(new CloseEvent(this)));

    HorizontalLayout buttonsLayout = new HorizontalLayout(saveButton, closeButton);
    buttonsLayout.setPadding(true);

    add(buttonsLayout);
  }

  public void setData(Role orgUnit, List<Person> persons) {
    this.orgUnit = orgUnit;
    if (orgUnit != null && persons != null) {
      // build org unit info string
      StringBuilder orgUnitInfos = new StringBuilder(orgUnit.getName());
      if (orgUnit.getDescription().length() > 0) {
        orgUnitInfos.append(" (");
        orgUnitInfos.append(orgUnit.getDescription());
        orgUnitInfos.append(")");
      }
      orgUnitTexField.setValue(orgUnitInfos.toString());

      // disable the assign button, if no persons available
      assignFromPersonsButton.setEnabled(persons.size() > 0);

      // you can filter the grid
      dataView = grid.setItems(persons);
      dataView.addFilter(person -> {
        String searchTerm = searchField.getValue().trim();
        if (searchTerm.isEmpty()) {
          return true;
        }
        boolean matchesLastName = matchesTerm(person.getLastName(), searchTerm);
        boolean matchesFirstName = matchesTerm(person.getFirstName(), searchTerm);
        boolean matchesCentralAccountName = matchesTerm(person.getCentralAccountName(), searchTerm);
        boolean matchesDepartmentName = matchesTerm(person.getDepartmentName(), searchTerm);

        return matchesLastName || matchesFirstName || matchesCentralAccountName || matchesDepartmentName;
      });

      // show first assigned persons
      dataView.setSortOrder(new ValueProvider<Person, String>() {
        @Override
        public String apply(Person person) {
          if (person == null) {
            return "";
          }
          if (selectedPersons.contains(person)) {
            return getTranslation("assigned");
          }
          return getTranslation("notAssigned");
        }
      }, SortDirection.DESCENDING);

      // add assigned roles to selected list
      selectedPersons.clear();
      selectedPersons.addAll(orgUnit.getPersons());
      grid.asMultiSelect().select(selectedPersons);
      grid.getColumnByKey(Global.Component.FOOTER_COLUMN_KEY)
              .setFooter(String.format(getTranslation("persons.assigned") + ": %s", selectedPersons.size()));
    }
  }

  private boolean matchesTerm(String value, String searchTerm) {
    return value.toLowerCase().contains(searchTerm.toLowerCase());
  }

  private void assignFromPersons() {
    selectedPersons.addAll(identityService.findAllPersonsWithDepartmentName(orgUnit.getName()));
    grid.getColumnByKey(Global.Component.FOOTER_COLUMN_KEY)
            .setFooter(String.format(getTranslation("persons.assigned") + ": %s", selectedPersons.size()));
    grid.asMultiSelect().select(selectedPersons);
  }
  private void validateAndSave() {
    if (orgUnit.getPersons().isEmpty()) {
      orgUnit.setPersons(selectedPersons);
    }  else {
      orgUnit.removeAllPersons();
      orgUnit.setPersons(selectedPersons);
    }
    fireEvent(new SaveEvent(this, orgUnit));
  }

  // Events
  public static abstract class OrgAssignPersonFormEvent extends ComponentEvent<OrgUnitAssignPersonForm> {
    private final Role orgUnit;

    protected OrgAssignPersonFormEvent(OrgUnitAssignPersonForm source, Role orgUnit) {
      super(source, false);
      this.orgUnit = orgUnit;
    }

    public Role getOrgUnit() {
      return orgUnit;
    }
  }

  public static class SaveEvent extends OrgAssignPersonFormEvent {
    SaveEvent(OrgUnitAssignPersonForm source, Role orgUnit) {
      super(source, orgUnit);
    }
  }

  public static class CloseEvent extends OrgAssignPersonFormEvent {
    CloseEvent(OrgUnitAssignPersonForm source) {
      super(source, null);
    }
  }

  public <T extends ComponentEvent<?>> Registration addListener(Class<T> eventType,
                                                                ComponentEventListener<T> listener) {
    return getEventBus().addListener(eventType, listener);
  }
}