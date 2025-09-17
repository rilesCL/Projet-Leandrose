package ca.cal.leandrose.repository;

import ca.cal.leandrose.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;


public interface StudentRepository extends JpaRepository<Student, Long> {

}
