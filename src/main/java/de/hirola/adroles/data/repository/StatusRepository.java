package de.hirola.adroles.data.repository;

import de.hirola.adroles.data.entity.Status;

import org.springframework.data.jpa.repository.JpaRepository;

public interface StatusRepository extends JpaRepository<Status, Integer> {

}
