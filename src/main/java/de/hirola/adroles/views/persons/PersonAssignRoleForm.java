package de.hirola.adroles.views.persons;

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.shared.Registration;
import de.hirola.adroles.Global;
import de.hirola.adroles.data.entity.Person;

public class PersonAssignRoleForm extends FormLayout {
  private Person person;
  private final TextField centralAccountName = new TextField("Person");
  private Button saveButton;

  public PersonAssignRoleForm() {
    addClassName("person-form");
    addComponents();
  }

  private void addComponents() {


    saveButton = new Button(getTranslation("save"));
    saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
    saveButton.addClickShortcut(Key.ENTER);
    //saveButton.addClickListener(event -> validateAndSave());

    Button deleteButton = new Button(getTranslation("delete"));
    deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
    deleteButton.addClickListener(event -> fireEvent(new DeleteEvent(this, person)));

    Button closeButton = new Button(getTranslation("cancel"));
    closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
    closeButton.addClickShortcut(Key.ESCAPE);
    closeButton.addClickListener(event -> this.setVisible(false));
    //closeButton.addClickListener(event -> fireEvent(new CloseEvent(this)));

    HorizontalLayout buttonsLayout_2 = new HorizontalLayout(saveButton, deleteButton, closeButton);

    add(centralAccountName, buttonsLayout_2);
  }

  public void setPerson(Person person) {
    this.person = person;
  }


  // Events
  public static abstract class PersonFormEvent extends ComponentEvent<PersonAssignRoleForm> {
    private final Person person;

    protected PersonFormEvent(PersonAssignRoleForm source, Person person) {
      super(source, false);
      this.person = person;
    }

    public Person getPerson() {
      return person;
    }
  }

  public static class SaveEvent extends PersonFormEvent {
    SaveEvent(PersonAssignRoleForm source, Person person) {
      super(source, person);
    }
  }

  public static class AssignRolesEvent extends PersonFormEvent {
    AssignRolesEvent(PersonAssignRoleForm source, Person person) {
      super(source, person);
    }
  }

  public static class AssignOrgEvent extends PersonFormEvent {
    AssignOrgEvent(PersonAssignRoleForm source, Person person) {
      super(source, person);
    }
  }

  public static class DeleteEvent extends PersonFormEvent {
    DeleteEvent(PersonAssignRoleForm source, Person person) {
      super(source, person);
    }

  }

  public static class CloseEvent extends PersonFormEvent {
    CloseEvent(PersonAssignRoleForm source) {
      super(source, null);
    }
  }

  public <T extends ComponentEvent<?>> Registration addListener(Class<T> eventType,
                                                                ComponentEventListener<T> listener) {
    return getEventBus().addListener(eventType, listener);
  }
}