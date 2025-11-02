package ca.cal.leandrose.repository;

import ca.cal.leandrose.model.Employeur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface EmployeurRepository extends JpaRepository<Employeur, Long> {

  @Query(
      """
    select case when count(e) > 0 then true else false end
    from Employeur e
    where e.credentials.email = :email
    """)
  boolean existsByEmail(@Param("email") String email);
}
