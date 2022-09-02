/*
 *
 *  Copyright 2022 by Michael Schmidt, Hirola Consulting
 *  This software us licensed under the AGPL-3.0 or later.
 *
 *
 *  @author Michael Schmidt (Hirola)
 *  @since v0.1
 *
 */

package de.hirola.adroles;

import java.time.LocalDate;
import java.time.ZoneId;

public final class Global {

    public enum CONFIG {
        ;
        public static final String CONFIG_DIR_VAR = "ADROLES_CONFIG_DIR";
        public static final String LOG_DIR_VAR = "ADROLES_LOG_DIR";
        public static final String DATASOURCE_TYPE = "db.type";
        public static final String DATASOURCE_NAME = "db.name";
        public static final String DATASOURCE_URL = "db.jdbcUrl";
        public static final String DATASOURCE_DRIVER_NAME = "db.jdbcDriver";
        public static final String DEFAULT_DATA_SOURCE = "H2/RAM";
        public static final String H2_DATA_SOURCE = "H2";
        public static final String POSTGRES_DATA_SOURCE = "Postgres";
        public static final String POSTGRES_HIBERNATE_DIALECT = "org.hibernate.dialect.PostgreSQL81Dialect";
    }

    public enum LOGGING_VALUES {
        ;
        public static final String UNKNOWN_USER_STRING = "Unknown";
    }

    public enum EMPLOYEE_DEFAULT_VALUES {
        ;
        public static final LocalDate ENTRY_DATE = LocalDate.now(ZoneId.systemDefault());
        public static final LocalDate EXIT_DATE = LocalDate.of(2100, 12, 31);
        public static final LocalDate MIN_UPPER_DATE = LocalDate.ofYearDay(1991, 31);
    }

    public enum ROLE_RESOURCE {
        ;
        public static final int DEFAULT_ROLE = 0;
        public static final String ROLE_RESOURCE_STRING = "Role";
        public static final int ORG_ROLE = 1;
        public static final String ORG_RESOURCE_STRING = "Organization";
        public static final int PROJECT_ROLE = 2;
        public static final String PROJECT_RESOURCE_STRING = "Project";
        public static final int FILE_SHARE_ROLE = 3;
        public static final String FILE_RESOURCE_STRING = "Share";
        public static final int EMAIL_RESOURCE_ROLE = 4;
        public static final String EMAIL_RESOURCE_STRING = "E-Mail";
    }

    public enum IMPORT_SETTINGS {
        ;
        public static final int MAX_STRING_LENGTH = 255;
        public static final String DEFAULT_IMPORT_TEXT = "Imported / Created by service";
        public static final String ADMIN_GROUP_TEXT = "admin";
        public static final String PROJECT_ROLE_TEXT = "proj";
        public static final String FILE_SHARE_ROLE_TEXT = "share";
        public static final String EMAIL_ROLE_TEXT = "mail";
    }

    public enum ADAttributes {
        ;
        public static final String GROUP_TYPE = "groupType";
        public static final String DISPLAY_NAME = "displayName";
        public static final String DESCRIPTION = "description";
        public static final String ACCOUNT_EXPIRES = "accountExpires";
        public static final String SID = "objectSid";
    }

    public enum ADGroupArea {
        ;
        public static final int LOCAL = 0;
        public static final int GLOBAL = 1;
        public static final int UNIVERSAL = 2;
    }

    public enum ADGroupType {
        ;
        public static final int SECURITY = 0;
        public static final int DISTRIBUTION = 1;
    }

    // TODO: set by user?
    public enum Component {
        ;
        public static final String DEFAULT_DIALOG_WIDTH = "350px";
        public static final String DEFAULT_BUTTON_WIDTH = "250px";
        public static final String DEFAULT_ICON_BUTTON_WIDTH = "100px";
        public static final String DEFAULT_TEXT_FIELD_WIDTH = "450px";
        public static final String DEFAULT_COLUMN_WIDTH = "200px";
        public static final String IMAGE_COLUMN_WIDTH = "50px";
        public static final String FOOTER_COLUMN_KEY = "footerColumn";
        public static final int DEFAULT_NOTIFICATION_DURATION = 30;
    }
}
