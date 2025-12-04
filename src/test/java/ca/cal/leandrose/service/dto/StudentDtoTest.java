package ca.cal.leandrose.service.dto;

import static org.junit.jupiter.api.Assertions.*;

import ca.cal.leandrose.model.Program;
import ca.cal.leandrose.model.SchoolTerm;
import ca.cal.leandrose.model.Student;
import ca.cal.leandrose.model.auth.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class StudentDtoTest {

  private Student student;
  private SchoolTerm schoolTerm;

  @BeforeEach
  void setUp() {
    schoolTerm = new SchoolTerm(SchoolTerm.Season.FALL, 2025);

    student =
        Student.builder()
            .id(1L)
            .firstName("John")
            .lastName("Doe")
            .email("john.doe@student.com")
            .password("password")
            .studentNumber("STU001")
            .program(Program.COMPUTER_SCIENCE.getTranslationKey())
            .internshipTerm(schoolTerm)
            .phoneNumber("514-123-4567")
            .build();
  }

  @Test
  void testStudentDtoBuilder() {
    StudentDto dto =
        StudentDto.builder()
            .id(1L)
            .firstName("John")
            .lastName("Doe")
            .email("john.doe@student.com")
            .role(Role.STUDENT)
            .studentNumber("STU001")
            .program("Computer Science")
            .internshipTerm("FALL 2025")
            .phoneNumber("514-123-4567")
            .isExpired(false)
            .build();

    assertEquals(1L, dto.getId());
    assertEquals("John", dto.getFirstName());
    assertEquals("Doe", dto.getLastName());
    assertEquals("john.doe@student.com", dto.getEmail());
    assertEquals(Role.STUDENT, dto.getRole());
    assertEquals("STU001", dto.getStudentNumber());
    assertEquals("Computer Science", dto.getProgram());
    assertEquals("FALL 2025", dto.getInternshipTerm());
    assertEquals("514-123-4567", dto.getPhoneNumber());
    assertFalse(dto.isExpired());
  }

  @Test
  void testStudentDtoNoArgsConstructor() {
    StudentDto dto = new StudentDto();

    assertNotNull(dto);
    assertNull(dto.getId());
    assertNull(dto.getFirstName());
    assertNull(dto.getStudentNumber());
  }

  @Test
  void testStudentDtoWithError() {
    StudentDto dto = new StudentDto("Test error message");

    assertNotNull(dto);
    assertNotNull(dto.getError());
    assertEquals("Test error message", dto.getError().get("error"));
  }

  @Test
  void testCreate_WithCompleteStudent() {
    StudentDto dto = StudentDto.create(student);

    assertNotNull(dto);
    assertEquals(1L, dto.getId());
    assertEquals("John", dto.getFirstName());
    assertEquals("Doe", dto.getLastName());
    assertEquals("john.doe@student.com", dto.getEmail());
    assertEquals(Role.STUDENT, dto.getRole());
    assertEquals("STU001", dto.getStudentNumber());
    assertEquals(Program.COMPUTER_SCIENCE.getTranslationKey(), dto.getProgram());
    assertEquals("FALL 2025", dto.getInternshipTerm());
    assertEquals("514-123-4567", dto.getPhoneNumber());
  }

  @Test
  void testGetName() {
    StudentDto dto =
        StudentDto.builder()
            .firstName("John")
            .lastName("Doe")
            .email("john.doe@student.com")
            .role(Role.STUDENT)
            .build();

    assertEquals("JohnDoe", dto.getName());
  }

  @Test
  void testGetName_WithNullNames() {
    StudentDto dto =
        StudentDto.builder()
            .firstName(null)
            .lastName(null)
            .email("test@example.com")
            .role(Role.STUDENT)
            .build();

    assertEquals("nullnull", dto.getName());
  }

  @Test
  void testGetName_WithNullFirstName() {
    StudentDto dto =
        StudentDto.builder()
            .firstName(null)
            .lastName("Doe")
            .email("test@example.com")
            .role(Role.STUDENT)
            .build();

    assertEquals("nullDoe", dto.getName());
  }

  @Test
  void testGetName_WithNullLastName() {
    StudentDto dto =
        StudentDto.builder()
            .firstName("John")
            .lastName(null)
            .email("test@example.com")
            .role(Role.STUDENT)
            .build();

    assertEquals("Johnnull", dto.getName());
  }

  @Test
  void testCreate_WithNullInternshipTerm() {

    Student studentWithoutTerm =
        Student.builder()
            .id(1L)
            .firstName("John")
            .lastName("Doe")
            .email("john@student.com")
            .password("password")
            .studentNumber("STU001")
            .program("Computer Science")
            .internshipTerm(null)
            .build();

    StudentDto dto = StudentDto.create(studentWithoutTerm);

    assertNotNull(dto);
    assertNotNull(dto.getInternshipTerm());
    assertEquals(1L, dto.getId());
    assertEquals("John", dto.getFirstName());
  }

  @Test
  void testEmpty() {
    StudentDto dto = StudentDto.empty();

    assertNotNull(dto);
    assertNull(dto.getId());
    assertNull(dto.getFirstName());
    assertNull(dto.getStudentNumber());
  }
}
