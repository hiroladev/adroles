package de.hirola.adroles.data.repository;

import de.hirola.adroles.data.entity.DBConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DBConfigRepository extends JpaRepository<DBConfig, Integer> {
    Optional<DBConfig> findByName(String name);
}
