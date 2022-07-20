package de.hirola.adroles.views.roles;

import com.vaadin.flow.component.Unit;
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
import de.hirola.adroles.data.service.RolesService;
import de.hirola.adroles.views.MainLayout;

import javax.annotation.security.PermitAll;

@Route(value="roles", layout = MainLayout.class)
@PageTitle("Roles Overview | AD-Roles")
@PermitAll
public class RolesListView extends VerticalLayout {
    private RoleForm form;
    private final Grid<Role> grid = new Grid<>(Role.class, false);
    private TextField filterTextField;
    private final RolesService service;

    public RolesListView(RolesService service) {
        this.service = service;
        addClassName("list-view");
        setSizeFull();
        addComponents();
        updateList();
        closeForm();
    }

    private void addComponents() {

        grid.addClassNames("roles-grid");
        grid.setSizeFull();
        grid.addColumn(Role::getName).setHeader(getTranslation("name"))
                .setFooter(String.format(getTranslation("roles.sum") + ": %s", service.countRoles()));
        grid.addColumn(Role::getDescription).setHeader(getTranslation("description"));
        grid.addColumn(role -> role.isAdminRole() ? getTranslation("yes") : getTranslation("no"))
                .setHeader(getTranslation("roles.adminRole"));
        grid.getColumns().forEach(col -> col.setAutoWidth(true));
        grid.asSingleSelect().addValueChangeListener(event -> editRole(event.getValue()));

        form = new RoleForm();
        form.setWidth("30em");
        form.addListener(RoleForm.SaveEvent.class, this::saveRole);
        form.addListener(RoleForm.AddPersonsEvent.class, this::addPersons);
        form.addListener(RoleForm.AddADGroupsEvent.class, this::addADGroups);
        form.addListener(RoleForm.DeleteEvent.class, this::deleteRole);
        form.addListener(RoleForm.CloseEvent.class, event -> closeForm());

        FlexLayout content = new FlexLayout(grid, form);
        content.setFlexGrow(2, grid);
        content.setFlexGrow(1, form);
        content.setFlexShrink(0, form);
        content.addClassNames("content", "gap-m");
        content.setSizeFull();

        filterTextField = new TextField();
        filterTextField.setPlaceholder(getTranslation("rolesListView.searchFilter"));
        filterTextField.setClearButtonVisible(true);
        filterTextField.setValueChangeMode(ValueChangeMode.LAZY);
        filterTextField.addValueChangeListener(e -> updateList());

        Button addPersonButton = new Button(getTranslation("rolesListView.addRole"));
        addPersonButton.setWidth(Global.DEFAULT_BUTTON_WIDTH, Unit.PIXELS);
        addPersonButton.setWidth(Global.DEFAULT_BUTTON_WIDTH, Unit.PIXELS);
        addPersonButton.addClickListener(click -> addRole());

        // import Roles from JSON
        Button importButton = new Button(getTranslation("rolesListView.importFromJSON"));
        importButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        importButton.addClickListener(click -> {
            if (service.countRoles() > 0) {
                // data can be override
                Dialog dialog = new Dialog();
                dialog.setHeaderTitle(getTranslation("rolesListView.importFromJSON.dialog.title"));

                TextArea messageArea =
                        new TextArea(getTranslation("rolesListView.importFromJSON.dialog.message"));

                Button okButton = new Button("Ok", (clickEvent) -> importRoles());
                okButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);
                okButton.getStyle().set("margin-right", "auto");

                Button cancelButton = new Button(getTranslation("cancel"), (clickEvent) -> dialog.close());
                cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

                dialog.add(messageArea);
                dialog.getFooter().add(okButton);
                dialog.getFooter().add(cancelButton);

                dialog.open();
            } else {
                importRoles();
            }
        });

        HorizontalLayout toolbar = new HorizontalLayout(filterTextField, addPersonButton, importButton);
        toolbar.addClassName("toolbar");

        add(toolbar, content);
    }

    private void addRole() {
        grid.asSingleSelect().clear();
        editRole(new Role());
    }

    private void importRoles() {
        service.importRolesFromJSON();
        updateList();
    }

    private void saveRole(RoleForm.SaveEvent event) {
        service.saveRole(event.getRole());
        updateList();
        closeForm();
    }

    private void addPersons(RoleForm.AddPersonsEvent event) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Add persons");
        dialog.open();
    }

    private void addADGroups(RoleForm.AddADGroupsEvent event) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Add groups");
        dialog.open();
    }

    private void deleteRole(RoleForm.DeleteEvent event) {
        service.deleteRole(event.getRole());
        updateList();
        closeForm();
    }

    public void editRole(Role role) {
        if (role == null) {
            closeForm();
        } else {
            form.setRole(role);
            form.setVisible(true);
            addClassName("editing");
        }
    }

    private void updateList() {
        grid.setItems(service.findAllRoles(filterTextField.getValue()));
    }

    private void closeForm() {
        form.setRole(null);
        form.setVisible(false);
        removeClassName("editing");
    }


}
