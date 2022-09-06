/*
 * Copyright 2022 by Michael Schmidt, Hirola Consulting
 * This software us licensed under the AGPL-3.0 or later.
 *
 * @author Michael Schmidt (Hirola)
 * @since v0.1
 */

package de.hirola.adroles.views.roles;

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.shared.Registration;
import de.hirola.adroles.Global;
import de.hirola.adroles.data.entity.*;

import java.util.Hashtable;

public class RoleForm extends VerticalLayout {
  private Role role;
  private final Hashtable<String, RoleResource> roleResourceList;
  private  final Binder<Role> binder = new BeanValidationBinder<>(Role.class);
  private final TextField name = new TextField(getTranslation("name"));
  private final TextField description = new TextField(getTranslation("description"));
  private final Grid<Person> personGrid = new Grid<>(Person.class, false);

  private final Grid<ADUser> adUserGrid = new Grid<>(ADUser.class, false);
  private final Grid<ADGroup> adGroupGrid = new Grid<>(ADGroup.class, false);
  private Button assignPersonsButton, assignADUsersButton, assignADGroupsButton, saveButton;

  public RoleForm(Hashtable<String, RoleResource> roleResourceList) {
    this.roleResourceList = roleResourceList;
    addClassName("resource-role-form");
    addComponents();
    binder.bindInstanceFields(this); // text fields -> fields of an org unit
    binder.addStatusChangeListener(event -> saveButton.setEnabled(binder.isValid()));
  }

  private void addComponents() {

    name.setWidth(Global.Component.DEFAULT_TEXT_FIELD_WIDTH);
    add(name);

    description.setWidth(Global.Component.DEFAULT_TEXT_FIELD_WIDTH);
    add(description);

    add(new H3(getTranslation("persons.assigned")));

    personGrid.addClassNames("person-grid");
    personGrid.setSizeFull();
    personGrid.setAllRowsVisible(true);
    personGrid.addColumn(Person::getLastName).setHeader(getTranslation("lastname"))
            .setSortable(true);
    personGrid.addColumn(Person::getFirstName).setHeader(getTranslation("firstname"))
            .setSortable(true);
    personGrid.addColumn(Person::getDepartmentName).setHeader(getTranslation("department"))
            .setSortable(true);
    personGrid.getColumns().forEach(col -> col.setAutoWidth(true));
    personGrid.setSelectionMode(Grid.SelectionMode.NONE);
    add(personGrid);

    add(new H3(getTranslation("adUsers.assigned")));

    adUserGrid.addClassNames("ad-user-grid");
    adUserGrid.setSizeFull();
    adUserGrid.setAllRowsVisible(true);
    adUserGrid.addColumn(ADUser::getLogonName).setHeader(getTranslation("logonName"))
            .setSortable(true);
    adUserGrid.addColumn(ADUser::getDistinguishedName).setHeader(getTranslation("distinguishedName"))
            .setSortable(true);
    adUserGrid.addColumn(adUser -> adUser.isAdminAccount() ? getTranslation("yes") : getTranslation("no"))
            .setHeader(getTranslation("adminAccount"))
            .setSortable(true);
    adUserGrid.getColumns().forEach(col -> col.setAutoWidth(true));
    adUserGrid.setSelectionMode(Grid.SelectionMode.NONE);
    add(adUserGrid);

    add(new H3(getTranslation("adGroups.assigned")));

    adGroupGrid.addClassNames("ad-group-grid");
    adGroupGrid.setSizeFull();
    adGroupGrid.setAllRowsVisible(true);
    adGroupGrid.addColumn(ADGroup::getName).setHeader(getTranslation("name"))
            .setSortable(true);
    adGroupGrid.addColumn(ADGroup::getDescription).setHeader(getTranslation("description"))
            .setSortable(true);
    adGroupGrid.addColumn(adGroup -> adGroup.isAdminGroup() ? getTranslation("yes") : getTranslation("no"))
            .setHeader(getTranslation("adminGroup"))
            .setSortable(true);
    adGroupGrid.getColumns().forEach(col -> col.setAutoWidth(true));
    adGroupGrid.setSelectionMode(Grid.SelectionMode.NONE);
    add(adGroupGrid);

    assignPersonsButton = new Button(getTranslation("assignPersons"), new Icon(VaadinIcon.PLUS));
    assignPersonsButton.setWidth(Global.Component.DEFAULT_BUTTON_WIDTH);
    assignPersonsButton.addClickListener(event -> fireEvent(new AssignPersonsEvent(this, role)));

    assignADUsersButton = new Button(getTranslation("assignADUsers"), new Icon(VaadinIcon.PLUS));
    assignADUsersButton.setWidth(Global.Component.DEFAULT_BUTTON_WIDTH);
    assignADUsersButton.addClickListener(event -> fireEvent(new AssignADUsersEvent(this, role)));

    assignADGroupsButton = new Button(getTranslation("assignADGroups"), new Icon(VaadinIcon.PLUS));
    assignADGroupsButton.setWidth(Global.Component.DEFAULT_BUTTON_WIDTH);
    assignADGroupsButton.addClickListener(event -> fireEvent(new AssignADGroupsEvent(this, role)));

    HorizontalLayout assignButtonsLayout = new HorizontalLayout(assignPersonsButton, assignADUsersButton,
            assignADGroupsButton);
    assignButtonsLayout.setPadding(true);
    add(assignButtonsLayout);

    saveButton = new Button(getTranslation("save"));
    saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
    saveButton.addClickShortcut(Key.ENTER);
    saveButton.addClickListener(event -> validateAndSave());

    Button closeButton = new Button(getTranslation("cancel"));
    closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
    closeButton.addClickShortcut(Key.ESCAPE);
    closeButton.addClickListener(event -> fireEvent(new CloseEvent(this)));

    HorizontalLayout buttonsLayout = new HorizontalLayout(saveButton, closeButton);
    buttonsLayout.setPadding(true);

    add(buttonsLayout);
  }

