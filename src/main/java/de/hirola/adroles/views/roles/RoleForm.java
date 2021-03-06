package de.hirola.adroles.views.roles;

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.formlayout.FormLayout;
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
import de.hirola.adroles.data.entity.Role;

public class RoleForm extends FormLayout {
  private Role role;
  private  final Binder<Role> binder = new BeanValidationBinder< >(Role.class);
  private final TextField name = new TextField(getTranslation("name"));
  private final TextField description = new TextField(getTranslation("description"));
  private final Checkbox isAdminRole = new Checkbox(getTranslation("adminRole"));
  private Button saveButton;

  public RoleForm() {
    addClassName("person-form");
    addComponents();
    binder.bindInstanceFields(this); // text fields -> fields of a person
    binder.addStatusChangeListener(event -> saveButton.setEnabled(binder.isValid()));
  }

  private void addComponents() {

    Button assignPersonsButton = new Button(getTranslation("assignPersons"), new Icon(VaadinIcon.PLUS));
    assignPersonsButton.setWidth(Global.DEFAULT_BUTTON_WIDTH, Unit.PIXELS);
    assignPersonsButton.addClickListener(event -> fireEvent(new AssignPersonsEvent(this, role)));

    Button assignADGroupsButton = new Button(getTranslation("assignADGroups"), new Icon(VaadinIcon.PLUS));
    assignADGroupsButton.setWidth(Global.DEFAULT_BUTTON_WIDTH, Unit.PIXELS);
    assignADGroupsButton.addClickListener(event -> fireEvent(new AssignADGroupsEvent(this, role)));

    Button assignOrganizationsButton = new Button(getTranslation("assignOrganisations"), new Icon(VaadinIcon.PLUS));
    assignOrganizationsButton.setWidth(Global.DEFAULT_BUTTON_WIDTH, Unit.PIXELS);
    assignOrganizationsButton.addClickListener(event -> fireEvent(new AddOrganizationsEvent(this, role)));

    VerticalLayout buttonsLayout_1 = new VerticalLayout(assignPersonsButton, assignADGroupsButton, assignOrganizationsButton);

    saveButton = new Button(getTranslation("save"));
    saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
    saveButton.addClickShortcut(Key.ENTER);
    saveButton.addClickListener(event -> validateAndSave());

    Button deleteButton = new Button(getTranslation("delete"));
    deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
    deleteButton.addClickListener(event -> fireEvent(new DeleteEvent(this, role)));

    Button closeButton = new Button(getTranslation("cancel"));
    closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
    closeButton.addClickShortcut(Key.ESCAPE);
    closeButton.addClickListener(event -> fireEvent(new CloseEvent(this)));

    HorizontalLayout buttonsLayout_2 = new HorizontalLayout(saveButton, deleteButton, closeButton);

    add(name, description, isAdminRole, buttonsLayout_1, buttonsLayout_2);
  }

  public void setRole(Role role) {
    this.role = role;
    binder.readBean(role);
    if (role != null) { // workaround: boolean not (correct) bound as instance field
      isAdminRole.setValue(role.isAdminRole());
    } else {
      isAdminRole.setValue(false);
    }
  }

  private void validateAndSave() {
    try {
      role.setAdminRole(isAdminRole.getValue()); // workaround: boolean not (correct) bound as instance field
      binder.writeBean(role);
      fireEvent(new SaveEvent(this, role));
    } catch (ValidationException exception) {
      exception.printStackTrace();
    }
  }

  // Events
  public static abstract class RoleFormEvent extends ComponentEvent<RoleForm> {
    private final Role role;

    protected RoleFormEvent(RoleForm source, Role role) {
      super(source, false);
      this.role = role;
    }

    public Role getRole() {
      return role;
    }
  }

  public static class SaveEvent extends RoleFormEvent {
    SaveEvent(RoleForm source, Role role) {
      super(source, role);
    }
  }
  public static class DeleteEvent extends RoleFormEvent {
    DeleteEvent(RoleForm source, Role role) {
      super(source, role);
    }

  }

  public static class AssignPersonsEvent extends RoleFormEvent {
    AssignPersonsEvent(RoleForm source, Role role) {
      super(source, role);
    }

  }

  public static class AssignADGroupsEvent extends RoleFormEvent {
    AssignADGroupsEvent(RoleForm source, Role role) {
      super(source, role);
    }

  }

  public static class AddOrganizationsEvent extends RoleFormEvent {
    AddOrganizationsEvent(RoleForm source, Role role) {
      super(source, role);
    }

  }

  public static class CloseEvent extends RoleFormEvent {
    CloseEvent(RoleForm source) {
      super(source, null);
    }
  }

  public <T extends ComponentEvent<?>> Registration addListener(Class<T> eventType,
                                                                ComponentEventListener<T> listener) {
    return getEventBus().addListener(eventType, listener);
  }
}