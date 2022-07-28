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

    public static final class IMPORT_SETTINGS {
        public static final int MAX_STRING_LENGTH = 255;
        public static final String ADMIN_GROUP_TEXT = "admin";
    }

    public static final class ADAttributes {
        public static final String GROUP_TYPE = "groupType";
        public static final String DISPLAY_NAME = "displayName";
        public static final String DESCRIPTION = "description";
    }

    public static final class ADGroupArea {
        public static final int LOCAL = 0;
        public static final int GLOBAL = 1;
        public static final int UNIVERSAL = 2;
    }

    public static final class ADGroupType {
        public static final int SECURITY = 0;
        public static final int DISTRIBUTION = 1;
    }

    // TODO: set by user?
    public static final class Component {
        public static final String DEFAULT_BUTTON_WIDTH = "250px";
        public static final String DEFAULT_TEXT_FIELD_WIDTH = "400px";
        public static final String DEFAULT_COLUMN_WIDTH = "50px";
        public static final String FOOTER_COLUMN_KEY = "footerColumn";
        public static final String NO_AUTO_WIDTH_COLUMN_KEY = "noAutoWithColumn";
    }
}
