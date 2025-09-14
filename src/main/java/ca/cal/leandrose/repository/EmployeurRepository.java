package ca.cal.leandrose.repository;

import ca.cal.leandrose.model.Employeur;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeurRepository extends JpaRepository<Employeur, Long> {
}