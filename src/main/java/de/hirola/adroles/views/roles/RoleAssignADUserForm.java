package de.hirola.adroles.views.roles;

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
import de.hirola.adroles.data.entity.Role;
import de.hirola.adroles.service.IdentityService;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

public class RoleAssignADUserForm extends VerticalLayout {
  private final IdentityService identityService;
  private Role role;
  private final Set<ADUser> selectedADUsers = new LinkedHashSet<>();
  private TextField roleTexField, searchField;
  private Button assignAutomaticallyButton;
  private final Grid<ADUser> grid = new Grid<>(ADUser.class, false);
  private GridListDataView<ADUser> dataView;
  
  public RoleAssignADUserForm(IdentityService identityService) {
    this.identityService = identityService;
    addClassName("role-assign-ad-users-form");
    addComponents();
  }

  private void addComponents() {

    roleTexField = new TextField(getTranslation("role"));
    roleTexField.setWidth(Global.Component.DEFAULT_TEXT_FIELD_WIDTH);
    roleTexField.setReadOnly(true);
    add(roleTexField);

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

    assignAutomaticallyButton = new Button(getTranslation("assignAutomatically"), new Icon(VaadinIcon.DOWNLOAD));
    assignAutomaticallyButton.addThemeVariants(ButtonVariant.LUMO_ICON);
    assignAutomaticallyButton.setWidth(Global.Component.DEFAULT_BUTTON_WIDTH);
    assignAutomaticallyButton.addClickListener(click -> assignFromADGroups());
    add(assignAutomaticallyButton);

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
    closeButton.addClickListener(event -> fireEvent(new CloseEvent(this, role)));

    HorizontalLayout buttonsLayout = new HorizontalLayout(saveButton, closeButton);
    buttonsLayout.setPadding(true);

    add(buttonsLayout);
  }

  public void setData(Role role, List<ADUser> adUsers) {
    this.role = role;
    if (role != null && adUsers != null) {
      // build org unit info string
      StringBuilder orgUnitInfos = new StringBuilder(role.getName());
      if (role.getDescription().length() > 0) {
        orgUnitInfos.append(" (");
        orgUnitInfos.append(role.getDescription());
        orgUnitInfos.append(")");
      }
      roleTexField.setValue(orgUnitInfos.toString());

      // disable the assign button, if no ad users available
      assignAutomaticallyButton.setEnabled(adUsers.size() > 0);

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
      selectedADUsers.addAll(role.getADUsers());
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

  private void assignFromADGroups() {
    selectedADUsers.addAll(identityService.findAllManageableADUsers());
    grid.getColumnByKey(Global.Component.FOOTER_COLUMN_KEY)
            .setFooter(String.format(getTranslation("assigned") + ": %s", selectedADUsers.size()));
    grid.asMultiSelect().select(selectedADUsers);
  }
  private void validateAndSave() {
    if (role.getADUsers().isEmpty()) {
      role.setADUsers(selectedADUsers);
    }  else {
      role.removeAllADUsers();
      role.setADUsers(selectedADUsers);
    }
    fireEvent(new SaveEvent(this, role));
  }

  // Events
  public static abstract class RoleAssignADUserFormEvent extends ComponentEvent<RoleAssignADUserForm> {
    private final Role role;

    protected RoleAssignADUserFormEvent(RoleAssignADUserForm source, Role role) {
      super(source, false);
      this.role = role;
    }

    public Role getRole() {
      return role;
    }
  }

  public static class SaveEvent extends RoleAssignADUserFormEvent {
    SaveEvent(RoleAssignADUserForm source, Role role) {
      super(source, role);
    }
  }

  public static class CloseEvent extends RoleAssignADUserFormEvent {
    CloseEvent(RoleAssignADUserForm source, Role role) {
      super(source, role);
    }
  }

  public <T extends ComponentEvent<?>> Registration addListener(Class<T> eventType,
                                                                ComponentEventListener<T> listener) {
    return getEventBus().addListener(eventType, listener);
  }
}