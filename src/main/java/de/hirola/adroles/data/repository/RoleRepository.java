package de.hirola.adroles.data.repository;

import de.hirola.adroles.data.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RoleRepository extends JpaRepository<Role, Integer> {

    @Query("select r from Role r " +
            "where lower(r.name) like lower(concat('%', :searchTerm, '%')) " +
            "or lower(r.description) like lower(concat('%', :searchTerm, '%'))")
    List<Role> search(@Param("searchTerm") String searchTerm);
}
