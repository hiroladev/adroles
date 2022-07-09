package de.hirola.adroles.data.repository;

import de.hirola.adroles.data.entity.Company;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CompanyRepository extends JpaRepository<Company, Integer> {

}
