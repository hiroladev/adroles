package de.hirola.adroles.views.persons;

import de.hirola.adroles.data.entity.Person;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.shared.Registration;

public class PersonForm extends FormLayout {
  private Person person;
  private  final Binder<Person> binder = new BeanValidationBinder<>(Person.class);
  private final TextField firstName = new TextField(getTranslation("person.firstName"));
  private final TextField lastName = new TextField(getTranslation("person.lastName"));
  private final EmailField emailAddress = new EmailField(getTranslation("emailAddress"));
  private Button saveButton;

  public PersonForm() {
    addClassName("person-form");
    addComponents();
    binder.bindInstanceFields(this); // text fields -> fields of a person
    binder.addStatusChangeListener(event -> saveButton.setEnabled(binder.isValid()));
  }

  private void addComponents() {

    saveButton = new Button(getTranslation("save"));
    saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
    saveButton.addClickShortcut(Key.ENTER);
    saveButton.addClickListener(event -> validateAndSave());

    Button deleteButton = new Button(getTranslation("delete"));
    deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
    deleteButton.addClickListener(event -> fireEvent(new DeleteEvent(this, person)));

    Button closeButton = new Button(getTranslation("cancel"));
    closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
    closeButton.addClickShortcut(Key.ESCAPE);
    closeButton.addClickListener(event -> fireEvent(new CloseEvent(this)));

    HorizontalLayout buttonsLayout = new HorizontalLayout(saveButton, deleteButton, closeButton);

    add(firstName, lastName, emailAddress, buttonsLayout);
  }

  public void setPerson(Person person) {
    this.person = person;
    binder.readBean(person);
  }

  private void validateAndSave() {
    try {
      binder.writeBean(person);
      fireEvent(new SaveEvent(this, person));
    } catch (ValidationException exception) {
      exception.printStackTrace();
    }
  }

  // Events
  public static abstract class PersonFormEvent extends ComponentEvent<PersonForm> {
    private final Person person;

    protected PersonFormEvent(PersonForm source, Person person) {
      super(source, false);
      this.person = person;
    }

    public Person getPerson() {
      return person;
    }
  }

  public static class SaveEvent extends PersonFormEvent {
    SaveEvent(PersonForm source, Person person) {
      super(source, person);
    }
  }

  public static class DeleteEvent extends PersonFormEvent {
    DeleteEvent(PersonForm source, Person person) {
      super(source, person);
    }

  }

  public static class CloseEvent extends PersonFormEvent {
    CloseEvent(PersonForm source) {
      super(source, null);
    }
  }

  public <T extends ComponentEvent<?>> Registration addListener(Class<T> eventType,
                                                                ComponentEventListener<T> listener) {
    return getEventBus().addListener(eventType, listener);
  }
}