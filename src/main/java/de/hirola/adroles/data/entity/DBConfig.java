/*
 * *
 *  * Copyright 2022 by Michael Schmidt, Hirola Consulting
 *  * This software us licensed under the AGPL-3.0 or later.
 *  *
 *  *
 *  * @author Michael Schmidt (Hirola)
 *  * @since v0.1
 *
 */

package de.hirola.adroles.data.entity;

import de.hirola.adroles.data.AbstractEntity;

import javax.persistence.Entity;
import javax.validation.constraints.NotEmpty;
import java.util.Objects;

@Entity
public class DBConfig extends AbstractEntity {
    @NotEmpty
    private String name;
    private String jdbcDriverName;
    private String jdbcUrl;
    private String username;
    private String password;

    public String getName() {
        return Objects.requireNonNullElse(name, "");
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getJdbcDriverName() {
        return Objects.requireNonNullElse(jdbcDriverName, "");
    }

    public void setJdbcDriverName(String jdbcDriverName) {
        this.jdbcDriverName = jdbcDriverName;
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
