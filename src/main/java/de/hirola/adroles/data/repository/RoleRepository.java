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

    @Query("select r from Role r " +
            "where lower(r.name) like lower(concat('%', :searchTerm, '%')) " +
            "or lower(r.description) like lower(concat('%', :searchTerm, '%')) " +
            "order by r.name")
    List<Role> searchOrg(@Param("searchTerm") String searchTerm);

    @Query("select r from Role r, RoleResource rr " +
            "where rr.isProjectResource = true " +
            "and (lower(r.name) like lower(concat('%', :searchTerm, '%')) " +
            "or lower(r.description) like lower(concat('%', :searchTerm, '%'))) " +
            "order by r.name")
    List<Role> searchProject(@Param("searchTerm") String searchTerm);

    @Query("select r from Role r " +
            "where lower(r.name) like lower(concat('%', :searchTerm, '%')) " +
            "or lower(r.description) like lower(concat('%', :searchTerm, '%')) " +
            "order by r.name")
    List<Role> searchFileShare(@Param("searchTerm") String searchTerm);

    List<Role> findByRoleResource_IsOrgResourceTrueOrderByNameAsc();

    List<Role> findByRoleResource_IsProjectResourceTrueOrderByNameAsc();

    List<Role> findByRoleResource_IsFileShareResourceTrueOrderByNameAsc();

    List<Role> findByPersons_Id(Integer id);

    long countByRoleResource_IsOrgResourceTrue();

    long countByRoleResource_IsProjectResourceTrue();

    long countByRoleResource_IsFileShareResourceTrue();

}
