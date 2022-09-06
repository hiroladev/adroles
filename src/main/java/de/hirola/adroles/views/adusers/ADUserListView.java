package de.hirola.adroles.views.adusers;

import com.google.common.eventbus.Subscribe;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
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
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import de.hirola.adroles.Global;
import de.hirola.adroles.data.entity.ADUser;
import de.hirola.adroles.service.IdentityService;
import de.hirola.adroles.util.ServiceEvent;
import de.hirola.adroles.util.ServiceResult;
import de.hirola.adroles.views.MainLayout;
import de.hirola.adroles.views.NotificationPopUp;
import de.hirola.adroles.views.ProgressModalDialog;
import de.hirola.adroles.views.persons.PersonAssignRoleForm;
import de.hirola.adroles.views.persons.PersonForm;
import de.hirola.adroles.views.persons.PersonListView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.annotation.security.PermitAll;
import java.util.ArrayList;
import java.util.List;

@Route(value="ad-user", layout = MainLayout.class)
@PageTitle("AD-Users | AD-Roles")
@PermitAll
public class ADUserListView extends VerticalLayout {
    private final Logger logger = LoggerFactory.getLogger(ADUserListView.class);
    private final IdentityService identityService;
    private final List<ADUser> selectedADUsers = new ArrayList<>();
    private ProgressModalDialog progressModalDialog;
    private ADUserForm adUserForm;
    private PersonAssignRoleForm assignRoleForm;
    private final Grid<ADUser> grid = new Grid<>(ADUser.class, false);
    private TextField filterTextField;
    private Button addADUserButton, updateButton, deleteADUsersButton;

    public ADUserListView(IdentityService identityService) {
        this.identityService = identityService;
        addClassName("ad-user-list-view");
        setSizeFull();
        addComponents();
        updateList();
        closeADUserForm();
        closeAssignRolesForm();
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            identityService.register(this, authentication.getName());
        } catch (RuntimeException exception) {
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
        filterTextField.addValueChangeListener(event -> updateList());

        addADUserButton = new Button(new Icon(VaadinIcon.PLUS));
        addADUserButton.addThemeVariants(ButtonVariant.LUMO_ICON);
        addADUserButton.getElement().setAttribute("aria-label", getTranslation("addADUser"));
        addADUserButton.setWidth(Global.Component.DEFAULT_ICON_BUTTON_WIDTH);
        addADUserButton.addClickListener(click -> addADUser());

        deleteADUsersButton = new Button(new Icon(VaadinIcon.MINUS));
        deleteADUsersButton.addThemeVariants(ButtonVariant.LUMO_ICON);
        deleteADUsersButton.getElement().setAttribute("aria-label", getTranslation("deleteADUsers"));
        deleteADUsersButton.setWidth(Global.Component.DEFAULT_ICON_BUTTON_WIDTH);
        deleteADUsersButton.addClickListener(click -> deleteADUsers());

        //TODO: enable / disable import by config
        updateButton = new Button(getTranslation("updateFromActiveDirectory"));
        updateButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        updateButton.setWidth(Global.Component.DEFAULT_BUTTON_WIDTH);
        updateButton.addClickListener(click -> importADUsers());

        HorizontalLayout toolbar = new HorizontalLayout(filterTextField, addADUserButton, deleteADUsersButton, updateButton);
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
        dialog.setWidth(Global.Component.DEFAULT_DIALOG_WIDTH);
        dialog.setHeaderTitle(getTranslation("question.updateData"));

        TextArea messageArea = new TextArea();
        messageArea.setWidthFull();
        messageArea.setValue(getTranslation("updateADUsersFromActiveDirectory.dialog.message"));

        Button okButton = new Button("Ok", clickEvent -> {
            progressModalDialog = new ProgressModalDialog(
                    "update",
                    "import.running.message",
                    "import.running.subMessage");
            progressModalDialog.open();
            new Thread(identityService::updatePersonsFromAD).start();
            dialog.close();
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
        assignRoleForm.setData(event.getPerson(), identityService.findAllRoles(null, null), null);
        addClassName("editing");
    }

    private void saveAssignedRoles(PersonAssignRoleForm.SaveEvent event) {
        identityService.savePerson(event.getPerson());
        updateList();
    }

    private void updateList() {
        List<ADUser> filteredADUsers = identityService.findAllADUsers(filterTextField.getValue());
        grid.setItems(filteredADUsers);
        grid.deselectAll();
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
        assignRoleForm.setData(null, null, null);
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
            updateButton.setEnabled(false);
        } else {
            updateButton.setEnabled(enabled);
        }
    }

    private boolean matchesTerm(String value, String searchTerm) {
        return value.toLowerCase().contains(searchTerm.toLowerCase());
    }
}
