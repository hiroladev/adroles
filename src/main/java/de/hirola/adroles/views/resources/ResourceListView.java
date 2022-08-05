package de.hirola.adroles.views.resources;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import de.hirola.adroles.Global;
import de.hirola.adroles.data.entity.RoleResource;
import de.hirola.adroles.data.entity.Role;
import de.hirola.adroles.service.IdentityService;
import de.hirola.adroles.views.NotificationPopUp;
import de.hirola.adroles.views.roles.RoleForm;
import de.hirola.adroles.views.roles.RoleAssignADGroupForm;
import de.hirola.adroles.views.roles.RoleAssignPersonForm;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

/**
 * Copyright 2022 by Michael Schmidt, Hirola Consulting
 * This software us licensed under the AGPL-3.0 or later.
 * <p>
 * An org unit also a role.
 *
 * @author Michael Schmidt (Hirola)
 * @since v0.1
 */


public class ResourceListView extends VerticalLayout {
    private final IdentityService identityService;
    private final Hashtable<String, RoleResource> roleResourceList = new Hashtable<>();
    private final RoleResource roleResource;
    private final List<Role> selectedResourceRoles = new ArrayList<>();
    private RoleForm roleForm;
    private RoleAssignPersonForm assignPersonForm;
    private RoleAssignADGroupForm assignADGroupForm;
    private final Grid<Role> grid = new Grid<>(Role.class, false);
    private TextField filterTextField;
    private Button addResourceRoleButton, deleteResourceRolesButton, updateButton, importFromJSONButton;

    public ResourceListView(IdentityService identityService, int resourceType) throws InstantiationException {
        this.identityService = identityService;
        roleResource = identityService.getRoleResource(resourceType);
        if (roleResource == null) {
            throw  new InstantiationException(getTranslation("error.resource.instantiation"));
        }
        loadAvailableRoleResources();
        addClassName(roleResource.getViewClassName());
        setSizeFull();
        addComponents();
        updateList();
        enableComponents(true);
    }

