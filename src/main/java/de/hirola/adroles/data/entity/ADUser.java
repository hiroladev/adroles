package de.hirola.adroles.data.entity;

import de.hirola.adroles.data.AbstractEntity;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Copyright 2022 by Michael Schmidt, Hirola Consulting
 * This software us licensed under the AGPL-3.0 or later.
 * <p>
 * Represents an account in an Active Directory.
 * A person can have multiple accounts, e.g. for administrative purposes.
 * Assignment / inheritance of permissions (via roles)
 * can be disabled for individual accounts.
 *
 * @author Michael Schmidt (Hirola)
 * @since v0.1
 */

@Entity
public class ADUser extends AbstractEntity implements Comparable<ADUser> {
    @NotEmpty
    private String logonName;
    @NotEmpty
    private String distinguishedName;
    @NotEmpty
    private String objectSID; // SID does never change
    private boolean enabled;
    private boolean passwordExpires;

    private boolean isRoleManaged;
    private boolean isAdminAccount;

    private boolean isServiceAccount;
    @ManyToOne
    @JoinColumn(name = "person_id")
    private Person person;

    @ManyToMany(mappedBy = "adUsers", cascade = CascadeType.PERSIST, fetch = FetchType.EAGER)
    private Set<Role> roles = new LinkedHashSet<>();

    public String getLogonName() {
        return logonName;
    }

    public void setLogonName(String logonName) {
        this.logonName = logonName;
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

    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isPasswordExpires() {
        return passwordExpires;
    }

    public void setPasswordExpires(boolean passwordExpires) {
        this.passwordExpires = passwordExpires;
    }

    public boolean isRoleManaged() {
        return isRoleManaged;
    }

    public void setRoleManaged(boolean roleManaged) {
        isRoleManaged = roleManaged;
    }

    public boolean isAdminAccount() {
        return isAdminAccount;
    }

    public void setAdminAccount(boolean adminAccount) {
        isAdminAccount = adminAccount;
    }

    public boolean isServiceAccount() {
        return isServiceAccount;
    }

    public void setServiceAccount(boolean serviceAccount) {
        isServiceAccount = serviceAccount;
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
        ADUser adUser = (ADUser) o;
        return Objects.equals(logonName, adUser.logonName) && Objects.equals(distinguishedName, adUser.distinguishedName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), logonName, distinguishedName);
    }

    @Override
    public int compareTo(ADUser o) {
        return logonName.compareTo(o.getLogonName());
    }
}

