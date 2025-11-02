package ca.cal.leandrose.repository;

import ca.cal.leandrose.model.Convocation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ConvocationRepository extends JpaRepository<Convocation, Long> {

  List<Convocation> findByCandidature_InternshipOffer_Id(Long internshipOfferId);

  Optional<Convocation> findByCandidatureId(Long candidatureId);

  List<Convocation> findByCandidature_Student_Id(Long studentId);
}
