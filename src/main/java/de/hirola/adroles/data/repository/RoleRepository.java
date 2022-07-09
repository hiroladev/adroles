package de.hirola.adroles.data.repository;

import de.hirola.adroles.data.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Integer> {

}
