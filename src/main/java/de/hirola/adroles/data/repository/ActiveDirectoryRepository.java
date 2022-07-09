package de.hirola.adroles.data.repository;

import de.hirola.adroles.data.entity.ActiveDirectory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ActiveDirectoryRepository extends JpaRepository<ActiveDirectory, Integer> {

}
