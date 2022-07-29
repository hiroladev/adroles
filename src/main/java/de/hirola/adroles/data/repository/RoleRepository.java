package de.hirola.adroles.data.repository;

import de.hirola.adroles.data.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RoleRepository extends JpaRepository<Role, Integer> {

    @Query("select r from Role r " +
            "where lower(r.name) like lower(concat('%', :searchTerm, '%')) " +
            "or lower(r.description) like lower(concat('%', :searchTerm, '%')) " +
            "order by r.name")
    List<Role> search(@Param("searchTerm") String searchTerm);

    @Query("select r from Role r join r.roleResource r " +
            "where r.isOrgResource = true " +
            "and (lower(r.name) like lower(concat('%', :searchTerm, '%')) " +
            "or lower(r.description) like lower(concat('%', :searchTerm, '%'))) " +
            "order by r.name")
    // @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name LIKE %?1%")
    List<Role> searchOrg(@Param("searchTerm") String searchTerm);

    @Query("select r from Role r " +
            "where lower(r.name) like lower(concat('%', :searchTerm, '%')) " +
            "or lower(r.description) like lower(concat('%', :searchTerm, '%')) " +
            "order by r.name")
    List<Role> searchProject(@Param("searchTerm") String searchTerm);

    @Query("select r from Role r " +
            "where lower(r.name) like lower(concat('%', :searchTerm, '%')) " +
            "or lower(r.description) like lower(concat('%', :searchTerm, '%')) " +
            "order by r.name")
    List<Role> searchFileShare(@Param("searchTerm") String searchTerm);


    List<Role> findByPersons_Id(Integer id);

    List<Role> findByRoleResource_IsOrgResourceTrueOrderByNameAsc();

    long countByRoleResource_IsOrgResourceTrue();

    long countByRoleResource_IsProjectResourceTrue();

    long countByRoleResource_IsFileShareResourceTrue();

}
