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
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import java.net.ConnectException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
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
    private org.springframework.security.core.userdetails.User sessionUser;
    private Endpoint endpoint;
    private boolean isConnected;
    private ActiveDirectory activeDirectory;
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
            connect();
        } else {
            activeDirectory = new ActiveDirectory();
        }
    }

    public @Nullable RoleResource getRoleResource(int type) {
        switch (type) {
            case Global.ROLE_RESOURCE.ORG_ROLE -> {
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
                    return null;
                }
            }
            case Global.ROLE_RESOURCE.PROJECT_ROLE -> {
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
                    return null;
                }
            }
            case Global.ROLE_RESOURCE.FILE_SHARE_ROLE -> {
                Optional<RoleResource> optionalShareResource = roleResourceRepository.getFileShareResource();
                if (optionalShareResource.isPresent()) {
                    return optionalShareResource.get();
                }
                // create resource for file shares
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
                    return null;
                }
            }
            case Global.ROLE_RESOURCE.EMAIL_RESOURCE_ROLE -> {
                Optional<RoleResource> optionalEmailResource = roleResourceRepository.getEmailResource();
                if (optionalEmailResource.isPresent()) {
                    return optionalEmailResource.get();
                }
                // create resource for email resources
                RoleResource emailRoleResource = new RoleResource();
                emailRoleResource.setName(UI.getCurrent().getTranslation("emailResource"));
                emailRoleResource.setDescription(UI.getCurrent().getTranslation("emailResource"));
                emailRoleResource.setViewClassName("email-view");
                emailRoleResource.setAddResourceTranslationKey("addEmailRole");
                emailRoleResource.setDeleteResourcesTranslationKey("deleteEmailRoles");
                emailRoleResource.setEmailResource(true);
                try {
                    roleResourceRepository.save(emailRoleResource);
                    logger.debug("Role resource for file shares created.");
                    return emailRoleResource;
                } catch (Exception exception) {
                    logger.debug("Error while creating resource for e-mail.", exception);
                    return null;
                }
            }
            default -> {
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
                    return null;
                }
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
        return isConnected;
    }

    public List<Person> findAllPersons(@Nullable String stringFilter) {
        if (stringFilter == null || stringFilter.isEmpty()) {
            return personRepository.findAll(Sort.by(Sort.Direction.ASC, "lastName"));
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
            } else if (roleResource.isEmailResource()) {
                return roleRepository.searchMailResource(stringFilter);
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
            } else if (roleResource.isEmailResource()) {
                return roleRepository.findByRoleResource_IsEmailResourceTrueOrderByNameAsc();
            }
        }
        if (stringFilter != null && !stringFilter.isEmpty()) {
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

    public boolean updatePersonsFromAD() {
        try {
            int added = 0, updated = 0;
            // load accounts from AD
            List<EntityResponse> responses = getADUserEntities();
            // create / update AD user from response
            // we need the accounts first to link with persons
            for (EntityResponse response : responses) {
                boolean[] returnValues = createOrUpdateADUser(response);
                if (returnValues[0]) {
                    added++;
                }
                if (returnValues[1]) {
                    updated++;
                }
            }
            added = 0;
            updated = 0;
            addLogEntry(added + " users added, " + updated + " users updated from AD");
            // create / update persons from response
            for (EntityResponse response : responses) {
                boolean[] returnValues = createOrUpdatePerson(response);
                if (returnValues[0]) {
                    added++;
                }
                if (returnValues[1]) {
                    updated++;
                }
            }
            // TODO: Logging
            addLogEntry(added + " persons added, " + updated + " persons updated");
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
            return true;
        } catch (Exception exception) {
            logger.debug("Update persons from AD failed: " + exception.getMessage());
            return false;
        }
    }

    public boolean updateUserFromAD() {
        try {
            int added = 0, updated = 0;
            // load accounts from AD
            List<EntityResponse> responses = getADUserEntities();
            // create / update AD accounts from response
            // we need the accounts first to link with persons and ad groups
            for (EntityResponse response : responses) {
                boolean[] returnValues = createOrUpdateADUser(response);
                if (returnValues[0]) {
                    added++;
                }
                if (returnValues[1]) {
                    updated++;
                }
            }
            addLogEntry(added + " users added, " + updated + " users updated from AD");
            return true;
        } catch (Exception exception) {
            logger.debug("Update users from AD failed.", exception);
            return false;
        }
    }

    public boolean updateGroupsFromAD() {
        try {
            int added = 0, updated = 0;
            // load groups from AD
            List<EntityResponse> responses = getADGroupEntities();
            // create / update AD groups from response
            // if AD users available - link by membership
            for (EntityResponse response : responses) {
                boolean[] returnValues = createOrUpdateADGroup(response);
                if (returnValues[0]) {
                    added++;
                }
                if (returnValues[1]) {
                    updated++;
                }
            }
            addLogEntry(added + " groups added, " + updated + " groups updated from AD");
            return true;
        } catch (Exception exception) {
            logger.debug("Updateing groups from AD failed.", exception);
            return false;
        }
    }

    @Transactional
    public boolean updateOrgRolesFromPersons() {
        Optional<RoleResource> optionalResource = roleResourceRepository.getOrgResource();
        RoleResource orgRoleRoleResource;
        if (optionalResource.isPresent()) {
            orgRoleRoleResource = optionalResource.get();
        } else {
            // try to create the role resource for org
            if ((orgRoleRoleResource = getRoleResource(Global.ROLE_RESOURCE.ORG_ROLE)) == null){
                logger.debug("Update organisations from persons failed. There are no organisations resource.");
                return false;
            }
        }
        try {
            // try to create org units from person attribute department
            int updatedOrgRolesCount = 0;
            List<String> departmentNames = getUniqueDepartmentNames();
            List<Role> allRoles = findAllRoles(null, null);
            for (Role role: allRoles) {
                String departmentName = role.getName();
                if (departmentNames.contains(departmentName)) {
                    // set as organisation role
                    role.setRoleResource(orgRoleRoleResource);
                    roleRepository.save(role);
                    departmentNames.remove(departmentName); // remove from list
                    addLogEntry("Existing Role \"" + role.getName() + "\" updated as organisation role.");
                }
            }
            // add roles for remaining department names
            for (String departmentName: departmentNames) {
                Role orgRole = new Role();
                orgRole.setRoleResource(orgRoleRoleResource);
                orgRole.setName(departmentName);
                orgRole.setDescription(UI.getCurrent().getTranslation("updateFromPersons.description"));
                roleRepository.save(orgRole);
                addLogEntry("Role \"" + orgRole.getName() + "\" added as organisation role.");
                updatedOrgRolesCount++;
            }
            logger.debug(updatedOrgRolesCount + " organizations added or updated from persons");

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
                        personRepository.save(employee);

                        orgRole.addPerson(employee);
                        roleRepository.save(orgRole);

                        addLogEntry("Person \"" + employee.getCentralAccountName() +
                                "\" added as employee to the department with name " + departmentName);
                    }
                }
            }
            return true;
        } catch (Exception exception) {
            logger.debug("Updating organisations from persons failed.", exception);
            return false;
        }
    }

    public boolean assignPersonsToRoles(List<Person> persons) {
        try {
            int assignedPersonsCount = 0;
            int assignedRolesCount = 0;
            if (persons == null) {
                return false;
            }
            for (Person person : persons) {
                // get managed AD users of person
                List<ADUser> assignedADUsers = adUserRepository.findByPerson_IdAndIsRoleManagedTrue(person.getId());
                for (ADUser assignedADUser : assignedADUsers) {
                    // get the AD groups of AD user from AD
                    List<ADGroup> adGroupsOfAssignedADUser = getADGroupsForUser(assignedADUser);
                    for (ADGroup adGroup : adGroupsOfAssignedADUser) {
                        // get the role with the name of the AD group
                        Optional<Role> optionalRole = roleRepository.findFirstByName(adGroup.getName());
                        if (optionalRole.isPresent()) {
                            // add the person and the AD group to the role
                            Role role = optionalRole.get();
                            role.addPerson(person);
                            role.addADGroup(adGroup);
                            roleRepository.save(role);
                            person.addRole(role);
                            personRepository.save(person);
                            assignedRolesCount++;
                            addLogEntry("Person \"" + person.getCentralAccountName() + "\" and \"" +
                                    adGroup.getName() + "\" added to the role \"" + role.getName() + "\"");
                        }
                    }
                    assignedPersonsCount++;
                }
            }
            logger.debug(assignedPersonsCount + " from " + persons.size() + " assigned to " + assignedRolesCount + " roles");
            return true;
        } catch (Exception exception) {
            logger.debug("Error while assign persons to roles automatically.", exception);
            return false;
        }
    }

    @Transactional
    public boolean updateRolesFromGroups() {
        try {
            if (adGroupRepository.count() == 0) {
                logger.debug("Update roles from AD groups failed. There are AD groups in database. Please import from AD first.");
                return false;
            }
            int added = 0, updated = 0;
            List<ADGroup> adGroups = adGroupRepository.findAll();
            for (ADGroup adGroup: adGroups) {
                String name = adGroup.getName();
                Optional<Role> optionalRole = roleRepository.findFirstByName(name);
                Role role;
                if (optionalRole.isPresent()) {
                    // update
                    role = optionalRole.get();
                    updated++;
                } else {
                    // add new role from AD group
                    role = new Role();
                    role.setName(adGroup.getName());
                    added++;
                }
                role.setDescription(adGroup.getDescription());
                role.setAdminRole(isAdminByName(name));
                role.addADGroup(adGroup);
                adGroup.addRole(role); // many-to-many relationship
                adGroupRepository.save(adGroup);
                RoleResource roleResource = getRoleResourceByADGroup(adGroup);
                if (roleResource != null) {
                    role.setRoleResource(roleResource);
                }
                roleRepository.save(role);
            }
            addLogEntry(added + " roles added and " + updated + " roles updated from AD groups");
            return true;
        } catch (Exception exception) {
            logger.debug("Update roles from AD groups failed.", exception);
            return false;
        }
    }

    public boolean importRolesFromJSON(RoleResource roleResource) {
        return false;
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
            logger.debug("Error while saving person \"" + person.getCentralAccountName() + "\"", exception);
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
            logger.debug("Error while saving AD group \"" + adGroup.getName() + "\"", exception);
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
            addLogEntry("Role \"" + role.getName() + "\" deleted.");
        } catch (Exception exception) {
            logger.debug("Error while deleting role \"" + role.getName() + "\"", exception);
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
            addLogEntry("Person \"" + person.getCentralAccountName() + " \"deleted.");
        } catch (Exception exception) {
            logger.debug("Error while deleting person \"" + person.getCentralAccountName() + "\"", exception);
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
            addLogEntry("AD user \"" + adUser.getDistinguishedName() + "\" deleted.");
        } catch (Exception exception) {
            logger.debug("Error while deleting AD user \"" + adUser.getDistinguishedName() + "\"", exception);
        }
    }

    @Transactional
    private void deleteADGroupComplete(ADGroup adGroup) {
        try {
            adGroupRepository.delete(adGroup);
            addLogEntry("AD group \"" + adGroup.getDistinguishedName() + "\" deleted.");
        } catch (Exception exception) {
            logger.debug("Error while deleting AD group \"" + adGroup.getDistinguishedName() + "\"", exception);
        }
    }

    private void connect() {
        try {
            if (isConnected) {
                return;
            }
            if (activeDirectory == null) {
                logger.debug("Can not connect to the AD - AD is not configured.");
                isConnected = false;
                return;
            }
            queryRequest = new QueryRequest();
            queryRequest.setDirectoryType(DirectoryType.MS_ACTIVE_DIRECTORY);
            queryRequest.setSizeLimit(1000); //TODO: read from config
            queryRequest.setTimeLimit(1000); //TODO: read from config
            // try to connect - add all valid endpoints to the query request
            endpoint = new Endpoint();
            endpoint.setSecuredConnection(activeDirectory.useSecureConnection());
            endpoint.setPort((int) activeDirectory.getPort());
            endpoint.setHost(activeDirectory.getIPAddress());
            endpoint.setUserAccountName(activeDirectory.getConnectionUserName());
            endpoint.setPassword(activeDirectory.getEncryptedConnectionPassword());
            final ConnectionResponse connectionResponse = DirectoryConnectorService.authenticate(endpoint);
            if (connectionResponse.isError()) {
                logger.debug("The connection to the Active Directory failed.");
            }
            queryRequest.addEndpoint(endpoint);
            isConnected = true;
        } catch (Exception exception) {
            logger.debug("The connection to the Active Directory failed.", exception);
            isConnected = false;
        }
    }

    private List<EntityResponse> getADUserEntities() {
        if (isConnected()) {
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
                queryRequest.addRequestedField(Global.ADAttributes.SID);
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
                queryRequest.addRequestedField(Global.ADAttributes.SID);
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
                // get assigned AD groups of the AD user
                List<String> logonNames = new ArrayList<>(1);
                logonNames.add(adUser.getLogonName());
                List<EntityResponse> entityResponses = DirectoryConnectorService.isMemberOf(logonNames, endpoint);
                for (EntityResponse entityResponse: entityResponses) {
                    List<Field> fields = entityResponse.getValue();
                    for (Field field : fields) {
                        FieldType fieldType = field.getType(); // can be null
                        if (fieldType != null) {
                            if (fieldType.equals(FieldType.COMMON_NAME)) {
                                String adGroupName = (String) field.getValue();
                                // add AD group with this name to the list
                                Optional<ADGroup> optionalADGroup = adGroupRepository.findFirstByName(adGroupName);
                                optionalADGroup.ifPresent(adGroups::add);
                            }
                        }
                    }
                }
            } catch (Exception exception) {
                logger.debug("Error occurred while getting groups for user \""
                        + adUser.getDistinguishedName() +  " from AD.", exception);
                return adGroups;
            }
        }
        return adGroups;
    }

    private boolean[] createOrUpdateADUser(EntityResponse response) {
        boolean[] returnValues = new boolean[2]; // {added, updated}
        ADUser updatedADUser = new ADUser();
        try {
            boolean isUpdate = countADUsers() > 0;
            List<Field> fields = response.getValue();
            for (Field field : fields) {
                FieldType fieldType = field.getType(); // can be null
                if (fieldType != null) {
                    if (fieldType.equals(FieldType.LOGON_NAME)) {
                        String fieldString = (String) field.getValue();
                        if (fieldString.isEmpty()) {
                            logger.debug("The logon name of an user could not be determined.");
                            return returnValues;
                        }
                        updatedADUser.setLogonName(fieldString);
                    }
                    if (fieldType.equals(FieldType.DISTINGUISHED_NAME)) {
                        String fieldString = (String) field.getValue();
                        if (fieldString.isEmpty()) {
                            logger.debug("The distinguished name of an user could not be determined.");
                            return returnValues;
                        }
                        updatedADUser.setDistinguishedName(fieldString);
                    }
                    // account enabled / password expiration
                    if (fieldType.equals(FieldType.USER_ACCOUNT_CONTROL)) {
                        updatedADUser.setEnabled(accountEnabled((String) field.getValue()));
                        updatedADUser.setPasswordExpires(passwordExpires((String) field.getValue()));
                    }
                }
                if (field.getName().equalsIgnoreCase(Global.ADAttributes.SID)) {
                    String objectSidString = convertSidToString((byte[]) field.getValue());
                    if (objectSidString.isEmpty()) {
                        logger.debug("The SID of an user could not be determined.");
                        return returnValues;
                    }
                    updatedADUser.setObjectSID(objectSidString);
                }
            }
            if (isUpdate) {
                Optional<ADUser> optionalADUser = adUserRepository.findFirstByObjectSID(updatedADUser.getObjectSID());
                if (optionalADUser.isPresent()) {
                    // update the existing AD user
                    ADUser adUser = optionalADUser.get();
                    // attributes can be changed
                    adUser.setDistinguishedName(updatedADUser.getDistinguishedName());
                    adUser.setLogonName(updatedADUser.getLogonName());
                    adUser.setAdminAccount(isAdminByName(updatedADUser.getLogonName()));
                    adUserRepository.save(adUser);
                    addLogEntry("AD user \"" + updatedADUser.getLogonName() + "\" updated.");
                    returnValues[1] = true; // update
                } else {
                    adUserRepository.save(updatedADUser);
                    addLogEntry("AD user \"" + updatedADUser.getLogonName() + "\" added.");
                    returnValues[0] = true; // add
                }
            } else {
                adUserRepository.save(updatedADUser);
                addLogEntry("AD user \"" + updatedADUser.getLogonName() + "\" added.");
                returnValues[0] = true; // add
            }
        } catch (Exception exception) {
            logger.debug("Add or update AD user \"" + updatedADUser.getLogonName() + "\" failed: "
                    + exception.getMessage());
        }
        return returnValues;
    }

    private boolean[] createOrUpdateADGroup(EntityResponse response) {
        boolean[] returnValues = new boolean[2]; // {added, updated}
        ADGroup updatedADGroup = new ADGroup();
        try {
            boolean isUpdate = countADGroups() > 0;
            List<Field> fields = response.getValue();
            for (Field field : fields) {
                FieldType fieldType = field.getType(); // can be null
                if (fieldType != null) {
                    if (fieldType.equals(FieldType.COMMON_NAME)) {
                        String fieldString = (String) field.getValue();
                        if (fieldString.isEmpty()) {
                            logger.debug("The name of a group could not be determined.");
                            return returnValues;
                        }
                        updatedADGroup.setName(fieldString);
                        // check for "admin group"
                        updatedADGroup.setAdminGroup(isAdminByName(fieldString));
                    }
                    if (fieldType.equals(FieldType.DISTINGUISHED_NAME)) {
                        String fieldString = (String) field.getValue();
                        if (fieldString.isEmpty()) {
                            logger.debug("The distinguished name of a group could not be determined.");
                            return returnValues;
                        }
                        updatedADGroup.setDistinguishedName(fieldString);
                    }
                }
                if (field.getName().equalsIgnoreCase(Global.ADAttributes.SID)) {
                    String objectSidString = convertSidToString((byte[]) field.getValue());
                    if (objectSidString.isEmpty()) {
                        logger.debug("The SID of an user could not be determined.");
                        return returnValues;
                    }
                    updatedADGroup.setObjectSID(objectSidString);
                }
                if (field.getName().equalsIgnoreCase(Global.ADAttributes.DESCRIPTION)) {
                    // the length is set to 255 characters
                    String description = (String) field.getValue();
                    if (description.length() > Global.IMPORT_SETTINGS.MAX_STRING_LENGTH) {
                        updatedADGroup.setDescription(description.substring(0, Global.IMPORT_SETTINGS.MAX_STRING_LENGTH - 1));
                    } else {
                        updatedADGroup.setDescription(description);
                    }

                }
                // group area / group type
                if (field.getName().equalsIgnoreCase(Global.ADAttributes.GROUP_TYPE)) {
                    updatedADGroup.setGroupArea(groupAreaFromString((String) field.getValue()));
                    updatedADGroup.setGroupType(groupTypeFromString((String) field.getValue()));
                }
            }
            if (isUpdate) {
                Optional<ADGroup> optionalADAGroup = adGroupRepository.findFirstByObjectSID(updatedADGroup.getObjectSID());
                if (optionalADAGroup.isPresent()) {
                    // update existing AD group
                    ADGroup adGroup = optionalADAGroup.get();
                    // attribute can be change
                    adGroup.setName(updatedADGroup.getName());
                    adGroup.setDistinguishedName(updatedADGroup.getDistinguishedName());
                    adGroup.setAdminGroup(isAdminByName(updatedADGroup.getName()));
                    adGroup.setDescription(updatedADGroup.getDescription());
                    // save as updated object
                    adGroupRepository.save(adGroup);
                    addLogEntry("AD group \"" + updatedADGroup.getName() + "\" updated.");
                    returnValues[1] = true; // update
                } else {
                    adGroupRepository.save(updatedADGroup);
                    addLogEntry("AD group \"" + updatedADGroup.getName() + "\" added.");
                    returnValues[0] = true; // add
                }
            } else {
                adGroupRepository.save(updatedADGroup);
                addLogEntry("AD group \"" + updatedADGroup.getName() + "\" added.");
                returnValues[0] = true; // add
            }
        } catch (Exception exception) {
            logger.debug("Add or update AD group \"" + updatedADGroup.getName() + "\" failed: " + exception.getMessage());
        }
        return returnValues;
    }

    private boolean[] createOrUpdatePerson(EntityResponse response) {
        boolean[] returnValues = new boolean[2]; // {added, updated}
        Person updatedPerson = new Person();
        try {
            boolean isUpdate = countPersons() > 0;
            List<Field> fields = response.getValue();
            for (Field field : fields) {
                FieldType fieldType = field.getType(); // can be null
                if (fieldType != null) {
                    // set values
                    if (fieldType.equals(FieldType.LOGON_NAME)) {
                        updatedPerson.setCentralAccountName((String) field.getValue());
                        if (updatedPerson.getLastName().length() == 0) {
                            // last name of person must be not empty
                            updatedPerson.setLastName((String) field.getValue());
                        }
                    }
                    if (fieldType.equals(FieldType.FIRST_NAME)) {
                        updatedPerson.setFirstName((String) field.getValue());
                    }
                    if (fieldType.equals(FieldType.LAST_NAME)) {
                        updatedPerson.setLastName((String) field.getValue());
                    }
                    if (fieldType.equals(FieldType.EMAIL)) {
                        updatedPerson.setEmailAddress((String) field.getValue());
                    }
                    if (fieldType.equals(FieldType.PHONE_NUMBER)) {
                        updatedPerson.setPhoneNumber((String) field.getValue());
                    }
                    if (fieldType.equals(FieldType.MOBILE_PHONE)) {
                        updatedPerson.setMobilePhoneNumber((String) field.getValue());
                    }
                    if (fieldType.equals(FieldType.DEPARTMENT)) {
                        updatedPerson.setDepartmentName((String) field.getValue());
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
                            updatedPerson.setEntryDate(entryDate);

                        } catch (Exception exception) {
                            updatedPerson.setEntryDate(Global.EMPLOYEE_DEFAULT_VALUES.ENTRY_DATE);
                            logger.debug("Error while get the entry date from AD attribute 'whenCreated'");
                        }

                    }
                }
                // last name of person must be not empty
                if (field.getName().equalsIgnoreCase(Global.ADAttributes.DISPLAY_NAME)) {
                    if (updatedPerson.getLastName().length() == 0) {
                        updatedPerson.setLastName((String) field.getValue());
                    }
                }
                // description
                if (field.getName().equalsIgnoreCase(Global.ADAttributes.DESCRIPTION)) {
                    updatedPerson.setDescription((String) field.getValue());
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
                                .toLocalDate().minusDays(1); // account is locked at 00:00
                        // if account does not expire -> year is setting to 30828
                        if (exitDate.isAfter(Global.EMPLOYEE_DEFAULT_VALUES.EXIT_DATE) ||
                                (exitDate.isAfter(Global.EMPLOYEE_DEFAULT_VALUES.MIN_UPPER_DATE))){
                            updatedPerson.setExitDate(exitDate);
                        } else {
                            updatedPerson.setExitDate(Global.EMPLOYEE_DEFAULT_VALUES.EXIT_DATE);
                        }
                    } catch (Exception exception) {
                        updatedPerson.setExitDate(Global.EMPLOYEE_DEFAULT_VALUES.EXIT_DATE);
                        logger.debug("Error while get the exit date from AD attribute "
                                + Global.ADAttributes.ACCOUNT_EXPIRES, exception);
                    }
                }
            }
            if (isUpdate) {
                Optional<Person> optionalPerson = personRepository.findByAdUsers_LogonName(updatedPerson.getCentralAccountName());
                if (optionalPerson.isPresent()) {
                    Person person = optionalPerson.get();
                    // attribute can be changed
                    person.setFirstName(updatedPerson.getFirstName());
                    person.setLastName(updatedPerson.getLastName());
                    person.setEmailAddress(updatedPerson.getEmailAddress());
                    person.setPhoneNumber(updatedPerson.getPhoneNumber());
                    person.setMobilePhoneNumber(updatedPerson.getMobilePhoneNumber());
                    person.setEntryDate(updatedPerson.getEntryDate());
                    person.setExitDate(updatedPerson.getExitDate());
                    // save as updated object
                    personRepository.save(person);
                    addLogEntry("Person \"" + updatedPerson.getCentralAccountName() + "\" updated.");
                    returnValues[1] = true; // update
                } else {
                    personRepository.save(updatedPerson);
                    addLogEntry("Person \"" + updatedPerson.getCentralAccountName() + "\" added.");
                    returnValues[0] = true; // add
                }
            } else {
                personRepository.save(updatedPerson);
                addLogEntry("Person \"" + updatedPerson.getCentralAccountName() + "\" added.");
                returnValues[0] = true; // add
            }
        } catch (Exception exception) {
            logger.debug("Add or update person \"" + updatedPerson.getCentralAccountName() + "\" failed: "
                    + exception.getMessage());
        }
        return returnValues;
    }

    // @see https://administrator.de/forum/mit-java-sid-des-angemeldeten-benutzers-auslesen-und-in-variable-schreiben-336662.html
    private String convertSidToString(byte[] sid) {
        try {
            if (sid == null) {
                return "";
            }
            if (sid.length < 8 || sid.length % 4 != 0) {
                return "";
            }
            StringBuilder sb = new StringBuilder();
            sb.append("S-").append(sid[0]);
            int c = sid[1];
            ByteBuffer bb = ByteBuffer.wrap(sid);
            sb.append("-").append(bb.getLong() & 0XFFFFFFFFFFFFL);
            bb.order(ByteOrder.LITTLE_ENDIAN);
            for (int i = 0; i < c; i++) {
                sb.append("-").append((long) bb.getInt() & 0xFFFFFFFFL);
            }
            return sb.toString();
        } catch (Exception exception) {
            logger.debug("Error while converting SID to String.", exception);
            return "";
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

    @Nullable
    private RoleResource getRoleResourceByADGroup(ADGroup adGroup) {
        String name = adGroup.getName();
        RoleResource roleResource;
        if (isProjectRoleByName(name)) {
            roleResource = getRoleResource(Global.ROLE_RESOURCE.PROJECT_ROLE);
        } else if (isFileShareRoleByName(name)) {
            roleResource = getRoleResource(Global.ROLE_RESOURCE.FILE_SHARE_ROLE);
        } else if (isEmailRoleByADGroup(adGroup)) {
            roleResource = getRoleResource(Global.ROLE_RESOURCE.EMAIL_RESOURCE_ROLE);
        } else {
            roleResource = getRoleResource(Global.ROLE_RESOURCE.DEFAULT_ROLE);
        }
        return roleResource;
    }

    private boolean isProjectRoleByName(String name) {
        Pattern pattern = Pattern.compile(Pattern.quote(Global.IMPORT_SETTINGS.PROJECT_ROLE_TEXT),
                Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(name);
        return matcher.find();
    }

    private boolean isFileShareRoleByName(String name) {
        Pattern pattern = Pattern.compile(Pattern.quote(Global.IMPORT_SETTINGS.FILE_SHARE_ROLE_TEXT),
                Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(name);
        return matcher.find();
    }
    
    private boolean isEmailRoleByADGroup(ADGroup adGroup) {
        String name = adGroup.getName();
        Pattern pattern = Pattern.compile(Pattern.quote(Global.IMPORT_SETTINGS.EMAIL_ROLE_TEXT),
                Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(name);
        return (matcher.find() || adGroup.getGroupType() == Global.ADGroupType.DISTRIBUTION
                || adGroup.getGroupArea() == Global.ADGroupArea.UNIVERSAL);
    }

    private void addLogEntry(String message) {
        // determine the actual user
        if (sessionUser == null) {
            try {
                sessionUser = (org.springframework.security.core.userdetails.User) SecurityContextHolder
                        .getContext().getAuthentication().getPrincipal();
            } catch (Exception exception) {
                logger.debug("Could not determine the logged in user.", exception);
                sessionUser = null;
            }
        }
        if (sessionUser != null) {
            // TODO: logging
            logger.debug("This action was triggered by \"" + sessionUser.getUsername() + "\": " + message);
        } else {
            logger.debug(message);
        }
    }
}
