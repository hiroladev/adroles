package de.hirola.adroles.views.persons;

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Key;
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
import de.hirola.adroles.data.entity.ADUser;
import de.hirola.adroles.data.entity.Person;
import de.hirola.adroles.service.IdentityService;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

public class PersonAssignADUserForm extends VerticalLayout {
  private Person person;
  private final Set<ADUser> selectedADUsers = new LinkedHashSet<>();
  private TextField personTextField, searchField;
  private final Grid<ADUser> grid = new Grid<>(ADUser.class, false);
  private GridListDataView<ADUser> dataView;
  
  public PersonAssignADUserForm(IdentityService identityService) {
    addClassName("person-assign-ad-users-form");
    addComponents();
  }

  private void addComponents() {

    personTextField = new TextField(getTranslation("person"));
    personTextField.setWidth(Global.Component.DEFAULT_TEXT_FIELD_WIDTH);
    personTextField.setReadOnly(true);
    add(personTextField);

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

    grid.addClassNames("ad-user-grid");
    grid.setSizeFull();
    grid.setAllRowsVisible(true);
    grid.addColumn(adUser -> selectedADUsers.contains(adUser) ? getTranslation("assigned") : getTranslation("notAssigned"), "status")
            .setHeader(getTranslation("status"))
            .setKey(Global.Component.FOOTER_COLUMN_KEY)
            .setSortOrderProvider(direction -> Stream.of(new QuerySortOrder("status", direction)))
            .setComparator((adUser1, adUser2) -> {
              if ((selectedADUsers.contains(adUser1) && selectedADUsers.contains(adUser2)) ||
                      (!selectedADUsers.contains(adUser1) && !selectedADUsers.contains(adUser2)) ) {
                return 0;
              }
              if (selectedADUsers.contains(adUser1) && !selectedADUsers.contains(adUser2)) {
                return 1;
              }
              return -1;
            });
    grid.addColumn(ADUser::getLogonName).setHeader(getTranslation("logonName"))
            .setSortable(true);
    grid.addColumn(adUser -> adUser.isAdminAccount() ? getTranslation("yes") : getTranslation("no"))
            .setHeader(getTranslation("adminAccount"))
            .setSortable(true);
    grid.addColumn(adUser -> adUser.isServiceAccount() ? getTranslation("yes") : getTranslation("no"))
            .setHeader(getTranslation("serviceAccount"))
            .setSortable(true);
    grid.getColumns().forEach(col -> col.setAutoWidth(true));
    grid.setSelectionMode(Grid.SelectionMode.MULTI);
    grid.addSelectionListener(selection -> {
      selectedADUsers.clear();
      selectedADUsers.addAll(selection.getAllSelectedItems());
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
    closeButton.addClickListener(event -> fireEvent(new CloseEvent(this, person)));

    HorizontalLayout buttonsLayout = new HorizontalLayout(saveButton, closeButton);
    buttonsLayout.setPadding(true);

    add(buttonsLayout);
  }

  public void setData(Person person, List<ADUser> adUsers) {
    this.person = person;
    if (person != null && adUsers != null) {
      // build person info string
      StringBuilder personInfos = new StringBuilder(person.getLastName());
      if (person.getFirstName() != null && person.getFirstName().length() > 0) {
        personInfos.append(", ");
        personInfos.append(person.getFirstName());
      }
      personInfos.append(" (");
      personInfos.append(person.getCentralAccountName());
      personInfos.append(")");
      personTextField.setValue(personInfos.toString());

      // you can filter the grid
      dataView = grid.setItems(adUsers);
      dataView.addFilter(adUser -> {
        String searchTerm = searchField.getValue().trim();
        if (searchTerm.isEmpty()) {
          return true;
        }
        boolean matchesLogonName = matchesTerm(adUser.getLogonName(), searchTerm);
        boolean matchesDistinguishedName = matchesTerm(adUser.getDistinguishedName(), searchTerm);

        return matchesLogonName || matchesDistinguishedName;
      });

      // show first assigned AD users
      dataView.setSortOrder((ValueProvider<ADUser, String>) adUser -> {
        if (adUser == null) {
          return "";
        }
        if (selectedADUsers.contains(adUser)) {
          return getTranslation("assigned");
        }
        return getTranslation("notAssigned");
      }, SortDirection.DESCENDING);

      // add assigned roles to selected list
      selectedADUsers.clear();
      selectedADUsers.addAll(person.getADUsers());
      grid.asMultiSelect().select(selectedADUsers);
      grid.getColumnByKey(Global.Component.FOOTER_COLUMN_KEY)
              .setFooter(String.format(getTranslation("assigned") + ": %s", selectedADUsers.size()));
    }
  }

  private boolean matchesTerm(String value, String searchTerm) {
    if (value == null ||searchTerm == null) {
      return false;
    }
    return value.toLowerCase().contains(searchTerm.toLowerCase());
  }

  private void validateAndSave() {
    if (person.getADUsers().isEmpty()) {
      person.setADUsers(selectedADUsers);
    }  else {
      person.removeAllADUsers();
      person.setADUsers(selectedADUsers);
    }
    fireEvent(new SaveEvent(this, person));
  }

  // Events
  public static abstract class PersonAssignADUserFormEvent extends ComponentEvent<PersonAssignADUserForm> {
    private final Person person;

    protected PersonAssignADUserFormEvent(PersonAssignADUserForm source, Person person) {
      super(source, false);
      this.person = person;
    }

    public Person getPerson() {
      return person;
    }
  }

  public static class SaveEvent extends PersonAssignADUserFormEvent {
    SaveEvent(PersonAssignADUserForm source, Person person) {
      super(source, person);
    }
  }

  public static class CloseEvent extends PersonAssignADUserFormEvent {
    CloseEvent(PersonAssignADUserForm source, Person person) {
      super(source, person);
    }
  }

  public <T extends ComponentEvent<?>> Registration addListener(Class<T> eventType,
                                                                ComponentEventListener<T> listener) {
    return getEventBus().addListener(eventType, listener);
  }
}