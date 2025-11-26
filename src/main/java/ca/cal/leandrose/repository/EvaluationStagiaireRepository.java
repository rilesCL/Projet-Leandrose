package ca.cal.leandrose.repository;

import ca.cal.leandrose.model.EvaluationStagiaire;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EvaluationStagiaireRepository extends JpaRepository<EvaluationStagiaire, Long> {

  List<EvaluationStagiaire> findByEmployeurId(Long employeurId);

  List<EvaluationStagiaire> findByStudentId(Long studentId);

  Optional<EvaluationStagiaire> findByStudentIdAndInternshipOfferId(
      Long studentId, Long internshipOfferId);

  boolean existsByInternshipOfferIdAndStudentId(Long internshipOfferId, Long studentId);
}
