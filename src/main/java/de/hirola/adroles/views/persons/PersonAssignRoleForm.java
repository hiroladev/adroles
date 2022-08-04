package de.hirola.adroles.views.persons;

import com.vaadin.flow.component.*;
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
import de.hirola.adroles.data.entity.RoleResource;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

public class PersonAssignRoleForm extends VerticalLayout {
  private Person person;
  private final Set<Role> selectedRoles = new LinkedHashSet<>();
  private TextField personTexField, searchField;
  private final Grid<Role> grid = new Grid<>(Role.class, false);
  private GridListDataView<Role> dataView;

  public PersonAssignRoleForm() {
    addClassName("person-assign-role-form");
    addComponents();
  }

  private void addComponents() {

    personTexField = new TextField(getTranslation("person"));
    personTexField.setWidth(Global.Component.DEFAULT_TEXT_FIELD_WIDTH);
    personTexField.setReadOnly(true);
    add(personTexField);

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

    grid.addClassNames("role-grid");
    grid.setSizeFull();
    grid.addColumn(role -> selectedRoles.contains(role) ? getTranslation("assigned") : getTranslation("notAssigned"), "status")
            .setHeader(getTranslation("status"))
            .setKey(Global.Component.FOOTER_COLUMN_KEY)
            .setSortOrderProvider(direction -> Stream.of(new QuerySortOrder("status", direction)))
            .setComparator((role1, role2) -> {
              if ((selectedRoles.contains(role1) && selectedRoles.contains(role2)) ||
                      (!selectedRoles.contains(role1) && !selectedRoles.contains(role2)) ) {
                return 0;
              }
              if (selectedRoles.contains(role1) && !selectedRoles.contains(role2)) {
                return 1;
              }
              return -1;
            });
    grid.addColumn(Role::getName).setHeader(getTranslation("name"))
            .setSortable(true);
    grid.addColumn(Role::getDescription).setHeader(getTranslation("description"))
            .setSortable(true);
    grid.addColumn(role -> role.isAdminRole() ? getTranslation("yes") : getTranslation("no"))
            .setHeader(getTranslation("adminRole"))
            .setSortable(true);
    grid.getColumns().forEach(col -> col.setAutoWidth(true));
    grid.setSelectionMode(Grid.SelectionMode.MULTI);
    grid.addSelectionListener(selection -> {
      selectedRoles.clear();
      selectedRoles.addAll(selection.getAllSelectedItems());
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

  public void setData(Person person, List<Role> roles, RoleResource roleResource) {
    this.person = person;
    if (person != null && roles != null & roleResource != null) {
      // filter the role list by role resource
      List<Role> filteredRoles = new ArrayList<>();
      if (roleResource.isOrgResource()) {
        filteredRoles.addAll(roles.stream().filter(role -> role.getRoleResource().isOrgResource()).toList());
      } else {
        filteredRoles.addAll(roles.stream().filter(role -> !role.getRoleResource().isOrgResource()).toList());
      }
      // build person info string
      StringBuilder personInfos = new StringBuilder(person.getLastName());
      if (person.getFirstName().length() > 0) {
        personInfos.append(", ");
        personInfos.append(person.getFirstName());
      }
      if (person.getCentralAccountName().length() > 0) {
        personInfos.append(" (");
        personInfos.append(person.getCentralAccountName());
        personInfos.append(")");
      }
      personTexField.setValue(personInfos.toString());

      // you can filter the grid
      dataView = grid.setItems(filteredRoles);
      dataView.addFilter(role -> {
        String searchTerm = searchField.getValue().trim();
        if (searchTerm.isEmpty()) {
          return true;
        }
        boolean matchesName = matchesTerm(role.getName(), searchTerm);
        boolean matchesDescription = matchesTerm(role.getDescription(), searchTerm);

        return matchesName || matchesDescription;
      });

      // show first assigned persons
      dataView.setSortOrder((ValueProvider<Role, String>) role -> {
        if (role == null) {
          return "";
        }
        if (selectedRoles.contains(role)) {
          return getTranslation("assigned");
        }
        return getTranslation("notAssigned");
      }, SortDirection.DESCENDING);

      // add assigned roles to selected list - filtered by role resource
      selectedRoles.clear();
      if (roleResource.isOrgResource()) {
        selectedRoles.addAll(person.getRoles().stream().filter(role -> role.getRoleResource().isOrgResource()).toList());
      } else {
        selectedRoles.addAll(person.getRoles().stream().filter(role -> !role.getRoleResource().isOrgResource()).toList());
      }
      grid.asMultiSelect().select(selectedRoles);
      grid.getColumnByKey(Global.Component.FOOTER_COLUMN_KEY)
              .setFooter(String.format(getTranslation("roles.assigned") + ": %s", selectedRoles.size()));
    }
  }
  private void validateAndSave() {
    if (person.getRoles().isEmpty()) {
      person.setRoles(selectedRoles);
    }  else {
      person.removeAllRoles();
      for (Role role: selectedRoles) {
        person.addRole(role);
      }
    }
    fireEvent(new SaveEvent(this, person));
  }

  private boolean matchesTerm(String value, String searchTerm) {
    if (value == null ||searchTerm == null) {
      return false;
    }
    return value.toLowerCase().contains(searchTerm.toLowerCase());
  }

  // Events
  public static abstract class PersonAssignRoleFormEvent extends ComponentEvent<PersonAssignRoleForm> {
    private final Person person;

    protected PersonAssignRoleFormEvent(PersonAssignRoleForm source, Person person) {
      super(source, false);
      this.person = person;
    }

    public Person getPerson() {
      return person;
    }
  }

  public static class SaveEvent extends PersonAssignRoleFormEvent {
    SaveEvent(PersonAssignRoleForm source, Person person) {
      super(source, person);
    }
  }

  public static class CloseEvent extends PersonAssignRoleFormEvent {
    CloseEvent(PersonAssignRoleForm source) {
      super(source, source.person);
    }
  }

  public <T extends ComponentEvent<?>> Registration addListener(Class<T> eventType,
                                                                ComponentEventListener<T> listener) {
    return getEventBus().addListener(eventType, listener);
  }
}