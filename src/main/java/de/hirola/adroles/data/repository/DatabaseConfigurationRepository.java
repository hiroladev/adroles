package de.hirola.adroles.data.repository;

import de.hirola.adroles.data.entity.DatabaseConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;


public interface DatabaseConfigurationRepository extends JpaRepository<DatabaseConfiguration, Integer> {

}
