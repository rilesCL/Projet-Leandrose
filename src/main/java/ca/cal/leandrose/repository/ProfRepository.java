package ca.cal.leandrose.repository;

import ca.cal.leandrose.model.Prof;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProfRepository extends JpaRepository<Prof, Long> {
  @Query(
      "select case when count(p) > 0 then true else false end from Prof p where p.credentials.email = :email")
  boolean existsByEmail(@Param("email") String email);

  @Query(
      "select case when count(p) > 0 then true else false end from Prof p where p.employeeNumber = :employeeNumber")
  boolean existsByEmployeeNumber(@Param("employeeNumber") String employeeNumber);
}
