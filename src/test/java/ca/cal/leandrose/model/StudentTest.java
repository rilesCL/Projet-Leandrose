package ca.cal.leandrose.model;

import ca.cal.leandrose.model.auth.Role;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StudentTest {

  @Test
  void testStudentBuilder() {
    Student student =
        Student.builder()
            .id(10L)
            .firstName("Alice")
            .lastName("Johnson")
            .email("alice.johnson@student.com")
            .password("alicePass")
            .studentNumber("STU12345")
            .program("Computer Science")
            .build();

    assertEquals(10L, student.getId());
    assertEquals("Alice", student.getFirstName());
    assertEquals("Johnson", student.getLastName());
    assertEquals("alice.johnson@student.com", student.getEmail());
    assertEquals("alicePass", student.getPassword());
    assertEquals(Role.STUDENT, student.getRole());
    assertEquals("STU12345", student.getStudentNumber());
    assertEquals("Computer Science", student.getProgram());
  }

  @Test
  void testStudentNoArgsConstructorAndSetters() {
    Student student = new Student();
    student.setId(20L);
    student.setFirstName("Bob");
    student.setLastName("Marley");
    student.setCredentials(
        new ca.cal.leandrose.model.auth.Credentials(
            "bob.marley@student.com", "bobPass", Role.STUDENT));
    student.setStudentNumber("STU67890");
    student.setProgram("Mathematics");

    assertEquals(20L, student.getId());
    assertEquals("Bob", student.getFirstName());
    assertEquals("Marley", student.getLastName());
    assertEquals("bob.marley@student.com", student.getEmail());
    assertEquals("bobPass", student.getPassword());
    assertEquals(Role.STUDENT, student.getRole());
    assertEquals("STU67890", student.getStudentNumber());
    assertEquals("Mathematics", student.getProgram());
  }
}
