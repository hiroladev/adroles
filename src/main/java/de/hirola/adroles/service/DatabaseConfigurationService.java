/*
 * Copyright 2022 by Michael Schmidt, Hirola Consulting
 * This software us licensed under the AGPL-3.0 or later.
 *
 * @author Michael Schmidt (Hirola)
 * @since v0.1
 */

package de.hirola.adroles.service;

import de.hirola.adroles.data.entity.DatabaseConfiguration;
import de.hirola.adroles.data.repository.DatabaseConfigurationRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DatabaseConfigurationService {

    private final DatabaseConfigurationRepository databaseConfigurationRepository;

    public DatabaseConfigurationService(DatabaseConfigurationRepository databaseConfigurationRepository) {
        this.databaseConfigurationRepository = databaseConfigurationRepository;
    }

    public List<DatabaseConfiguration> getDatabases() {
        return databaseConfigurationRepository.findAll();
    }
}
