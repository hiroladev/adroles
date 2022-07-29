package de.hirola.adroles.views.adusers;

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
import de.hirola.adroles.data.entity.ADUser;
import de.hirola.adroles.service.IdentityService;
import de.hirola.adroles.views.MainLayout;
import de.hirola.adroles.views.NotificationPopUp;
import de.hirola.adroles.views.persons.PersonAssignRoleForm;
import de.hirola.adroles.views.persons.PersonForm;

import javax.annotation.security.PermitAll;
import java.util.ArrayList;
import java.util.List;

@Route(value="ad-user", layout = MainLayout.class)
@PageTitle("AD-Users | AD-Roles")
@PermitAll
public class ADUserListView extends VerticalLayout {
    private final IdentityService identityService;
    private final List<ADUser> selectedADUsers = new ArrayList<>();
    private ADUserForm adUserForm;
    private PersonAssignRoleForm assignRoleForm;
    private final Grid<ADUser> grid = new Grid<>(ADUser.class, false);
    private TextField filterTextField;
    private Button addADUserButton, importButton, deleteADUsersButton;

    public ADUserListView(IdentityService identityService) {
        this.identityService = identityService;
        addClassName("ad-user-list-view");
        setSizeFull();
        addComponents();
        updateList();
        closeADUserForm();
        closeAssignRolesForm();
    }

    private void addComponents() {

        filterTextField = new TextField();
        filterTextField.setPlaceholder(getTranslation("searchFilter"));
        filterTextField.setClearButtonVisible(true);
        filterTextField.setValueChangeMode(ValueChangeMode.LAZY);
        filterTextField.addValueChangeListener(event -> updateList());

        addADUserButton = new Button(getTranslation("addADUser"));
        addADUserButton.setWidth(Global.Component.DEFAULT_BUTTON_WIDTH);
        addADUserButton.addClickListener(click -> addADUser());

        deleteADUsersButton = new Button(getTranslation("deleteADUsers"));
        deleteADUsersButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);
        deleteADUsersButton.setWidth(Global.Component.DEFAULT_BUTTON_WIDTH);
        deleteADUsersButton.addClickListener(click -> deleteADUsers());

        //TODO: enable / disable import by config
        importButton = new Button(getTranslation("importFromActiveDirectory"));
        importButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        importButton.setWidth(Global.Component.DEFAULT_BUTTON_WIDTH);
        importButton.addClickListener(click -> importADUsers());

        HorizontalLayout toolbar = new HorizontalLayout(filterTextField, addADUserButton, deleteADUsersButton, importButton);
        toolbar.addClassName("toolbar");

        grid.addClassNames("ad-user-grid");
        grid.setSizeFull();
        grid.addColumn(ADUser::getLogonName).setHeader(getTranslation("logonName"))
                .setKey(Global.Component.FOOTER_COLUMN_KEY)
                .setSortable(true)
                .setFooter(String.format(getTranslation("adUsers.sum") + ": %s", identityService.countADUsers()));
        grid.addColumn(adUser -> adUser.isEnabled() ? getTranslation("enabled") : getTranslation("disabled"))
                .setHeader(getTranslation("status"))
                .setSortable(true);
        grid.addColumn(adUser -> adUser.isPasswordExpires() ? getTranslation("yes") : getTranslation("no"))
                .setHeader(getTranslation("passwordExpires"))
                .setSortable(true);
        grid.addColumn(adUser -> adUser.isAdminAccount() ? getTranslation("yes") : getTranslation("no"))
                .setHeader(getTranslation("adminAccount"))
                .setSortable(true);
        grid.addColumn(adUser -> adUser.isServiceAccount() ? getTranslation("yes") : getTranslation("no"))
                .setHeader(getTranslation("serviceAccount"))
                .setSortable(true);
        grid.getColumns().forEach(col -> {
            String columnKey = col.getKey();
            if (columnKey != null) {
                if (!columnKey.equals(Global.Component.FOOTER_COLUMN_KEY)) {
                    col.setAutoWidth(false);
                    col.setWidth(Global.Component.DEFAULT_COLUMN_WIDTH);
                } else {
                    col.setAutoWidth(true);
                }
            } else {
                col.setAutoWidth(true);
            }
        });
        grid.setSelectionMode(Grid.SelectionMode.MULTI);
        grid.addItemClickListener(event -> editADUser(event.getItem()));
        grid.addSelectionListener(selection -> {
            selectedADUsers.clear();
            if (selection.getAllSelectedItems().size() == 0) {
                deleteADUsersButton.setEnabled(false);
            } else {
                deleteADUsersButton.setEnabled(true);
                selectedADUsers.addAll(selection.getAllSelectedItems());
            }
        });

