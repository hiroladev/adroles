package de.hirola.adroles.views.organizations;

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
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

public class OrgUnitForm extends FormLayout {
  private Role orgUnit;
  private  final Binder<Role> binder = new BeanValidationBinder<>(Role.class);
  private final TextField name = new TextField(getTranslation("name"));
  private final TextField description = new TextField(getTranslation("description"));
  private Button assignPersonsButton, assignRolesButton, saveButton;

  public OrgUnitForm() {
    addClassName("org-unit-form");
    setResponsiveSteps(new ResponsiveStep("500px", 1));
    addComponents();
    binder.bindInstanceFields(this); // text fields -> fields of an org unit
    binder.addStatusChangeListener(event -> saveButton.setEnabled(binder.isValid()));
  }

  private void addComponents() {

    name.setWidth(Global.Component.DEFAULT_TEXT_FIELD_WIDTH);
    description.setWidth(Global.Component.DEFAULT_TEXT_FIELD_WIDTH);

    assignPersonsButton = new Button(getTranslation("assignPersons"), new Icon(VaadinIcon.PLUS));
    assignPersonsButton.setWidth(Global.Component.DEFAULT_BUTTON_WIDTH);
    assignPersonsButton.addClickListener(event -> fireEvent(new AssignPersonsEvent(this, orgUnit)));

    assignRolesButton = new Button(getTranslation("assignRoles"), new Icon(VaadinIcon.PLUS));
    assignRolesButton.setWidth(Global.Component.DEFAULT_BUTTON_WIDTH);
    assignRolesButton.addClickListener(event -> fireEvent(new AssignADGroupsEvent(this, orgUnit)));

    VerticalLayout formsLayout = new VerticalLayout(name, description, assignPersonsButton, assignRolesButton);
    formsLayout.setPadding(true);

    saveButton = new Button(getTranslation("save"));
    saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
    saveButton.addClickShortcut(Key.ENTER);
    saveButton.addClickListener(event -> validateAndSave());

    Button deleteButton = new Button(getTranslation("delete"));
    deleteButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);
    deleteButton.addClickListener(event -> fireEvent(new DeleteEvent(this, orgUnit)));

    Button closeButton = new Button(getTranslation("cancel"));
    closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
    closeButton.addClickShortcut(Key.ESCAPE);
    closeButton.addClickListener(event -> fireEvent(new CloseEvent(this)));

    HorizontalLayout buttonsLayout_1 = new HorizontalLayout(saveButton, deleteButton, closeButton);
    buttonsLayout_1.setPadding(true);

    add(formsLayout, buttonsLayout_1);
  }

  public void setOrgUnit(Role orgUnit) {
    this.orgUnit = orgUnit;
    binder.readBean(this.orgUnit);
    if (this.orgUnit != null) {
      assignRolesButton.setEnabled(true);
      assignPersonsButton.setEnabled(true);
    } else {
      assignRolesButton.setEnabled(false);
      assignPersonsButton.setEnabled(false);
    }
  }

  private void validateAndSave() {
    try {
      binder.writeBean(orgUnit);
      fireEvent(new SaveEvent(this, orgUnit));
      assignRolesButton.setEnabled(true);
      assignPersonsButton.setEnabled(true);
    } catch (ValidationException exception) {
      exception.printStackTrace();
    }
  }

  // Events
  public static abstract class OrgUnitFormEvent extends ComponentEvent<OrgUnitForm> {
    private final Role orgUnit;

    protected OrgUnitFormEvent(OrgUnitForm source, Role orgUnit) {
      super(source, false);
      this.orgUnit = orgUnit;
    }

    public Role getOrgUnit() {
      return orgUnit;
    }
  }

  public static class SaveEvent extends OrgUnitFormEvent {
    SaveEvent(OrgUnitForm source, Role orgUnit) {
      super(source, orgUnit);
    }
  }

  public static class AssignADGroupsEvent extends OrgUnitFormEvent {
    AssignADGroupsEvent(OrgUnitForm source, Role orgUnit) {
      super(source, orgUnit);
    }
  }

  public static class AssignPersonsEvent extends OrgUnitFormEvent {
    AssignPersonsEvent(OrgUnitForm source, Role orgUnit) {
      super(source, orgUnit);
    }
  }

  public static class DeleteEvent extends OrgUnitFormEvent {
    DeleteEvent(OrgUnitForm source, Role orgUnit) {
      super(source, orgUnit);
    }

  }

  public static class CloseEvent extends OrgUnitFormEvent {
    CloseEvent(OrgUnitForm source) {
      super(source, null);
    }
  }

  public <T extends ComponentEvent<?>> Registration addListener(Class<T> eventType,
                                                                ComponentEventListener<T> listener) {
    return getEventBus().addListener(eventType, listener);
  }
}