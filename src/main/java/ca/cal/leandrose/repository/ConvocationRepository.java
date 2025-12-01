package ca.cal.leandrose.repository;

import ca.cal.leandrose.model.Convocation;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConvocationRepository extends JpaRepository<Convocation, Long> {

  List<Convocation> findByCandidature_InternshipOffer_Id(Long internshipOfferId);

  List<Convocation> findByCandidature_Student_Id(Long studentId);
}
