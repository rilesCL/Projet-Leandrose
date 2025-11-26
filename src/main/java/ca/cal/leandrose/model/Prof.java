package ca.cal.leandrose.model;

import ca.cal.leandrose.model.auth.Credentials;
import ca.cal.leandrose.model.auth.Role;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@DiscriminatorValue("P")
@Getter
@Setter
@NoArgsConstructor
public class Prof extends UserApp {
  @Column(unique = true, nullable = false)
  private String employeeNumber;
  private String nameCollege;
  private String address;
  private String fax_machine;
  private String department;
  private String phoneNumber;

  @Builder
  public Prof(
      Long id,
      String firstName,
      String lastName,
      String email,
      String password,
      String employeeNumber,
      String nameCollege,
      String address,
      String fax_machine,
      String department,
      String phoneNumber) {
    super(
        id,
        firstName,
        lastName,
        Credentials.builder().email(email).password(password).role(Role.PROF).build());
    this.employeeNumber = employeeNumber;
    this.nameCollege = nameCollege;
    this.address = address;
    this.fax_machine = fax_machine;
    this.department = department;
    this.phoneNumber = phoneNumber;
  }
}

