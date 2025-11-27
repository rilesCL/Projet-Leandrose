package ca.cal.leandrose.presentation.request;

import ca.cal.leandrose.presentation.request.UpdateUserRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class UpdateUserRequestTest {

    @Test
    @DisplayName("Valid UpdateUserRequest should be created")
    void testValidUpdateUserRequest() {
        UpdateUserRequest request = new UpdateUserRequest();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setEmail("john.doe@example.com");
        request.setNewPassword("NewSecurePass123");
        request.setCurrentPassword("OldSecurePass123");
        request.setPhoneNumber("123-456-7890");

        assertNotNull(request);
        assertEquals("John", request.getFirstName());
        assertEquals("Doe", request.getLastName());
        assertEquals("john.doe@example.com", request.getEmail());
        assertEquals("NewSecurePass123", request.getNewPassword());
        assertEquals("OldSecurePass123", request.getCurrentPassword());
        assertEquals("123-456-7890", request.getPhoneNumber());
    }

    @Test
    @DisplayName("Partial update should work")
    void testPartialUpdate() {
        UpdateUserRequest request = new UpdateUserRequest();
        request.setFirstName("Jane");
        request.setEmail("jane@example.com");

        assertEquals("Jane", request.getFirstName());
        assertNull(request.getLastName());
        assertEquals("jane@example.com", request.getEmail());
        assertNull(request.getNewPassword());
    }

    @Test
    @DisplayName("All fields nullable")
    void testAllFieldsNullable() {
        UpdateUserRequest request = new UpdateUserRequest();

        assertNull(request.getFirstName());
        assertNull(request.getLastName());
        assertNull(request.getEmail());
        assertNull(request.getNewPassword());
        assertNull(request.getCurrentPassword());
        assertNull(request.getPhoneNumber());
    }
}