package de.hirola.adroles.data.entity;

import de.hirola.adroles.data.AbstractEntity;

import javax.persistence.Entity;
import javax.validation.constraints.NotEmpty;

/**
 * Copyright 2022 by Michael Schmidt, Hirola Consulting
 * This software us licensed under the AGPL-3.0 or later.
 * <p>
 * Represents the managed Active Directory of the company.
 *
 * @author Michael Schmidt (Hirola)
 * @since v0.1
 */
@Entity
public class ActiveDirectory extends AbstractEntity {
    @NotEmpty
    private String domainName;
    @NotEmpty
    private String ipAddress;
    private double port;
    private boolean useSecureConnection;
    @NotEmpty
    private String connectionUserName;
    @NotEmpty
    private String connectionPassword;

    public String getDomainName() {
        return domainName;
    }
    public void setDomainName(String domainName) {
        this.domainName = domainName;
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

    public String getConnectionUserName() {
        return connectionUserName;
    }

    /**
     * Set the username for the connection to the Active Directory.
     * <P>The username can be specified as follows:</P>
     * <UL>
     *     <LI>DOMAIN\\sAMAccountName</LI>
     *     <LI>Distinguished Name</LI>
     * </UL>
     *
     * @param userName for the connection
     */
    public void setConnectionUserName(String userName) {
        this.connectionUserName = userName;
    }

    public String getEncryptedConnectionPassword() {
        return connectionPassword;
    }

    public void setEncryptedConnectionPassword(String password) {
        connectionPassword = password;
    }

}
