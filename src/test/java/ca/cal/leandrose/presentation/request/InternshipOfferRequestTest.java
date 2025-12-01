package ca.cal.leandrose.presentation.request;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InternshipOfferRequestTest {

    @Test
    @DisplayName("Valid InternshipOfferRequest should be created")
    void testValidInternshipOfferRequest() {
        InternshipOfferRequest request = new InternshipOfferRequest();
        request.setDescription("Software Development Internship");
        request.setStartDate("2024-06-01");
        request.setDurationInWeeks(12);
        request.setAddress("123 Tech Street, Montreal, QC");
        request.setRemuneration(25.50f);

        assertNotNull(request);
        assertEquals("Software Development Internship", request.getDescription());
        assertEquals("2024-06-01", request.getStartDate());
        assertEquals(12, request.getDurationInWeeks());
        assertEquals("123 Tech Street, Montreal, QC", request.getAddress());
        assertEquals(25.50f, request.getRemuneration(), 0.01);
    }

    @Test
    @DisplayName("All fields should be nullable")
    void testAllFieldsNullable() {
        InternshipOfferRequest request = new InternshipOfferRequest();

        assertNull(request.getDescription());
        assertNull(request.getStartDate());
        assertEquals(0, request.getDurationInWeeks());
        assertNull(request.getAddress());
        assertNull(request.getRemuneration());
    }

    @Test
    @DisplayName("Should handle zero duration")
    void testZeroDuration() {
        InternshipOfferRequest request = new InternshipOfferRequest();
        request.setDescription("Test Internship");
        request.setStartDate("2024-06-01");
        request.setDurationInWeeks(0);
        request.setAddress("123 Test St");
        request.setRemuneration(20.0f);

        assertEquals(0, request.getDurationInWeeks());
    }

    @Test
    @DisplayName("Should handle negative duration")
    void testNegativeDuration() {
        InternshipOfferRequest request = new InternshipOfferRequest();
        request.setDurationInWeeks(-5);

        assertEquals(-5, request.getDurationInWeeks());
    }

    @Test
    @DisplayName("Should handle zero remuneration")
    void testZeroRemuneration() {
        InternshipOfferRequest request = new InternshipOfferRequest();
        request.setDescription("Unpaid Internship");
        request.setStartDate("2024-06-01");
        request.setDurationInWeeks(8);
        request.setAddress("456 Volunteer Ave");
        request.setRemuneration(0.0f);

        assertEquals(0.0f, request.getRemuneration(), 0.01);
    }

    @Test
    @DisplayName("Should handle negative remuneration")
    void testNegativeRemuneration() {
        InternshipOfferRequest request = new InternshipOfferRequest();
        request.setRemuneration(-15.0f);

        assertEquals(-15.0f, request.getRemuneration(), 0.01);
    }

    @Test
    @DisplayName("Should handle large remuneration values")
    void testLargeRemuneration() {
        InternshipOfferRequest request = new InternshipOfferRequest();
        request.setRemuneration(999.99f);

        assertEquals(999.99f, request.getRemuneration(), 0.01);
    }

    @Test
    @DisplayName("Should handle date in correct format")
    void testDateFormat() {
        InternshipOfferRequest request = new InternshipOfferRequest();
        request.setStartDate("2024-12-25");

        assertEquals("2024-12-25", request.getStartDate());
    }

    @Test
    @DisplayName("Should accept any string as date (no validation)")
    void testInvalidDateFormat() {
        InternshipOfferRequest request = new InternshipOfferRequest();
        request.setStartDate("invalid-date");

        assertEquals("invalid-date", request.getStartDate());
    }

    @Test
    @DisplayName("Should handle empty strings")
    void testEmptyStrings() {
        InternshipOfferRequest request = new InternshipOfferRequest();
        request.setDescription("");
        request.setStartDate("");
        request.setAddress("");

        assertEquals("", request.getDescription());
        assertEquals("", request.getStartDate());
        assertEquals("", request.getAddress());
    }

    @Test
    @DisplayName("Should handle very long description")
    void testLongDescription() {
        InternshipOfferRequest request = new InternshipOfferRequest();
        String longDescription = "A".repeat(1000);
        request.setDescription(longDescription);

        assertEquals(1000, request.getDescription().length());
    }

    @Test
    @DisplayName("Should handle special characters in fields")
    void testSpecialCharacters() {
        InternshipOfferRequest request = new InternshipOfferRequest();
        request.setDescription("Software Dev @ Tech Co. (2024)");
        request.setAddress("123 Rue Saint-Jean, Montréal, QC H2X 1Y5");

        assertEquals("Software Dev @ Tech Co. (2024)", request.getDescription());
        assertEquals("123 Rue Saint-Jean, Montréal, QC H2X 1Y5", request.getAddress());
    }

    @Test
    @DisplayName("Equals and HashCode should work correctly")
    void testEqualsAndHashCode() {
        InternshipOfferRequest request1 = new InternshipOfferRequest();
        request1.setDescription("Test");
        request1.setStartDate("2024-06-01");
        request1.setDurationInWeeks(10);
        request1.setAddress("123 Street");
        request1.setRemuneration(20.0f);

        InternshipOfferRequest request2 = new InternshipOfferRequest();
        request2.setDescription("Test");
        request2.setStartDate("2024-06-01");
        request2.setDurationInWeeks(10);
        request2.setAddress("123 Street");
        request2.setRemuneration(20.0f);

        assertEquals(request1, request2);
        assertEquals(request1.hashCode(), request2.hashCode());
    }

    @Test
    @DisplayName("ToString should work correctly")
    void testToString() {
        InternshipOfferRequest request = new InternshipOfferRequest();
        request.setDescription("Test Internship");
        request.setStartDate("2024-06-01");
        request.setDurationInWeeks(12);
        request.setAddress("123 Test St");
        request.setRemuneration(25.0f);

        String toString = request.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("Test Internship"));
        assertTrue(toString.contains("2024-06-01"));
        assertTrue(toString.contains("12"));
        assertTrue(toString.contains("123 Test St"));
        assertTrue(toString.contains("25.0"));
    }
}