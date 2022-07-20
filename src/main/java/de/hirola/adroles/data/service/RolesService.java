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
import de.hirola.adroles.data.repository.ADAccountRepository;
import de.hirola.adroles.data.repository.ActiveDirectoryRepository;
import de.hirola.adroles.data.repository.RoleRepository;
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
public class RolesService {
    private final Logger logger = LoggerFactory.getLogger(RolesService.class);
    private  final RoleRepository roleRepository;

    public RolesService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    public List<Role> findAllRoles(@Nullable String stringFilter) {
        if (stringFilter == null || stringFilter.isEmpty()) {
            return roleRepository.findAll();
        } else {
            return roleRepository.search(stringFilter);
        }
    }

    public long countRoles() {
        return roleRepository.count();
    }

    public void importRolesFromJSON() {
    }

    public void saveRole(Role role) {
        if (role == null) {
            return;
        }
        roleRepository.save(role);
    }

    public void deleteRole(Role role) {
        if (role == null) {
            return;
        }
        roleRepository.delete(role);
    }
}
