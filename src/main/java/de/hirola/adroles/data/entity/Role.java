package de.hirola.adroles.data.entity;

import de.hirola.adroles.data.AbstractEntity;

import javax.annotation.Nullable;
import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Copyright 2022 by Michael Schmidt, Hirola Consulting
 * This software us licensed under the AGPL-3.0 or later.
 *
 * <P>A role summarizes authorizations, e.g. in AD.</P>
 *
 * @author Michael Schmidt (Hirola)
 * @since v0.1
 */

@Entity
public class Role extends AbstractEntity {
    @NotEmpty
    private String name;
    private String description;
    public boolean isAdminRole;

    @ManyToOne(cascade = CascadeType.MERGE, fetch= FetchType.EAGER)
    @JoinColumn(name = "role_resource_id")
    private RoleResource roleResource;

    @OneToOne(cascade = CascadeType.PERSIST, orphanRemoval = true)
    @JoinColumn(name = "responsible_id")
    private Person responsible;

    @OneToOne(cascade = CascadeType.PERSIST, orphanRemoval = true)
    @JoinColumn(name = "second_responsible_id")
    private Person secondResponsible;

    @ManyToMany(cascade = CascadeType.PERSIST, fetch= FetchType.EAGER)
    @JoinTable(name = "role_adgroup",
            joinColumns = @JoinColumn(name = "role_id"),
            inverseJoinColumns = @JoinColumn(name = "adgroup_id"))
    private Set<ADGroup> adGroups = new LinkedHashSet<>();

    @ManyToMany(cascade = CascadeType.PERSIST, fetch = FetchType.EAGER)
    @JoinTable(name = "role_person",
            joinColumns = @JoinColumn(name = "role_id"),
            inverseJoinColumns = @JoinColumn(name = "person_id"))
    private Set<Person> persons = new LinkedHashSet<>();

    @ManyToMany(cascade = CascadeType.PERSIST, fetch = FetchType.EAGER)
    @JoinTable(name = "role_aduser",
            joinColumns = @JoinColumn(name = "role_id"),
            inverseJoinColumns = @JoinColumn(name = "aduser_id"))
    private Set<ADUser> adUsers = new LinkedHashSet<>();

    public String getName() {
        return Objects.requireNonNullElse(name, "");
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return Objects.requireNonNullElse(description, "");
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isAdminRole() {
        return isAdminRole;
    }

    public void setAdminRole(boolean adminRole) {
        isAdminRole = adminRole;
    }

    public RoleResource getRoleResource() {
        return roleResource;
    }

    public void setRoleResource(RoleResource roleResource) {
        this.roleResource = roleResource;
    }

    public @Nullable Person getResponsible() {
        return responsible;
    }

    public void setResponsible(Person responsible) {
        this.responsible = responsible;
    }

    public @Nullable Person getSecondResponsible() {
        return secondResponsible;
    }

    public void setSecondResponsible(Person secondResponsible) {
        this.secondResponsible = secondResponsible;
    }

    public Set<ADGroup> getADGroups() {
        return adGroups;
    }

    public void setADGroups(Set<ADGroup> adGroups) {
        if (adGroups == null) {
            return;
        }
        this.adGroups = adGroups;
    }

    public void addADGroup(ADGroup adGroup) {
        if (adGroup == null) {
            return;
        }
        this.adGroups.add(adGroup);
    }

    public void removeADGroup(ADGroup adGroup) {
        if (adGroup == null) {
            return;
        }
        adGroups.remove(adGroup);
    }

    public void removeAllADGroups() {
        adGroups.clear();
    }

    public Set<Person> getPersons() {
        return persons;
    }

    public void setPersons(Set<Person> persons) {
        if (persons == null) {
            return;
        }
        this.persons = persons;
    }

    public void addPerson(Person person) {
        persons.add(person);
    }

    public void removePerson(Person person) {
        if (person == null) {
            return;
        }
        persons.remove(person);
    }

    public void removeAllPersons() {
        persons.clear();
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
        adUsers.add(adUser);
    }

    public void removeADUser(ADUser adUser) {
        if (adUser == null) {
            return;
        }
        adUsers.remove(adUser);
    }

    public void removeAllADUsers() {
        adUsers.clear();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Role role = (Role) o;
        return Objects.equals(name, role.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), name);
    }
}
