package de.hirola.adroles.data.entity;

import de.hirola.adroles.data.AbstractEntity;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotEmpty;

/**
 * Copyright 2022 by Michael Schmidt, Hirola Consulting
 * This software us licensed under the AGPL-3.0 or later.
 *
 * Represents an account in an Active Directory.
 * A person can have multiple accounts, e.g. for administrative purposes.
 * Assignment / inheritance of permissions (via roles)
 * can be disabled for individual accounts.
 *
 * @author Michael Schmidt (Hirola)
 * @since v0.1
 */

@Entity
public class ADAccount extends AbstractEntity {
    @NotEmpty
    private String logonName;
    @NotEmpty
    private String distinguishedName;
    private boolean enabled;
    private boolean passwordNeverExpires;
    @ManyToOne
    @JoinColumn(name = "person_id")
    private Person person;

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

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isPasswordNeverExpires() {
        return passwordNeverExpires;
    }

    public void setPasswordNeverExpires(boolean passwordNeverExpires) {
        this.passwordNeverExpires = passwordNeverExpires;
    }

    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }
}

