package de.hirola.adroles.views.roles;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import de.hirola.adroles.Global;
import de.hirola.adroles.data.entity.Role;
import de.hirola.adroles.service.IdentityService;
import de.hirola.adroles.views.MainLayout;
import de.hirola.adroles.views.NotificationPopUp;

import javax.annotation.security.PermitAll;
import java.util.ArrayList;
import java.util.List;

@Route(value="roles", layout = MainLayout.class)
@PageTitle("Roles Overview | AD-Roles")
@PermitAll
public class RolesListView extends VerticalLayout {
    private RoleForm roleForm;
    private final List<Role> selectedRoles = new ArrayList<>();
    private final Grid<Role> grid = new Grid<>(Role.class, false);
    private TextField filterTextField;
    private final IdentityService identityService;

    private Button addRoleButton, importFromGroupsButton, deleteRolesButton;

    public RolesListView(IdentityService identityService) {
        this.identityService = identityService;
        addClassName("rol-list-view");
        setSizeFull();
        addComponents();
        updateList();
        closeForm();
    }

    private void addComponents() {

        filterTextField = new TextField();
        filterTextField.setPlaceholder(getTranslation("searchFilter"));
        filterTextField.setClearButtonVisible(true);
        filterTextField.setValueChangeMode(ValueChangeMode.LAZY);
        filterTextField.addValueChangeListener(e -> updateList());

        addRoleButton = new Button(getTranslation("addRole"));
        addRoleButton.setWidth(Global.Component.DEFAULT_BUTTON_WIDTH);
        addRoleButton.addClickListener(click -> addRole());

        deleteRolesButton = new Button(getTranslation("deleteRoles"));
        deleteRolesButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);
        deleteRolesButton.setWidth(Global.Component.DEFAULT_BUTTON_WIDTH);
        deleteRolesButton.addClickListener(click -> deleteRoles());

        importFromGroupsButton = new Button(getTranslation("roles.importFromGroups"));
        importFromGroupsButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        importFromGroupsButton.setWidth(Global.Component.DEFAULT_BUTTON_WIDTH);
        importFromGroupsButton.addClickListener(click -> importRolesFromAD());

        // import Roles from JSON
        Button importFromJSONButton = new Button(getTranslation("roles.importFromJSON"));
        importFromJSONButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        importFromJSONButton.setWidth(Global.Component.DEFAULT_BUTTON_WIDTH);
        importFromJSONButton.addClickListener(click -> importRolesFromJSON());

        grid.addClassNames("roles-grid");
        grid.setSizeFull();
        grid.addColumn(Role::getName).setHeader(getTranslation("name"))
                .setKey(Global.Component.FOOTER_COLUMN_KEY)
                .setFooter(String.format(getTranslation("roles.sum") + ": %s", identityService.countRoles()))
                .setSortable(true);
        grid.addColumn(Role::getDescription).setHeader(getTranslation("description"))
                .setSortable(true);
        grid.addColumn(role -> role.isOrgRole() ? getTranslation("yes") : getTranslation("no"))
                .setHeader(getTranslation("orgRole"))
                .setSortable(true);
        grid.addColumn(role -> role.isAdminRole() ? getTranslation("yes") : getTranslation("no"))
                .setHeader(getTranslation("adminRole"))
                .setSortable(true);
        grid.getColumns().forEach(col -> col.setAutoWidth(true));
        grid.setSelectionMode(Grid.SelectionMode.MULTI);
        grid.addItemClickListener(event -> editRole(event.getItem()));
        grid.addSelectionListener(selection -> {
            selectedRoles.clear();
            if (selection.getAllSelectedItems().size() == 0) {
                deleteRolesButton.setEnabled(false);
            } else {
                deleteRolesButton.setEnabled(true);
                selectedRoles.addAll(selection.getAllSelectedItems());
            }
        });

        roleForm = new RoleForm();
        roleForm.setSizeFull();
        roleForm.addListener(RoleForm.SaveEvent.class, this::saveRole);
        roleForm.addListener(RoleForm.AssignPersonsEvent.class, this::addPersons);
        roleForm.addListener(RoleForm.AssignADGroupsEvent.class, this::addADGroups);
        roleForm.addListener(RoleForm.DeleteEvent.class, this::deleteRole);
        roleForm.addListener(RoleForm.CloseEvent.class, event -> closeForm());

