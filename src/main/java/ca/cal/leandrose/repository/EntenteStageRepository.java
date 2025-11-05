package ca.cal.leandrose.repository;

import ca.cal.leandrose.model.EntenteStage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EntenteStageRepository extends JpaRepository<EntenteStage, Long> {

  boolean existsByCandidatureId(Long candidatureId);

    Optional<EntenteStage> findByCandidature_Student_IdAndCandidature_InternshipOffer_IdAndStatut(
            Long studentId, Long internshipOfferId, EntenteStage.StatutEntente statut);

    List<EntenteStage> findByCandidature_InternshipOffer_Employeur_IdAndStatut(
            Long employeurId, EntenteStage.StatutEntente statut);
}
