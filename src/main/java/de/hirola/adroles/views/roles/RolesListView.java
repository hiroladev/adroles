package de.hirola.adroles.views.roles;

import com.google.common.eventbus.Subscribe;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.contextmenu.GridContextMenu;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.QuerySortOrder;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import de.hirola.adroles.Global;
import de.hirola.adroles.data.entity.Role;
import de.hirola.adroles.data.entity.RoleResource;
import de.hirola.adroles.service.IdentityService;
import de.hirola.adroles.util.ServiceEvent;
import de.hirola.adroles.util.ServiceResult;
import de.hirola.adroles.views.MainLayout;
import de.hirola.adroles.views.NotificationPopUp;
import de.hirola.adroles.views.ProgressModalDialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.annotation.security.PermitAll;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.stream.Stream;

@Route(value="roles", layout = MainLayout.class)
@PageTitle("Roles Overview | AD-Roles")
@PermitAll
public class RolesListView extends VerticalLayout {
    private final Logger logger = LoggerFactory.getLogger(RolesListView.class);
    private final Hashtable<String, RoleResource> roleResourceList = new Hashtable<>();
    private ProgressModalDialog progressModalDialog;
    private RoleContextMenu contextMenu;
    private RoleForm roleForm;
    private RoleAssignPersonForm assignPersonForm;
    private RoleAssignADUserForm assignADUserForm;
    private RoleAssignADGroupForm assignADGroupForm;
    private final List<Role> selectedRoles = new ArrayList<>();
    private final Grid<Role> grid = new Grid<>(Role.class, false);
    private TextField filterTextField;
    private final IdentityService identityService;

    private Button addRoleButton, updateButton, importFromJSONButton, deleteRolesButton;

