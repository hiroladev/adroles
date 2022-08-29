/*
 * Copyright 2022 by Michael Schmidt, Hirola Consulting
 * This software us licensed under the AGPL-3.0 or later.
 *
 * @author Michael Schmidt (Hirola)
 * @since v0.1
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


@Configuration
@PropertySource(value = "file:${ADROLES_CONF_DIR}/db.properties", ignoreResourceNotFound = true)
public class DataSourceConfig {
    private final Logger logger = LoggerFactory.getLogger(DataSourceConfig.class);
    @Autowired
    private Environment env;
    @Value("${db.type}")
    private String type;
    @Value("${db.name}")
    private String name;
    @Value("${db.jdbcDriver}")
    private String jdbcDriver;
    @Value("${db.jdbcURL}")
    private String jdbcUrl;
    @Value("${db.username}")
    private String username;
    @Value("${db.password}")
    private String password;

    @Bean
    public DataSource getDataSource() {
        //spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
        DataSourceBuilder<?> dataSourceBuilder = DataSourceBuilder.create();
        if (isDefault()) {
            logger.debug("Using default datasource ...");
            return dataSourceBuilder.build(); // default database
        }
        if (type.compareToIgnoreCase(Global.CONFIG.POSTGRES_DATA_SOURCE_STRING) == 0) {

        }
        if (!jdbcDriver.isEmpty()) {
            dataSourceBuilder.driverClassName(jdbcDriver);
        }
        dataSourceBuilder.url(jdbcUrl);
        dataSourceBuilder.username(username);
        dataSourceBuilder.password(password);
        return dataSourceBuilder.build();
    }

    public String getName() {
        return name;
    }

    public String getJdbcDriver() {
        return jdbcDriver;
    }

    public String getJdbcUrl() {
        return jdbcUrl;
    }

    public String getUsername() {
        return username;
    }

    public boolean isDefault() {
        return type.isEmpty() ||
                jdbcUrl.isEmpty() ||
                type.compareToIgnoreCase(Global.CONFIG.DEFAULT_DATA_SOURCE_STRING) == 0;
    }
}
