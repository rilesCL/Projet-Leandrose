package ca.cal.leandrose.model;

import ca.cal.leandrose.model.auth.Role;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserAppTest {

    @Test
    void testUserAppGetters() {
        // Using Employeur as a concrete implementation
        Employeur employeur = Employeur.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .password("password123")
                .companyName("TechCorp")
                .field("IT")
                .build();

        assertEquals(1L, employeur.getId());
        assertEquals("John", employeur.getFirstName());
        assertEquals("Doe", employeur.getLastName());
        assertEquals("john.doe@example.com", employeur.getEmail());
        assertEquals("password123", employeur.getPassword());
        assertEquals(Role.EMPLOYEUR, employeur.getRole());
        assertNotNull(employeur.getAuthorities());
    }

    @Test
    void testUserAppSetters() {
        Employeur employeur = new Employeur();
        employeur.setId(2L);
        employeur.setFirstName("Jane");
        employeur.setLastName("Smith");
        employeur.setCredentials(new ca.cal.leandrose.model.auth.Credentials("jane.smith@example.com", "pass456", Role.EMPLOYEUR));

        assertEquals(2L, employeur.getId());
        assertEquals("Jane", employeur.getFirstName());
        assertEquals("Smith", employeur.getLastName());
        assertEquals("jane.smith@example.com", employeur.getEmail());
        assertEquals("pass456", employeur.getPassword());
        assertEquals(Role.EMPLOYEUR, employeur.getRole());
    }
}
