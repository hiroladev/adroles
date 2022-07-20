package de.hirola.adroles.data.entity;

import de.hirola.adroles.data.AbstractEntity;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.util.*;

/**
 * Copyright 2022 by Michael Schmidt, Hirola Consulting
 * This software us licensed under the AGPL-3.0 or later.
 *
 * An organizational unit can be hierarchically subordinate or superior to another.
 * The first created OU represents the company and cannot have a parent.
 *
 * @author Michael Schmidt (Hirola)
 * @since v0.1
 */

@Entity
public class OrganisationUnit extends AbstractEntity {
    @NotBlank
    private String name;
    private String description;
    @ManyToOne
    @JoinColumn(name = "parent_id")
    private OrganisationUnit parent;
    @ManyToOne
    @JoinColumn(name = "child_id")
    private OrganisationUnit child;
    @ManyToMany
    @JoinTable(name = "org_role",
            joinColumns = { @JoinColumn(name = "org_id") },
            inverseJoinColumns = { @JoinColumn(name = "role_id") })
    private final Set<Role> roles = new LinkedHashSet<>();
    @OneToMany(mappedBy = "organisationUnit")
    private final List<Person> members = new LinkedList<>();

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

    public OrganisationUnit getParent() {
        return parent;
    }

    public void setParent(OrganisationUnit parent) {
        this.parent = parent;
    }

    public OrganisationUnit getChild() {
        return child;
    }

    public void setChild(OrganisationUnit child) {
        this.child = child;
    }

    private void addRole(Role role) {
        roles.add(role);
    }

    private void removeRole(Role role) {
        roles.remove(role);
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public List<Person> getMembers() {
        return members;
    }
}
