package ca.cal.leandrose.repository;

import ca.cal.leandrose.model.Emprunteur;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmprunteurRepository extends JpaRepository<Emprunteur, Long> {
}