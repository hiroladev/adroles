package de.hirola.adroles.data.repository;

import de.hirola.adroles.data.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Integer> {

}
