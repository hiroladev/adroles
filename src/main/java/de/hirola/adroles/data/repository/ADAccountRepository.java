package de.hirola.adroles.data.repository;

import de.hirola.adroles.data.entity.ADAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;

import java.util.List;
import java.util.Optional;

public interface ADAccountRepository extends JpaRepository<ADAccount, Integer> {

    Optional<ADAccount> findFirstByLogonName(@NonNull String logonName);


}
