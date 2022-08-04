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
        public static final int ORG_ROLE = 1;
        public static final int PROJECT_ROLE = 2;
        public static final int FILE_SHARE_ROLE = 3;
        public static final int EMAIL_RESOURCE_ROLE = 4;
    }

    public enum IMPORT_SETTINGS {
        ;
        public static final int MAX_STRING_LENGTH = 255;
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
    }
}
