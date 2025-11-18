package ca.cal.leandrose.repository;

import ca.cal.leandrose.model.EntenteStage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EntenteStageRepository extends JpaRepository<EntenteStage, Long> {
    List<EntenteStage> findAllByProf_Id(Long profId);

  boolean existsByCandidatureId(Long candidatureId);

    Optional<EntenteStage> findByCandidature_Student_IdAndCandidature_InternshipOffer_IdAndStatut(
            Long studentId, Long internshipOfferId, EntenteStage.StatutEntente statut);
    Optional<EntenteStage>findByCandidature_Student_IdAndCandidature_InternshipOffer_Id(
            Long studentId, Long internshipId
    );

    List<EntenteStage> findByCandidature_InternshipOffer_Employeur_IdAndStatut(
            Long employeurId, EntenteStage.StatutEntente statut);
    List<EntenteStage> findByProf_IdAndStatut(
            Long profId, EntenteStage.StatutEntente statut);

    Optional<EntenteStage> findByProf_IdAndCandidature_Student_IdAndCandidature_InternshipOffer_IdAndStatut(
            Long profId, Long studentId, Long internshipOfferId, EntenteStage.StatutEntente statut);
    Optional<EntenteStage> findByProf_IdAndCandidature_Student_IdAndCandidature_InternshipOffer_Id(
            Long professeurId,
            Long studentId,
            Long internshipOfferId
    );

}
