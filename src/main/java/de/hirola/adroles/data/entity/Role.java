package de.hirola.adroles.data.entity;

import de.hirola.adroles.data.AbstractEntity;
import org.springframework.context.annotation.Lazy;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import java.util.*;

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
    @ManyToMany(fetch= FetchType.EAGER)
    @JoinTable(name = "role_adgroup",
            joinColumns = { @JoinColumn(name = "role_id") },
            inverseJoinColumns = { @JoinColumn(name = "adgroup_id") })
    private Set<ADGroup> adGroups = new LinkedHashSet<>();
    @ManyToMany(fetch= FetchType.EAGER)
    @JoinTable(name = "role_person",
            joinColumns = { @JoinColumn(name = "role_id") },
            inverseJoinColumns = { @JoinColumn(name = "person_id") })
    private Set<Person> persons = new LinkedHashSet<>();

    public Set<Person> getPersons() {
        return persons;
    }

    public void setPersons(Set<Person> persons) {
        this.persons = persons;
    }

    private void addPerson(Person person) {
        persons.add(person);
    }

    private void removePerson(Person person) {
        persons.remove(person);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
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

    public Set<ADGroup> getADGroups() {
        return adGroups;
    }

    public void setADGroups(Set<ADGroup> groups) {
        this.adGroups = groups;
    }

    public void addADGroup(ADGroup group) {
        if (adGroups.contains(group)) {
            adGroups.add(group);
        }
    }

    public void removeADGroup(ADGroup group) {
        adGroups.remove(group);
    }
}
