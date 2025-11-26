package ca.cal.leandrose.repository;


import ca.cal.leandrose.model.EvaluationStagiaire;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EvaluationStagiaireRepository extends JpaRepository<EvaluationStagiaire, Long> {

    List<EvaluationStagiaire> findByEmployeurId(Long employeurId);
    List<EvaluationStagiaire> findByProfesseurId(Long professeurId);
    List<EvaluationStagiaire> findByStudentId(Long studentId);
    Optional<EvaluationStagiaire> findByStudentIdAndInternshipOfferId(Long studentId, Long internshipOfferId);

    boolean existsByInternshipOfferIdAndStudentId(Long internshipOfferId, Long studentId);

}
