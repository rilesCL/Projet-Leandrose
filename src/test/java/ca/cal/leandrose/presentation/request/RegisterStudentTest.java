package ca.cal.leandrose.presentation.request;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RegisterStudentTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("Valid RegisterStudent should pass validation")
    void testValidRegisterStudent() {
        RegisterStudent request = new RegisterStudent();
        request.setFirstName("Alice");
        request.setLastName("Smith");
        request.setEmail("alice.smith@example.com");
        request.setPassword("SecurePass123");
        request.setStudentNumber("123456");
        request.setProgram("Computer Science");

        Set<ConstraintViolation<RegisterStudent>> violations = validator.validate(request);
        assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("Blank first name should fail validation")
    void testBlankFirstName() {
        RegisterStudent request = new RegisterStudent();
        request.setFirstName("");
        request.setLastName("Smith");
        request.setEmail("alice.smith@example.com");
        request.setPassword("SecurePass123");
        request.setStudentNumber("123456");
        request.setProgram("Computer Science");

        Set<ConstraintViolation<RegisterStudent>> violations = validator.validate(request);
        assertEquals(1, violations.size());
        assertEquals("First name is required", violations.iterator().next().getMessage());
    }

    @Test
    @DisplayName("Blank last name should fail validation")
    void testBlankLastName() {
        RegisterStudent request = new RegisterStudent();
        request.setFirstName("Alice");
        request.setLastName("");
        request.setEmail("alice.smith@example.com");
        request.setPassword("SecurePass123");
        request.setStudentNumber("123456");
        request.setProgram("Computer Science");

        Set<ConstraintViolation<RegisterStudent>> violations = validator.validate(request);
        assertEquals(1, violations.size());
        assertEquals("last name is required", violations.iterator().next().getMessage());
    }

    @Test
    @DisplayName("Blank email should fail validation")
    void testBlankEmail() {
        RegisterStudent request = new RegisterStudent();
        request.setFirstName("Alice");
        request.setLastName("Smith");
        request.setEmail("");
        request.setPassword("SecurePass123");
        request.setStudentNumber("123456");
        request.setProgram("Computer Science");

        Set<ConstraintViolation<RegisterStudent>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().equals("Email name is required")));
    }

    @Test
    @DisplayName("Invalid email should fail validation")
    void testInvalidEmail() {
        RegisterStudent request = new RegisterStudent();
        request.setFirstName("Alice");
        request.setLastName("Smith");
        request.setEmail("not-an-email");
        request.setPassword("SecurePass123");
        request.setStudentNumber("123456");
        request.setProgram("Computer Science");

        Set<ConstraintViolation<RegisterStudent>> violations = validator.validate(request);
        assertEquals(1, violations.size());
    }

    @Test
    @DisplayName("Short password should fail validation")
    void testShortPassword() {
        RegisterStudent request = new RegisterStudent();
        request.setFirstName("Alice");
        request.setLastName("Smith");
        request.setEmail("alice.smith@example.com");
        request.setPassword("Short1");
        request.setStudentNumber("123456");
        request.setProgram("Computer Science");

        Set<ConstraintViolation<RegisterStudent>> violations = validator.validate(request);
        assertEquals(1, violations.size());
        assertEquals("Password must be at least 8 characters long", violations.iterator().next().getMessage());
    }

    @Test
    @DisplayName("Blank student number should fail validation")
    void testBlankStudentNumber() {
        RegisterStudent request = new RegisterStudent();
        request.setFirstName("Alice");
        request.setLastName("Smith");
        request.setEmail("alice.smith@example.com");
        request.setPassword("SecurePass123");
        request.setStudentNumber("");
        request.setProgram("Computer Science");

        Set<ConstraintViolation<RegisterStudent>> violations = validator.validate(request);
        assertEquals(1, violations.size());
        assertEquals("numero matricule is required", violations.iterator().next().getMessage());
    }

    @Test
    @DisplayName("Blank program should fail validation")
    void testBlankProgram() {
        RegisterStudent request = new RegisterStudent();
        request.setFirstName("Alice");
        request.setLastName("Smith");
        request.setEmail("alice.smith@example.com");
        request.setPassword("SecurePass123");
        request.setStudentNumber("123456");
        request.setProgram("");

        Set<ConstraintViolation<RegisterStudent>> violations = validator.validate(request);
        assertEquals(1, violations.size());
        assertEquals("Name of the program is required", violations.iterator().next().getMessage());
    }
}