package de.hirola.adroles.data.entity;

import de.hirola.adroles.data.AbstractEntity;

import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.validation.constraints.NotEmpty;
import java.util.ArrayList;
import java.util.List;

/**
 * Copyright 2022 by Michael Schmidt, Hirola Consulting
 * This software us licensed under the AGPL-3.0 or later.
 *
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
    private String connectionUserName;
    @NotEmpty
    private String connectionPassword;
    @OneToMany(mappedBy = "activeDirectory")
    private List<DomainController> servers = new ArrayList<>();

    public String getDomainName() {
        return domainName;
    }
    public void setDomainName(String domainName) {
        this.domainName = domainName;
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
    public List<DomainController> getServers() {
        return servers;
    }
    public void setServers(List<DomainController> servers) {
        this.servers = servers;
    }

    public void addServer(DomainController domainController) {
        if (!servers.contains(domainController)) {
            servers.add(domainController);
        }
    }
    public void removeServer(DomainController domainController){
        servers.remove(domainController);
    }
}
