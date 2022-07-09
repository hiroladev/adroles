package de.hirola.adroles.data.entity;

import de.hirola.adroles.data.AbstractEntity;

import javax.persistence.Entity;
import javax.validation.constraints.NotBlank;

/**
 * Copyright 2022 by Michael Schmidt, Hirola Consulting
 * This software us licensed under the AGPL-3.0 or later.
 *
 * Users can log in to the application and manage it.
 *
 * @author Michael Schmidt (Hirola)
 * @since v0.1
 */

@Entity
public class User extends AbstractEntity {
    @NotBlank
    private String loginName;
    @NotBlank
    private String password;
    private boolean enabled = true;

    public String getLoginName() {
        return loginName;
    }

    public void setLoginName(String loginName) {
        this.loginName = loginName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}

