package de.hirola.adroles.data.repository;

import de.hirola.adroles.data.entity.RoleResource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface RoleResourceRepository extends JpaRepository<RoleResource, Integer> {
    @Query("select r from RoleResource r " +
            "where r.isOrgResource = false and r.isProjectResource = false and r.isFileShareResource = false")
    Optional<RoleResource> getDefaultResource();

    @Query("select r from RoleResource r where r.isOrgResource = true")
    Optional<RoleResource> getOrgResource();

    @Query("select r from RoleResource r where r.isProjectResource = true")
    Optional<RoleResource> getProjResource();

    @Query("select r from RoleResource r where r.isFileShareResource = true")
    Optional<RoleResource> getFileShareResource();
}
