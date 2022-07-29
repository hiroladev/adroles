package de.hirola.adroles.data.repository;

import de.hirola.adroles.data.entity.ADGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ADGroupRepository extends JpaRepository<ADGroup, Integer> {

    @Query("select g from ADGroup g " +
            "where lower(g.name) like lower(concat('%', :searchTerm, '%')) " +"" +
            "or lower(g.description) like lower(concat('%', :searchTerm, '%')) " +
            "or lower(g.distinguishedName) like lower(concat('%', :searchTerm, '%')) " +
            "order by g.name")
    List<ADGroup> search(@Param("searchTerm") String searchTerm);

    Optional<ADGroup> findByDistinguishedName(String distinguishedName);

    long countByIsAdminGroupTrue();



}