    public RolesListView(IdentityService identityService) {
        this.identityService = identityService;
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            identityService.register(this, authentication.getName());
        } catch (RuntimeException exception) {
            logger.debug("Could not determine currently user.", exception);
        }
        loadAvailableRoleResources();
        addClassName("role-list-view");
        setSizeFull();
        addComponents();
        updateList();
        enableComponents(true);
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            identityService.register(this, authentication.getName());
        } catch (RuntimeException exception) {
            identityService.register(this, null);
            logger.debug("Could not determine currently user.", exception);
        }

    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        super.onDetach(detachEvent);
        identityService.unregister(this);
    }

    @Subscribe
    public void onServiceEvent(ServiceEvent event) {
        if (getUI().isPresent()) {
            getUI().get().access(() -> {
                if (progressModalDialog != null) {
                    progressModalDialog.close();
                    ServiceResult serviceResult = event.getServiceResult();
                    if (serviceResult.operationSuccessful) {
                        NotificationPopUp.show(NotificationPopUp.INFO,
                                getTranslation("import.successful"), serviceResult.resultMessage);
                    } else {
                        NotificationPopUp.show(NotificationPopUp.ERROR,
                                getTranslation("error.import"), serviceResult.resultMessage);
                    }
                    updateList();
                }
            });
        }
    }

    private void addComponents() {

        filterTextField = new TextField();
        filterTextField.setPlaceholder(getTranslation("searchFilter"));
        filterTextField.setClearButtonVisible(true);
        filterTextField.setValueChangeMode(ValueChangeMode.LAZY);
        filterTextField.addValueChangeListener(e -> updateList());

        addRoleButton = new Button(new Icon(VaadinIcon.PLUS));
        addRoleButton.addThemeVariants(ButtonVariant.LUMO_ICON);
        addRoleButton.getElement().setAttribute("aria-label", getTranslation("addRole"));
        addRoleButton.setWidth(Global.Component.DEFAULT_ICON_BUTTON_WIDTH);
        addRoleButton.addClickListener(click -> addRole());

        deleteRolesButton = new Button(new Icon(VaadinIcon.MINUS));
        deleteRolesButton.addThemeVariants(ButtonVariant.LUMO_ICON);
        deleteRolesButton.getElement().setAttribute("aria-label", getTranslation("deleteRoles"));
        deleteRolesButton.setWidth(Global.Component.DEFAULT_ICON_BUTTON_WIDTH);
        deleteRolesButton.addClickListener(click -> deleteRoles());

        updateButton = new Button(new Icon(VaadinIcon.INSERT));
        updateButton.addThemeVariants(ButtonVariant.LUMO_ICON);
        updateButton.setText(getTranslation("updateFromGroups"));
        updateButton.setIconAfterText(true);
        updateButton.getElement().setAttribute("aria-label", getTranslation("updateFromGroups"));
        updateButton.setWidth(Global.Component.DEFAULT_BUTTON_WIDTH);
        updateButton.addClickListener(click -> importRolesFromGroups());

        // import Roles from JSON
        importFromJSONButton = new Button(new Icon(VaadinIcon.INSERT));
        importFromJSONButton.addThemeVariants(ButtonVariant.LUMO_ICON);
        importFromJSONButton.setText(getTranslation("importFromJSON"));
        importFromJSONButton.setIconAfterText(true);
        importFromJSONButton.getElement().setAttribute("aria-label", getTranslation("importFromJSON"));
        importFromJSONButton.setWidth(Global.Component.DEFAULT_BUTTON_WIDTH);
        importFromJSONButton.addClickListener(click -> importRolesFromJSON());

        grid.addClassNames("roles-grid");
        grid.setSizeFull();
        grid.addColumn(new ComponentRenderer<>(role -> {
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
        grid.addColumn(Role::getName).setHeader(getTranslation("name"))
                .setSortable(true)
                .setKey(Global.Component.FOOTER_COLUMN_KEY)
                .setFooter(String.format(getTranslation("role.sum") + ": %s", identityService.countRoles(null)));
        grid.addColumn(Role::getDescription).setHeader(getTranslation("description"))
                .setWidth(Global.Component.DEFAULT_COLUMN_WIDTH)
                .setSortable(true);
        grid.addColumn(role -> role.isAdminRole() ? getTranslation("yes") : getTranslation("no"))
                .setHeader(getTranslation("adminRole"))
                .setSortable(true);
        grid.getColumns().forEach(col -> {
            if (col.getWidth() == null) {
                col.setAutoWidth(true);
            }
        });
        grid.setSelectionMode(Grid.SelectionMode.MULTI);
        grid.addItemClickListener(event -> editRole(event.getItem()));
        grid.addSelectionListener(selection -> {
            selectedRoles.clear();
            if (selection.getAllSelectedItems().isEmpty()) {
                deleteRolesButton.setEnabled(false);
                contextMenu.setEnabled(false);
            } else {
                selectedRoles.addAll(selection.getAllSelectedItems());
                deleteRolesButton.setEnabled(true);
                contextMenu.setEnabled(true);
            }
        });

        roleForm = new RoleForm(roleResourceList);
        roleForm.setWidthFull();
        roleForm.addListener(RoleForm.SaveEvent.class, this::saveRole);
        roleForm.addListener(RoleForm.AssignPersonsEvent.class, this::assignPersons);
        roleForm.addListener(RoleForm.AssignADUsersEvent.class, this::assignADUsers);
        roleForm.addListener(RoleForm.AssignADGroupsEvent.class, this::assignADGroups);
        roleForm.addListener(RoleForm.CloseEvent.class, event -> closeRoleForm());
        roleForm.setVisible(false);

        assignPersonForm = new RoleAssignPersonForm(identityService);
        assignPersonForm.setWidthFull();
        assignPersonForm.addListener(RoleAssignPersonForm.SaveEvent.class, this::saveAssignedPersons);
        assignPersonForm.addListener(RoleAssignPersonForm.CloseEvent.class, event -> closeAssignPersonsForm(event.getRole()));
        assignPersonForm.setVisible(false);

        assignADUserForm = new RoleAssignADUserForm(identityService);
        assignADUserForm.setWidthFull();
        assignADUserForm.addListener(RoleAssignADUserForm.SaveEvent.class, this::saveAssignedADUsers);
        assignADUserForm.addListener(RoleAssignADUserForm.CloseEvent.class, event -> closeAssignADUsersForm(event.getRole()));
        assignADUserForm.setVisible(false);

        assignADGroupForm = new RoleAssignADGroupForm(identityService);
        assignADGroupForm.setWidthFull();
        assignADGroupForm.addListener(RoleAssignADGroupForm.SaveEvent.class, this::saveAssignedADGroups);
        assignADGroupForm.addListener(RoleAssignADGroupForm.CloseEvent.class, event -> closeAssignADGroupsForm(event.getRole()));
        assignADGroupForm.setVisible(false);

        // context menu to change the role resource of selected roles
        contextMenu = new RoleContextMenu(grid, this);
        contextMenu.setEnabled(false);

        FlexLayout content = new FlexLayout(grid, contextMenu, roleForm,
                assignPersonForm, assignADUserForm, assignADGroupForm);
        content.setFlexGrow(2.0, grid);
        content.setFlexGrow(1.0, roleForm, assignPersonForm, assignADUserForm, assignADGroupForm);
        content.setFlexShrink((double) 0, roleForm, assignPersonForm, assignADUserForm, assignADGroupForm);
        content.addClassNames("content", "gap-m");
        content.setSizeFull();

        HorizontalLayout toolbar = new HorizontalLayout(filterTextField, addRoleButton, deleteRolesButton,
                updateButton, importFromJSONButton);
        toolbar.addClassName("toolbar");

        add(toolbar, content);
    }

    private void importRolesFromGroups() {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader(getTranslation("question.updateData"));
        dialog.setText(getTranslation("updateRolesFromGroups.dialog.message"));
        dialog.setCancelable(true);
        dialog.addCancelListener(clickEvent -> dialog.close());
        dialog.setRejectable(false);
        dialog.setConfirmText("Ok");
        dialog.addConfirmListener(clickEvent -> {
            if (progressModalDialog == null) {
                progressModalDialog = new ProgressModalDialog();
            }
            progressModalDialog.open("update",
                    "import.running.message",
                    "import.running.subMessage");
            new Thread(identityService::updateRolesFromGroups).start();
            dialog.close();
        });
        dialog.open();
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
        closeRoleForm();
    }

    private void editRole(Role role) {
        if (role == null) {
            closeRoleForm();
        } else {
            enableComponents(false);
            roleForm.setRole(role);
            roleForm.setVisible(true);
            addClassName("editing");
        }
    }

    private void deleteRoles() {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader(getTranslation("question.delete"));
        dialog.setCancelable(true);
        dialog.addCancelListener(clickEvent -> {
            grid.deselectAll();
            dialog.close();
        });
        dialog.setRejectable(false);
        dialog.setConfirmText("Ok");
        dialog.addConfirmListener(clickEvent -> {
            identityService.deleteRoles(selectedRoles);
            updateList();
            selectedRoles.clear();
            deleteRolesButton.setEnabled(false);
            dialog.close();
        });
        dialog.open();
    }

    private void assignPersons(RoleForm.AssignPersonsEvent event) {
        closeRoleForm();
        enableComponents(false);
        assignPersonForm.setData(event.getRole(), identityService.findAllPersons(null));
        assignPersonForm.setVisible(true);
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

    private void assignADUsers(RoleForm.AssignADUsersEvent event) {
        closeRoleForm();
        enableComponents(false);
        assignADUserForm.setData(event.getRole(), identityService.findAllManageableADUsers());
        assignADUserForm.setVisible(true);
        addClassName("editing-assign-ad-users-form");
    }

    private void saveAssignedADUsers(RoleAssignADUserForm.SaveEvent event) {
        Role role = event.getRole();
        if (identityService.saveRole(role)) {
            closeAssignADUsersForm(role);
            updateList();
        } else {
            NotificationPopUp.show(NotificationPopUp.ERROR, getTranslation("error.save"));
        }
    }

    private void assignADGroups(RoleForm.AssignADGroupsEvent event) {
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
        List<Role> filteredRoles = identityService.findAllRoles(filterTextField.getValue(), null);
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
            } else {
                roleResourceList.put(getTranslation("standard"), roleResource);
            }
        }
    }

    private void updateResourceOfSelectedRoles(int type) {
        if (!selectedRoles.isEmpty()) {
            RoleResource roleResource = identityService.getRoleResource(type);
            if (roleResource != null) {
                for (Role role : selectedRoles) {
                    role.setRoleResource(roleResource);
                    if (!identityService.saveRole(role)) {
                        NotificationPopUp.show(NotificationPopUp.ERROR,
                                getTranslation("error.save"));
                        break;
                    }
                }
            } else {
                NotificationPopUp.show(NotificationPopUp.ERROR,
                        getTranslation("error.changeRoleResource"),
                        getTranslation("error.roleResource.isNull"));
            }
        }
        updateList();
    }

    private void closeRoleForm() {
        roleForm.setRole(null);
        enableComponents(true);
        roleForm.setVisible(false);
        removeClassName("editing");
    }

    private void closeAssignPersonsForm(Role role) {
        assignPersonForm.setData(null, null);
        assignPersonForm.setVisible(false);
        roleForm.setRole(role);
        roleForm.setVisible(true);
        removeClassName("editing-assign-persons-form");
    }

    private void closeAssignADUsersForm(Role role) {
        assignADUserForm.setData(null, null);
        assignADUserForm.setVisible(false);
        roleForm.setRole(role);
        roleForm.setVisible(true);
        removeClassName("editing-assign-ad-users-form");
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
        addRoleButton.setEnabled(enabled);
        importFromJSONButton.setEnabled(enabled);
        if (selectedRoles.isEmpty()) {
            deleteRolesButton.setEnabled(false);
        } else {
            deleteRolesButton.setEnabled(enabled);
        }
        if (identityService.countADGroups() == 0L) {
            updateButton.setEnabled(false);
        } else {
            updateButton.setEnabled(enabled);
        }
    }

    private static class RoleContextMenu extends GridContextMenu<Role> {

        private final RolesListView listView;
        RoleContextMenu(Grid<Role> target, RolesListView listView) {
            super(target);
            this.listView = listView;

            addItem(getTranslation("changeRoleResource")); // info

            add(new Hr());

            addItem(getTranslation("standard"), event -> event.getItem().ifPresent(role ->
                    showDialogForType(Global.ROLE_RESOURCE.DEFAULT_ROLE)));

            addItem(getTranslation("project"), event -> event.getItem().ifPresent(role ->
                    showDialogForType(Global.ROLE_RESOURCE.PROJECT_ROLE)));

            addItem(getTranslation("fileShare"), event -> event.getItem().ifPresent(role ->
                    showDialogForType(Global.ROLE_RESOURCE.FILE_SHARE_ROLE)));

            addItem(getTranslation("emailResource"), event -> event.getItem().ifPresent(role ->
                    showDialogForType(Global.ROLE_RESOURCE.EMAIL_RESOURCE_ROLE)));
        }

        private void showDialogForType(int type) {
            ConfirmDialog dialog = new ConfirmDialog();
            dialog.setHeader(getTranslation("question.roleResourceChange"));
            dialog.setCancelable(true);
            dialog.addCancelListener(clickEvent -> dialog.close());
            dialog.setRejectable(false);
            dialog.setConfirmText("Ok");
            dialog.addConfirmListener(clickEvent -> {
                listView.updateResourceOfSelectedRoles(type);
                dialog.close();
            });
            dialog.open();
        }
    }
}
