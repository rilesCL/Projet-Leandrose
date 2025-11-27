package ca.cal.leandrose.model;

import static org.junit.jupiter.api.Assertions.*;

import ca.cal.leandrose.model.auth.Role;
import org.junit.jupiter.api.Test;

class GestionnaireTest {

  @Test
  void testGestionnaireBuilder() {
    Gestionnaire gestionnaire =
        Gestionnaire.builder()
            .id(1L)
            .firstName("Manager")
            .lastName("Test")
            .email("manager@college.com")
            .password("password")
            .phoneNumber("514-123-4567")
            .build();

    assertEquals(1L, gestionnaire.getId());
    assertEquals("Manager", gestionnaire.getFirstName());
    assertEquals("Test", gestionnaire.getLastName());
    assertEquals("manager@college.com", gestionnaire.getEmail());
    assertEquals("password", gestionnaire.getPassword());
    assertEquals(Role.GESTIONNAIRE, gestionnaire.getRole());
    assertEquals("514-123-4567", gestionnaire.getPhoneNumber());
  }

  @Test
  void testGestionnaireNoArgsConstructorAndSetters() {
    Gestionnaire gestionnaire = new Gestionnaire();
    gestionnaire.setId(2L);
    gestionnaire.setFirstName("Admin");
    gestionnaire.setLastName("User");
    gestionnaire.setCredentials(
        new ca.cal.leandrose.model.auth.Credentials(
            "admin@college.com", "adminPass", Role.GESTIONNAIRE));
    gestionnaire.setPhoneNumber("514-987-6543");

    assertEquals(2L, gestionnaire.getId());
    assertEquals("Admin", gestionnaire.getFirstName());
    assertEquals("User", gestionnaire.getLastName());
    assertEquals("admin@college.com", gestionnaire.getEmail());
    assertEquals("adminPass", gestionnaire.getPassword());
    assertEquals(Role.GESTIONNAIRE, gestionnaire.getRole());
    assertEquals("514-987-6543", gestionnaire.getPhoneNumber());
  }

  @Test
  void testGestionnaireInheritsFromUserApp() {
    Gestionnaire gestionnaire =
        Gestionnaire.builder()
            .id(1L)
            .firstName("Manager")
            .lastName("Test")
            .email("manager@college.com")
            .password("password")
            .phoneNumber("514-123-4567")
            .build();

    assertTrue(gestionnaire instanceof UserApp);
    assertEquals(Role.GESTIONNAIRE, gestionnaire.getRole());
  }
}




