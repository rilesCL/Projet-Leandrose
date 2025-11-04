package ca.cal.leandrose.model;

import ca.cal.leandrose.model.auth.Credentials;
import ca.cal.leandrose.model.auth.Role;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@DiscriminatorValue("S")
@Getter
@Setter
@NoArgsConstructor
public class Student extends UserApp {
  private String studentNumber;
  private String program;

  @Embedded private SchoolTerm internshipTerm;

  @Builder
  public Student(
      Long id,
      String firstName,
      String lastName,
      String email,
      String password,
      String studentNumber,
      String program,
      SchoolTerm internshipTerm) {
    super(
        id,
        firstName,
        lastName,
        Credentials.builder().email(email).password(password).role(Role.STUDENT).build());
    this.studentNumber = studentNumber;
    this.program = program;
    this.internshipTerm = internshipTerm != null ? internshipTerm : SchoolTerm.getNextTerm();
  }

  public String getTermAsString() {
    return internshipTerm.getSeason().toString() + " " + internshipTerm.getYear();
  }
}
