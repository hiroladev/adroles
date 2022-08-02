package de.hirola.adroles.data.entity;

import de.hirola.adroles.data.AbstractEntity;

import javax.persistence.Entity;
import java.util.Objects;

/**
 * Copyright 2022 by Michael Schmidt, Hirola Consulting
 * This software us licensed under the AGPL-3.0 or later.
 * <p>
 *  Needed for Spring security.
 *
 * @author Michael Schmidt (Hirola)
 * @since v0.1
 */

@Entity
public class Authorities extends AbstractEntity {
    private String loginName;
    private String authority;

    public String getLoginName() {
        return Objects.requireNonNullElse(loginName, "");
    }

    public void setLoginName(String loginName) {
        this.loginName = loginName;
    }

    public String getAuthority() {
        return Objects.requireNonNullElse(authority, "");
    }

    public void setAuthority(String authority) {
        this.authority = authority;
    }
}

