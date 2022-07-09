package de.hirola.adroles.data.service;

import de.hirola.adroles.data.entity.Company;
import de.hirola.adroles.data.entity.Person;
import de.hirola.adroles.data.entity.Status;
import de.hirola.adroles.data.repository.CompanyRepository;
import de.hirola.adroles.data.repository.PersonRepository;
import de.hirola.adroles.data.repository.StatusRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ADRolesService {

    private final PersonRepository personRepository;
    private final CompanyRepository companyRepository;
    private final StatusRepository statusRepository;

    public ADRolesService(PersonRepository personRepository,
                          CompanyRepository companyRepository,
                          StatusRepository statusRepository) {
        this.personRepository = personRepository;
        this.companyRepository = companyRepository;
        this.statusRepository = statusRepository;
    }

    public List<Person> findAllPersons(String stringFilter) {
        if (stringFilter == null || stringFilter.isEmpty()) {
            return personRepository.findAll();
        } else {
            return personRepository.search(stringFilter);
        }
    }

    public long countPersons() {
        return personRepository.count();
    }

    public void deletePerson(Person person) {
        personRepository.delete(person);
    }

    public void savePerson(Person person) {
        if (person == null) {
            System.err.println("Person is null. Are you sure you have connected your form to the application?");
            return;
        }
        personRepository.save(person);
    }

    public List<Company> findAllCompanies() {
        return companyRepository.findAll();
    }

    public List<Status> findAllStatuses(){
        return statusRepository.findAll();
    }
}
