package de.hirola.adroles.data.entity;

import de.hirola.adroles.data.AbstractEntity;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;

/**
 * Copyright 2022 by Michael Schmidt, Hirola Consulting
 * This software us licensed under the AGPL-3.0 or later.
 *
 * A Domain Controller for the managed Active Directory.
 *
 * @author Michael Schmidt (Hirola)
 * @since v0.1
 */

@Entity
public class DomainController extends AbstractEntity {

    @ManyToOne
    @JoinColumn(name = "active_directory_id")
    private ActiveDirectory activeDirectory;
    @NotEmpty
    private String ipAddress;
    private double port;
    private boolean useSecureConnection;

    public ActiveDirectory getActiveDirectory() {
        return activeDirectory;
    }

    public void setActiveDirectory(ActiveDirectory activeDirectory) {
        this.activeDirectory = activeDirectory;
    }

    public String getIPAddress() {
        return ipAddress;
    }

    public void setIPAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public double getPort() {
        return port;
    }

    public void setPort(double port) {
        this.port = port;
    }

    public boolean useSecureConnection() {
        return useSecureConnection;
    }

    public void setUseSecureConnection(boolean useSecureConnection) {
        this.useSecureConnection = useSecureConnection;
    }
}
