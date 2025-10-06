package ca.cal.leandrose.repository;

import ca.cal.leandrose.model.Cv;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CvRepository extends JpaRepository<Cv, Long> {
    Optional<Cv> findByStudentId(Long studentId);

    List<Cv> findByStatus(Cv.Status status);
    Optional<Cv> findByStudentIdAndStatus(Long id, Cv.Status status);
}
