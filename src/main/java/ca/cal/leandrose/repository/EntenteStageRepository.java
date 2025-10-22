package ca.cal.leandrose.repository;

import ca.cal.leandrose.model.EntenteStage;
import ca.cal.leandrose.model.EntenteStage.StatutEntente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EntenteStageRepository extends JpaRepository<EntenteStage, Long> {

    @Query("select e from EntenteStage e join e.candidature c join c.student s where s.id = :studentId")
    List<EntenteStage> findByStudentId(@Param("studentId") Long studentId);

    @Query("select e from EntenteStage e join e.candidature c join c.internshipOffer o join o.employeur emp where emp.id = :employeurId")
    List<EntenteStage> findByEmployeurId(@Param("employeurId") Long employeurId);

    List<EntenteStage> findByStatut(StatutEntente statut);

    Optional<EntenteStage> findByCandidatureId(Long candidatureId);

    boolean existsByCandidatureId(Long candidatureId);

    @Query("select e from EntenteStage e where e.statut = 'EN_ATTENTE_SIGNATURE'")
    List<EntenteStage> findEntentesEnAttenteSignature();
}