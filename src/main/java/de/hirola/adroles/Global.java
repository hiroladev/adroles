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

public final class Global {

    public enum ROLE_RESOURCE {
        ;
        public static final int DEFAULT_ROLE = 0;
        public static final int ORG_ROLE = 1;
        public static final int PROJECT_ROLE = 2;
        public static final int FILE_SHARE_ROLE = 3;
    }

    public enum IMPORT_SETTINGS {
        ;
        public static final int MAX_STRING_LENGTH = 255;
        public static final String ADMIN_GROUP_TEXT = "admin";
        public static final String PROJECT_ROLE_TEXT = "proj";
    }

    public enum ADAttributes {
        ;
        public static final String GROUP_TYPE = "groupType";
        public static final String DISPLAY_NAME = "displayName";
        public static final String DESCRIPTION = "description";
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
        public static final String DEFAULT_BUTTON_WIDTH = "250px";
        public static final String DEFAULT_TEXT_FIELD_WIDTH = "400px";
        public static final String DEFAULT_COLUMN_WIDTH = "200px";
        public static final String IMAGE_COLUMN_WIDTH = "50px";
        public static final String FOOTER_COLUMN_KEY = "footerColumn";
    }
}
