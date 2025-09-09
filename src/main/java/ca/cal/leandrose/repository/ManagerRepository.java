package ca.cal.leandrose.repository;

import ca.cal.leandrose.model.Gestionnaire;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ManagerRepository extends JpaRepository<Gestionnaire, Long> {

    Optional<Gestionnaire> findFirstByFirstNameAndLastName(String firstName, String lastName);
}
