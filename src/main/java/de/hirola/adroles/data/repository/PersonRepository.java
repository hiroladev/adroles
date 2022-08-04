package de.hirola.adroles.data.repository;

import de.hirola.adroles.data.entity.Person;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;

import java.util.List;
import java.util.Optional;

public interface PersonRepository extends JpaRepository<Person, Integer> {

    @Query("select p from Person p " +
        "where lower(p.firstName) like lower(concat('%', :searchTerm, '%')) " +
            "or lower(p.lastName) like lower(concat('%', :searchTerm, '%'))" +
            "or lower(p.departmentName) like lower(concat('%', :searchTerm, '%'))" +
            "or lower(p.description) like lower(concat('%', :searchTerm, '%'))" +
            "or lower(p.emailAddress) like lower(concat('%', :searchTerm, '%'))" +
            "or lower(p.centralAccountName) like lower(concat('%', :searchTerm, '%')) " +
            "order by p.lastName")
    List<Person> search(@Param("searchTerm") String searchTerm);

    @Query("select p from Person p " +
            "where p.isEmployee = true and " +
            "(lower(p.firstName) like lower(concat('%', :searchTerm, '%')) " +
            "or lower(p.lastName) like lower(concat('%', :searchTerm, '%'))" +
            "or lower(p.departmentName) like lower(concat('%', :searchTerm, '%'))" +
            "or lower(p.description) like lower(concat('%', :searchTerm, '%'))" +
            "or lower(p.emailAddress) like lower(concat('%', :searchTerm, '%'))" +
            "or lower(p.centralAccountName) like lower(concat('%', :searchTerm, '%'))) " +
            "order by p.lastName")
    List<Person> searchEmployees(@Param("searchTerm") String searchTerm);

    Optional<Person> findByAdUsers_LogonName(@NonNull String logonName);

    List<Person> findByIsEmployeeTrueOrderByLastNameAscFirstNameAsc();

    @Query("select distinct departmentName from Person where departmentName <> ''")
    List<String> getUniqueDepartmentNames();

    List<Person> findByDepartmentNameOrderByLastNameAsc(String departmentName);


}
