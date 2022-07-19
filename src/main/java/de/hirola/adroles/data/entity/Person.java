package de.hirola.adroles.data.entity;

import de.hirola.adroles.data.AbstractEntity;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import java.time.LocalDate;
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
    @NotEmpty
    private String centralAccountName; // used the first logon name
    private String lastName;
    private String firstName;
    private String emailAddress;
    private String phoneNumber;
    private String mobilePhoneNumber;
    private String department;
    @ManyToOne
    @JoinColumn(name = "organisation_unit_id")
    private OrganisationUnit organisationUnit;
    @ManyToMany(mappedBy = "persons")
    private final Set<Role> roles = new LinkedHashSet<>();
    @OneToMany(orphanRemoval = true)
    @JoinColumn(name = "person_id")
    private List<ADAccount> adAccounts = new LinkedList<>();

    private LocalDate entryDate, exitDate;

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



    public String getCentralAccountName() {
        return centralAccountName;
    }

    public void setCentralAccountName(String centralAccountName) {
        this.centralAccountName = centralAccountName;
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

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public OrganisationUnit getOrganisationUnit() {
        return organisationUnit;
    }

    public void setOrganisationUnit(OrganisationUnit organisationUnit) {
        this.organisationUnit = organisationUnit;
    }

    public LocalDate getEntryDate() {
        return entryDate;
    }

    public void setEntryDate(LocalDate entryDate) {
        this.entryDate = entryDate;
    }

    public LocalDate getExitDate() {
        return exitDate;
    }

    public void setExitDate(LocalDate exitDate) {
        this.exitDate = exitDate;
    }
}
