package de.hirola.adroles.data.repository;

import de.hirola.adroles.data.entity.DomainController;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface DomainControllerRepository extends JpaRepository<DomainController, Integer> {
}
