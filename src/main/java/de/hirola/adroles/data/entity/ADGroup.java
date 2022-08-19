package de.hirola.adroles.data.entity;

import de.hirola.adroles.data.AbstractEntity;

import javax.persistence.*;
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
public class ADGroup extends AbstractEntity implements Comparable<ADGroup> {
    @NotEmpty
    private String name;
    @NotEmpty
    private String distinguishedName;
    @NotEmpty
    private String objectSID = UUID.randomUUID().toString(); // SID does never change
    private String description;
    private int groupArea; // local, global, universal ==> Global.ADGroupArea
    private int groupType; // security, distribution ==> Global.ADGroupType
    private boolean isAdminGroup;
    @ManyToMany(mappedBy = "adGroups", cascade = CascadeType.PERSIST, fetch = FetchType.EAGER)
    private Set<Role> roles = new LinkedHashSet<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDistinguishedName() {
        return distinguishedName;
    }

    public void setDistinguishedName(String distinguishedName) {
       this.distinguishedName = distinguishedName;
    }

    public String getObjectSID() {
        return objectSID;
    }

    public void setObjectSID(String objectSID) {
        this.objectSID = objectSID;
    }

    public String getDescription() {
        return Objects.requireNonNullElse(description, "");
    }

    public void setDescription(String description) {
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

    @Override
    public int compareTo(ADGroup o) {
        return name.compareTo(o.getName());
    }
}

