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
import de.hirola.adroles.data.entity.ADGroup;
import de.hirola.adroles.data.entity.Role;
import de.hirola.adroles.service.IdentityService;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

public class RoleAssignADGroupForm extends VerticalLayout {
  private final IdentityService identityService;
  private Role role;
  private final Set<ADGroup> selectedADGroups = new LinkedHashSet<>();
  private TextField roleTexField, searchField;
  private Button assignAutomaticallyButton;
  private final Grid<ADGroup> grid = new Grid<>(ADGroup.class, false);

  private GridListDataView<ADGroup> dataView;
  public RoleAssignADGroupForm(IdentityService identityService) {
    this.identityService = identityService;
    addClassName("role-assign-ad-groups-form");
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

    grid.addClassNames("ad-group-grid");
    grid.setSizeFull();
    grid.setAllRowsVisible(true);
    grid.addColumn(adGroup -> selectedADGroups.contains(adGroup) ? getTranslation("assigned") : getTranslation("notAssigned"), "status")
            .setHeader(getTranslation("status"))
            .setKey(Global.Component.FOOTER_COLUMN_KEY)
            .setSortOrderProvider(direction -> Stream.of(new QuerySortOrder("status", direction)))
            .setComparator((adGroup1, adGroup2) -> {
              if ((selectedADGroups.contains(adGroup1) && selectedADGroups.contains(adGroup2)) ||
                      (!selectedADGroups.contains(adGroup1) && !selectedADGroups.contains(adGroup2)) ) {
                return 0;
              }
              if (selectedADGroups.contains(adGroup1) && !selectedADGroups.contains(adGroup2)) {
                return 1;
              }
              return -1;
            });
    grid.addColumn(ADGroup::getName).setHeader(getTranslation("name"))
            .setSortable(true);
    grid.addColumn(ADGroup::getDescription).setHeader(getTranslation("description"))
            .setSortable(true);
    grid.getColumns().forEach(col -> col.setAutoWidth(true));
    grid.setSelectionMode(Grid.SelectionMode.MULTI);
    grid.addSelectionListener(selection -> {
      selectedADGroups.clear();
      selectedADGroups.addAll(selection.getAllSelectedItems());
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

  public void setData(Role role, List<ADGroup> adGroups) {
    this.role = role;
    if (role != null && adGroups != null) {
      // build org unit info string
      StringBuilder orgUnitInfos = new StringBuilder(role.getName());
      if (role.getDescription().length() > 0) {
        orgUnitInfos.append(" (");
        orgUnitInfos.append(role.getDescription());
        orgUnitInfos.append(")");
      }
      roleTexField.setValue(orgUnitInfos.toString());

      // disable the assign button, if no AD groups available
      assignAutomaticallyButton.setEnabled(adGroups.size() > 0);

      // you can filter the grid
      dataView = grid.setItems(adGroups);
      dataView.addFilter(adGroup -> {
        String searchTerm = searchField.getValue().trim();
        if (searchTerm.isEmpty()) {
          return true;
        }
        boolean matchesName = matchesTerm(adGroup.getName(), searchTerm);
        boolean matchesDescription = matchesTerm(adGroup.getDescription(), searchTerm);
        boolean matchesDistinguishedName = matchesTerm(adGroup.getDistinguishedName(), searchTerm);

        return matchesName || matchesDescription || matchesDistinguishedName;
      });

      // show first assigned adGroups
      dataView.setSortOrder((ValueProvider<ADGroup, String>) adGroup -> {
        if (adGroup == null) {
          return "";
        }
        if (selectedADGroups.contains(adGroup)) {
          return getTranslation("assigned");
        }
        return getTranslation("notAssigned");
      }, SortDirection.DESCENDING);

      // add assigned roles to selected list
      selectedADGroups.clear();
      selectedADGroups.addAll(role.getADGroups());
      grid.asMultiSelect().select(selectedADGroups);
      grid.getColumnByKey(Global.Component.FOOTER_COLUMN_KEY)
              .setFooter(String.format(getTranslation("assigned") + ": %s", selectedADGroups.size()));
    }
  }

  private boolean matchesTerm(String value, String searchTerm) {
    if (value == null ||searchTerm == null) {
      return false;
    }
    return value.toLowerCase().contains(searchTerm.toLowerCase());
  }

  private void assignFromADGroups() {
    selectedADGroups.addAll(identityService.findAllADGroupsForPersons(role.getPersons()));
    grid.getColumnByKey(Global.Component.FOOTER_COLUMN_KEY)
            .setFooter(String.format(getTranslation("assigned") + ": %s", selectedADGroups.size()));
    grid.asMultiSelect().select(selectedADGroups);
  }
  private void validateAndSave() {
    if (role.getADGroups().isEmpty()) {
      role.setADGroups(selectedADGroups);
    }  else {
      role.removeAllADGroups();
      role.setADGroups(selectedADGroups);
    }
    fireEvent(new SaveEvent(this, role));
  }

  // Events
  public static abstract class RoleAssignadGroupFormEvent extends ComponentEvent<RoleAssignADGroupForm> {
    private final Role role;

    protected RoleAssignadGroupFormEvent(RoleAssignADGroupForm source, Role role) {
      super(source, false);
      this.role = role;
    }

    public Role getRole() {
      return role;
    }
  }

  public static class SaveEvent extends RoleAssignadGroupFormEvent {
    SaveEvent(RoleAssignADGroupForm source, Role role) {
      super(source, role);
    }
  }

  public static class CloseEvent extends RoleAssignadGroupFormEvent {
    CloseEvent(RoleAssignADGroupForm source, Role role) {
      super(source, role);
    }
  }

  public <T extends ComponentEvent<?>> Registration addListener(Class<T> eventType,
                                                                ComponentEventListener<T> listener) {
    return getEventBus().addListener(eventType, listener);
  }
}