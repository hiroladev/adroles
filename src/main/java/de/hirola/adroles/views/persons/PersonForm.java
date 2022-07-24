package de.hirola.adroles.views.persons;

import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import de.hirola.adroles.Global;
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
  private final TextField centralAccountName = new TextField(getTranslation("centralAccountName"));
  private final TextField firstName = new TextField(getTranslation("firstname"));
  private final TextField lastName = new TextField(getTranslation("lastname"));
  private final EmailField emailAddress = new EmailField(getTranslation("emailAddress"));
  private final TextField description = new TextField(getTranslation("description"));
  private Button assignRolesButton;
  private Button assignOrganizationsButton;
  private Button saveButton;

  public PersonForm() {
    addClassName("person-form");
    setResponsiveSteps(new ResponsiveStep("500px", 1));
    addComponents();
    binder.bindInstanceFields(this); // text fields -> fields of a person
    binder.addStatusChangeListener(event -> saveButton.setEnabled(binder.isValid()));
  }

  private void addComponents() {

    centralAccountName.setWidth(Global.DEFAULT_TEXT_FIELD_WIDTH, Unit.PIXELS);
    lastName.setWidth(Global.DEFAULT_TEXT_FIELD_WIDTH, Unit.PIXELS);
    firstName.setWidth(Global.DEFAULT_TEXT_FIELD_WIDTH, Unit.PIXELS);
    emailAddress.setWidth(Global.DEFAULT_TEXT_FIELD_WIDTH, Unit.PIXELS);
    description.setWidth(Global.DEFAULT_TEXT_FIELD_WIDTH, Unit.PIXELS);

    assignRolesButton = new Button(getTranslation("assignRoles"), new Icon(VaadinIcon.PLUS));
    assignRolesButton.setWidth(Global.DEFAULT_BUTTON_WIDTH, Unit.PIXELS);
    assignRolesButton.addClickListener(event -> fireEvent(new AssignRolesEvent(this, person)));

    assignOrganizationsButton = new Button(getTranslation("assignOrganisations"), new Icon(VaadinIcon.PLUS));
    assignOrganizationsButton.setWidth(Global.DEFAULT_BUTTON_WIDTH, Unit.PIXELS);
    assignOrganizationsButton.addClickListener(event -> fireEvent(new AssignOrgEvent(this, person)));

    VerticalLayout formsLayout = new VerticalLayout(centralAccountName, firstName, lastName, emailAddress, description,
            assignRolesButton, assignOrganizationsButton);
    formsLayout.setPadding(true);

    saveButton = new Button(getTranslation("save"));
    saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
    saveButton.addClickShortcut(Key.ENTER);
    saveButton.addClickListener(event -> validateAndSave());

    Button deleteButton = new Button(getTranslation("delete"));
    deleteButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);
    deleteButton.addClickListener(event -> fireEvent(new DeleteEvent(this, person)));

    Button closeButton = new Button(getTranslation("cancel"));
    closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
    closeButton.addClickShortcut(Key.ESCAPE);
    closeButton.addClickListener(event -> fireEvent(new CloseEvent(this)));

    HorizontalLayout buttonsLayout_1 = new HorizontalLayout(saveButton, deleteButton, closeButton);
    buttonsLayout_1.setPadding(true);

    add(formsLayout, buttonsLayout_1);
  }

  public void setPerson(Person person) {
    this.person = person;
    binder.readBean(person);
    if (person != null) {
      if (person.getId() != null) {
        assignRolesButton.setEnabled(true);
        assignOrganizationsButton.setEnabled(true);
      } else {
        assignRolesButton.setEnabled(false);
        assignOrganizationsButton.setEnabled(false);
      }
    } else {
      assignRolesButton.setEnabled(false);
      assignOrganizationsButton.setEnabled(false);
    }
  }

  private void validateAndSave() {
    try {
      binder.writeBean(person);
      fireEvent(new SaveEvent(this, person));
      assignRolesButton.setEnabled(true);
      assignOrganizationsButton.setEnabled(true);
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

  public static class AssignRolesEvent extends PersonFormEvent {
    AssignRolesEvent(PersonForm source, Person person) {
      super(source, person);
    }
  }

  public static class AssignOrgEvent extends PersonFormEvent {
    AssignOrgEvent(PersonForm source, Person person) {
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