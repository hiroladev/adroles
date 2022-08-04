package de.hirola.adroles.data.repository;

import de.hirola.adroles.data.entity.ADUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ADUserRepository extends JpaRepository<ADUser, Integer> {
    @Query("select a from ADUser a " +
            "where lower(a.logonName) like lower(concat('%', :searchTerm, '%')) " +"" +
            "or lower(a.distinguishedName) like lower(concat('%', :searchTerm, '%')) " +
            "order by a.logonName")
    List<ADUser> search(@Param("searchTerm") String searchTerm);

    Optional<ADUser> findFirstByObjectSID(String objectSID);

    List<ADUser> findByIsRoleManagedTrueOrderByLogonNameAsc();

    List<ADUser> findByPerson_IdAndIsRoleManagedTrue(Integer id);

    long countByPasswordExpiresFalse();
}
