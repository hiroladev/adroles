package de.hirola.adroles.data.repository;

import de.hirola.adroles.data.entity.ADAccount;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ADAccountRepository extends JpaRepository<ADAccount, Integer> {

}
