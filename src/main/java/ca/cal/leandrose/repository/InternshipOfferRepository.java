package ca.cal.leandrose.repository;

import ca.cal.leandrose.model.InternshipOffer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface InternshipOfferRepository extends JpaRepository<InternshipOffer, Long> {

    @Query("""
        select o from InternshipOffer o
        where o.employeur.id = :employeurId
        order by o.startDate desc
    """)
    List<InternshipOffer> findOffersByEmployeurId(@Param("employeurId") Long employeurId);
}
