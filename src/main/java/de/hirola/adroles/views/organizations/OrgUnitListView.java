package de.hirola.adroles.views.organizations;

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
import de.hirola.adroles.views.roles.RoleAssignADGroupForm;

import javax.annotation.security.PermitAll;
import java.util.ArrayList;
import java.util.List;

/**
 * Copyright 2022 by Michael Schmidt, Hirola Consulting
 * This software us licensed under the AGPL-3.0 or later.
 *
 * An org unit also a role.
 *
 * @author Michael Schmidt (Hirola)
 * @since v0.1
 */

@Route(value = "organizations", layout = MainLayout.class)
@PageTitle("Organizations | AD-Roles")
@PermitAll
public class OrgUnitListView extends VerticalLayout {
    private final IdentityService identityService;
    private final List<Role> selectedOrgUnits = new ArrayList<>();
    private OrgUnitForm orgUnitForm;
    private OrgUnitAssignPersonForm assignPersonForm;
    private RoleAssignADGroupForm assignADGroupForm;
    private final Grid<Role> grid = new Grid<>(Role.class, false);
    private TextField filterTextField;
    private Button addOrgUnitButton, deleteOrgUnitsButton, importFromPersonsButton, importFromJSONButton;

    public OrgUnitListView(IdentityService identityService) {
        this.identityService = identityService;
        addClassName("organizations-list-view");
        setSizeFull();
        addComponents();
        updateList();
        closeOrgUnitForm();
        closeAssignPersonsForm();
        closeAssignADGroupsForm();
    }

    private void addComponents() {

        filterTextField = new TextField();
        filterTextField.setPlaceholder(getTranslation("searchFilter"));
        filterTextField.setClearButtonVisible(true);
        filterTextField.setValueChangeMode(ValueChangeMode.LAZY);
        filterTextField.addValueChangeListener(e -> updateList());

        addOrgUnitButton = new Button(getTranslation("addOrgUnit"));
        addOrgUnitButton.setWidth(Global.Component.DEFAULT_BUTTON_WIDTH);
        addOrgUnitButton.addClickListener(click -> addOrgUnit());

        deleteOrgUnitsButton = new Button(getTranslation("deleteOrgUnits"));
        deleteOrgUnitsButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);
        deleteOrgUnitsButton.setWidth(Global.Component.DEFAULT_BUTTON_WIDTH);
        deleteOrgUnitsButton.addClickListener(click -> deleteOrgUnits());
        deleteOrgUnitsButton.setEnabled(false);

        HorizontalLayout toolbar_1 = new HorizontalLayout(filterTextField, addOrgUnitButton, deleteOrgUnitsButton);
        toolbar_1.addClassName("toolbar_1");

        TextField placeHolder = new TextField();
        placeHolder.setReadOnly(true);
        placeHolder.setEnabled(false);

        importFromPersonsButton = new Button(getTranslation("orgUnits.importFromPersons"));
        importFromPersonsButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        importFromPersonsButton.setWidth(Global.Component.DEFAULT_BUTTON_WIDTH);
        importFromPersonsButton.addClickListener(click -> importOrgUnitsFromPersons());
        if (identityService.countPersons() == 0) {
            importFromPersonsButton.setEnabled(false);
        }

        importFromJSONButton = new Button(getTranslation("orgUnits.importFromJSON"));
        importFromJSONButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        importFromJSONButton.setWidth(Global.Component.DEFAULT_BUTTON_WIDTH);
        importFromJSONButton.addClickListener(click -> importOrgUnitsFromJSON());

        HorizontalLayout toolbar_2 = new HorizontalLayout(placeHolder, importFromPersonsButton, importFromJSONButton);
        toolbar_2.addClassName("toolbar_2");

        grid.addClassNames("orgUnit-grid");
        grid.setSizeFull();
        grid.addColumn(Role::getName).setHeader(getTranslation("name"))
                .setKey(Global.Component.FOOTER_COLUMN_KEY)
                .setSortable(true)
                .setFooter(String.format(getTranslation("orgUnits.sum") + ": %s", identityService.countOrganisations()));
        grid.addColumn(Role::getDescription).setHeader(getTranslation("description"))
                .setSortable(true);
        grid.getColumns().forEach(col -> col.setAutoWidth(true));
        grid.setSelectionMode(Grid.SelectionMode.MULTI);
        grid.addItemClickListener(event -> editOrgUnit(event.getItem()));
        grid.addSelectionListener(selection -> {
            selectedOrgUnits.clear();
            if (selection.getAllSelectedItems().size() == 0) {
                deleteOrgUnitsButton.setEnabled(false);
            } else {
                deleteOrgUnitsButton.setEnabled(true);
                selectedOrgUnits.addAll(selection.getAllSelectedItems());
            }
        });

        orgUnitForm = new OrgUnitForm();
        orgUnitForm.setWidthFull();
        orgUnitForm.addListener(OrgUnitForm.SaveEvent.class, this::saveOrgUnit);
        orgUnitForm.addListener(OrgUnitForm.AssignPersonsEvent.class, this::addPersons);
        orgUnitForm.addListener(OrgUnitForm.AssignADGroupsEvent.class, this::addADGroups);
        orgUnitForm.addListener(OrgUnitForm.DeleteEvent.class, this::deleteOrgUnit);
        orgUnitForm.addListener(OrgUnitForm.CloseEvent.class, event -> closeOrgUnitForm());

