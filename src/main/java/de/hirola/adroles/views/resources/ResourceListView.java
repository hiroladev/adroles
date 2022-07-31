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
import de.hirola.adroles.views.roles.ResourceAssignADGroupForm;

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
    private ResourceRoleForm resourceRoleForm;
    private ResourceAssignPersonForm assignPersonForm;
    private ResourceAssignADGroupForm assignADGroupForm;
    private final Grid<Role> grid = new Grid<>(Role.class, false);
    private TextField filterTextField;
    private Button addResourceRoleButton, deleteResourceRolesButton, importFromPersonsButton, importFromJSONButton;

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
        closeResourceForm();
        closeAssignPersonsForm();
        closeAssignADGroupsForm();
    }

    private void addComponents() {

        filterTextField = new TextField();
        filterTextField.setPlaceholder(getTranslation("searchFilter"));
        filterTextField.setClearButtonVisible(true);
        filterTextField.setValueChangeMode(ValueChangeMode.LAZY);
        filterTextField.addValueChangeListener(e -> updateList());

        addResourceRoleButton = new Button(new Icon(VaadinIcon.PLUS));
        addResourceRoleButton.addThemeVariants(ButtonVariant.LUMO_ICON);
        addResourceRoleButton.getElement().setAttribute("aria-label", getTranslation(roleResource.getAddResourceTranslationKey()));
        addResourceRoleButton.setWidth(Global.Component.DEFAULT_ICON_BUTTON_WIDTH);
        addResourceRoleButton.addClickListener(click -> addResourceRole());

        deleteResourceRolesButton = new Button(new Icon(VaadinIcon.MINUS));
        deleteResourceRolesButton.addThemeVariants(ButtonVariant.LUMO_ICON);
        deleteResourceRolesButton.getElement().setAttribute("aria-label", roleResource.getDeleteResourcesTranslationKey());
        deleteResourceRolesButton.setWidth(Global.Component.DEFAULT_ICON_BUTTON_WIDTH);
        deleteResourceRolesButton.addClickListener(click -> deleteResourceRoles());
        deleteResourceRolesButton.setEnabled(false);

        importFromPersonsButton = new Button(new Icon(VaadinIcon.INSERT));
        importFromPersonsButton.addThemeVariants(ButtonVariant.LUMO_ICON);
        importFromPersonsButton.setText(getTranslation("importFromPersons"));
        importFromPersonsButton.setIconAfterText(true);
        importFromPersonsButton.getElement().setAttribute("aria-label", getTranslation("importFromPersons"));
        importFromPersonsButton.setWidth(Global.Component.DEFAULT_BUTTON_WIDTH);
        importFromPersonsButton.addClickListener(click -> {
            if (roleResource.isOrgResource()) {
                importOrgUnitsFromPersons();
            }
        });

        // import Roles from JSON
        importFromJSONButton = new Button(new Icon(VaadinIcon.INSERT));
        importFromJSONButton.addThemeVariants(ButtonVariant.LUMO_ICON);
        importFromJSONButton.setText(getTranslation("importFromJSON"));
        importFromJSONButton.setIconAfterText(true);
        importFromJSONButton.getElement().setAttribute("aria-label", getTranslation("importFromJSON"));
        importFromJSONButton.setWidth(Global.Component.DEFAULT_BUTTON_WIDTH);
        importFromJSONButton.addClickListener(click -> importResourcesFromJSON());

        HorizontalLayout toolbar = new HorizontalLayout(filterTextField, addResourceRoleButton,
                deleteResourceRolesButton, importFromPersonsButton, importFromJSONButton);
        toolbar.addClassName("toolbar");

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

        resourceRoleForm = new ResourceRoleForm();
        resourceRoleForm.setWidthFull();
        resourceRoleForm.addListener(ResourceRoleForm.SaveEvent.class, this::saveResourceRole);
        resourceRoleForm.addListener(ResourceRoleForm.AssignPersonsEvent.class, this::addPersons);
        resourceRoleForm.addListener(ResourceRoleForm.AssignADGroupsEvent.class, this::addADGroups);
        resourceRoleForm.addListener(ResourceRoleForm.DeleteEvent.class, this::deleteResourceRole);
        resourceRoleForm.addListener(ResourceRoleForm.CloseEvent.class, event -> closeResourceForm());

        assignPersonForm = new ResourceAssignPersonForm(identityService);
        assignPersonForm.setWidthFull();
        assignPersonForm.addListener(ResourceAssignPersonForm.SaveEvent.class, this::saveAssignedPersons);
        assignPersonForm.addListener(ResourceAssignPersonForm.CloseEvent.class, event -> closeAssignPersonsForm());

        assignADGroupForm = new ResourceAssignADGroupForm(identityService);
        assignADGroupForm.setWidthFull();
        assignADGroupForm.addListener(ResourceAssignADGroupForm.SaveEvent.class, this::saveAssignedADGroups);
        assignADGroupForm.addListener(ResourceAssignADGroupForm.CloseEvent.class, event -> closeAssignADGroupsForm());

        FlexLayout content = new FlexLayout(grid, resourceRoleForm, assignPersonForm, assignADGroupForm);
        content.setFlexGrow(2, grid);
        content.setFlexGrow(1, resourceRoleForm, assignPersonForm, assignADGroupForm);
        content.setFlexShrink(0, resourceRoleForm, assignPersonForm, assignADGroupForm);
        content.addClassNames("content", "gap-m");
        content.setSizeFull();

        add(toolbar, content);
    }

    private void saveResourceRole(ResourceRoleForm.SaveEvent event) {
        identityService.saveRole(event.getItem());
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

    private void deleteResourceRole(ResourceRoleForm.DeleteEvent event) {
        identityService.deleteRole(event.getItem());
        updateList();
        closeResourceForm();
    }

    public void editResource(Role role) {
        if (role == null) {
            closeResourceForm();
        } else {
            resourceRoleForm.setResourceRole(role);
            enableComponents(false);
            resourceRoleForm.setVisible(true);
            addClassName("editing");
        }
    }

    private void addResourceRole() {
        Role role = new Role();
        role.setRoleResource(roleResource);
        editResource(role);
    }

    private void importOrgUnitsFromPersons() {
        if (roleResource.isOrgResource()) {
            Dialog dialog = new Dialog();
            if (identityService.countRoles(roleResource) > 0) {
                // data can be override
                dialog.setHeaderTitle(getTranslation("question.updateData"));

                TextArea messageArea = new TextArea();
                messageArea.setWidthFull();
                messageArea.setValue(getTranslation("org.importFromPersons.dialog.message"));

                Button okButton = new Button("Ok", clickEvent -> {
                    dialog.close();
                    if (!identityService.importOrgUnitsFromPersons(false)) {
                        NotificationPopUp.show(NotificationPopUp.ERROR, getTranslation("error.import"));
                    }
                    updateList();
                });
                okButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);
                okButton.getStyle().set("margin-right", "auto");

                Button partiallyButton = new Button(getTranslation("question.missing"), clickEvent -> {
                    dialog.close();
                    if (!identityService.importOrgUnitsFromPersons(true)) {
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
                dialog.getFooter().add(partiallyButton);
                dialog.getFooter().add(cancelButton);

                dialog.open();
            } else {
                dialog.close();
                if (!identityService.importOrgUnitsFromPersons(false)) {
                    NotificationPopUp.show(NotificationPopUp.ERROR, getTranslation("error.import"));
                }
                updateList();
            }
        }
    }

    private void importResourcesFromJSON() {
        NotificationPopUp.show(NotificationPopUp.INFO, getTranslation("not.implemented"));
    }

    private void addPersons(ResourceRoleForm.AssignPersonsEvent event) {
        closeResourceForm();
        enableComponents(false);
        assignPersonForm.setVisible(true);
        assignPersonForm.setData(event.getItem(), identityService.findAllPersons(null));
        addClassName("editing");
    }

    private void saveAssignedPersons(ResourceAssignPersonForm.SaveEvent event) {
        identityService.saveRole(event.getOrgUnit());
        closeAssignPersonsForm();
        updateList();
    }

    private void addADGroups(ResourceRoleForm.AssignADGroupsEvent event) {
        closeResourceForm();
        enableComponents(false);
        assignADGroupForm.setVisible(true);
        // assignADGroupForm.setData(event.getItem(), identityService.findAllRoles(null));
        addClassName("editing");
    }

    private void saveAssignedADGroups(ResourceAssignADGroupForm.SaveEvent event) {
        //orgUnitService.saveResourceRole(event.getItem());
        closeAssignADGroupsForm();
        updateList();
    }

    private void updateList() {
        List<Role> filteredRoles = identityService.findAllRoles(filterTextField.getValue(), roleResource);
        grid.setItems(filteredRoles);
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
            } else {
                roleResourceList.put(getTranslation("standard"), roleResource);
            }
        }
    }

    private void closeResourceForm() {
        resourceRoleForm.setResourceRole(null);
        resourceRoleForm.setVisible(false);
        enableComponents(true);
        removeClassName("editing");
    }

    private void closeAssignPersonsForm() {
        assignPersonForm.setData(null, null);
        assignPersonForm.setVisible(false);
        enableComponents(true);
        removeClassName("editing");
    }

    private void closeAssignADGroupsForm() {
        assignADGroupForm.setData(null, null);
        assignADGroupForm.setVisible(false);
        enableComponents(true);
        removeClassName("editing");
    }

    private void enableComponents(boolean enabled) {
        filterTextField.setEnabled(enabled);
        addResourceRoleButton.setEnabled(enabled);
        importFromJSONButton.setEnabled(enabled);

        if (enabled) {
            if (identityService.countPersons() == 0) {
                importFromPersonsButton.setEnabled(false);
            }
            if (selectedResourceRoles.size() == 0) {
                deleteResourceRolesButton.setEnabled(false);
            }
        } else {
            importFromPersonsButton.setEnabled(enabled);
        }
    }
}