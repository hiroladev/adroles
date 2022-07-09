package de.hirola.adroles.data.entity;

import de.hirola.adroles.data.AbstractEntity;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotBlank;

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
    @NotBlank
    private String name;
    @NotBlank
    private String objectSID;
    @NotBlank
    private String distinguishedName;
    @ManyToOne
    @JoinColumn(name = "person_id")
    private Person person;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getObjectSID() {
        return objectSID;
    }

    public void setObjectSID(String objectSID) {
        this.objectSID = objectSID;
    }

    public String getDistinguishedName() {
        return distinguishedName;
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
}

