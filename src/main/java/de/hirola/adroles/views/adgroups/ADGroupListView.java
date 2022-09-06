package de.hirola.adroles.views.adgroups;

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
import de.hirola.adroles.data.entity.ADGroup;
import de.hirola.adroles.service.IdentityService;
import de.hirola.adroles.util.ServiceEvent;
import de.hirola.adroles.util.ServiceResult;
import de.hirola.adroles.views.MainLayout;
import de.hirola.adroles.views.NotificationPopUp;
import de.hirola.adroles.views.ProgressModalDialog;
import de.hirola.adroles.views.adusers.ADUserListView;
import de.hirola.adroles.views.persons.PersonAssignRoleForm;
import de.hirola.adroles.views.persons.PersonForm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.annotation.security.PermitAll;
import java.util.ArrayList;
import java.util.List;

@Route(value="ad-group", layout = MainLayout.class)
@PageTitle("AD-Groups | AD-Roles")
@PermitAll
public class ADGroupListView extends VerticalLayout {
    private final Logger logger = LoggerFactory.getLogger(ADGroupListView.class);
    private final IdentityService identityService;
    private final List<ADGroup> selectedADGroups = new ArrayList<>();
    private ProgressModalDialog progressModalDialog;
    private ADGroupForm adGroupForm;
    private PersonAssignRoleForm assignRoleForm;
    private final Grid<ADGroup> grid = new Grid<>(ADGroup.class, false);
    private TextField filterTextField;
    private Button addADGroupButton, updateButton, deleteADGroupsButton;

    public ADGroupListView(IdentityService identityService) {
        this.identityService = identityService;
        addClassName("ad-group-list-view");
        setSizeFull();
        addComponents();
        updateList();
        closeADGroupForm();
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

        addADGroupButton = new Button(new Icon(VaadinIcon.PLUS));
        addADGroupButton.addThemeVariants(ButtonVariant.LUMO_ICON);
        addADGroupButton.getElement().setAttribute("aria-label", getTranslation("addADGroup"));
        addADGroupButton.setWidth(Global.Component.DEFAULT_ICON_BUTTON_WIDTH);
        addADGroupButton.addClickListener(click -> addADgroup());

        deleteADGroupsButton = new Button(new Icon(VaadinIcon.MINUS));
        deleteADGroupsButton.addThemeVariants(ButtonVariant.LUMO_ICON);
        deleteADGroupsButton.getElement().setAttribute("aria-label", getTranslation("deleteADGroups"));
        deleteADGroupsButton.setWidth(Global.Component.DEFAULT_ICON_BUTTON_WIDTH);
        deleteADGroupsButton.addClickListener(click -> deleteADGroups());

        //TODO: enable / disable import by config
        updateButton = new Button(getTranslation("updateFromActiveDirectory"));
        updateButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        updateButton.setWidth(Global.Component.DEFAULT_BUTTON_WIDTH);
        updateButton.addClickListener(click -> importADGroups());

        HorizontalLayout toolbar = new HorizontalLayout(filterTextField, addADGroupButton, deleteADGroupsButton, updateButton);
        toolbar.addClassName("toolbar");

        grid.addClassNames("ad-group-grid");
        grid.setSizeFull();
        grid.addColumn(ADGroup::getName).setHeader(getTranslation("name"))
                .setKey(Global.Component.FOOTER_COLUMN_KEY)
                .setSortable(true)
                .setFooter(String.format(getTranslation("adGroups.sum") + ": %s", identityService.countADGroups()));
        grid.addColumn(ADGroup::getDescription).setHeader(getTranslation("description"))
                .setSortable(true);
        grid.addColumn(adGroup -> adGroup.isAdminGroup() ? getTranslation("yes") : getTranslation("no"))
                .setHeader(getTranslation("adminGroup"))
                .setSortable(true);
        grid.getColumns().forEach(col -> col.setAutoWidth(true));
        grid.setSelectionMode(Grid.SelectionMode.MULTI);
        grid.addItemClickListener(event -> editADGroup(event.getItem()));
        grid.addSelectionListener(selection -> {
            selectedADGroups.clear();
            if (selection.getAllSelectedItems().size() == 0) {
                deleteADGroupsButton.setEnabled(false);
            } else {
                deleteADGroupsButton.setEnabled(true);
                selectedADGroups.addAll(selection.getAllSelectedItems());
            }
        });

        adGroupForm = new ADGroupForm();
        adGroupForm.setWidthFull();
        adGroupForm.addListener(ADGroupForm.SaveEvent.class, this::saveADGroup);
        adGroupForm.addListener(ADGroupForm.CloseEvent.class, event -> closeADGroupForm());

        assignRoleForm = new PersonAssignRoleForm();
        assignRoleForm.setWidthFull();
        assignRoleForm.addListener(PersonAssignRoleForm.SaveEvent.class, this::saveAssignedRoles);
        assignRoleForm.addListener(PersonAssignRoleForm.CloseEvent.class, event -> closeAssignRolesForm());

        FlexLayout content = new FlexLayout(grid, adGroupForm, assignRoleForm);
        content.setFlexGrow(2, grid);
        content.setFlexGrow(1, adGroupForm, assignRoleForm);
        content.setFlexShrink(0, adGroupForm, assignRoleForm);
        content.addClassNames("content", "gap-m");
        content.setSizeFull();

        add(toolbar, content);
    }

    private void importADGroups() {
        Dialog dialog = new Dialog();
        dialog.setWidth(Global.Component.DEFAULT_DIALOG_WIDTH);
        dialog.setHeaderTitle(getTranslation("question.updateData"));

        TextArea messageArea = new TextArea();
        messageArea.setWidthFull();
        messageArea.setValue(getTranslation("adGroup.importFromActiveDirectory.dialog.message"));

        Button okButton = new Button("Ok", clickEvent -> {
            progressModalDialog = new ProgressModalDialog(
                    "update",
                    "import.running.message",
                    "import.running.subMessage");
            progressModalDialog.open();
            new Thread(identityService::updateGroupsFromAD).start();
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

    private void addADgroup() {
        editADGroup(new ADGroup());
    }

    private void saveADGroup(ADGroupForm.SaveEvent event) {
        identityService.saveADGroup(event.getAdGroup());
        closeADGroupForm();
        updateList();
    }

    public void editADGroup(ADGroup adGroup) {
        if (adGroup == null) {
            closeADGroupForm();
        } else {
            enableComponents(false);
            adGroupForm.setAdGroup(adGroup);
            adGroupForm.setVisible(true);
            addClassName("editing");
        }
    }

    private void deleteADGroups() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(getTranslation("question.delete"));

        Button okButton = new Button("Ok", clickEvent -> {
            identityService.deleteADGroups(selectedADGroups);
            updateList();
            selectedADGroups.clear();
            deleteADGroupsButton.setEnabled(false);
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
        closeADGroupForm();
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
        List<ADGroup> filteredADGroups = identityService.findAllADGroups(filterTextField.getValue());
        grid.setItems(filteredADGroups);
        grid.deselectAll();
        grid.getColumnByKey(Global.Component.FOOTER_COLUMN_KEY)
                .setFooter(String.format(getTranslation("adGroups.sum") + ": %s", filteredADGroups.size()));
    }

    private void closeADGroupForm() {
        adGroupForm.setAdGroup(null);
        enableComponents(true);
        adGroupForm.setVisible(false);
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
        addADGroupButton.setEnabled(enabled);
        if (selectedADGroups.size() == 0) {
            deleteADGroupsButton.setEnabled(false);
        } else {
            deleteADGroupsButton.setEnabled(enabled);
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
