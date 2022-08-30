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

package de.hirola.adroles.data;

import de.hirola.adroles.Global;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

import javax.sql.DataSource;
import java.util.Objects;

@Configuration
@PropertySource(value = "file:${ADROLES_CONFIG_DIR}/db.properties", ignoreResourceNotFound = true)
public class DataSourceFactory {

    @Autowired
    private Environment env;
    @Value("${db.type}")
    private String type;
    @Value("${db.name}")
    private String name;
    @Value("${db.jdbcDriver}")
    private String jdbcDriverName;
    @Value("${db.jdbcUrl}")
    private String jdbcUrl;
    @Value("${db.username}")
    private String username;
    @Value("${db.password}")
    private String password;

    @Bean
    public DataSource getDataSource() {
        Logger logger = LoggerFactory.getLogger(DataSourceFactory.class);
        if (type.isEmpty() || name.isEmpty() || jdbcUrl.isEmpty()) {
            if (!env.containsProperty(Global.CONFIG.CONFIG_DIR_VAR)) {
                logger.debug("System var \"" + Global.CONFIG.CONFIG_DIR_VAR
                        + " \" not set. Using H2 database in RAM ...");
            } else {
                logger.debug("Missing datasource properties. Using H2 database in RAM ...");
            }
            // default database from application.properties
            System.setProperty(Global.CONFIG.DATASOURCE_TYPE, Global.CONFIG.DEFAULT_DATA_SOURCE);
            System.setProperty(Global.CONFIG.DATASOURCE_NAME, Global.CONFIG.DEFAULT_DATA_SOURCE);
            return DataSourceBuilder.create().build();
        }
        if (type.compareToIgnoreCase(Global.CONFIG.POSTGRES_DATA_SOURCE) == 0) {
            // set hibernate dialect property
            System.setProperty("spring.jpa.properties.hibernate.dialect",
                    Global.CONFIG.POSTGRES_HIBERNATE_DIALECT);
        } else {
            logger.debug("Unknown datasource of type +\"" + type + "\". Using H2 database in RAM ...");
            // default database from application.properties
            System.setProperty(Global.CONFIG.DATASOURCE_TYPE, Global.CONFIG.DEFAULT_DATA_SOURCE);
            System.setProperty(Global.CONFIG.DATASOURCE_NAME, Global.CONFIG.DEFAULT_DATA_SOURCE);
            return DataSourceBuilder.create().build();
        }
        System.setProperty(Global.CONFIG.DATASOURCE_TYPE, type);
        System.setProperty(Global.CONFIG.DATASOURCE_NAME, name);
        System.setProperty(Global.CONFIG.DATASOURCE_URL, jdbcUrl);
        DataSourceBuilder<?> dataSourceBuilder = DataSourceBuilder.create();
        if (!jdbcDriverName.isEmpty()) {
            System.setProperty(Global.CONFIG.DATASOURCE_DRIVER_NAME, jdbcDriverName);
            dataSourceBuilder.driverClassName(jdbcDriverName);
        }
        dataSourceBuilder.url(jdbcUrl);
        dataSourceBuilder.username(username);
        dataSourceBuilder.password(password);
        logger.debug("Using " + type + " database \"" + name + "\" ...");
        return dataSourceBuilder.build();
    }

    public String getName() {
        return Objects.requireNonNullElse(name, "");
    }

    public String getJdbcDriverName() {
        return Objects.requireNonNullElse(jdbcDriverName, "");
    }

    public String getJdbcUrl() {
        return Objects.requireNonNullElse(jdbcUrl, "");
    }

    public String getUsername() {
        return Objects.requireNonNullElse(username, "");
    }

    public String getType() {
        return type;
    }

    public boolean isDefaultDataSource() {
        return name.isEmpty() || type.isEmpty() ||
                type.compareToIgnoreCase(Global.CONFIG.DEFAULT_DATA_SOURCE) == 0;
    }

}
