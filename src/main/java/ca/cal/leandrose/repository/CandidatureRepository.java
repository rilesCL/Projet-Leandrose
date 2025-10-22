package ca.cal.leandrose.repository;

import ca.cal.leandrose.model.Candidature;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CandidatureRepository extends JpaRepository<Candidature, Long> {

    Optional<Candidature> findByStudentIdAndInternshipOfferId(Long studentId, Long offerId);

    List<Candidature> findByStudentIdOrderByApplicationDateDesc(Long studentId);

    List<Candidature> findByInternshipOfferIdOrderByApplicationDateDesc(Long offerId);

    @Query("""
    select c from Candidature c
    where c.internshipOffer.employeur.id = :employeurId
    order by c.applicationDate desc
    """)
    List<Candidature> findByEmployeurIdOrderByApplicationDateDesc(@Param("employeurId") Long employeurId);

    List<Candidature> findByStatus(Candidature.Status status);
}