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
import org.apache.directory.api.util.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import java.net.ConnectException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
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
            isConnected = connect();
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
                orgRoleResource.setDeleteResourcesTranslationKey("deleteOrg");
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
                projRoleResource.setProjectResource(true);
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
            // try to (re)-connect
            isConnected = connect();
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

    public List<Person> findAllEmployees(@Nullable String stringFilter) {
        if (stringFilter == null || stringFilter.isEmpty()) {
            return personRepository.findByIsEmployeeTrueOrderByLastNameAscFirstNameAsc();
        } else {
            return personRepository.searchEmployees(stringFilter);
        }
    }

    public List<Person> findAllPersonsWithDepartmentName(String departmentName) {
        if (departmentName == null || departmentName.isEmpty()) {
            return personRepository.findAll();
        } else {
            return personRepository.findByDepartmentNameOrderByLastNameAsc(departmentName);
        }
    }

    public List<Role> findAllRoles(@Nullable String stringFilter, @Nullable RoleResource roleResource) {
        if ((stringFilter != null && !stringFilter.isEmpty()) && roleResource != null) {
            if (roleResource.isOrgResource()) {
                return roleRepository.searchOrg(stringFilter);
            } else if (roleResource.isProjectResource()) {
                return roleRepository.searchProject(stringFilter);
            } else if (roleResource.isFileShareResource()) {
                return roleRepository.searchFileShare(stringFilter);
            }
            return roleRepository.search(stringFilter);
        }
        if ((stringFilter == null || stringFilter.isEmpty()) && roleResource != null) {
            if (roleResource.isOrgResource()) {
                return roleRepository.findByRoleResource_IsOrgResourceTrueOrderByNameAsc();
            } else if (roleResource.isProjectResource()) {
                return roleRepository.findByRoleResource_IsProjectResourceTrueOrderByNameAsc();
            } else if (roleResource.isFileShareResource()) {
                return roleRepository.findByRoleResource_IsFileShareResourceTrueOrderByNameAsc();
            }
        }
        if ((stringFilter != null && !stringFilter.isEmpty()) && roleResource == null) {
            return roleRepository.search(stringFilter);
        }
        return roleRepository.findAll();
    }

    public List<ADUser> findAllADUsers(@Nullable String value) {
        if (value == null || value.isEmpty()) {
            return adUserRepository.findAll();
        } else {
            return adUserRepository.search(value);
        }
    }

    public List<ADUser> findAllManageableADUsers() {
        return adUserRepository.findByIsRoleManagedTrueOrderByLogonNameAsc();
    }

    public List<ADGroup> findAllADGroups(@Nullable String value) {
        if (value == null || value.isEmpty()) {
            return adGroupRepository.findAll();
        } else {
            return adGroupRepository.search(value);
        }
    }

    public List<ADGroup> findAllADGroupsForPersons(Set<Person> persons) {
        List<ADGroup> assignedADGroups = new ArrayList<>();
        if (persons == null) {
            return assignedADGroups;
        }
        for (Person person: persons) {
            // get the assigned ad users
            Set<ADUser> personAccountList = person.getADUsers();
            for (ADUser adUser: personAccountList) {
                // get the assigned ad groups
                List<ADGroup> adGroups = getADGroupsForUser(adUser);
                // add as possible ad group for the role
                assignedADGroups.addAll(adGroups);
            }
        }
        return assignedADGroups;
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

    public boolean importPersonsFromAD() {
        try {
            // load accounts from ad
            List<EntityResponse> responses = getADUserEntities();
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
            // link AD accounts with persons
            List<Person> persons = findAllPersons(null);
            List<ADUser> adUsers = findAllADUsers(null);
            for (Person person: persons) {
                String accountName = person.getCentralAccountName();
                // search in AD accounts for login name
                for (ADUser adUser:adUsers) {
                    if (adUser.getLogonName().equalsIgnoreCase(accountName)) {
                        person.addADUser(adUser);
                        personRepository.save(person);
                    }
                }
            }
            logger.debug(responses.size() + " persons imported from AD");
            return true;
        } catch (Exception exception) {
            logger.debug("Import persons from AD failed: " + exception.getMessage());
            return false;
        }
    }

    public boolean importUserFromAD() {
        try {
            // load accounts from AD
            List<EntityResponse> responses = getADUserEntities();
            // create / update AD accounts from response
            // we need the accounts first to link with persons and ad groups
            for (EntityResponse response : responses) {
                createOrUpdateADUser(response);
            }
            logger.debug(responses.size() + " user imported from AD");
            return true;
        } catch (Exception exception) {
            logger.debug("Import persons from AD failed: " + exception.getMessage());
            return false;
        }
    }

    public boolean importGroupsFromAD() {
        try {
            // load groups from AD
            List<EntityResponse> responses = getADGroupEntities();
            // create / update AD groups from response
            // if AD users available - link by membership
            for (EntityResponse response : responses) {
                createOrUpdateADGroup(response);
            }
            logger.debug(responses.size() + " groups imported from AD");
            return true;
        } catch (Exception exception) {
            logger.debug("Import persons from AD failed: " + exception.getMessage());
            return false;
        }
    }

    @Transactional
    public boolean importOrgRolesFromPersons() {
        Optional<RoleResource> optionalResource = roleResourceRepository.getOrgResource();
        RoleResource orgRoleRoleResource;
        if (optionalResource.isPresent()) {
            orgRoleRoleResource = optionalResource.get();
        } else {
            // try to create the role resource for org
            if ((orgRoleRoleResource = getRoleResource(Global.ROLE_RESOURCE.ORG_ROLE)) == null){
                logger.debug("Import org units from persons failed. There are no org unit resource.");
                return false;
            }
        }
        try {
            // try to create org units from person attribute department
            // get all existing org from database
            int importedOrgRoles = 0;
            List<Role> existingOrgRoles = roleRepository.findByRoleResource_IsOrgResourceTrueOrderByNameAsc();
            List<String> departmentNames = getUniqueDepartmentNames();
            // filter the list of department names
            for(Role existingOrgRole: existingOrgRoles ) {
                String departmentName = existingOrgRole.getName();
                departmentNames.remove(departmentName);
            }
            // create only new department roles
            for (String departmentName: departmentNames) {
                Role orgRole = new Role();
                orgRole.setRoleResource(orgRoleRoleResource);
                orgRole.setName(departmentName);
                orgRole.setDescription(UI.getCurrent().getTranslation("importFromPersons.description"));
                roleRepository.save(orgRole);
                orgRoleRoleResource.addRole(orgRole); // we need to save the relation on role resource
                roleResourceRepository.save(orgRoleRoleResource);
                importedOrgRoles++;
            }
            logger.debug(importedOrgRoles + " organizations imported from persons");

            // set employee flag for persons with equal name of department
            // set managed flag for AD user of this persons
            List<Person> possibleEmployees = personRepository.findAll();
            List<Role> orgRoles = roleRepository.findByRoleResource_IsOrgResourceTrueOrderByNameAsc();
            for (Person employee: possibleEmployees) {
                String departmentName = employee.getDepartmentName();
                for (Role orgRole: orgRoles) {
                    if (orgRole.getName().equalsIgnoreCase(departmentName)) {

                        // set the assigner AD users as managed
                        Set<ADUser> adUsers = employee.getADUsers();
                        for (ADUser adUser: adUsers) {
                            adUser.setRoleManaged(true);
                            adUserRepository.save(adUser);
                        }

                        // add person as employee to the org role
                        employee.addRole(orgRole);
                        employee.setEmployee(true);
                        savePerson(employee);

                        logger.debug("Person " + employee.getCentralAccountName() +
                                " added as employee to the department with name " + departmentName);
                    }
                }
            }
            logger.debug(importedOrgRoles + " organizations imported from persons");
            return true;
        } catch (Exception exception) {
            logger.debug("Import org units from persons failed.", exception);
            return false;
        }
    }

    public boolean importOrgRolesFromJSON() {
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

    public boolean importRolesFromGroups(RoleResource roleResource) {
        try {
            if (adGroupRepository.count() == 0) {
                logger.debug("Import roles from AD groups failed. There are no AD groups in database. Please import from AD first.");
                return false;
            }
            if (roleResource == null) {
                logger.debug("Import roles from AD groups failed. The role resource is null.");
                return false;
            }
            int importedRoles = 0;
            List<ADGroup> adGroups;
            if (roleResource.isProjectResource()) {
                adGroups = adGroupRepository.search(Global.IMPORT_SETTINGS.PROJECT_ROLE_TEXT);
            } else if (roleResource.isFileShareResource()) {
                adGroups = adGroupRepository.search(Global.IMPORT_SETTINGS.FILE_SHARE_ROLE_TEXT);
            } else {
                adGroups = new ArrayList<>(); // empty list
            }
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

    @Transactional
    public boolean savePerson(Person person) {
        if (person == null) {
            logger.debug("Can not saved a zero person.");
            return false;
        }

        // in bidirectional relation the mapping infos not automatically saved?
        // https://stackoverflow.com/questions/47903876
        try {
            // roles
            Set<Role> roles = person.getRoles();
            for (Role role: roles) {
                role.addPerson(person);
                roleRepository.save(role);
            }
            // AD users
            Set<ADUser> adUsers = person.getADUsers();
            for (ADUser adUser: adUsers) {
                adUser.setPerson(person);
                adUserRepository.save(adUser);
            }

            personRepository.save(person);
            return true;
        } catch (Exception exception) {
            logger.debug("Error while saving person " + person.getCentralAccountName() + " .", exception);
            return false;
        }
    }

    @Transactional
    public boolean saveRole(Role role) {
        if (role == null) {
            logger.debug("Can not saved a zero role.");
            return false;
        }

        // in bidirectional relation the mapping infos not automatically saved?
        // https://stackoverflow.com/questions/47903876
        try {
            // persons
            Set<Person> persons = role.getPersons();
            for (Person person: persons) {
                person.addRole(role);
                roleRepository.save(role);
            }
            // AD users
            Set<ADUser> adUsers = role.getADUsers();
            for (ADUser adUser: adUsers) {
                adUser.addRole(role);
                adUserRepository.save(adUser);
            }
            // AD users
            Set<ADGroup> adGroups = role.getADGroups();
            for (ADGroup adGroup: adGroups) {
                adGroup.addRole(role);
                adGroupRepository.save(adGroup);
            }

            roleRepository.save(role);
            return true;
        } catch (Exception exception) {
            logger.debug("Error while saving role " + role.getName() + " .", exception);
            return false;
        }
    }

    @Transactional
    public boolean saveADUser(ADUser adUser) {
        if (adUser == null) {
            logger.debug("Can not saved a zero AD user.");
            return false;
        }

        // in bidirectional relation the mapping infos not automatically saved?
        // https://stackoverflow.com/questions/47903876
        try {
            // roles
            Set<Role> roles = adUser.getRoles();
            for (Role role: roles) {
                role.addADUser(adUser);
                roleRepository.save(role);
            }

            adUserRepository.save(adUser);
            return true;
        } catch (Exception exception) {
            logger.debug("Error while saving AD user " + adUser.getDistinguishedName() + " .", exception);
            return false;
        }
    }

    @Transactional
    public boolean saveADGroup(ADGroup adGroup) {
        if (adGroup == null) {
            logger.debug("Can not saved a zero AD group.");
            return false;
        }

        // in bidirectional relation the mapping infos not automatically saved?
        // https://stackoverflow.com/questions/47903876
        try {
            // roles
            Set<Role> roles = adGroup.getRoles();
            for (Role role: roles) {
                role.addADGroup(adGroup);
                roleRepository.save(role);
            }

            adGroupRepository.save(adGroup);
            return true;
        } catch (Exception exception) {
            logger.debug("Error while saving AD group " + adGroup.getName() + " .", exception);
            return false;
        }
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
        try {
            Set<Person> persons = role.getPersons();
            for (Person person: persons) {
                person.removeRole(role);
                personRepository.save(person);
            }

            Set<ADUser> adUsers = role.getADUsers();
            for (ADUser adUser: adUsers) {
                adUser.removeRole(role);
                adUserRepository.save(adUser);
            }

            Set<ADGroup> adGroups = role.getADGroups();
            for (ADGroup adGroup: adGroups) {
                adGroup.removeRole(role);
                adGroupRepository.save(adGroup);
            }

           roleRepository.delete(role);
            logger.debug("Role " + role.getName() + " deleted.");
        } catch (Exception exception) {
            logger.debug("Error while deleting role " + role.getName(), exception);
        }
    }

    // delete all relations with this object
    @Transactional
    private void deletePersonComplete(Person person) {
        try {
            Set<Role> roles = person.getRoles();
            for (Role role: roles) {
                role.removePerson(person);
                roleRepository.save(role);
            }

            personRepository.delete(person);
            logger.debug("Person " + person.getCentralAccountName() + " deleted.");
        } catch (Exception exception) {
            logger.debug("Error while deleting person " + person.getCentralAccountName(), exception);
        }
    }

    @Transactional
    private void deleteADUserComplete(ADUser adUser) {
        try {
            Set<Role> roles = adUser.getRoles();
            for (Role role: roles) {
                role.removeADUser(adUser);
                roleRepository.save(role);
            }
            adUserRepository.delete(adUser);
            logger.debug("AD user " + adUser.getDistinguishedName() + " deleted.");
        } catch (Exception exception) {
            logger.debug("Error while deleting AD user " + adUser.getDistinguishedName(), exception);
        }
    }

    @Transactional
    private void deleteADGroupComplete(ADGroup adGroup) {
        try {
            adGroupRepository.delete(adGroup);
            logger.debug("AD group " + adGroup.getDistinguishedName() + " deleted.");
        } catch (Exception exception) {
            logger.debug("Error while deleting AD group " + adGroup.getDistinguishedName(), exception);
        }
    }

    private boolean connect() {
        try {
            if (isConnected) {
                return true;
            }
            if (activeDirectory == null) {
                String errorMessage = UI.getCurrent().getTranslation("error.domain.connection.ad.empty");
                logger.debug(errorMessage);
                return false;
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
                logger.debug("The connection to the Active Directory failed.");
                return false;
            }
            queryRequest.addEndpoint(endpoint);
            return isConnected = true;
        } catch (Exception exception) {
            logger.debug("The connection to the Active Directory failed.", exception);
            return false;
        }
    }

    private List<EntityResponse> getADUserEntities() {
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
                queryRequest.addRequestedField(Global.ADAttributes.ACCOUNT_EXPIRES);
                queryRequest.addRequestedField(FieldType.LOGON_NAME);
                queryRequest.addRequestedField(FieldType.DISTINGUISHED_NAME);
                queryRequest.addRequestedField(FieldType.FIRST_NAME);
                queryRequest.addRequestedField(FieldType.LAST_NAME);
                queryRequest.addRequestedField(FieldType.DEPARTMENT);
                queryRequest.addRequestedField(FieldType.EMAIL);
                queryRequest.addRequestedField(FieldType.PHONE_NUMBER);
                queryRequest.addRequestedField(FieldType.MOBILE_PHONE);
                queryRequest.addRequestedField(FieldType.USER_ACCOUNT_CONTROL);
                queryRequest.addRequestedField(FieldType.CREATION_TIME); // possible employee entry date
                Connector connector = new Connector(queryRequest);
                QueryResponse queryResponse = connector.execute();
                logger.debug(queryResponse.getAll().size() + " user objects queried from AD.");
                return queryResponse.getAll();
            } catch (Exception exception) {
                logger.debug("Error occurred while loading users from AD.", exception);
            }
        }
        return new ArrayList<>();
    }

    private List<EntityResponse> getADGroupEntities() {
        if (isConnected) {
            try {
                queryRequest.setObjectType(ObjectType.GROUP);
                // get all fields needed for entity ad group
                queryRequest.addRequestedField(Global.ADAttributes.GROUP_TYPE);
                queryRequest.addRequestedField(Global.ADAttributes.DESCRIPTION);
                queryRequest.addRequestedField(FieldType.COMMON_NAME);
                queryRequest.addRequestedField(FieldType.DISTINGUISHED_NAME);
                queryRequest.addRequestedField(FieldType.MEMBER);
                Connector connector = new Connector(queryRequest);
                QueryResponse queryResponse = connector.execute();
                logger.debug(queryResponse.getAll().size() + " group objects queried from AD.");
                return queryResponse.getAll();
            } catch (Exception exception) {
                logger.debug("Error occurred while loading groups from AD.", exception);
            }
        }
        return new ArrayList<>();
    }

    private List<ADGroup> getADGroupsForUser(ADUser adUser) {
        List<ADGroup> adGroups = new ArrayList<>();
        if (isConnected) {
            try {
                queryRequest.setObjectType(ObjectType.USER);
                // get AD user by distinguished name
                queryRequest.addSearchSentence(new QueryAssembler()
                        .addPhrase(FieldType.DISTINGUISHED_NAME, PhraseOperator.EQUAL, adUser.getDistinguishedName())
                        .closeSentence());
                // get membership for the user
                queryRequest.addRequestedField(FieldType.MEMBER);
                Connector connector = new Connector(queryRequest);
                QueryResponse queryResponse = connector.execute();
                List<EntityResponse> responses =  queryResponse.getAll();
                if (responses.size() == 0) {
                    return adGroups;
                }
                if (responses.size() > 1) {
                    logger.debug("More than one AD user found with distinguished name " + adUser.getDistinguishedName());
                    return adGroups;
                }
                for (EntityResponse response: responses) {
                    List<Field> fields = response.getValue();
                    for (Field field : fields) {
                        FieldType fieldType = field.getType(); // can be null
                        if (fieldType != null) {
                            if (fieldType.equals(FieldType.MEMBER)) {
                                Object object = field.getValue();
                                if (object instanceof List) {
                                    System.out.println("JA");
                                }
                            }
                        }
                    }
                }

            } catch (Exception exception) {
                logger.debug("Error occurred while loading groups from AD.", exception);
                return adGroups;
            }
        }
        return adGroups;
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
            logger.debug("Add or update AD user " + adUser.getLogonName() + " .");
        } catch (Exception exception) {
            logger.debug("Add or update AD user " + adUser.getLogonName() + " failed: " + exception.getMessage());
        }
    }

    @Transactional
    private boolean createOrUpdateADGroup(EntityResponse response) {
        ADGroup adGroup = new ADGroup();
        try {
            boolean isUpdate = countADGroups() > 0;
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
                    Set<Role> roles = optionalADAGroup.get().getRoles();
                    adGroup.setRoles(roles);
                    for (Role role: roles) {
                        role.addADGroup(adGroup);
                        roleRepository.save(role);
                    }
                    // save as updated object
                    adGroupRepository.save(adGroup);
                } else {
                    adGroupRepository.save(adGroup);
                }
            } else {
                adGroupRepository.save(adGroup);
            }
            logger.debug("Add or update AD group " + adGroup.getName() + " .");
            return true;
        } catch (Exception exception) {
            logger.debug("Add or update AD group " + adGroup.getName() + " failed: " + exception.getMessage());
            return false;
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
                    if (fieldType.equals(FieldType.CREATION_TIME)) {
                        // possible entry date
                        // whenCreated in format e.g. 20111101000413.0Z
                        try {
                            String adDateString = ((String) field.getValue()).substring(0, 8);
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
                            Date convertedEntryDate = sdf.parse(adDateString);
                            LocalDate entryDate = Instant.ofEpochMilli(convertedEntryDate.getTime())
                                    .atZone(ZoneId.systemDefault())
                                    .toLocalDate();
                            person.setEntryDate(entryDate);

                        } catch (Exception exception) {
                            person.setEntryDate(Global.EMPLOYEE_DEFAULT_VALUES.ENTRY_DATE);
                            logger.debug("Error while get the entry date from AD attribute 'whenCreated'");
                        }

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
                // possible exit date
                if (field.getName().equalsIgnoreCase(Global.ADAttributes.ACCOUNT_EXPIRES)) {
                    // DateUtils.convertIntervalDate converts the 18-digit Active Directory timestamps,
                    // also named 'Windows NT time format' or 'Win32 FILETIME or SYSTEMTIME'.
                    try {
                        String adDateString = (String) field.getValue();
                        Date convertedExitDate = DateUtils.convertIntervalDate(adDateString);
                        LocalDate exitDate = Instant.ofEpochMilli(convertedExitDate.getTime())
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate();
                        // if account does not expire -> year is setting to 30828
                        if (exitDate.isBefore(Global.EMPLOYEE_DEFAULT_VALUES.EXIT_DATE)){
                            person.setExitDate(exitDate);
                        } else {
                            person.setExitDate(Global.EMPLOYEE_DEFAULT_VALUES.EXIT_DATE);
                        }
                    } catch (Exception exception) {
                        person.setExitDate(Global.EMPLOYEE_DEFAULT_VALUES.EXIT_DATE);
                        logger.debug("Error while get the exit date from AD attribute "
                                + Global.ADAttributes.ACCOUNT_EXPIRES, exception);
                    }
                }
            }
            if (isUpdate) {
                Optional<Person> optionalPerson = personRepository.findByAdUsers_LogonName(person.getCentralAccountName());
                if (optionalPerson.isPresent()) {
                    Person p = optionalPerson.get();
                    // set the id of the existing object
                    person.setId(p.getId());
                    // set the linked ad accounts
                    person.setADUsers(p.getADUsers());
                    // update central account - if was ad accounts linked
                    if (person.getADUsers().size() > 0) {
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
