package de.hirola.adroles.views;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import de.hirola.adroles.Global;
import de.hirola.adroles.data.entity.RoleResource;
import de.hirola.adroles.service.IdentityService;

import javax.annotation.security.PermitAll;

@Route(value = "first-steps", layout = MainLayout.class) // value = "" -> start page
@PageTitle("First steps | AD-Roles")
@PermitAll
public class FirstStepsView extends VerticalLayout {
    private final IdentityService identityService;

    public FirstStepsView(IdentityService identityService) {
        this.identityService = identityService;
        addClassName("first-steps-view");
        setSizeFull();
        addComponents();
    }

    private void addComponents() {
        TextField personsCountLabel = new TextField(getTranslation("persons.sum"));
        personsCountLabel.setWidth(Global.Component.DEFAULT_TEXT_FIELD_WIDTH);
        personsCountLabel.setReadOnly(true);
        personsCountLabel.setValue(String.valueOf(identityService.countPersons()));

        TextField orgUnitsCountLabel = new TextField(getTranslation("org.sum"));
        orgUnitsCountLabel.setWidth(Global.Component.DEFAULT_TEXT_FIELD_WIDTH);
        orgUnitsCountLabel.setReadOnly(true);
        RoleResource roleResource = identityService.getRoleResource(Global.ROLE_RESOURCE.ORG_ROLE);
        if (roleResource != null) {
            orgUnitsCountLabel.setValue(String.valueOf(identityService.countRoles(roleResource)));
        } else {
            orgUnitsCountLabel.setValue(getTranslation("notDetermined"));
        }

        TextField rolesCountLabel = new TextField(getTranslation("role.sum"));
        rolesCountLabel.setWidth(Global.Component.DEFAULT_TEXT_FIELD_WIDTH);
        rolesCountLabel.setReadOnly(true);
        rolesCountLabel.setValue(String.valueOf(identityService.countRoles(null)));

        TextField adUsersCountLabel = new TextField(getTranslation("adUsers.sum"));
        adUsersCountLabel.setWidth(Global.Component.DEFAULT_TEXT_FIELD_WIDTH);
        adUsersCountLabel.setReadOnly(true);
        adUsersCountLabel.setValue(String.valueOf(identityService.countADUsers()));

        TextField adGroupsCountLabel = new TextField(getTranslation("adGroups.sum"));
        adGroupsCountLabel.setWidth(Global.Component.DEFAULT_TEXT_FIELD_WIDTH);
        adGroupsCountLabel.setReadOnly(true);
        adGroupsCountLabel.setValue(String.valueOf(identityService.countADGroups()));

        // administrative groups
        long countAdminGroups = identityService.countAdminGroups();
        TextField adminGroupCountLabel = new TextField(getTranslation("adAdminGroups.sum"));
        adminGroupCountLabel.setWidth(Global.Component.DEFAULT_TEXT_FIELD_WIDTH);
        adminGroupCountLabel.setReadOnly(true);
        adminGroupCountLabel.setValue(String.valueOf(countAdminGroups));
        if (countAdminGroups > 0) {
            adminGroupCountLabel.getElement().getStyle().set("color", "red");
            adminGroupCountLabel.getElement().getStyle().set("-webkit-text-fill-color", "red");
        }

        // password never expires
        long countPasswordNeverExpires = identityService.countPasswordNotExpires();
        TextField passwordNeverExpiresCountLabel = new TextField(getTranslation("passwordNeverExpires.sum"));
        passwordNeverExpiresCountLabel.setWidth(Global.Component.DEFAULT_TEXT_FIELD_WIDTH);
        passwordNeverExpiresCountLabel.setReadOnly(true);
        passwordNeverExpiresCountLabel.setValue(String.valueOf(countPasswordNeverExpires));
        if (countPasswordNeverExpires > 0) {
            passwordNeverExpiresCountLabel.getElement().getStyle().set("color", "red");
            passwordNeverExpiresCountLabel.getElement().getStyle().set("-webkit-text-fill-color", "red");
        }

        VerticalLayout labelLayout = new VerticalLayout(personsCountLabel, orgUnitsCountLabel, rolesCountLabel,
                adUsersCountLabel, adGroupsCountLabel, adminGroupCountLabel, passwordNeverExpiresCountLabel);
        labelLayout.addClassName("toolbar");
        labelLayout.setPadding(true);

        add(labelLayout);
    }
}