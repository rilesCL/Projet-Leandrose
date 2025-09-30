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

    @Query("""
        select o from InternshipOffer o
        join fetch o.employeur e
        where o.status = :status
        order by o.startDate desc
    """)
    List<InternshipOffer> findByStatusWithEmployeur(@Param("status") InternshipOffer.Status status);

    @Query("""
        select o from InternshipOffer o
        where o.status = :status
        order by o.startDate desc
    """)
    List<InternshipOffer> findByStatusOrderByStartDateDesc(InternshipOffer.Status status);
}
