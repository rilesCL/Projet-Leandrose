package ca.cal.leandrose.repository;

import ca.cal.leandrose.model.EntenteStage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EntenteStageRepository extends JpaRepository<EntenteStage, Long> {
    List<EntenteStage> findAllByProf_Id(Long profId);

  boolean existsByCandidatureId(Long candidatureId);
}
