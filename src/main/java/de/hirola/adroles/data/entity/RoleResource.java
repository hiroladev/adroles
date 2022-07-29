package de.hirola.adroles.data.entity;

import de.hirola.adroles.data.AbstractEntity;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.validation.constraints.NotEmpty;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Copyright 2022 by Michael Schmidt, Hirola Consulting
 * This software us licensed under the AGPL-3.0 or later.
 *
 * <P>Roles can be used for various permissions, such as for folders and distribution lists.
 * Roles can also represent organizations.</P>
 *
 * @author Michael Schmidt (Hirola)
 * @since v0.1
 */

@Entity
public class RoleResource extends AbstractEntity {
    @NotEmpty
    private String name;
    private String description;
    @NotEmpty
    private String viewClassName;
    @NotEmpty
    String addResourceTranslationKey;
    @NotEmpty
    String deleteResourcesTranslationKey;
    private boolean isOrgResource;
    private boolean isProjectResource;
    private boolean isFileShareResource;

    @OneToMany(mappedBy = "roleResource", fetch= FetchType.EAGER, orphanRemoval = true)
    private Set<Role> roles = new LinkedHashSet<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getViewClassName() {
        return viewClassName;
    }

    public void setViewClassName(String viewClassName) {
        if (viewClassName == null) {
            return;
        }
        this.viewClassName = viewClassName;
    }

    public String getAddResourceTranslationKey() {
        return addResourceTranslationKey;
    }

    public void setAddResourceTranslationKey(String addResourceTranslationKey) {
        if (addResourceTranslationKey == null) {
            return;
        }
        this.addResourceTranslationKey = addResourceTranslationKey;
    }

    public String getDeleteResourcesTranslationKey() {
        return deleteResourcesTranslationKey;
    }

    public void setDeleteResourcesTranslationKey(String deleteResourcesTranslationKey) {
        if (deleteResourcesTranslationKey == null) {
            return;
        }
        this.deleteResourcesTranslationKey = deleteResourcesTranslationKey;
    }

    public void setDefaultResource() {
        isOrgResource = false;
        isProjectResource = false;
        isFileShareResource = false;
    }
    public boolean isOrgResource() {
        return isOrgResource;
    }

    public void setOrgResource(boolean orgResource) {
        isOrgResource = orgResource;
        isProjectResource = false;
        isFileShareResource = false;
    }

    public boolean isProjectResource() {
        return isProjectResource;
    }

    public void setProjectResource(boolean projectResource) {
        isProjectResource = projectResource;
        isOrgResource = false;
        isFileShareResource = false;
    }

    public boolean isFileShareResource() {
        return isFileShareResource;
    }

    public void setFileShareResource(boolean fileShareResource) {
        isFileShareResource = fileShareResource;
        isOrgResource = false;
        isProjectResource = false;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public void addRole(Role role) {
        if (role == null) {
            return;
        }
        roles.add(role);
    }

    public void setRoles(Set<Role> roles) {
        if (roles == null) {
            return;
        }
        this.roles = roles;
    }

    public void removeRole(Role role) {
        if (role == null) {
            return;
        }
        roles.remove(role);
    }

    public void removeAllRoles() {
        roles.clear();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        RoleResource roleResource = (RoleResource) o;
        return Objects.equals(name, roleResource.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), name);
    }
}

