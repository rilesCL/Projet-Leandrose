package ca.cal.leandrose.repository;

import ca.cal.leandrose.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {
    @Query("select case when count(s) > 0 then true else false end from Student s where s.credentials.email = :email")
    boolean existsByEmail(@Param("email") String email);
}

