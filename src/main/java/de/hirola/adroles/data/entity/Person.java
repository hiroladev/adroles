package de.hirola.adroles.data.entity;

import de.hirola.adroles.data.AbstractEntity;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.util.*;

/**
 * Copyright 2022 by Michael Schmidt, Hirola Consulting
 * This software us licensed under the AGPL-3.0 or later.
 *
 * A person is an employee of the company and can have multiple identities,
 * e.g. an account in the Active Directory.
 *
 * @author Michael Schmidt (Hirola)
 * @since v0.1
 */

@Entity
public class Person extends AbstractEntity {
    @NotBlank
    private String lastName;
    private String firstName;
    private String emailAddress;
    private String phoneNumber;
    private String mobilePhoneNumber;
    private String office;
    @ManyToOne
    @JoinColumn(name = "organisation_unit_id")
    private OrganisationUnit organisationUnit;
    @ManyToMany(mappedBy = "persons")
    private final Set<Role> roles = new LinkedHashSet<>();
    @OneToMany(orphanRemoval = true)
    @JoinColumn(name = "person_id")
    private List<ADAccount> adAccounts = new LinkedList<>();

    public List<ADAccount> getADAccounts() {
        return adAccounts;
    }

    public void setADAccounts(List<ADAccount> accounts) {
        this.adAccounts = accounts;
    }

    private void addADAccount(ADAccount account) {
        if (!adAccounts.contains(account)) {
            adAccounts.add(account);
        }
    }

    private void removeADAccount(ADAccount account) {
        adAccounts.remove(account);
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getMobilePhoneNumber() {
        return mobilePhoneNumber;
    }

    public void setMobilePhoneNumber(String mobilePhoneNumber) {
        this.mobilePhoneNumber = mobilePhoneNumber;
    }

    public String getOffice() {
        return office;
    }

    public void setOffice(String office) {
        this.office = office;
    }

    public OrganisationUnit getOrganisationUnit() {
        return organisationUnit;
    }

    public void setOrganisationUnit(OrganisationUnit organisationUnit) {
        this.organisationUnit = organisationUnit;
    }
}
