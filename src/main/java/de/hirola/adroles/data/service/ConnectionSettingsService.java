package de.hirola.adroles.data.service;

import com.imperva.ddc.core.query.*;
import com.imperva.ddc.service.DirectoryConnectorService;
import de.hirola.adroles.data.entity.ActiveDirectory;
import de.hirola.adroles.data.entity.DomainController;
import de.hirola.adroles.data.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotNull;
import java.net.ConnectException;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ConnectionSettingsService {
    private final Logger logger = LoggerFactory.getLogger(ConnectionSettingsService.class);
    private ActiveDirectory activeDirectory;
    private boolean isConnected = false;
    private final ActiveDirectoryRepository activeDirectoryRepository;
    private final DomainControllerRepository domainControllerRepository;
    private QueryRequest queryRequest = null;

    public ConnectionSettingsService(ActiveDirectoryRepository activeDirectoryRepository,
                                     DomainControllerRepository domainControllerRepository) {
        this.activeDirectoryRepository = activeDirectoryRepository;
        this.domainControllerRepository = domainControllerRepository;
        // we manage only one AD
        if (activeDirectoryRepository.count() == 1) {
            activeDirectory = activeDirectoryRepository.findAll().get(0);
        } else {
            activeDirectory = new ActiveDirectory();
        }
        return;
    }

    public ActiveDirectory getActiveDirectory() {
        return activeDirectory;
    }

    public void saveActiveDirectory(ActiveDirectory activeDirectory) {
        // if there is no configuration for AD
        if (activeDirectoryRepository.count() == 0) {
            this.activeDirectory = activeDirectory;
            activeDirectoryRepository.save(activeDirectory);
            saveDomainController();
            return;
        }
        // we manage only one AD
        Optional<ActiveDirectory> activeDirectoryOptional = activeDirectoryRepository.findById(activeDirectory.getId());
        if (activeDirectoryOptional.isPresent()) {
            activeDirectoryRepository.save(activeDirectory);
            saveDomainController();
        }
    }

    public void connect() throws ConnectException {
        if (isConnected) {
            return;
        }
        if (activeDirectory == null) {
            String errorMessage = "A connection is not possible. No Active Directory has been configured.";
            logger.debug(errorMessage);
            throw new ConnectException(errorMessage);
        }
        // get the list of dc
        final List<DomainController> domainControllers = activeDirectory.getServers();
        if (domainControllers.isEmpty()) {
            String errorMessage = "A connection is not possible. No Active Directory has been configured.";
            logger.debug(errorMessage);
            throw new ConnectException(errorMessage);
        }
        queryRequest = new QueryRequest();
        queryRequest.setDirectoryType(DirectoryType.MS_ACTIVE_DIRECTORY);
        queryRequest.setSizeLimit(1000); //TODO: read from config
        queryRequest.setTimeLimit(1000); //TODO: read from config
        // try to connect - add all valid endpoints to the query request
        for (DomainController dc : domainControllers) {
            final Endpoint endpoint = new Endpoint();
            endpoint.setSecuredConnection(dc.useSecureConnection());
            endpoint.setPort((int) dc.getPort());
            endpoint.setHost(dc.getIPAddress());
            endpoint.setUserAccountName(activeDirectory.getConnectionUserName());
            endpoint.setPassword(activeDirectory.getEncryptedConnectionPassword());
            final ConnectionResponse connectionResponse = DirectoryConnectorService.authenticate(endpoint);
            if (connectionResponse.isError()) {
                throw new ConnectException(connectionResponse.toString());
            }
            queryRequest.addEndpoint(endpoint);
        }
        isConnected = true;
    }

    public void verifyConnection(@NotNull ActiveDirectory activeDirectory,
                                 @NotNull DomainController domainController) throws ConnectException, GeneralSecurityException {
        final Endpoint endpoint = new Endpoint();
        endpoint.setSecuredConnection(domainController.useSecureConnection());
        endpoint.setPort((int) domainController.getPort());
        endpoint.setHost(domainController.getIPAddress());
        endpoint.setUserAccountName(activeDirectory.getConnectionUserName());
        // decrypt password
        endpoint.setPassword(activeDirectory.getEncryptedConnectionPassword());
        final ConnectionResponse connectionResponse = DirectoryConnectorService.authenticate(endpoint);
        if (connectionResponse.isError()) {
            Map<String, Status> statuses = connectionResponse.getStatuses();
            throw new ConnectException(statuses.keySet().toString());
        }
    }

    private void saveDomainController() {
        for (DomainController domainController: activeDirectory.getServers()) {
            domainControllerRepository.save(domainController);
        }
    }
}
