package de.hirola.adroles.views.adusers;

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.shared.Registration;
import de.hirola.adroles.Global;
import de.hirola.adroles.data.entity.ADUser;

public class ADUserForm extends FormLayout {
  private ADUser adUser;
  private  final Binder<ADUser> binder = new BeanValidationBinder<>(ADUser.class);
  private final TextField logonName = new TextField(getTranslation("logonName"));
  private final TextField distinguishedName = new TextField(getTranslation("distinguishedName"));
  private final Checkbox isRoleManaged = new Checkbox(getTranslation("isRoleManaged"));
  private final Checkbox isAdminAccount = new Checkbox(getTranslation("adminAccount"));
  private final Checkbox isServiceAccount = new Checkbox(getTranslation("serviceAccount"));
  private Button saveButton;

  public ADUserForm() {
    addClassName("ad-user-form");
    setResponsiveSteps(new ResponsiveStep("500px", 1));
    addComponents();
    binder.bindInstanceFields(this); // text fields -> fields of a person
    binder.addStatusChangeListener(event -> saveButton.setEnabled(binder.isValid()));
  }

  private void addComponents() {

    logonName.setWidth(Global.Component.DEFAULT_TEXT_FIELD_WIDTH);
    distinguishedName.setWidth(Global.Component.DEFAULT_TEXT_FIELD_WIDTH);

    VerticalLayout formsLayout = new VerticalLayout(logonName, distinguishedName,
            isRoleManaged, isAdminAccount, isServiceAccount);
    formsLayout.setPadding(true);

    saveButton = new Button(getTranslation("save"));
    saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
    saveButton.addClickShortcut(Key.ENTER);
    saveButton.addClickListener(event -> validateAndSave());

    Button deleteButton = new Button(getTranslation("delete"));
    deleteButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);
    deleteButton.addClickListener(event -> fireEvent(new DeleteEvent(this, adUser)));

    Button closeButton = new Button(getTranslation("cancel"));
    closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
    closeButton.addClickShortcut(Key.ESCAPE);
    closeButton.addClickListener(event -> fireEvent(new CloseEvent(this)));

    HorizontalLayout buttonsLayout_1 = new HorizontalLayout(saveButton, deleteButton, closeButton);
    buttonsLayout_1.setPadding(true);

    add(formsLayout, buttonsLayout_1);
  }

  public void setAdUser(ADUser adUser) {
    this.adUser = adUser;
    binder.readBean(adUser);
    if (adUser != null) { // workaround: boolean not (correct) bound as instance field
      isRoleManaged.setValue(adUser.isRoleManaged());
      isAdminAccount.setValue(adUser.isAdminAccount());
      isServiceAccount.setValue(adUser.isServiceAccount());
    } else {
      isRoleManaged.setValue(false);
      isAdminAccount.setValue(false);
      isServiceAccount.setValue(false);
    }
  }

  private void validateAndSave() {
    try {
      // boolean not (correct) bound as instance field
      adUser.setRoleManaged(isRoleManaged.getValue());
      adUser.setAdminAccount(isAdminAccount.getValue());
      adUser.setServiceAccount(isServiceAccount.getValue());
      binder.writeBean(adUser);
      fireEvent(new SaveEvent(this, adUser));
    } catch (ValidationException exception) {
      exception.printStackTrace();
    }
  }

  // Events
  public static abstract class ADUserFormEvent extends ComponentEvent<ADUserForm> {
    private final ADUser adUser;

    protected ADUserFormEvent(ADUserForm source, ADUser adUser) {
      super(source, false);
      this.adUser = adUser;
    }

    public ADUser getAdUser() {
      return adUser;
    }
  }

  public static class SaveEvent extends ADUserFormEvent {
    SaveEvent(ADUserForm source, ADUser adUser) {
      super(source, adUser);
    }
  }

  public static class DeleteEvent extends ADUserFormEvent {
    DeleteEvent(ADUserForm source,  ADUser adUser) {
      super(source, adUser);
    }

  }

  public static class CloseEvent extends ADUserFormEvent {
    CloseEvent(ADUserForm source) {
      super(source, null);
    }
  }

  public <T extends ComponentEvent<?>> Registration addListener(Class<T> eventType,
                                                                ComponentEventListener<T> listener) {
    return getEventBus().addListener(eventType, listener);
  }
}