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
public class ADUser extends AbstractEntity {
    @NotEmpty
    private String logonName;
    @NotEmpty
    private String distinguishedName;
    private boolean enabled;
    private boolean passwordExpires;

    private boolean isAdminAccount;

    private boolean isServiceAccount;
    @ManyToOne
    @JoinColumn(name = "person_id")
    private Person person;

    @ManyToMany(cascade = CascadeType.PERSIST)
    @JoinTable(name = "aduser_adgroups",
            joinColumns = @JoinColumn(name = "aduser_id"),
            inverseJoinColumns = @JoinColumn(name = "adgroups_id"))
    private Set<ADGroup> adGroups = new LinkedHashSet<>();

    public Set<ADGroup> getAdGroups() {
        return adGroups;
    }

    public void setAdGroups(Set<ADGroup> adGroups) {
        this.adGroups = adGroups;
    }

    public String getLogonName() {
        return Objects.requireNonNullElse(logonName, "");
    }

    public void setLogonName(String logonName) {
        this.logonName = logonName;
    }

    public String getDistinguishedName() {
        return Objects.requireNonNullElse(distinguishedName, "");
    }

    public void setDistinguishedName(String distinguishedName) {
        this.distinguishedName = distinguishedName;
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
}

