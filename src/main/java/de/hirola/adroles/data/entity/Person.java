package de.hirola.adroles.data.entity;

import de.hirola.adroles.Global;
import de.hirola.adroles.data.AbstractEntity;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;


/**
 * Copyright 2022 by Michael Schmidt, Hirola Consulting
 * This software us licensed under the AGPL-3.0 or later.
 * <p>
 * A person is an employee of the company and can have multiple identities,
 * e.g. an account in the Active Directory.
 *
 * @author Michael Schmidt (Hirola)
 * @since v0.1
 */

@Entity
public class Person extends AbstractEntity {
    private String centralAccountName; // used the first logon name
    @NotEmpty
    private String lastName;
    private String firstName;
    private String emailAddress;
    private String phoneNumber;
    private String mobilePhoneNumber;
    private String departmentName;
    private String description;
    private LocalDate entryDate, exitDate;
    private boolean isEmployee;

    @OneToMany(cascade = CascadeType.PERSIST, fetch = FetchType.EAGER)
    @JoinColumn(name = "person_id")
    private Set<ADUser> adUsers = new LinkedHashSet<>();

    @ManyToMany(mappedBy = "persons", cascade = CascadeType.PERSIST, fetch = FetchType.EAGER)
    private Set<Role> roles = new LinkedHashSet<>();

    public String getCentralAccountName() {
        return Objects.requireNonNullElse(centralAccountName, "");
    }

    public void setCentralAccountName(String centralAccountName) {
        this.centralAccountName = centralAccountName;
    }

    public String getLastName() {
        return Objects.requireNonNullElse(lastName, "");
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFirstName() {
        return Objects.requireNonNullElse(firstName, "");
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getEmailAddress() {
        return Objects.requireNonNullElse(emailAddress, "");
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public String getPhoneNumber() {
        return Objects.requireNonNullElse(phoneNumber, "");
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getMobilePhoneNumber() {
        return Objects.requireNonNullElse(mobilePhoneNumber, "");
    }

    public void setMobilePhoneNumber(String mobilePhoneNumber) {
        this.mobilePhoneNumber = mobilePhoneNumber;
    }

    public String getDepartmentName() {
        return Objects.requireNonNullElse(departmentName, "");
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    public String getDescription() {
        return Objects.requireNonNullElse(description, "");
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getEntryDate() {
        return Objects.requireNonNullElse(entryDate, Global.EMPLOYEE_DEFAULT_VALUES.ENTRY_DATE);
    }

    public void setEntryDate(LocalDate entryDate) {
        this.entryDate = entryDate;
    }

    public LocalDate getExitDate() {
        return Objects.requireNonNullElse(exitDate, Global.EMPLOYEE_DEFAULT_VALUES.EXIT_DATE);
    }

    public void setExitDate(LocalDate exitDate) {
        this.exitDate = exitDate;
    }

    public Set<ADUser> getADUsers() {
        return adUsers;
    }

    public void setADUsers(Set<ADUser> adUsers) {
        if (this.adUsers == null) {
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

    public void removeADUser(ADUser account) {
        if (account == null) {
            return;
        }
        adUsers.remove(account);
    }

    public void removeAllADUsers() {
        adUsers.clear();
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

    public boolean isEmployee() {
        return isEmployee;
    }

    public void setEmployee(boolean employee) {
        isEmployee = employee;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Person person = (Person) o;
        return Objects.equals(centralAccountName, person.centralAccountName)
                && Objects.equals(lastName, person.lastName)
                && Objects.equals(firstName, person.firstName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), centralAccountName, lastName, firstName);
    }
}
