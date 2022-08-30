/*
 * *
 *  * Copyright 2022 by Michael Schmidt, Hirola Consulting
 *  * This software us licensed under the AGPL-3.0 or later.
 *  *
 *  *
 *  * @author Michael Schmidt (Hirola)
 *  * @since v0.1
 *
 */

package de.hirola.adroles.service;

import de.hirola.adroles.data.entity.DBConfig;
import de.hirola.adroles.data.repository.DBConfigRepository;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

@Service
public class ConfigurationService {

    private final DBConfigRepository dbConfigRepository;

    public ConfigurationService(DBConfigRepository dbConfigRepository) {
        this.dbConfigRepository = dbConfigRepository;
    }

    public List<DBConfig> findAllDBConfigurations() {
        return dbConfigRepository.findAll();
    }

    @Nullable
    public DBConfig getDBConfiguration(String name) {
        Optional<DBConfig> optionalDBConfig = dbConfigRepository.findByName(name);
        return optionalDBConfig.orElse(null);
    }
}
