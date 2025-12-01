package ca.cal.leandrose.presentation.request;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class RegisterEmployeurTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("Valid RegisterEmployeur should pass validation")
    void testValidRegisterEmployeur() {
        RegisterEmployeur request = new RegisterEmployeur();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setEmail("john.doe@example.com");
        request.setPassword("SecurePass123");
        request.setCompanyName("Tech Corp");
        request.setField("Technology");

        Set<ConstraintViolation<RegisterEmployeur>> violations = validator.validate(request);
        assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("Blank first name should fail validation")
    void testBlankFirstName() {
        RegisterEmployeur request = new RegisterEmployeur();
        request.setFirstName("");
        request.setLastName("Doe");
        request.setEmail("john.doe@example.com");
        request.setPassword("SecurePass123");
        request.setCompanyName("Tech Corp");
        request.setField("Technology");

        Set<ConstraintViolation<RegisterEmployeur>> violations = validator.validate(request);
        assertEquals(1, violations.size());
        assertEquals("First name is required", violations.iterator().next().getMessage());
    }

    @Test
    @DisplayName("Blank last name should fail validation")
    void testBlankLastName() {
        RegisterEmployeur request = new RegisterEmployeur();
        request.setFirstName("John");
        request.setLastName("");
        request.setEmail("john.doe@example.com");
        request.setPassword("SecurePass123");
        request.setCompanyName("Tech Corp");
        request.setField("Technology");

        Set<ConstraintViolation<RegisterEmployeur>> violations = validator.validate(request);
        assertEquals(1, violations.size());
        assertEquals("Last name is required", violations.iterator().next().getMessage());
    }

    @Test
    @DisplayName("Invalid email should fail validation")
    void testInvalidEmail() {
        RegisterEmployeur request = new RegisterEmployeur();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setEmail("invalid-email");
        request.setPassword("SecurePass123");
        request.setCompanyName("Tech Corp");
        request.setField("Technology");

        Set<ConstraintViolation<RegisterEmployeur>> violations = validator.validate(request);
        assertEquals(1, violations.size());
        assertEquals("Email must be valid", violations.iterator().next().getMessage());
    }

    @Test
    @DisplayName("Short password should fail validation")
    void testShortPassword() {
        RegisterEmployeur request = new RegisterEmployeur();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setEmail("john.doe@example.com");
        request.setPassword("Short1");
        request.setCompanyName("Tech Corp");
        request.setField("Technology");

        Set<ConstraintViolation<RegisterEmployeur>> violations = validator.validate(request);
        assertEquals(1, violations.size());
        assertEquals("Password must be at least 8 characters long", violations.iterator().next().getMessage());
    }

    @Test
    @DisplayName("Blank company name should fail validation")
    void testBlankCompanyName() {
        RegisterEmployeur request = new RegisterEmployeur();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setEmail("john.doe@example.com");
        request.setPassword("SecurePass123");
        request.setCompanyName("");
        request.setField("Technology");

        Set<ConstraintViolation<RegisterEmployeur>> violations = validator.validate(request);
        assertEquals(1, violations.size());
    }

    @Test
    @DisplayName("Blank field should fail validation")
    void testBlankField() {
        RegisterEmployeur request = new RegisterEmployeur();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setEmail("john.doe@example.com");
        request.setPassword("SecurePass123");
        request.setCompanyName("Tech Corp");
        request.setField("");

        Set<ConstraintViolation<RegisterEmployeur>> violations = validator.validate(request);
        assertEquals(1, violations.size());
    }
}
