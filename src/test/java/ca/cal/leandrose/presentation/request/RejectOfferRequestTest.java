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

class RejectOfferRequestTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("Valid RejectOfferRequest should pass validation")
    void testValidRejectOfferRequest() {
        RejectOfferRequest request = new RejectOfferRequest();
        request.setComment("The position doesn't match my skills");

        Set<ConstraintViolation<RejectOfferRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("Blank comment should fail validation")
    void testBlankComment() {
        RejectOfferRequest request = new RejectOfferRequest();
        request.setComment("");

        Set<ConstraintViolation<RejectOfferRequest>> violations = validator.validate(request);
        assertEquals(1, violations.size());
        assertEquals("Un commentaire est obligatoire pour rejeter une offre", violations.iterator().next().getMessage());
    }

    @Test
    @DisplayName("Null comment should fail validation")
    void testNullComment() {
        RejectOfferRequest request = new RejectOfferRequest();
        request.setComment(null);

        Set<ConstraintViolation<RejectOfferRequest>> violations = validator.validate(request);
        assertEquals(1, violations.size());
        assertEquals("Un commentaire est obligatoire pour rejeter une offre", violations.iterator().next().getMessage());
    }

    @Test
    @DisplayName("Whitespace only comment should fail validation")
    void testWhitespaceComment() {
        RejectOfferRequest request = new RejectOfferRequest();
        request.setComment("   ");

        Set<ConstraintViolation<RejectOfferRequest>> violations = validator.validate(request);
        assertEquals(1, violations.size());
    }
}