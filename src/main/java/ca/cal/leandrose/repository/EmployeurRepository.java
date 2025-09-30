package ca.cal.leandrose.repository;

import ca.cal.leandrose.model.Employeur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface EmployeurRepository extends JpaRepository<Employeur, Long> {

    @Query("SELECT CASE WHEN COUNT(e) > 0 THEN TRUE ELSE FALSE END FROM Employeur e WHERE e.credentials.email = :email")
    boolean existsByEmail(@Param("email") String email);
}
