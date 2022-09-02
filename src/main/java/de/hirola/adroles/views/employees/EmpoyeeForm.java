package de.hirola.adroles.views.employees;

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.provider.QuerySortOrder;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.shared.Registration;
import de.hirola.adroles.Global;
import de.hirola.adroles.data.entity.ADUser;
import de.hirola.adroles.data.entity.Person;
import de.hirola.adroles.data.entity.Role;
import de.hirola.adroles.data.entity.RoleResource;

import java.util.stream.Stream;

public class EmpoyeeForm extends VerticalLayout {
  private Person person;
  private  final Binder<Person> binder = new BeanValidationBinder<>(Person.class);
  private final TextField centralAccountName = new TextField(getTranslation("centralAccountName"));
  private final TextField firstName = new TextField(getTranslation("firstname"));
  private final TextField lastName = new TextField(getTranslation("lastname"));
  private final EmailField emailAddress = new EmailField(getTranslation("emailAddress"));
  private final TextField departmentName = new TextField(getTranslation("department"));
  private final TextField phoneNumber = new TextField(getTranslation("phoneNumber"));
  private final TextField mobilePhoneNumber = new TextField(getTranslation("mobilePhoneNumber"));
  private final TextField description = new TextField(getTranslation("description"));
  private final DatePicker entryDate = new DatePicker(getTranslation("entryDate"));
  private final DatePicker exitDate = new DatePicker(getTranslation("exitDate"));
  private final Grid<ADUser> adUserGrid = new Grid<>(ADUser.class, false);
  private final Grid<de.hirola.adroles.data.entity.Role> rolesGrid = new Grid<>(Role.class, false);
  private Button assignRolesButton, assignOrganizationsButton, saveButton;

  public EmpoyeeForm() {
    addClassName("person-form");
    addComponents();
    binder.bindInstanceFields(this); // text fields -> fields of a person
    binder.addStatusChangeListener(event -> saveButton.setEnabled(binder.isValid()));
  }

