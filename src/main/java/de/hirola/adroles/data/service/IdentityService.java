package de.hirola.adroles.data.service;

import com.imperva.ddc.core.Connector;
import com.imperva.ddc.core.language.PhraseOperator;
import com.imperva.ddc.core.language.QueryAssembler;
import com.imperva.ddc.core.language.SentenceOperator;
import com.imperva.ddc.core.query.*;
import com.imperva.ddc.service.DirectoryConnectorService;
import com.vaadin.flow.component.UI;
import de.hirola.adroles.Global;
import de.hirola.adroles.data.entity.ADAccount;
import de.hirola.adroles.data.entity.ActiveDirectory;
import de.hirola.adroles.data.entity.Person;
import de.hirola.adroles.data.entity.Role;
import de.hirola.adroles.data.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class IdentityService {
    private final Logger logger = LoggerFactory.getLogger(IdentityService.class);
    private ActiveDirectory activeDirectory;
    private boolean isConnected = false;
    private final ActiveDirectoryRepository activeDirectoryRepository;
    private final ADAccountRepository adAccountRepository;
    private  final PersonRepository personRepository;
    private QueryRequest queryRequest = null;

    public IdentityService(ActiveDirectoryRepository activeDirectoryRepository, ADAccountRepository adAccountRepository,
                           PersonRepository personRepository) {
        this.activeDirectoryRepository = activeDirectoryRepository;
        this.adAccountRepository = adAccountRepository;
        this.personRepository = personRepository;
        // we manage only one AD
        if (activeDirectoryRepository.count() == 1) {
            activeDirectory = activeDirectoryRepository.findAll().get(0);
            try {
                connect();
                isConnected = true;
            } catch (ConnectException exception) {
                logger.debug("");
                isConnected = false;
            }
        } else {
            activeDirectory = new ActiveDirectory();
        }
    }

    public List<Person> findAllPersons(@Nullable String stringFilter) {
        if (stringFilter == null || stringFilter.isEmpty()) {
            return personRepository.findAll();
        } else {
            return personRepository.search(stringFilter);
        }
    }

    public long countPersons() {
        return personRepository.count();
    }

    public long countADAccounts() {
        return adAccountRepository.count();
    }

    public void importPersonsFromAD() {
        // load accounts from ad
        List<EntityResponse> responses = getADEntityResponses();
        // create / update AD accounts from response
        // we need the accounts first to link with persons
        for (EntityResponse response: responses) {
            createOrUpdateADAccount(response);
        }
        // create / update persons from response
        for (EntityResponse response: responses) {
            createOrUpdatePerson(response);
        }
    }

    public void savePerson(Person person) {
        if (person == null) {
            return;
        }
        personRepository.save(person);
    }

    public void deletePerson(Person person) {
        if (person == null) {
            return;
        }
        personRepository.delete(person);
    }

    public ActiveDirectory getActiveDirectory() {
        return activeDirectory;
    }

    public void saveActiveDirectory(ActiveDirectory activeDirectory) {
        // if there is no configuration for AD
        if (activeDirectoryRepository.count() == 0) {
            this.activeDirectory = activeDirectory;
            activeDirectoryRepository.save(activeDirectory);
            return;
        }
        // we manage only one AD
        Optional<ActiveDirectory> activeDirectoryOptional = activeDirectoryRepository.findById(activeDirectory.getId());
        if (activeDirectoryOptional.isPresent()) {
            activeDirectoryRepository.save(activeDirectory);
        }
    }

    public void verifyConnection(@NotNull ActiveDirectory activeDirectory)
            throws ConnectException {
        final Endpoint endpoint = new Endpoint();
        endpoint.setSecuredConnection(activeDirectory.useSecureConnection());
        endpoint.setPort((int) activeDirectory.getPort());
        endpoint.setHost(activeDirectory.getIPAddress());
        endpoint.setUserAccountName(activeDirectory.getConnectionUserName());
        // decrypt password
        endpoint.setPassword(activeDirectory.getEncryptedConnectionPassword());
        final ConnectionResponse connectionResponse = DirectoryConnectorService.authenticate(endpoint);
        if (connectionResponse.isError()) {
            Map<String, Status> statuses = connectionResponse.getStatuses();
            throw new ConnectException(statuses.keySet().toString());
        }
    }

    private void connect() throws ConnectException {
        if (isConnected) {
            return;
        }
        if (activeDirectory == null) {
            String errorMessage = UI.getCurrent().getTranslation("error.domain.connection.ad.empty");
            logger.debug(errorMessage);
            throw new ConnectException(errorMessage);
        }
        queryRequest = new QueryRequest();
        queryRequest.setDirectoryType(DirectoryType.MS_ACTIVE_DIRECTORY);
        queryRequest.setSizeLimit(1000); //TODO: read from config
        queryRequest.setTimeLimit(1000); //TODO: read from config
        // try to connect - add all valid endpoints to the query request
        final Endpoint endpoint = new Endpoint();
        endpoint.setSecuredConnection(activeDirectory.useSecureConnection());
        endpoint.setPort((int) activeDirectory.getPort());
        endpoint.setHost(activeDirectory.getIPAddress());
        endpoint.setUserAccountName(activeDirectory.getConnectionUserName());
        endpoint.setPassword(activeDirectory.getEncryptedConnectionPassword());
        final ConnectionResponse connectionResponse = DirectoryConnectorService.authenticate(endpoint);
        if (connectionResponse.isError()) {
            throw new ConnectException(connectionResponse.toString());
        }
        queryRequest.addEndpoint(endpoint);
        isConnected = true;
    }

    private List<EntityResponse> getADEntityResponses() {
        if (isConnected) {
            queryRequest.setObjectType(ObjectType.USER);
            //TODO: set filter by config
            // e.g. load only enabled accounts
            queryRequest.addSearchSentence(new QueryAssembler()
                    .addPhrase("userAccountControl", PhraseOperator.EQUAL, "512")
                    .addPhrase("userAccountControl", PhraseOperator.EQUAL, "66048")
                    .closeSentence(SentenceOperator.OR));

            // get all fields needed for entities person and ad account
            queryRequest.addRequestedField(Global.ADAttributes.ACCOUNT_STATE);
            queryRequest.addRequestedField(FieldType.LOGON_NAME);
            queryRequest.addRequestedField(FieldType.DISTINGUISHED_NAME);
            queryRequest.addRequestedField(FieldType.FIRST_NAME);
            queryRequest.addRequestedField(FieldType.LAST_NAME);
            queryRequest.addRequestedField(FieldType.DEPARTMENT);
            queryRequest.addRequestedField(FieldType.EMAIL);
            queryRequest.addRequestedField(FieldType.PHONE_NUMBER);
            queryRequest.addRequestedField(FieldType.MOBILE_PHONE);
            Connector connector = new Connector(queryRequest);
            QueryResponse queryResponse = connector.execute();
            return queryResponse.getAll();
        }
        return new ArrayList<>();
    }

    private void createOrUpdateADAccount(EntityResponse response) {
        boolean isUpdate = countADAccounts() > 0;
        ADAccount adAccount = new ADAccount();
        List<Field> fields = response.getValue();
        for (Field field: fields) {
            if (field.getType().equals(FieldType.LOGON_NAME)) {
                adAccount.setLogonName((String) field.getValue());
            }
            if (field.getType().equals(FieldType.DISTINGUISHED_NAME)) {
                adAccount.setDistinguishedName((String) field.getValue());
            }
            // account enabled / password expiration
            if (field.getName().equalsIgnoreCase(Global.ADAttributes.ACCOUNT_STATE)) {
                adAccount.setEnabled(accountEnabled((String) field.getValue()));
                adAccount.setPasswordNeverExpires(passwordNeverExpires((String) field.getValue()));
            }
        }
        if (isUpdate) {
            Optional<ADAccount> optionalADAccount = adAccountRepository.findFirstByLogonName(adAccount.getLogonName());
            if (optionalADAccount.isPresent()) {
                // set the id of the existing object
                adAccount.setId(optionalADAccount.get().getId());
                // set linked person
                adAccount.setPerson(optionalADAccount.get().getPerson());
                // save as updated object
                adAccountRepository.save(adAccount);
            } else {
                adAccountRepository.save(adAccount);
            }
        } else {
            adAccountRepository.save(adAccount);
        }
    }

    private void createOrUpdatePerson(EntityResponse response) {
        boolean isUpdate = countPersons() > 0;
        Person person = new Person();
        List<Field> fields = response.getValue();
        for (Field field: fields) {
            // set values
            if (field.getType().equals(FieldType.LOGON_NAME)) {
                person.setCentralAccountName((String) field.getValue());
            }
            if (field.getType().equals(FieldType.FIRST_NAME)) {
                person.setFirstName((String) field.getValue());
            }
            if (field.getType().equals(FieldType.LAST_NAME)) {
                person.setLastName((String) field.getValue());
            }
            if (field.getType().equals(FieldType.EMAIL)) {
                person.setEmailAddress((String) field.getValue());
            }
            if (field.getType().equals(FieldType.PHONE_NUMBER)) {
                person.setPhoneNumber((String) field.getValue());
            }
            if (field.getType().equals(FieldType.MOBILE_PHONE)) {
                person.setMobilePhoneNumber((String) field.getValue());
            }
            if (field.getType().equals(FieldType.DEPARTMENT)) {
                person.setDepartment((String) field.getValue());
            }
        }
        if (isUpdate) {
            Optional<Person> optionalPerson = personRepository.findByAdAccounts_LogonName(person.getCentralAccountName());
            if (optionalPerson.isPresent()) {
                Person p = optionalPerson.get();
                // set the id of the existing object
                person.setId(p.getId());
                // set the linked ad accounts
                person.setADAccounts(p.getADAccounts());
                // update central account - if was ad accounts linked
                if (person.getADAccounts().size() > 0) {
                    person.setCentralAccountName(p.getCentralAccountName());
                }
                // save as updated object
                personRepository.save(person);
            } else {
                personRepository.save(person);
            }
        } else {
            personRepository.save(person);
        }
    }

    private boolean accountEnabled(String flag) {
        /*
            AD attribute userAccountControl contains the flag
            512=Enabled
            514= Disabled
            66048 = Enabled, password never expires
            66050 = Disabled, password never expires
         */
        return flag.compareToIgnoreCase("512") == 0 || flag.compareToIgnoreCase("66048") == 0;
    }

    private boolean passwordNeverExpires(String flag) {
        /*
            AD attribute userAccountControl contains the flag
            512=Enabled
            514= Disabled
            66048 = Enabled, password never expires
            66050 = Disabled, password never expires
         */
        return flag.compareToIgnoreCase("66048") == 0;
    }
}
