package ca.cal.leandrose.service.dto;

import ca.cal.leandrose.model.Student;
import ca.cal.leandrose.model.auth.Role;
import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
public class StudentDto extends UserDTO {
  private String studentNumber;
  private String program;
  private String internshipTerm;
  private Map<String, String> error;
  private boolean isExpired;

  @Builder
  public StudentDto(
      Long id,
      String firstName,
      String lastName,
      String email,
      Role role,
      String studentNumber,
      String program,
      String internshipTerm,
      boolean isExpired) {
    super(id, firstName, lastName, email, role);
    this.studentNumber = studentNumber;
    this.program = program;
    this.internshipTerm = internshipTerm;
    this.isExpired = isExpired;
  }

  public StudentDto() {}

  public StudentDto(String error) {
    this.error = Map.of("error", error);
  }

  public static StudentDto create(Student student) {
    boolean isExpired = student.getInternshipTerm().isBeforeNextTerm();

    return StudentDto.builder()
        .id(student.getId())
        .firstName(student.getFirstName())
        .lastName(student.getLastName())
        .email(student.getEmail())
        .role(student.getRole())
        .studentNumber(student.getStudentNumber())
        .program(student.getProgram())
        .internshipTerm(student.getTermAsString())
        .isExpired(isExpired)
        .build();
  }

  public static StudentDto empty() {
    return new StudentDto();
  }

  public String getName() {
    return this.getFirstName() + this.getLastName();
  }
}
