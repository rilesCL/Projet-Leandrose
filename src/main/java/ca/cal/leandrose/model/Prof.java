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
  private String nomCollege;
  private String addresse;

  private String department;

  @Builder
  public Prof(
      Long id,
      String firstName,
      String lastName,
      String email,
      String password,
      String employeeNumber,
      String nomCollege,
      String addresse,
      String department) {
    super(
        id,
        firstName,
        lastName,
        Credentials.builder().email(email).password(password).role(Role.PROF).build());
    this.employeeNumber = employeeNumber;
    this.nomCollege = nomCollege;
    this.addresse = addresse;
    this.department = department;
  }
}

