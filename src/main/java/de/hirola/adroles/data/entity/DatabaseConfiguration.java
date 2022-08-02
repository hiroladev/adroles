package de.hirola.adroles.data.entity;

import de.hirola.adroles.data.AbstractEntity;

import javax.persistence.Entity;
import javax.validation.constraints.NotEmpty;
import java.util.Objects;

/**
 * Copyright 2022 by Michael Schmidt, Hirola Consulting
 * This software us licensed under the AGPL-3.0 or later.
 * <p>
 *  DatabaseConfiguration for AD-Roles.
 *
 * @author Michael Schmidt (Hirola)
 * @since v0.1
 */
@Entity
public class DatabaseConfiguration extends AbstractEntity {
    @NotEmpty
    private String name;
    @NotEmpty
    private String jdbcDriver;
    private String jdbcUrl;
    private String username;
    private String password;

    public String getName() {
        return Objects.requireNonNullElse(name, "");
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getJdbcDriver() {
        return Objects.requireNonNullElse(jdbcDriver, "");
    }

    public void setJdbcDriver(String jdbcDriver) {
        this.jdbcDriver = jdbcDriver;
    }

    public String getJdbcUrl() {
        return Objects.requireNonNullElse(jdbcUrl, "");
    }

    public void setJdbcUrl(String jdbcUrl) {
        this.jdbcUrl = jdbcUrl;
    }

    public String getUsername() {
        return Objects.requireNonNullElse(username, "");
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return Objects.requireNonNullElse(password, "");
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
