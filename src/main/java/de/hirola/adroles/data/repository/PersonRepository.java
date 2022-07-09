package de.hirola.adroles.data.repository;

import de.hirola.adroles.data.entity.Person;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PersonRepository extends JpaRepository<Person, Integer> {

    @Query("select p from Person p " +
        "where lower(p.firstName) like lower(concat('%', :searchTerm, '%')) " +
        "or lower(p.lastName) like lower(concat('%', :searchTerm, '%'))")
    List<Person> search(@Param("searchTerm") String searchTerm);
}
