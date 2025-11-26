package ca.cal.leandrose.presentation.request;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Set;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RegisterStudentTest {

  private static Validator validator;

  @BeforeAll
  static void setupValidator() {
    ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    validator = factory.getValidator();
  }

  @Test
  @DisplayName("Valid RegisterStudent passes validation")
  void testValidRegisterStudent() {
    RegisterStudent req = new RegisterStudent();
    req.setFirstName("Marie");
    req.setLastName("Dupont");
    req.setEmail("marie.dupont@student.com");
    req.setPassword("Password123");
    req.setStudentNumber("STU12345");
    req.setProgram("Informatique");

    Set<ConstraintViolation<RegisterStudent>> violations = validator.validate(req);

    assertThat(violations).isEmpty();
  }

  @Test
  @DisplayName("Invalid RegisterStudent fails validation")
  void testInvalidRegisterStudent() {
    RegisterStudent req = new RegisterStudent();
    req.setFirstName("");
    req.setLastName("");
    req.setEmail("");
    req.setPassword("123");
    req.setStudentNumber("");
    req.setProgram("");

    Set<ConstraintViolation<RegisterStudent>> violations = validator.validate(req);

    assertThat(violations).isNotEmpty();
    assertThat(violations).hasSizeGreaterThanOrEqualTo(5);
  }
}
