package ca.cal.leandrose.presentation.request;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Set;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class VerifyPasswordRequestTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("Valid VerifyPasswordRequest should pass validation")
    void testValidVerifyPasswordRequest() {
        VerifyPasswordRequest request = new VerifyPasswordRequest();
        request.setPassword("MySecurePassword123");

        Set<ConstraintViolation<VerifyPasswordRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("Blank password should fail validation")
    void testBlankPassword() {
        VerifyPasswordRequest request = new VerifyPasswordRequest();
        request.setPassword("");

        Set<ConstraintViolation<VerifyPasswordRequest>> violations = validator.validate(request);
        assertEquals(1, violations.size());
    }

    @Test
    @DisplayName("Null password should fail validation")
    void testNullPassword() {
        VerifyPasswordRequest request = new VerifyPasswordRequest();
        request.setPassword(null);

        Set<ConstraintViolation<VerifyPasswordRequest>> violations = validator.validate(request);
        assertEquals(1, violations.size());
    }

    @Test
    @DisplayName("Whitespace only password should fail validation")
    void testWhitespacePassword() {
        VerifyPasswordRequest request = new VerifyPasswordRequest();
        request.setPassword("   ");

        Set<ConstraintViolation<VerifyPasswordRequest>> violations = validator.validate(request);
        assertEquals(1, violations.size());
    }
}