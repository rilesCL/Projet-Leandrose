package ca.cal.leandrose.repository;

import ca.cal.leandrose.model.EntenteStage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EntenteStageRepository extends JpaRepository<EntenteStage, Long> {

  boolean existsByCandidatureId(Long candidatureId);
}