  public void setRole(Role role) {
    this.role = role;
    binder.readBean(this.role);
    if (this.role != null) {
      assignPersonsButton.setEnabled(true);
      assignADUsersButton.setEnabled(true);
      assignADGroupsButton.setEnabled(true);
      updateList();
    } else {
      assignPersonsButton.setEnabled(false);
      assignADUsersButton.setEnabled(false);
      assignADGroupsButton.setEnabled(false);
    }
  }

  private void updateList() {
    if (role != null) {
      personGrid.setItems(role.getPersons());
      adUserGrid.setItems(role.getADUsers());
      adGroupGrid.setItems(role.getADGroups());
    }
  }

  private void validateAndSave() {
    try {
      binder.writeBean(role);
      fireEvent(new SaveEvent(this, role));
      assignADGroupsButton.setEnabled(true);
      assignPersonsButton.setEnabled(true);
      updateList();
    } catch (ValidationException exception) {
      exception.printStackTrace();
    }
  }

  // Events
  public static abstract class ResourceRoleFormEvent extends ComponentEvent<RoleForm> {
    private final Role role;

    protected ResourceRoleFormEvent(RoleForm source, Role role) {
      super(source, false);
      this.role = role;
    }

    public Role getRole() {
      return role;
    }
  }

  public static class SaveEvent extends ResourceRoleFormEvent {
    SaveEvent(RoleForm source, Role role) {
      super(source, role);
    }
  }

  public static class AssignPersonsEvent extends ResourceRoleFormEvent {
    AssignPersonsEvent(RoleForm source, Role role) {
      super(source, role);
    }
  }

  public static class AssignADUsersEvent extends ResourceRoleFormEvent {
    AssignADUsersEvent(RoleForm source, Role role) {
      super(source, role);
    }
  }

  public static class AssignADGroupsEvent extends ResourceRoleFormEvent {
    AssignADGroupsEvent(RoleForm source, Role role) {
      super(source, role);
    }
  }

  public static class CloseEvent extends ResourceRoleFormEvent {
    CloseEvent(RoleForm source) {
      super(source, null);
    }
  }

  public <T extends ComponentEvent<?>> Registration addListener(Class<T> eventType,
                                                                ComponentEventListener<T> listener) {
    return getEventBus().addListener(eventType, listener);
  }
}