        adUserForm = new ADUserForm();
        adUserForm.setWidthFull();
        adUserForm.addListener(ADUserForm.SaveEvent.class, this::saveADUser);
        adUserForm.addListener(ADUserForm.DeleteEvent.class, this::deleteADuser);
        adUserForm.addListener(ADUserForm.CloseEvent.class, event -> closeADUserForm());

        assignRoleForm = new PersonAssignRoleForm();
        assignRoleForm.setWidthFull();
        assignRoleForm.addListener(PersonAssignRoleForm.SaveEvent.class, this::saveAssignedRoles);
        assignRoleForm.addListener(PersonAssignRoleForm.CloseEvent.class, event -> closeAssignRolesForm());

        FlexLayout content = new FlexLayout(grid, adUserForm, assignRoleForm);
        content.setFlexGrow(2, grid);
        content.setFlexGrow(1, adUserForm, assignRoleForm);
        content.setFlexShrink(0, adUserForm, assignRoleForm);
        content.addClassNames("content", "gap-m");
        content.setSizeFull();

        add(toolbar, content);
    }

    private void importADUsers() {
        Dialog dialog = new Dialog();
        if (identityService.countPersons() > 0) {
            // data can be override
            dialog.setHeaderTitle(getTranslation("question.updateData"));

            TextArea messageArea = new TextArea();
            messageArea.setWidthFull();
            messageArea.setValue(getTranslation("adUser.importFromActiveDirectory.dialog.message"));

            Button okButton = new Button("Ok", clickEvent -> {
                dialog.close();
                if (!identityService.importUserFromAD(false)) {
                    NotificationPopUp.show(NotificationPopUp.ERROR, getTranslation("error.import"));
                }
                updateList();
            });
            okButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);
            okButton.getStyle().set("margin-right", "auto");

            Button partiallyButton = new Button(getTranslation("question.missing"), clickEvent -> {
                dialog.close();
                NotificationPopUp.show(NotificationPopUp.INFO, getTranslation("not.implemented"));
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
            if (!identityService.importPersonsFromAD(false)) {
                NotificationPopUp.show(NotificationPopUp.ERROR, getTranslation("error.import"));
            }
            updateList();
        }
    }

    private void addADUser() {
        editADUser(new ADUser());
    }

    private void saveADUser(ADUserForm.SaveEvent event) {
        identityService.saveADUser(event.getAdUser());
        closeADUserForm();
        updateList();
    }

    public void editADUser(ADUser adUser) {
        if (adUser == null) {
            closeADUserForm();
        } else {
            enableComponents(false);
            adUserForm.setAdUser(adUser);
            adUserForm.setVisible(true);
            addClassName("editing");
        }
    }

    private void deleteADuser(ADUserForm.DeleteEvent event) {
        identityService.deleteADUser(event.getAdUser());
        updateList();
        closeADUserForm();
    }

    private void deleteADUsers() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(getTranslation("question.delete"));

        Button okButton = new Button("Ok", clickEvent -> {
            identityService.deleteADUsers(selectedADUsers);
            updateList();
            selectedADUsers.clear();
            deleteADUsersButton.setEnabled(false);
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

    private void addRoles(PersonForm.AssignRolesEvent event) {
        closeADUserForm();
        enableComponents(false);
        assignRoleForm.setVisible(true);
        assignRoleForm.setData(event.getPerson(), identityService.findAllRoles(null, null));
        addClassName("editing");
    }

    private void saveAssignedRoles(PersonAssignRoleForm.SaveEvent event) {
        identityService.savePerson(event.getPerson());
        updateList();
    }

    private void updateList() {
        List<ADUser> filteredADUsers = identityService.findAllADUsers(filterTextField.getValue());
        grid.setItems(filteredADUsers);
        grid.getColumnByKey(Global.Component.FOOTER_COLUMN_KEY)
                .setFooter(String.format(getTranslation("adUsers.sum") + ": %s", filteredADUsers.size()));
    }

    private void closeADUserForm() {
        adUserForm.setAdUser(null);
        enableComponents(true);
        adUserForm.setVisible(false);
        removeClassName("editing");
    }

    private void closeAssignRolesForm() {
        assignRoleForm.setData(null, null);
        assignRoleForm.setVisible(false);
        enableComponents(true);
        removeClassName("editing");
    }

    private void enableComponents(boolean enabled) {
        filterTextField.setEnabled(enabled);
        addADUserButton.setEnabled(enabled);
        if (selectedADUsers.size() == 0) {
            deleteADUsersButton.setEnabled(false);
        } else {
            deleteADUsersButton.setEnabled(enabled);
        }
        if (!identityService.isConnected()) {
            importButton.setEnabled(false);
        } else {
            importButton.setEnabled(enabled);
        }
    }

    private boolean matchesTerm(String value, String searchTerm) {
        return value.toLowerCase().contains(searchTerm.toLowerCase());
    }
}
