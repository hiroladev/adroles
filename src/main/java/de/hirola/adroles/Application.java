package de.hirola.adroles;

import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.server.PWA;
import de.hirola.adroles.util.SimpleI18NProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

import static java.lang.System.setProperty;

/**
 * The entry point of the Spring Boot application.
 */
@SpringBootApplication
@NpmPackage(value = "lumo-css-framework", version = "^4.0.10")
// @Theme("flowcrmtutorial")
@PWA(name = "Hirola AD-Roles", shortName = "AD-Roles", offlinePath="offline.html", offlineResources = { "./images/offline.png"})
public class Application extends SpringBootServletInitializer implements AppShellConfigurator {

    public static void main(String[] args) {
        setProperty("vaadin.i18n.provider", SimpleI18NProvider.class.getName()); // initialize localization
        SpringApplication.run(Application.class, args);
    }

}