  private void addComponents() {

    centralAccountName.setWidth(Global.Component.DEFAULT_TEXT_FIELD_WIDTH);
    add(centralAccountName);

    lastName.setWidth(Global.Component.DEFAULT_TEXT_FIELD_WIDTH);
    add(lastName);

    firstName.setWidth(Global.Component.DEFAULT_TEXT_FIELD_WIDTH);
    add(firstName);

    emailAddress.setWidth(Global.Component.DEFAULT_TEXT_FIELD_WIDTH);
    add(emailAddress);

    departmentName.setWidth(Global.Component.DEFAULT_TEXT_FIELD_WIDTH);
    add(departmentName);

    phoneNumber.setWidth(Global.Component.DEFAULT_TEXT_FIELD_WIDTH);
    phoneNumber.setPattern("^[+]?[(]?[0-9]{3}[)]?[-s.]?[0-9]{3}[-s.]?[0-9]{4,6}$");
    phoneNumber.setHelperText(getTranslation("phoneHelperText"));
    add(phoneNumber);

    mobilePhoneNumber.setWidth(Global.Component.DEFAULT_TEXT_FIELD_WIDTH);
    mobilePhoneNumber.setPattern("^[+]?[(]?[0-9]{3}[)]?[-s.]?[0-9]{3}[-s.]?[0-9]{4,6}$");
    mobilePhoneNumber.setHelperText(getTranslation("mobilePhoneHelperText"));
    add(mobilePhoneNumber);

    description.setWidth(Global.Component.DEFAULT_TEXT_FIELD_WIDTH);
    add(description);

    DatePicker.DatePickerI18n datePickerI18n = new DatePicker.DatePickerI18n();
    datePickerI18n.setDateFormat(getTranslation("dateFormat"));

    entryDate.setI18n(datePickerI18n);
    entryDate.setPlaceholder(getTranslation("datePlaceHolder"));
    entryDate.setHelperText(getTranslation("dateHelperText"));
    add(entryDate);

    exitDate.setI18n(datePickerI18n);
    exitDate.setPlaceholder(getTranslation("datePlaceHolder"));
    exitDate.setHelperText(getTranslation("dateHelperText"));
    add(exitDate);

    TextField adUserGridLabel = new TextField();
    adUserGridLabel.setWidth(Global.Component.DEFAULT_TEXT_FIELD_WIDTH);
    adUserGridLabel.setReadOnly(true);
    adUserGridLabel.setValue(getTranslation("adUsers.assigned"));
    add(adUserGridLabel);

    adUserGrid.addClassNames("ad-user-grid");
    adUserGrid.setSizeFull();
    adUserGrid.setAllRowsVisible(true);
    adUserGrid.addColumn(ADUser::getLogonName).setHeader(getTranslation("logonName"))
            .setSortable(true);
    adUserGrid.addColumn(adUser -> adUser.isAdminAccount() ? getTranslation("yes") : getTranslation("no"))
            .setHeader(getTranslation("adminAccount"))
            .setSortable(true);
    adUserGrid.addColumn(adUser -> adUser.isRoleManaged() ? getTranslation("yes") : getTranslation("no"))
            .setHeader(getTranslation("isRoleManaged"))
            .setSortable(true);
    adUserGrid.getColumns().forEach(col -> col.setAutoWidth(true));
    adUserGrid.setSelectionMode(Grid.SelectionMode.NONE);
    add(adUserGrid);

    TextField roleGridLabel = new TextField();
    roleGridLabel.setWidth(Global.Component.DEFAULT_TEXT_FIELD_WIDTH);
    roleGridLabel.setReadOnly(true);
    roleGridLabel.setValue(getTranslation("roles.assigned"));
    add(roleGridLabel);

    rolesGrid.addClassNames("roles-grid");
    rolesGrid.setSizeFull();
    rolesGrid.setAllRowsVisible(true);
    rolesGrid.addColumn(new ComponentRenderer<>(role -> {
              RoleResource roleResource = role.getRoleResource();
              if (roleResource != null) {
                if (roleResource.isOrgResource()) {
                  return VaadinIcon.OFFICE.create();
                } else if (roleResource.isProjectResource()) {
                  return VaadinIcon.CALENDAR_BRIEFCASE.create();
                } else if (roleResource.isEmailResource()) {
                  return VaadinIcon.MAILBOX.create();
                } else if (roleResource.isFileShareResource()) {
                  return VaadinIcon.FOLDER.create();
                } else {
                  return VaadinIcon.CONNECT.create();
                }
              }
              return VaadinIcon.CUBE.create();
            }), "roleResource")
            .setHeader(getTranslation("roleResource"))
            .setWidth(Global.Component.IMAGE_COLUMN_WIDTH)
            .setSortable(true)
            .setSortOrderProvider(direction -> Stream.of(new QuerySortOrder("roleResource", direction)))
            .setComparator((role1, role2) -> {
              RoleResource roleResource1 = role1.getRoleResource();
              RoleResource roleResource2 = role2.getRoleResource();
              return roleResource1.compareTo(roleResource2);
            });
    rolesGrid.addColumn(Role::getName).setHeader(getTranslation("name"))
            .setSortable(true)
            .setKey(Global.Component.FOOTER_COLUMN_KEY);
    rolesGrid.addColumn(Role::getDescription).setHeader(getTranslation("description"))
            .setWidth(Global.Component.DEFAULT_COLUMN_WIDTH)
            .setSortable(true);
    rolesGrid.addColumn(role -> role.isAdminRole() ? getTranslation("yes") : getTranslation("no"))
            .setHeader(getTranslation("adminRole"))
            .setSortable(true);
    rolesGrid.getColumns().forEach(col -> {
      if (col.getWidth() == null) {
        col.setAutoWidth(true);
      }
    });
    rolesGrid.setSelectionMode(Grid.SelectionMode.NONE);
    add(rolesGrid);

    assignRolesButton = new Button(getTranslation("assignRoles"), new Icon(VaadinIcon.PLUS));
    assignRolesButton.setWidth(Global.Component.DEFAULT_BUTTON_WIDTH);
    assignRolesButton.addClickListener(event -> fireEvent(new AssignRolesEvent(this, person)));

    assignOrganizationsButton = new Button(getTranslation("assignOrg"), new Icon(VaadinIcon.PLUS));
    assignOrganizationsButton.setWidth(Global.Component.DEFAULT_BUTTON_WIDTH);
    assignOrganizationsButton.addClickListener(event -> fireEvent(new AssignOrgEvent(this, person)));

    HorizontalLayout assignButtonsLayout = new HorizontalLayout(assignRolesButton, assignOrganizationsButton);
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

  public void setPerson(Person person) {
    this.person = person;
    binder.readBean(person);
    updateList();
    if (person != null) {
      entryDate.setValue(person.getEntryDate());
      exitDate.setValue(person.getExitDate());
    }
  }

  private void updateList() {
      if (person != null) {
        adUserGrid.setItems(person.getADUsers().stream().sorted().toList());
        rolesGrid.setItems(person.getRoles().stream().sorted().toList());
      }
  }

  private void validateAndSave() {
    try {
      person.setEntryDate(entryDate.getValue());
      person.setExitDate(exitDate.getValue());
      binder.writeBean(person);
      fireEvent(new SaveEvent(this, person));
      assignRolesButton.setEnabled(true);
      assignOrganizationsButton.setEnabled(true);
    } catch (ValidationException exception) {
      exception.printStackTrace();
    }
  }

  // Events
  public static abstract class EmployeeFormEvent extends ComponentEvent<EmpoyeeForm> {
    private final Person person;

    protected EmployeeFormEvent(EmpoyeeForm source, Person person) {
      super(source, false);
      this.person = person;
    }

    public Person getPerson() {
      return person;
    }
  }

  public static class SaveEvent extends EmployeeFormEvent {
    SaveEvent(EmpoyeeForm source, Person person) {
      super(source, person);
    }
  }

  public static class AssignRolesEvent extends EmployeeFormEvent {
    AssignRolesEvent(EmpoyeeForm source, Person person) {
      super(source, person);
    }
  }

  public static class AssignOrgEvent extends EmployeeFormEvent {
    AssignOrgEvent(EmpoyeeForm source, Person person) {
      super(source, person);
    }
  }

  public static class DeleteEvent extends EmployeeFormEvent {
    DeleteEvent(EmpoyeeForm source, Person person) {
      super(source, person);
    }

  }

  public static class CloseEvent extends EmployeeFormEvent {
    CloseEvent(EmpoyeeForm source) {
      super(source, null);
    }
  }

  public <T extends ComponentEvent<?>> Registration addListener(Class<T> eventType,
                                                                ComponentEventListener<T> listener) {
    return getEventBus().addListener(eventType, listener);
  }
}