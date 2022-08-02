package de.hirola.adroles.views.adgroups;

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
import de.hirola.adroles.views.MainLayout;
import de.hirola.adroles.views.NotificationPopUp;
import de.hirola.adroles.views.persons.PersonAssignRoleForm;
import de.hirola.adroles.views.persons.PersonForm;

import javax.annotation.security.PermitAll;
import java.util.ArrayList;
import java.util.List;

@Route(value="ad-group", layout = MainLayout.class)
@PageTitle("AD-Groups | AD-Roles")
@PermitAll
public class ADGroupListView extends VerticalLayout {
    private final IdentityService identityService;
    private final List<ADGroup> selectedADGroups = new ArrayList<>();
    private ADGroupForm adGroupForm;
    private PersonAssignRoleForm assignRoleForm;
    private final Grid<ADGroup> grid = new Grid<>(ADGroup.class, false);
    private TextField filterTextField;
    private Button addADGroupButton, importButton, deleteADGroupsButton;

    public ADGroupListView(IdentityService identityService) {
        this.identityService = identityService;
        addClassName("ad-group-list-view");
        setSizeFull();
        addComponents();
        updateList();
        closeADGroupForm();
        closeAssignRolesForm();
    }

    private void addComponents() {

        filterTextField = new TextField();
        filterTextField.setPlaceholder(getTranslation("searchFilter"));
        filterTextField.setClearButtonVisible(true);
        filterTextField.setValueChangeMode(ValueChangeMode.LAZY);
        filterTextField.addValueChangeListener(event -> updateList());

        addADGroupButton = new Button(new Icon(VaadinIcon.PLUS));
        addADGroupButton.addThemeVariants(ButtonVariant.LUMO_ICON);
        addADGroupButton.getElement().setAttribute("aria-label", getTranslation("adGroup"));
        addADGroupButton.setWidth(Global.Component.DEFAULT_ICON_BUTTON_WIDTH);
        addADGroupButton.addClickListener(click -> addADgroup());

        deleteADGroupsButton = new Button(new Icon(VaadinIcon.MINUS));
        deleteADGroupsButton.addThemeVariants(ButtonVariant.LUMO_ICON);
        deleteADGroupsButton.getElement().setAttribute("aria-label", getTranslation("deleteADGroups"));
        deleteADGroupsButton.setWidth(Global.Component.DEFAULT_ICON_BUTTON_WIDTH);
        deleteADGroupsButton.addClickListener(click -> deleteADGroups());

        //TODO: enable / disable import by config
        importButton = new Button(getTranslation("importFromActiveDirectory"));
        importButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        importButton.setWidth(Global.Component.DEFAULT_BUTTON_WIDTH);
        importButton.addClickListener(click -> importADGroups());

        HorizontalLayout toolbar = new HorizontalLayout(filterTextField, addADGroupButton, deleteADGroupsButton, importButton);
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
        adGroupForm.addListener(ADGroupForm.DeleteEvent.class, this::deleteADGroup);
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
        if (identityService.countADGroups() > 0) {
            // data can be override
            dialog.setHeaderTitle(getTranslation("question.updateData"));

            TextArea messageArea = new TextArea();
            messageArea.setWidthFull();
            messageArea.setValue(getTranslation("adGroup.importFromActiveDirectory.dialog.message"));

            Button okButton = new Button("Ok", clickEvent -> {
                dialog.close();
                if (!identityService.importGroupsFromAD()) {
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
            if (!identityService.importGroupsFromAD()) {
                NotificationPopUp.show(NotificationPopUp.ERROR, getTranslation("error.import"));
            }
            updateList();
        }
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

    private void deleteADGroup(ADGroupForm.DeleteEvent event) {
        identityService.deleteADGroup(event.getAdGroup());
        updateList();
        closeADGroupForm();
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
        assignRoleForm.setData(event.getPerson(), identityService.findAllRoles(null, null));
        addClassName("editing");
    }

    private void saveAssignedRoles(PersonAssignRoleForm.SaveEvent event) {
        identityService.savePerson(event.getPerson());
        updateList();
    }

    private void updateList() {
        List<ADGroup> filteredADGroups = identityService.findAllADGroups(filterTextField.getValue());
        grid.setItems(filteredADGroups);
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
        assignRoleForm.setData(null, null);
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
            importButton.setEnabled(false);
        } else {
            importButton.setEnabled(enabled);
        }
    }

    private boolean matchesTerm(String value, String searchTerm) {
        return value.toLowerCase().contains(searchTerm.toLowerCase());
    }
}