        FlexLayout content = new FlexLayout(grid, roleForm);
        content.setFlexGrow(2, grid);
        content.setFlexGrow(1, roleForm);
        content.setFlexShrink(0, roleForm);
        content.addClassNames("content", "gap-m");
        content.setSizeFull();

        HorizontalLayout toolbar_1 = new HorizontalLayout(filterTextField, addRoleButton, deleteRolesButton);
        toolbar_1.addClassName("toolbar_1");

        HorizontalLayout toolbar_2 = new HorizontalLayout(importFromGroupsButton, importFromJSONButton);
        toolbar_2.addClassName("toolbar_2");

        add(toolbar_1, toolbar_2, content);
    }

    private void importRolesFromAD() {
        Dialog dialog = new Dialog();
        if (identityService.countPersons() > 0) {
            // data can be override
            dialog.setHeaderTitle(getTranslation("question.updateData"));

            TextArea messageArea = new TextArea();
            messageArea.setWidthFull();
            messageArea.setValue(getTranslation("roles.importFromGroups.dialog.message"));

            Button okButton = new Button("Ok", clickEvent -> {
                dialog.close();
                if (!identityService.importRolesFromGroups()) {
                    NotificationPopUp.show(NotificationPopUp.ERROR, getTranslation("error.import"));
                }
                updateList();
            });
            okButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);
            okButton.getStyle().set("margin-right", "auto");

            Button cancelButton = new Button(getTranslation("cancel"), (clickEvent) -> dialog.close());
            cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

            dialog.add(messageArea);
            dialog.getFooter().add(okButton);
            dialog.getFooter().add(cancelButton);

            dialog.open();
        } else {
            dialog.close();
            if (!identityService.importRolesFromGroups()) {
                NotificationPopUp.show(NotificationPopUp.ERROR, getTranslation("error.import"));
            }
            updateList();
        }
    }

    private void importRolesFromJSON() {
        NotificationPopUp.show(NotificationPopUp.INFO, getTranslation("not.implemented"));
    }

    private void addRole() {
        editRole(new Role());
    }

    private void saveRole(RoleForm.SaveEvent event) {
        identityService.saveRole(event.getRole());
        updateList();
        closeForm();
    }

    public void editRole(Role role) {
        if (role == null) {
            closeForm();
        } else {
            enableComponents(false);
            roleForm.setRole(role);
            roleForm.setVisible(true);
            addClassName("editing");
        }
    }

    private void deleteRole(RoleForm.DeleteEvent event) {
        identityService.deleteRole(event.getRole());
        updateList();
        closeForm();
    }

    private void deleteRoles() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(getTranslation("question.delete"));

        Button okButton = new Button("Ok", clickEvent -> {
            identityService.deleteRoles(selectedRoles);
            updateList();
            selectedRoles.clear();
            deleteRolesButton.setEnabled(false);
            dialog.close();
        });
        okButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);
        okButton.getStyle().set("margin-right", "auto");

        Button cancelButton = new Button(getTranslation("cancel"), clickEvent -> {
            grid.deselectAll();
            dialog.close();
        });
        cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        dialog.getFooter().add(okButton);
        dialog.getFooter().add(cancelButton);

        dialog.open();

    }

    private void addPersons(RoleForm.AssignPersonsEvent event) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Add persons");
        dialog.open();
    }

    private void addADGroups(RoleForm.AssignADGroupsEvent event) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Add groups");
        dialog.open();
    }

    private void updateList() {
        List<Role> filteredRoles = identityService.findAllRoles(filterTextField.getValue());
        grid.setItems(filteredRoles);
        grid.getColumnByKey(Global.Component.FOOTER_COLUMN_KEY)
                .setFooter(String.format(getTranslation("roles.sum") + ": %s", filteredRoles.size()));
    }

    private void closeForm() {
        roleForm.setRole(null);
        enableComponents(true);
        roleForm.setVisible(false);
        removeClassName("editing");
    }

    private void enableComponents(boolean enabled) {
        filterTextField.setEnabled(enabled);
        addRoleButton.setEnabled(enabled);
        if (selectedRoles.size() == 0) {
            deleteRolesButton.setEnabled(false);
        } else {
            deleteRolesButton.setEnabled(enabled);
        }
        if (identityService.countADGroups() == 0) {
            importFromGroupsButton.setEnabled(false);
        } else {
            importFromGroupsButton.setEnabled(enabled);
        }
    }
}
