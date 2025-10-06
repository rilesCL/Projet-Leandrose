package ca.cal.leandrose.repository;

import ca.cal.leandrose.model.Convocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ConvocationRepository extends JpaRepository<Convocation, Long> {

    @Query("""
        SELECT c FROM Convocation c
        WHERE c.candidature.internshipOffer.id = :id
    """)
    List<Convocation> findConvocationByInternShipOfferId(@Param("id") Long id);
}