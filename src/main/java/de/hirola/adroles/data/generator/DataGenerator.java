package de.hirola.adroles.data.generator;

import de.hirola.adroles.data.entity.*;
import de.hirola.adroles.data.repository.*;
import com.vaadin.flow.spring.annotation.SpringComponent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringComponent
public class DataGenerator {

    @Bean
    public CommandLineRunner loadData(UserRepository userRepository, AuthoritiesRepository authoritiesRepository) {

        return args -> {
            Logger logger = LoggerFactory.getLogger(getClass());
            /*if (contactRepository.count() != 0L) {
                logger.info("Using existing database");
                return;
            }*/

            logger.info("... generating 1 user entity for login ...");
            User user = new User();
            user.setLoginName("user");
            user.setPassword(new BCryptPasswordEncoder().encode("userpass"));
            user.setEnabled(true);
            userRepository.save(user);

            Authorities authorities = new Authorities();
            authorities.setLoginName(user.getLoginName());
            authorities.setAuthority("ADMIN");
            authoritiesRepository.save(authorities);

        };
    }

}
