package ca.cal.leandrose.presentation.request;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class RegisterEmployeurTest {

    private static Validator validator;

    @BeforeAll
    static void setupValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("Valid RegisterEmployeur passes validation")
    void testValidRegisterEmployeur() {
        RegisterEmployeur req = new RegisterEmployeur();
        req.setFirstName("John");
        req.setLastName("Doe");
        req.setEmail("john.doe@example.com");
        req.setPassword("Password123");
        req.setCompanyName("TechCorp");
        req.setField("IT");

        Set<ConstraintViolation<RegisterEmployeur>> violations = validator.validate(req);

        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("Invalid RegisterEmployeur fails validation")
    void testInvalidRegisterEmployeur() {
        RegisterEmployeur req = new RegisterEmployeur();
        req.setFirstName("");
        req.setLastName("");
        req.setEmail("not-an-email");
        req.setPassword("123");
        req.setCompanyName("");
        req.setField("");

        Set<ConstraintViolation<RegisterEmployeur>> violations = validator.validate(req);

        assertThat(violations).isNotEmpty();
        assertThat(violations).hasSizeGreaterThanOrEqualTo(5);
    }
}