    private void addComponents() {

        filterTextField = new TextField();
        filterTextField.setPlaceholder(getTranslation("searchFilter"));
        filterTextField.setClearButtonVisible(true);
        filterTextField.setValueChangeMode(ValueChangeMode.LAZY);
        filterTextField.addValueChangeListener(e -> updateList());

        addResourceRoleButton = new Button(new Icon(VaadinIcon.PLUS));
        addResourceRoleButton.addThemeVariants(ButtonVariant.LUMO_ICON);
        addResourceRoleButton.getElement().setAttribute("aria-label",
                getTranslation(roleResource.getAddResourceTranslationKey()));
        addResourceRoleButton.setWidth(Global.Component.DEFAULT_ICON_BUTTON_WIDTH);
        addResourceRoleButton.addClickListener(click -> addResourceRole());

        deleteResourceRolesButton = new Button(new Icon(VaadinIcon.MINUS));
        deleteResourceRolesButton.addThemeVariants(ButtonVariant.LUMO_ICON);
        deleteResourceRolesButton.getElement().setAttribute("aria-label",
                roleResource.getDeleteResourcesTranslationKey());
        deleteResourceRolesButton.setWidth(Global.Component.DEFAULT_ICON_BUTTON_WIDTH);
        deleteResourceRolesButton.addClickListener(click -> deleteResourceRoles());
        deleteResourceRolesButton.setEnabled(false);

        // update org roles and employees
        updateButton = new Button(new Icon(VaadinIcon.INSERT));
        updateButton.addThemeVariants(ButtonVariant.LUMO_ICON);
        updateButton.setText(getTranslation("updateFromPersons"));
        updateButton.setIconAfterText(true);
        updateButton.getElement().setAttribute("aria-label", getTranslation("updateFromPersons"));
        updateButton.setWidth(Global.Component.DEFAULT_BUTTON_WIDTH);
        updateButton.addClickListener(click -> updateOrgRoles());

        // import Roles from JSON
        importFromJSONButton = new Button(new Icon(VaadinIcon.INSERT));
        importFromJSONButton.addThemeVariants(ButtonVariant.LUMO_ICON);
        importFromJSONButton.setText(getTranslation("importFromJSON"));
        importFromJSONButton.setIconAfterText(true);
        importFromJSONButton.getElement().setAttribute("aria-label", getTranslation("importFromJSON"));
        importFromJSONButton.setWidth(Global.Component.DEFAULT_BUTTON_WIDTH);
        importFromJSONButton.addClickListener(click -> importResourcesFromJSON());

        HorizontalLayout toolbar = new HorizontalLayout();
        toolbar.addClassName("toolbar");
        if (roleResource.isOrgResource()) { // only org roles can be separately updated
            toolbar.add(filterTextField, addResourceRoleButton, deleteResourceRolesButton, updateButton, importFromJSONButton);
        } else {
            toolbar.add(filterTextField, addResourceRoleButton, deleteResourceRolesButton, importFromJSONButton);
        }

        grid.addClassNames(roleResource.getViewClassName().concat("-grid"));
        grid.setSizeFull();
        grid.addColumn(Role::getName).setHeader(getTranslation("name"))
                .setKey(Global.Component.FOOTER_COLUMN_KEY)
                .setSortable(true)
                .setFooter(String.format(
                        getTranslation("role.sum") + ": %s", identityService.countRoles(roleResource)));
        grid.addColumn(Role::getDescription).setHeader(getTranslation("description"))
                .setSortable(true);
        grid.getColumns().forEach(col -> col.setAutoWidth(true));
        grid.setSelectionMode(Grid.SelectionMode.MULTI);
        grid.addItemClickListener(event -> editResource(event.getItem()));
        grid.addSelectionListener(selection -> {
            selectedResourceRoles.clear();
            if (selection.getAllSelectedItems().size() == 0) {
                deleteResourceRolesButton.setEnabled(false);
            } else {
                deleteResourceRolesButton.setEnabled(true);
                selectedResourceRoles.addAll(selection.getAllSelectedItems());
            }
        });

        roleForm = new RoleForm(roleResourceList);
        roleForm.setWidthFull();
        roleForm.addListener(RoleForm.SaveEvent.class, this::saveResourceRole);
        roleForm.addListener(RoleForm.AssignPersonsEvent.class, this::addPersons);
        roleForm.addListener(RoleForm.AssignADGroupsEvent.class, this::addADGroups);
        roleForm.addListener(RoleForm.CloseEvent.class, event -> closeRoleForm());
        roleForm.setVisible(false);

        assignPersonForm = new RoleAssignPersonForm(identityService);
        assignPersonForm.setWidthFull();
        assignPersonForm.addListener(RoleAssignPersonForm.SaveEvent.class, this::saveAssignedPersons);
        assignPersonForm.addListener(RoleAssignPersonForm.CloseEvent.class, event -> closeAssignPersonsForm(event.getRole()));
        assignPersonForm.setVisible(false);

        assignADGroupForm = new RoleAssignADGroupForm(identityService);
        assignADGroupForm.setWidthFull();
        assignADGroupForm.addListener(RoleAssignADGroupForm.SaveEvent.class, this::saveAssignedADGroups);
        assignADGroupForm.addListener(RoleAssignADGroupForm.CloseEvent.class, event -> closeAssignADGroupsForm(event.getRole()));
        assignADGroupForm.setVisible(false);

        FlexLayout content = new FlexLayout(grid, roleForm, assignPersonForm, assignADGroupForm);
        content.setFlexGrow(2, grid);
        content.setFlexGrow(1, roleForm, assignPersonForm, assignADGroupForm);
        content.setFlexShrink(0, roleForm, assignPersonForm, assignADGroupForm);
        content.addClassNames("content", "gap-m");
        content.setSizeFull();

        add(toolbar, content);
    }

    private void saveResourceRole(RoleForm.SaveEvent event) {
        identityService.saveRole(event.getRole());
        updateList();
    }

    private void deleteResourceRoles() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(getTranslation("question.delete"));

