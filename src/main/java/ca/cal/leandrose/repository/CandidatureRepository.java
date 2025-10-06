package ca.cal.leandrose.repository;

import ca.cal.leandrose.model.Candidature;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CandidatureRepository extends JpaRepository<Candidature, Long> {

    Optional<Candidature> findByStudentIdAndInternshipOfferId(Long studentId, Long offerId);


    List<Candidature> findByStudentIdOrderByApplicationDateDesc(Long studentId);


    List<Candidature> findByInternshipOfferIdOrderByApplicationDateDesc(Long offerId);
}
