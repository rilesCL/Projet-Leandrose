package ca.cal.leandrose.repository;

import ca.cal.leandrose.model.Convocation;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.List;

public interface ConvocationRepository extends JpaRepository<Convocation, Long> {

    List<Convocation> findByCandidatureInternshipOfferId(Long id);
}