        assignPersonForm = new OrgUnitAssignPersonForm(identityService);
        assignPersonForm.setWidthFull();
        assignPersonForm.addListener(OrgUnitAssignPersonForm.SaveEvent.class, this::saveAssignedPersons);
        assignPersonForm.addListener(OrgUnitAssignPersonForm.CloseEvent.class, event -> closeAssignPersonsForm());

        assignADGroupForm = new RoleAssignADGroupForm(identityService);
        assignADGroupForm.setWidthFull();
        assignADGroupForm.addListener(RoleAssignADGroupForm.SaveEvent.class, this::saveAssignedADGroups);
        assignADGroupForm.addListener(RoleAssignADGroupForm.CloseEvent.class, event -> closeAssignADGroupsForm());

        FlexLayout content = new FlexLayout(grid, orgUnitForm, assignPersonForm, assignADGroupForm);
        content.setFlexGrow(2, grid);
        content.setFlexGrow(1, orgUnitForm, assignPersonForm, assignADGroupForm);
        content.setFlexShrink(0, orgUnitForm, assignPersonForm, assignADGroupForm);
        content.addClassNames("content", "gap-m");
        content.setSizeFull();

        add(toolbar_1, toolbar_2, content);
    }

    private void saveOrgUnit(OrgUnitForm.SaveEvent event) {
        identityService.saveRole(event.getOrgUnit());
        updateList();
    }

    private void deleteOrgUnits() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(getTranslation("question.delete"));

        Button okButton = new Button("Ok", clickEvent -> {
            identityService.deleteRoles(selectedOrgUnits);
            updateList();
            selectedOrgUnits.clear();
            deleteOrgUnitsButton.setEnabled(false);
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

    private void deleteOrgUnit(OrgUnitForm.DeleteEvent event) {
        identityService.deleteRole(event.getOrgUnit());
        updateList();
        closeOrgUnitForm();
    }

    public void editOrgUnit(Role orgUnit) {
        if (orgUnit == null) {
            closeOrgUnitForm();
        } else {
            orgUnitForm.setOrgUnit(orgUnit);
            enableComponents(false);
            orgUnitForm.setVisible(true);
            addClassName("editing");
        }
    }

    private void addOrgUnit() {
        Role orgUnit = new Role();
        orgUnit.setOrgRole(true);
        editOrgUnit(orgUnit);
    }

    private void importOrgUnitsFromPersons() {
        Dialog dialog = new Dialog();
        if (identityService.countOrganisations() > 0) {
            // data can be override
            dialog.setHeaderTitle(getTranslation("question.updateData"));

            TextArea messageArea = new TextArea();
            messageArea.setWidthFull();
            messageArea.setValue(getTranslation("orgUnits.importFromPersons.dialog.message"));

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

    private void importOrgUnitsFromJSON() {
        NotificationPopUp.show(NotificationPopUp.INFO, getTranslation("not.implemented"));
    }

    private void addPersons(OrgUnitForm.AssignPersonsEvent event) {
        closeOrgUnitForm();
        enableComponents(false);
        assignPersonForm.setVisible(true);
        assignPersonForm.setData(event.getOrgUnit(), identityService.findAllPersons(null));
        addClassName("editing");
    }

    private void saveAssignedPersons(OrgUnitAssignPersonForm.SaveEvent event) {
        identityService.saveRole(event.getOrgUnit());
        closeAssignPersonsForm();
        updateList();
    }

    private void addADGroups(OrgUnitForm.AssignADGroupsEvent event) {
        closeOrgUnitForm();
        enableComponents(false);
        assignADGroupForm.setVisible(true);
        // assignADGroupForm.setData(event.getOrgUnit(), identityService.findAllRoles(null));
        addClassName("editing");
    }

    private void saveAssignedADGroups(RoleAssignADGroupForm.SaveEvent event) {
        //orgUnitService.saveOrgUnit(event.getOrgUnit());
        closeAssignADGroupsForm();
        updateList();
    }

    private void updateList() {
        List<Role> filteredOrgUnits = identityService.findAllOrgUnits(filterTextField.getValue());
        grid.setItems(filteredOrgUnits);
        grid.getColumnByKey(Global.Component.FOOTER_COLUMN_KEY)
                .setFooter(String.format(getTranslation("orgUnits.sum") + ": %s", filteredOrgUnits.size()));
    }

    private void closeOrgUnitForm() {
        orgUnitForm.setOrgUnit(null);
        orgUnitForm.setVisible(false);
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
        addOrgUnitButton.setEnabled(enabled);
        importFromJSONButton.setEnabled(enabled);

        if (enabled) {
            if (identityService.countPersons() == 0) {
                importFromPersonsButton.setEnabled(false);
            }
            if (selectedOrgUnits.size() == 0) {
                deleteOrgUnitsButton.setEnabled(false);
            }
        } else {
            importFromPersonsButton.setEnabled(enabled);
        }
    }
}