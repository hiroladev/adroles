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
 *
 * A role summarizes authorizations, e.g. in AD.
 *
 * @author Michael Schmidt (Hirola)
 * @since v0.1
 */

@Entity
public class Role extends AbstractEntity {
    @NotEmpty
    private String name;
    private String description;
    private boolean isAdminRole;
    private boolean isOrgRole; // an org is also a role

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

    public void addPerson(Person person) {
        persons.add(person);
    }

    public void removePerson(Person person) {
        persons.remove(person);
    }

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

    public boolean isOrgRole() {
        return isOrgRole;
    }

    public void setOrgRole(boolean orgRole) {
        isOrgRole = orgRole;
    }

    public Set<ADGroup> getAdGroups() {
        return adGroups;
    }

    public void setAdGroups(Set<ADGroup> adGroups) {
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


    public Set<Person> getPersons() {
        return persons;
    }

    public void setPersons(Set<Person> persons) {
        if (persons == null) {
            return;
        }
        this.persons = persons;
    }

    public void removeAllPersons() {
        persons.clear();
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