        Button okButton = new Button("Ok", clickEvent -> {
            identityService.deleteRoles(selectedResourceRoles);
            updateList();
            selectedResourceRoles.clear();
            deleteResourceRolesButton.setEnabled(false);
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

    public void editResource(Role role) {
        if (role == null) {
            closeRoleForm();
        } else {
            roleForm.setRole(role);
            enableComponents(false);
            roleForm.setVisible(true);
            addClassName("editing");
        }
    }

    private void addResourceRole() {
        Role role = new Role();
        role.setRoleResource(roleResource);
        editResource(role);
    }

    private void updateOrgRoles() {
        if (identityService.countPersons() > 0) {
            Dialog dialog = new Dialog();
            dialog.setWidth(Global.Component.DEFAULT_DIALOG_WIDTH);
            // data can be override
            dialog.setHeaderTitle(getTranslation("question.updateData"));

            TextArea messageArea = new TextArea();
            messageArea.setWidthFull();
            messageArea.setValue(getTranslation("updateOrgFromPersons"));

            Button okButton = new Button("Ok", clickEvent -> {
                dialog.close();
                if (!identityService.updateOrgRolesFromPersons()) {
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
        }
    }

    private void importResourcesFromJSON() {
        if (identityService.importRolesFromJSON(roleResource)) {
            NotificationPopUp.show(NotificationPopUp.INFO, getTranslation("error.import"));
        }
        updateList();
    }

    private void addPersons(RoleForm.AssignPersonsEvent event) {
        closeRoleForm();
        enableComponents(false);
        assignPersonForm.setVisible(true);
        assignPersonForm.setData(event.getRole(), identityService.findAllPersons(null));
        addClassName("editing-assign-persons-form");
    }

    private void saveAssignedPersons(RoleAssignPersonForm.SaveEvent event) {
        Role role = event.getRole();
        if (identityService.saveRole(role)) {
            closeAssignPersonsForm(role);
            updateList();
        } else {
            NotificationPopUp.show(NotificationPopUp.ERROR, getTranslation("error.save"));
        }
    }

    private void addADGroups(RoleForm.AssignADGroupsEvent event) {
        closeRoleForm();
        enableComponents(false);
        assignADGroupForm.setData(event.getRole(), identityService.findAllADGroups(null));
        assignADGroupForm.setVisible(true);
        addClassName("editing-assign-ad-groups-form");
    }

    private void saveAssignedADGroups(RoleAssignADGroupForm.SaveEvent event) {
        Role role = event.getRole();
        if (identityService.saveRole(role)) {
            closeAssignADGroupsForm(role);
            updateList();
        } else {
            NotificationPopUp.show(NotificationPopUp.ERROR, getTranslation("error.save"));
        }
    }

    private void updateList() {
        List<Role> filteredRoles = identityService.findAllRoles(filterTextField.getValue(), roleResource);
        grid.setItems(filteredRoles);
        grid.deselectAll();
        grid.getColumnByKey(Global.Component.FOOTER_COLUMN_KEY)
                .setFooter(String.format(getTranslation("role.sum") + ": %s", filteredRoles.size()));
    }

    private void loadAvailableRoleResources() {
        List<RoleResource> roleResources = identityService.getAllRoleResources();
        for (RoleResource roleResource: roleResources) {
            if (roleResource.isOrgResource()) {
                roleResourceList.put(getTranslation("org"), roleResource);
            } else if (roleResource.isProjectResource()) {
                roleResourceList.put(getTranslation("project"), roleResource);
            } else if (roleResource.isFileShareResource()) {
                roleResourceList.put(getTranslation("fileShare"), roleResource);
            } else if (roleResource.isEmailResource()) {
                roleResourceList.put(getTranslation("emailResource"), roleResource);
            } else {
                roleResourceList.put(getTranslation("standard"), roleResource);
            }
        }
    }

    private void closeRoleForm() {
        roleForm.setRole(null);
        roleForm.setVisible(false);
        enableComponents(true);
        removeClassName("editing");
    }

    private void closeAssignPersonsForm(Role role) {
        assignPersonForm.setData(null, null);
        assignPersonForm.setVisible(false);
        roleForm.setRole(role);
        roleForm.setVisible(true);
        removeClassName("editing-assign-persons-form");
    }

    private void closeAssignADGroupsForm(Role role) {
        assignADGroupForm.setData(null, null);
        assignADGroupForm.setVisible(false);
        roleForm.setRole(role);
        roleForm.setVisible(true);
        removeClassName("editing-assign-ad-groups-form");
    }

    private void enableComponents(boolean enabled) {
        filterTextField.setEnabled(enabled);
        addResourceRoleButton.setEnabled(enabled);
        updateButton.setEnabled(enabled);
        importFromJSONButton.setEnabled(enabled);

        if (enabled) {
            if (identityService.countPersons() == 0) {
                updateButton.setEnabled(false);
            }
            if (selectedResourceRoles.size() == 0) {
                deleteResourceRolesButton.setEnabled(false);
            }
        }
    }
}