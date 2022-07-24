package de.hirola.adroles.views.persons;

import com.vaadin.flow.component.*;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.shared.Registration;
import de.hirola.adroles.Global;
import de.hirola.adroles.data.entity.Person;
import de.hirola.adroles.data.entity.Role;
import de.hirola.adroles.views.roles.RoleForm;

import java.util.ArrayList;
import java.util.List;

public class PersonAssignRoleForm extends VerticalLayout {
  private Person person;
  private final List<Role> selectedRoles = new ArrayList<>();
  private TextField personTexField;
  private final Grid<Role> grid = new Grid<>(Role.class, false);

  public PersonAssignRoleForm() {
    addClassName("person-assign-role-form");
    addComponents();
  }

  private void addComponents() {

    personTexField = new TextField(getTranslation("person"));
    personTexField.setWidth(Global.DEFAULT_TEXT_FIELD_WIDTH, Unit.PIXELS);
    personTexField.setReadOnly(true);
    add(personTexField);

    grid.setSizeFull();
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

  public void setData(Person person, List<Role> roles) {
    this.person = person;
    if (person != null && roles != null) {
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
      // set all available roles
      grid.setItems(roles);
      // add assigned roles to selected list
      selectedRoles.clear();
      selectedRoles.addAll(person.getRoles());
      for (Role role: selectedRoles) {
        grid.select(role);
      }
    }
  }
  private void validateAndSave() {
    if (person.getRoles().isEmpty()) {
      person.setRoles(selectedRoles);
    }  else {
      for (Role role: selectedRoles) {
        person.addRole(role);
      }
    }
    fireEvent(new SaveEvent(this, person));
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
      super(source, null);
    }
  }

  public <T extends ComponentEvent<?>> Registration addListener(Class<T> eventType,
                                                                ComponentEventListener<T> listener) {
    return getEventBus().addListener(eventType, listener);
  }
}