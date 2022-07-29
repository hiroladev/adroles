package de.hirola.adroles.data.entity;

import de.hirola.adroles.data.AbstractEntity;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToMany;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.*;

/**
 * Copyright 2022 by Michael Schmidt, Hirola Consulting
 * This software us licensed under the AGPL-3.0 or later.
 * <p>
 * Represents a group in an Active Directory.
 *
 * @author Michael Schmidt (Hirola)
 * @since v0.1
 */

@Entity
public class ADGroup extends AbstractEntity {
    @NotBlank
    private String name;
    @NotEmpty
    private String distinguishedName;
    private String description;
    private int groupArea; // local, global, universal ==> Global.ADGroupArea
    private int groupType; // security, distribution ==> Global.ADGroupType
    private boolean isAdminGroup;
    @ManyToMany(mappedBy = "adGroups", cascade = CascadeType.PERSIST, fetch= FetchType.EAGER)
    private Set<Role> roles = new LinkedHashSet<>();

    @ManyToMany(mappedBy = "adGroups", cascade = CascadeType.PERSIST)
    private Set<ADUser> adUsers = new LinkedHashSet<>();

    public String getName() {
        return Objects.requireNonNullElse(name, "");
    }

    public void setName(String name) {
        if (name == null) {
            return;
        }
        this.name = name;
    }

    public String getDistinguishedName() {
        return distinguishedName;
    }

    public void setDistinguishedName(String distinguishedName) {
        if (distinguishedName == null) {
            return;
        }
        this.distinguishedName = distinguishedName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        if (description == null) {
            return;
        }
        this.description = description;
    }

    public int getGroupArea() {
        return groupArea;
    }

    public void setGroupArea(int groupArea) {
        this.groupArea = groupArea;
    }

    public int getGroupType() {
        return groupType;
    }

    public void setGroupType(int groupType) {
        this.groupType = groupType;
    }

    public boolean isAdminGroup() {
        return isAdminGroup;
    }

    public void setAdminGroup(boolean adminGroup) {
        isAdminGroup = adminGroup;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public void setRoles(Set<Role> roles) {
        if (roles == null) {
            return;
        }
        this.roles = roles;
    }

    public void addRole(Role role) {
        if (role == null) {
            return;
        }
        roles.add(role);
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

    public Set<ADUser> getADUsers() {
        return adUsers;
    }

    public void setADUsers(Set<ADUser> adUsers) {
        if (adUsers == null) {
            return;
        }
        this.adUsers = adUsers;
    }

    public void addADUser(ADUser adUser) {
        if (adUser == null) {
            return;
        }
        adUsers.add(adUser);
    }

    public void removeADUser(ADUser adUser) {
        if (adUser == null) {
            return;
        }
        adUsers.remove(adUser);
    }

    public void removeAllADUser() {
        adUsers.clear();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        ADGroup adGroup = (ADGroup) o;
        return groupArea == adGroup.groupArea && Objects.equals(name, adGroup.name) &&
                Objects.equals(distinguishedName, adGroup.distinguishedName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), name, distinguishedName, groupArea);
    }
}

