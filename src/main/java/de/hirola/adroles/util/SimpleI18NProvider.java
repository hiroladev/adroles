package de.hirola.adroles.util;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.vaadin.flow.i18n.I18NProvider;

/**
 * Copyright 2022 by Michael Schmidt, Hirola Consulting
 * This software us licensed under the AGPL-3.0 or later.
 * <p>
 * Simple implementation of {@link I18NProvider}.
 * <p> Actual translations can be found in the translate_{lang_code}.properties files.
 * <p> Singleton scope.
 *
 * @author Michael Schmidt (Hirola)
 * @since v0.1
 */
@Component
public class SimpleI18NProvider implements I18NProvider {

    public static final java.util.Locale GERMAN = new Locale("de");
    // Use no-country versions, so that e.g. both en_US and en_GB work.
    public static final java.util.Locale ENGLISH = new Locale("en");
    private final static Logger logger = LoggerFactory.getLogger(SimpleI18NProvider.class);
    private Map<String, ResourceBundle> localeMap;

    @PostConstruct
    private void initMap() {
        localeMap = new HashMap<>();
        // Read translations file for each locale
        for (final Locale locale : getProvidedLocales()) {
            final ResourceBundle resourceBundle = ResourceBundle.getBundle("translate", locale);
            localeMap.put(locale.getLanguage(), resourceBundle);
        }
    }

    @Override
    public List<Locale> getProvidedLocales() {
        return List.of(ENGLISH, GERMAN);
    }

    @Override
    public String getTranslation(String key, Locale locale, Object... params) {

        String rawString = null;
        try {
            rawString = localeMap.get(locale.getLanguage()).getString(key);
            return MessageFormat.format(rawString, params);
        } catch (final MissingResourceException exception) {
            // Translation not found, return error message instead of null as per API
            logger.debug("Missing key in translate resources: " + key, exception);
            return String.format("!{%s}", key);
        } catch (final IllegalArgumentException exception) {
            logger.debug("Translation exception occurred.", exception);
            return rawString;
        }
    }
}