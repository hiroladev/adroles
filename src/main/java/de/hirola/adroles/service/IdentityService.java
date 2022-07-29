/*
 * Copyright 2022 by Michael Schmidt, Hirola Consulting
 * This software us licensed under the AGPL-3.0 or later.
 *
 * @author Michael Schmidt (Hirola)
 * @since v0.1
 */

package de.hirola.adroles.service;

import com.imperva.ddc.core.Connector;
import com.imperva.ddc.core.language.PhraseOperator;
import com.imperva.ddc.core.language.QueryAssembler;
import com.imperva.ddc.core.language.SentenceOperator;
import com.imperva.ddc.core.query.*;
import com.imperva.ddc.service.DirectoryConnectorService;
import com.vaadin.flow.component.UI;
import de.hirola.adroles.Global;
import de.hirola.adroles.data.entity.*;
import de.hirola.adroles.data.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import java.net.ConnectException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class IdentityService {
    private final Logger logger = LoggerFactory.getLogger(IdentityService.class);
    private ActiveDirectory activeDirectory;
    private boolean isConnected = false;
    private final ActiveDirectoryRepository activeDirectoryRepository;
    private  final PersonRepository personRepository;
    private  final RoleRepository roleRepository;
    private final RoleResourceRepository roleResourceRepository;
    private final ADUserRepository adUserRepository;
    private final ADGroupRepository adGroupRepository;
    private QueryRequest queryRequest = null;

    public IdentityService(ActiveDirectoryRepository activeDirectoryRepository,
                           PersonRepository personRepository,
                           RoleRepository roleRepository,
                           RoleResourceRepository roleResourceRepository,
                           ADUserRepository adUserRepository,
                           ADGroupRepository adGroupRepository) {
        this.activeDirectoryRepository = activeDirectoryRepository;
        this.personRepository = personRepository;
        this.roleRepository = roleRepository;
        this.roleResourceRepository = roleResourceRepository;
        this.adUserRepository = adUserRepository;
        this.adGroupRepository = adGroupRepository;
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

    public @Nullable RoleResource getRoleResource(int type) {
        switch (type) {
            case Global.ROLE_RESOURCE.ORG_ROLE:
                Optional<RoleResource> optionalOrgResource = roleResourceRepository.getOrgResource();
                if (optionalOrgResource.isPresent()) {
                    return optionalOrgResource.get();
                }
                // create resource for org units
                RoleResource orgRoleResource = new RoleResource();
                orgRoleResource.setName(UI.getCurrent().getTranslation("org"));
                orgRoleResource.setDescription(UI.getCurrent().getTranslation("org"));
                orgRoleResource.setViewClassName("org-view");
                orgRoleResource.setAddResourceTranslationKey("addOrg");
                orgRoleResource.setDeleteResourcesTranslationKey("deleteOrgs");
                orgRoleResource.setOrgResource(true);
                try {
                    roleResourceRepository.save(orgRoleResource);
                    logger.debug("Role resource for orgs created.");
                    return orgRoleResource;
                } catch (Exception exception) {
                    logger.debug("Error while creating resource for orgs.", exception);
                    return  null;
                }
            case Global.ROLE_RESOURCE.PROJECT_ROLE:
                Optional<RoleResource> optionalProjectResource = roleResourceRepository.getProjResource();
                if (optionalProjectResource.isPresent()) {
                    return optionalProjectResource.get();
                }
                // create resource for org units
                RoleResource projRoleResource = new RoleResource();
                projRoleResource.setName(UI.getCurrent().getTranslation("project"));
                projRoleResource.setDescription(UI.getCurrent().getTranslation("project"));
                projRoleResource.setViewClassName("project-view");
                projRoleResource.setAddResourceTranslationKey("addProject");
                projRoleResource.setDeleteResourcesTranslationKey("deleteProjects");
                projRoleResource.setOrgResource(true);
                try {
                    roleResourceRepository.save(projRoleResource);
                    logger.debug("Role resource for projects created.");
                    return projRoleResource;
                } catch (Exception exception) {
                    logger.debug("Error while creating resource for projects.", exception);
                    return  null;
                }
            case Global.ROLE_RESOURCE.FILE_SHARE_ROLE:
                Optional<RoleResource> optionalShareResource = roleResourceRepository.getFileShareResource();
                if (optionalShareResource.isPresent()) {
                    return optionalShareResource.get();
                }
                // create resource for org units
                RoleResource shareRoleResource = new RoleResource();
                shareRoleResource.setName(UI.getCurrent().getTranslation("fileShare"));
                shareRoleResource.setDescription(UI.getCurrent().getTranslation("fileShare"));
                shareRoleResource.setViewClassName("fileShare-view");
                shareRoleResource.setAddResourceTranslationKey("addFileShare");
                shareRoleResource.setDeleteResourcesTranslationKey("deleteFileShares");
                shareRoleResource.setFileShareResource(true);
                try {
                    roleResourceRepository.save(shareRoleResource);
                    logger.debug("Role resource for file shares created.");
                    return shareRoleResource;
                } catch (Exception exception) {
                    logger.debug("Error while creating resource for file shares.", exception);
                    return  null;
                }
            default :
                Optional<RoleResource> optionalResource = roleResourceRepository.getDefaultResource();
                if (optionalResource.isPresent()) {
                    return optionalResource.get();
                }
                // create resource for org units
                RoleResource defaultRoleResource = new RoleResource();
                defaultRoleResource.setName(UI.getCurrent().getTranslation("role"));
                defaultRoleResource.setDescription(UI.getCurrent().getTranslation("role"));
                defaultRoleResource.setViewClassName("role-view");
                defaultRoleResource.setAddResourceTranslationKey("addRole");
                defaultRoleResource.setDeleteResourcesTranslationKey("deleteRole");
                defaultRoleResource.setDefaultResource();
                try {
                    roleResourceRepository.save(defaultRoleResource);
                    logger.debug("Standard role resource created.");
                    return defaultRoleResource;
                } catch (Exception exception) {
                    logger.debug("Error while creating default resource for roles.", exception);
                    return  null;
                }
        }
    }

    public List<RoleResource> getAllRoleResources() {
        return roleResourceRepository.findAll();
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
    public boolean isConnected() {
        if (!isConnected && activeDirectory.getId() != null) {
            try {
                connect();
                isConnected = true;
            } catch (ConnectException exception) {
                isConnected = false;
            }
        }
        return isConnected;
    }

    public List<Person> findAllPersons(@Nullable String stringFilter) {
        if (stringFilter == null || stringFilter.isEmpty()) {
            return personRepository.findAll();
        } else {
            return personRepository.search(stringFilter);
        }
    }

    public List<Person> findAllPersonsWithDepartmentName(String departmentName) {
        if (departmentName == null || departmentName.isEmpty()) {
            return personRepository.findAll();
        } else {
            return personRepository.findByDepartmentNameLike(departmentName);
        }
    }

    public List<Role> findAllRoles(@Nullable String stringFilter, @Nullable RoleResource roleResource) {
        if (stringFilter == null || stringFilter.isEmpty() || roleResource == null) {
            return roleRepository.findAll();
        } else {
            if (roleResource.isOrgResource()) {
                return roleRepository.searchOrg(stringFilter);
            } else if (roleResource.isProjectResource()) {
                return roleRepository.searchProject(stringFilter);
            } else if (roleResource.isFileShareResource()) {
                return roleRepository.searchFileShare(stringFilter);
            }
            return roleRepository.search(stringFilter);
        }
    }

    public List<ADUser> findAllADUsers(String value) {
        return adUserRepository.search(value);
    }

    public List<ADGroup> findAllADGroups(String value) {
        return adGroupRepository.search(value);
    }

    public List<String> getUniqueDepartmentNames() {
        return personRepository.getUniqueDepartmentNames();
    }

    public long countRoles(@Nullable RoleResource roleResource) {
        if (roleResource == null) {
            return roleRepository.count();
        }
        if (roleResource.isOrgResource()) {
            return roleRepository.countByRoleResource_IsOrgResourceTrue();
        }
        if (roleResource.isProjectResource()) {
            return roleRepository.countByRoleResource_IsProjectResourceTrue();
        }
        if (roleResource.isFileShareResource()) {
            return roleRepository.countByRoleResource_IsFileShareResourceTrue();
        }
        return roleRepository.count();
    }
    public long countPersons() {
        return personRepository.count();
    }

    public long countADUsers() {
        return adUserRepository.count();
    }

    public long countADGroups() {
        return adGroupRepository.count();
    }

    public long countAdminGroups() {
        return adGroupRepository.countByIsAdminGroupTrue();
    }

    public long countPasswordNotExpires() {
        return adUserRepository.countByPasswordExpiresFalse();
    }

    public boolean importPersonsFromAD(boolean onlyDifferences) {
        try {
            // load accounts from ad
            List<EntityResponse> responses = getADUser();
            if (onlyDifferences) {
                logger.debug("Not implemented yet");
                return false;
            } else {
                // delete all persons and accounts
                personRepository.deleteAll();
                adUserRepository.deleteAll();
                // create / update AD accounts from response
                // we need the accounts first to link with persons
                for (EntityResponse response : responses) {
                    createOrUpdateADUser(response);
                }
                // create / update persons from response
                for (EntityResponse response : responses) {
                    createOrUpdatePerson(response);
                }
                logger.debug(responses.size() + " persons imported from AD");
                return true;
            }
        } catch (Exception exception) {
            logger.debug("Import persons from AD failed: " + exception.getMessage());
            return false;
        }
    }

    public boolean importUserFromAD(boolean onlyDifferences) {
        try {
            // load accounts from ad
            List<EntityResponse> responses = getADUser();
            if (onlyDifferences) {
                logger.debug("Not implemented yet");
                return false;
            } else {
                // delete all ad accounts
                adUserRepository.deleteAll();
                // create / update AD accounts from response
                // we need the accounts first to link with persons
                for (EntityResponse response : responses) {
                    createOrUpdateADUser(response);
                }
                logger.debug(responses.size() + " user imported from AD");
                return true;
            }
        } catch (Exception exception) {
            logger.debug("Import persons from AD failed: " + exception.getMessage());
            return false;
        }
    }

    public boolean importGroupsFromAD(boolean onlyDifferences) {
        try {
            // load groups from ad
            List<EntityResponse> responses = getADGroups();
            if (onlyDifferences) {
                logger.debug("Not implemented yet");
                return false;
            } else {
                // delete all ad groups
                adGroupRepository.deleteAll();
                // create / update AD accounts from response
                // we need the accounts first to link with persons
                for (EntityResponse response : responses) {
                    createOrUpdateADGroup(response);
                }
                logger.debug(responses.size() + " groups imported from AD");
                return true;
            }
        } catch (Exception exception) {
            logger.debug("Import persons from AD failed: " + exception.getMessage());
            return false;
        }
    }

    public boolean importOrgUnitsFromPersons(boolean onlyDifferences) {
        Optional<RoleResource> optionalResource = roleResourceRepository.getOrgResource();
        RoleResource orgUnitRoleResource;
        if (optionalResource.isPresent()) {
            orgUnitRoleResource = optionalResource.get();
        } else {
            // try to create the role resource for org
            if ((orgUnitRoleResource = getRoleResource(Global.ROLE_RESOURCE.ORG_ROLE)) == null){
                logger.debug("Import org units from persons failed. There are no org unit resource.");
                return false;
            }
        }
        try {
            // try to create org units from person attribute department
            if (onlyDifferences) {
                logger.debug("Not implemented yet");
                return false;
            } else {
                // delete all org units (roles)
                List<Role> orgUnits = roleRepository.findByRoleResource_IsOrgResourceTrueOrderByNameAsc();
                for(Role orgUnit: orgUnits) {
                    deleteRoleComplete(orgUnit);
                }
                // create new org units from department names
                int importedOrgUnits = 0;
                List<String> departmentNames = getUniqueDepartmentNames();
                for (String departmentName: departmentNames) {
                    Role orgUnit = new Role();
                    orgUnit.setRoleResource(orgUnitRoleResource);
                    orgUnit.setName(departmentName);
                    orgUnit.setDescription(UI.getCurrent().getTranslation("importFromPersons.description"));
                    roleRepository.save(orgUnit);
                    orgUnitRoleResource.addRole(orgUnit); // we need to save the relation on role resource
                    roleResourceRepository.save(orgUnitRoleResource);
                    importedOrgUnits++;
                }
                logger.debug(importedOrgUnits + " org units imported from persons");
                return true;
            }
        } catch (Exception exception) {
            logger.debug("Import org units from persons failed.", exception);
            return false;
        }
    }

    public boolean importOrgUnitsFromJSON() {
        return false;
    }

    public boolean importRolesFromGroups() {
        try {
            if (adGroupRepository.count() == 0) {
                logger.debug("Import roles from AD groups failed. There are AD groups in database. Please import from AD first.");
                return false;
            }
            int importedRoles = 0;
            List<ADGroup> adGroups = adGroupRepository.findAll();
            List<Role> roles = roleRepository.findAll();
            for (ADGroup adGroup: adGroups) {
                boolean roleCanBeAdded = true;
                String name = adGroup.getName();
                for (Role role: roles) {
                    if (role.getName().equalsIgnoreCase(name)) {
                        roleCanBeAdded = false;
                    }
                }
                if (roleCanBeAdded) {
                    // set the role resource
                    RoleResource roleResource;
                    if (isProjectByName(name)) {
                        roleResource = getRoleResource(Global.ROLE_RESOURCE.PROJECT_ROLE);
                    } else {
                        roleResource = getRoleResource(Global.ROLE_RESOURCE.DEFAULT_ROLE);
                    }
                    if (roleResource == null) {
                        logger.debug("Role " + name + " can not be imported. Role resource ist null.");
                        break;
                    }
                    // create a role from ad group
                    Role role = new Role();
                    role.setName(name);
                    role.setDescription(adGroup.getDescription());
                    role.setAdminRole(isAdminByName(name)); // admin role
                    role.setRoleResource(roleResource);
                    roleRepository.save(role);
                    roleResource.addRole(role); // we need to save the relation on role resource
                    roleResourceRepository.save(roleResource);
                    importedRoles++;
                }
            }
            logger.debug(importedRoles + " roles imported from AD groups");
            return true;
        } catch (Exception exception) {
            logger.debug("Import roles from AD groups failed: " + exception.getMessage());
            return false;
        }
    }
    public void importRolesFromJSON() {
    }

    public void savePerson(Person person) {
        if (person == null) {
            return;
        }

        // in bidirectional relation the mapping infos not automatically saved?
        // https://stackoverflow.com/questions/47903876

        // roles
        List<Role> assignedRoles = roleRepository.findByPersons_Id(person.getId());
        Set<Role> activesRoles = person.getRoles();
        // remove from unassigned roles
        assignedRoles.removeAll(activesRoles);
        for (Role role: assignedRoles) {
            role.removePerson(person);
            roleRepository.save(role);
        }
        // add to assigned roles
        for (Role role: activesRoles) {
            role.addPerson(person);
            roleRepository.save(role);
        }

        personRepository.save(person);
    }

    public void saveRole(Role role) {
        if (role == null) {
            return;
        }
        roleRepository.save(role);
    }

    public void saveADUser(ADUser adUser) {
        if (adUser == null) {
            return;
        }
        adUserRepository.save(adUser);
    }

    @Transactional
    public void saveADGroup(ADGroup adGroup) {
        if (adGroup == null) {
            return;
        }
        adGroupRepository.save(adGroup);
    }

    @Transactional
    public void deletePerson(Person person) {
        if (person == null) {
            return;
        }
        deletePersonComplete(person);
    }

    public void deleteRole(Role role) {
        if (role == null) {
            return;
        }
        deleteRoleComplete(role);
    }

    public void deleteADUser(ADUser adUser) {
        if (adUser == null) {
            return;
        }
        deleteADUserComplete(adUser);
    }

    public void deleteADGroup(ADGroup adGroup) {
        if (adGroup == null) {
            return;
        }
        deleteADGroupComplete(adGroup);
    }

    @Transactional
    public void deletePersons(List<Person> persons) {
        if (persons == null) {
            return;
        }
        for (Person person: persons) {
            deletePersonComplete(person);
        }
    }

    @Transactional
    public void deleteRoles(List<Role> roles) {
        if (roles == null) {
            return;
        }
        for (Role role: roles) {
            deleteRoleComplete(role);
        }
    }

    @Transactional
    public void deleteADUsers(List<ADUser> adUsers) {
        if (adUsers == null) {
            return;
        }
        for (ADUser adUser: adUsers) {
            deleteADUserComplete(adUser);
        }
    }

    @Transactional
    public void deleteADGroups(List<ADGroup> adGroups) {
        if (adGroups == null) {
            return;
        }
        for (ADGroup adGroup: adGroups) {
            deleteADGroupComplete(adGroup);
        }
    }

    @Transactional
    private void deleteRoleComplete(Role role) {

        Set<Person> persons = role.getPersons();
        for (Person person: persons) {
            person.removeRole(role);
            personRepository.save(person);
        }

        Set<ADGroup> adGroups = role.getAdGroups();
        for (ADGroup adGroup: adGroups) {
            adGroup.removeRole(role);
            adGroupRepository.save(adGroup);
        }

        roleRepository.delete(role);
    }

    // delete all relations with this object
    @Transactional
    private void deletePersonComplete(Person person) {
        Set<Role> roles = person.getRoles();
        for (Role role: roles) {
            role.removePerson(person);
            roleRepository.save(role);
        }

        personRepository.delete(person);
    }

    @Transactional
    private void deleteADUserComplete(ADUser adUser) {
        adUserRepository.delete(adUser);
    }

    @Transactional
    private void deleteADGroupComplete(ADGroup adGroup) {
        adGroupRepository.delete(adGroup);
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

    private List<EntityResponse> getADUser() {
        if (isConnected) {
            try {
                queryRequest.setObjectType(ObjectType.USER);
                //TODO: set filter by config
                // e.g. load only enabled accounts
                queryRequest.addSearchSentence(new QueryAssembler()
                        .addPhrase("userAccountControl", PhraseOperator.EQUAL, "512")
                        .addPhrase("userAccountControl", PhraseOperator.EQUAL, "66048")
                        .closeSentence(SentenceOperator.OR));

                // get all fields needed for entities person and ad account
                queryRequest.addRequestedField(Global.ADAttributes.DISPLAY_NAME);
                queryRequest.addRequestedField(Global.ADAttributes.DESCRIPTION);
                queryRequest.addRequestedField(FieldType.LOGON_NAME);
                queryRequest.addRequestedField(FieldType.DISTINGUISHED_NAME);
                queryRequest.addRequestedField(FieldType.FIRST_NAME);
                queryRequest.addRequestedField(FieldType.LAST_NAME);
                queryRequest.addRequestedField(FieldType.DEPARTMENT);
                queryRequest.addRequestedField(FieldType.EMAIL);
                queryRequest.addRequestedField(FieldType.PHONE_NUMBER);
                queryRequest.addRequestedField(FieldType.MOBILE_PHONE);
                queryRequest.addRequestedField(FieldType.USER_ACCOUNT_CONTROL);
                Connector connector = new Connector(queryRequest);
                QueryResponse queryResponse = connector.execute();
                return queryResponse.getAll();
            } catch (Exception exception) {
                logger.debug("Error occurred while loading users from AD.", exception);
            }
        }
        return new ArrayList<>();
    }

    private List<EntityResponse> getADGroups() {
        if (isConnected) {
            try {
                queryRequest.setObjectType(ObjectType.GROUP);
                // get all fields needed for entity ad group
                queryRequest.addRequestedField(Global.ADAttributes.GROUP_TYPE);
                queryRequest.addRequestedField(Global.ADAttributes.DESCRIPTION);
                queryRequest.addRequestedField(FieldType.COMMON_NAME);
                queryRequest.addRequestedField(FieldType.DISTINGUISHED_NAME);
                Connector connector = new Connector(queryRequest);
                QueryResponse queryResponse = connector.execute();
                return queryResponse.getAll();
            } catch (Exception exception) {
                logger.debug("Error occurred while loading groups from AD.", exception);
            }
        }
        return new ArrayList<>();
    }

    private void createOrUpdateADUser(EntityResponse response) {
        ADUser adUser = new ADUser();
        try {
            boolean isUpdate = countADUsers() > 0;
            List<Field> fields = response.getValue();
            for (Field field : fields) {
                FieldType fieldType = field.getType(); // can be null
                if (fieldType != null) {
                    if (fieldType.equals(FieldType.LOGON_NAME)) {
                        adUser.setLogonName((String) field.getValue());
                    }
                    if (fieldType.equals(FieldType.DISTINGUISHED_NAME)) {
                        adUser.setDistinguishedName((String) field.getValue());
                    }
                    // account enabled / password expiration
                    if (fieldType.equals(FieldType.USER_ACCOUNT_CONTROL)) {
                        adUser.setEnabled(accountEnabled((String) field.getValue()));
                        adUser.setPasswordExpires(passwordExpires((String) field.getValue()));
                    }
                }
            }
            if (isUpdate) {
                Optional<ADUser> optionalADUser = adUserRepository.findFirstByDistinguishedName(adUser.getDistinguishedName());
                if (optionalADUser.isPresent()) {
                    // set the id of the existing object
                    adUser.setId(optionalADUser.get().getId());
                    // set linked person
                    adUser.setPerson(optionalADUser.get().getPerson());
                    // save as updated object
                    adUserRepository.save(adUser);
                } else {
                    adUserRepository.save(adUser);
                }
            } else {
                adUserRepository.save(adUser);
            }
        } catch (Exception exception) {
            logger.debug("Add or update AD user " + adUser.getLogonName() + " failed: " + exception.getMessage());
        }
    }

    private void createOrUpdateADGroup(EntityResponse response) {
        ADGroup adGroup = new ADGroup();
        try {
            boolean isUpdate = countADUsers() > 0;
            List<Field> fields = response.getValue();
            for (Field field : fields) {
                FieldType fieldType = field.getType(); // can be null
                if (fieldType != null) {
                    if (fieldType.equals(FieldType.COMMON_NAME)) {
                        String name = (String) field.getValue();
                        adGroup.setName(name);
                        // check for "admin group"
                        adGroup.setAdminGroup(isAdminByName(name));
                    }
                    if (fieldType.equals(FieldType.DISTINGUISHED_NAME)) {
                        adGroup.setDistinguishedName((String) field.getValue());
                    }
                }
                if (field.getName().equalsIgnoreCase(Global.ADAttributes.DESCRIPTION)) {
                    // the length is set to 255 characters
                    String description = (String) field.getValue();
                    if (description.length() > Global.IMPORT_SETTINGS.MAX_STRING_LENGTH) {
                        adGroup.setDescription(description.substring(0, Global.IMPORT_SETTINGS.MAX_STRING_LENGTH - 1));
                    } else {
                        adGroup.setDescription(description);
                    }

                }
                // group area / group type
                if (field.getName().equalsIgnoreCase(Global.ADAttributes.GROUP_TYPE)) {
                    adGroup.setGroupArea(groupAreaFromString((String) field.getValue()));
                    adGroup.setGroupType(groupTypeFromString((String) field.getValue()));
                }
            }
            if (isUpdate) {
                Optional<ADGroup> optionalADAGroup = adGroupRepository.findByDistinguishedName(adGroup.getDistinguishedName());
                if (optionalADAGroup.isPresent()) {
                    // set the id of the existing object
                    adGroup.setId(optionalADAGroup.get().getId());
                    // set linked roles
                    adGroup.setRoles(optionalADAGroup.get().getRoles());
                    // save as updated object
                    adGroupRepository.save(adGroup);
                } else {
                    adGroupRepository.save(adGroup);
                }
            } else {
                adGroupRepository.save(adGroup);
            }
        } catch (Exception exception) {
            logger.debug("Add or update AD group " + adGroup.getName() + " failed: " + exception.getMessage());
        }
    }

    private void createOrUpdatePerson(EntityResponse response) {
        Person person = new Person();
        try {
            boolean isUpdate = countPersons() > 0;
            List<Field> fields = response.getValue();
            for (Field field : fields) {
                FieldType fieldType = field.getType(); // can be null
                if (fieldType != null) {
                    // set values
                    if (fieldType.equals(FieldType.LOGON_NAME)) {
                        person.setCentralAccountName((String) field.getValue());
                        if (person.getLastName().length() == 0) {
                            // last name of person must be not empty
                            person.setLastName((String) field.getValue());
                        }
                    }
                    if (fieldType.equals(FieldType.FIRST_NAME)) {
                        person.setFirstName((String) field.getValue());
                    }
                    if (fieldType.equals(FieldType.LAST_NAME)) {
                        person.setLastName((String) field.getValue());
                    }
                    if (fieldType.equals(FieldType.EMAIL)) {
                        person.setEmailAddress((String) field.getValue());
                    }
                    if (fieldType.equals(FieldType.PHONE_NUMBER)) {
                        person.setPhoneNumber((String) field.getValue());
                    }
                    if (fieldType.equals(FieldType.MOBILE_PHONE)) {
                        person.setMobilePhoneNumber((String) field.getValue());
                    }
                    if (fieldType.equals(FieldType.DEPARTMENT)) {
                        person.setDepartmentName((String) field.getValue());
                    }
                }
                // last name of person must be not empty
                if (field.getName().equalsIgnoreCase(Global.ADAttributes.DISPLAY_NAME)) {
                    if (person.getLastName().length() == 0) {
                        person.setLastName((String) field.getValue());
                    }
                }
                // description
                if (field.getName().equalsIgnoreCase(Global.ADAttributes.DESCRIPTION)) {
                    person.setDescription((String) field.getValue());
                }
            }
            if (isUpdate) {
                Optional<Person> optionalPerson = personRepository.findByAdUsers_LogonName(person.getCentralAccountName());
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
        } catch (Exception exception) {
            logger.debug("Add or update person " + person.getCentralAccountName() + " failed: " + exception.getMessage());
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

    private boolean passwordExpires(String flag) {
        /*
            AD attribute userAccountControl contains the flag
            512=Enabled
            514= Disabled
            66048 = Enabled, password never expires
            66050 = Disabled, password never expires
         */
        return flag.compareToIgnoreCase("66048") != 0 && flag.compareToIgnoreCase("66050") != 0;
    }

    private int groupAreaFromString(String value) {
        /*
            2   Global distribution group
            4   Domain local distribution group
            8   Universal distribution group
            -2147483646     Global security group
            -2147483644     Domain local security group
            -2147483640     Universal security group
        */
        if (value.compareToIgnoreCase("-2147483640") == 0 || value.compareToIgnoreCase("8") == 0) {
            return Global.ADGroupArea.UNIVERSAL;
        }
        if (value.compareToIgnoreCase("-2147483646") == 0 || value.compareToIgnoreCase("2") == 0) {
            return Global.ADGroupArea.GLOBAL;
        }
        return Global.ADGroupArea.LOCAL;
    }

    private int groupTypeFromString(String value) {
        /*
            2   Global distribution group
            4   Domain local distribution group
            8   Universal distribution group
            -2147483646     Global security group
            -2147483644     Domain local security group
            -2147483640     Universal security group
        */
        if (value.compareToIgnoreCase("2") == 0 ||
                value.compareToIgnoreCase("4") == 0 ||
                value.compareToIgnoreCase("8") == 0) {
            return Global.ADGroupType.DISTRIBUTION;
        }
        return Global.ADGroupType.SECURITY;
    }

    private boolean isAdminByName(String name) {
        Pattern pattern = Pattern.compile(Pattern.quote(Global.IMPORT_SETTINGS.ADMIN_GROUP_TEXT),
                Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(name);
        return matcher.find();
    }

    private boolean isProjectByName(String name) {
        Pattern pattern = Pattern.compile(Pattern.quote(Global.IMPORT_SETTINGS.PROJECT_ROLE_TEXT),
                Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(name);
        return matcher.find();
    }
}
