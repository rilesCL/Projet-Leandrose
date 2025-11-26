package ca.cal.leandrose.model;

import static org.junit.jupiter.api.Assertions.*;

import ca.cal.leandrose.model.auth.Role;
import org.junit.jupiter.api.Test;

class EmployeurTest {

  @Test
  void testEmployeurBuilder() {
    Employeur employeur =
        Employeur.builder()
            .id(10L)
            .firstName("Alice")
            .lastName("Johnson")
            .email("alice.johnson@example.com")
            .password("alicePass")
            .companyName("InnoTech")
            .field("Software")
            .build();

    assertEquals(10L, employeur.getId());
    assertEquals("Alice", employeur.getFirstName());
    assertEquals("Johnson", employeur.getLastName());
    assertEquals("alice.johnson@example.com", employeur.getEmail());
    assertEquals("alicePass", employeur.getPassword());
    assertEquals(Role.EMPLOYEUR, employeur.getRole());
    assertEquals("InnoTech", employeur.getCompanyName());
    assertEquals("Software", employeur.getField());
  }

  @Test
  void testEmployeurNoArgsConstructorAndSetters() {
    Employeur employeur = new Employeur();
    employeur.setId(20L);
    employeur.setFirstName("Bob");
    employeur.setLastName("Marley");
    employeur.setCredentials(
        new ca.cal.leandrose.model.auth.Credentials(
            "bob.marley@example.com", "bobPass", Role.EMPLOYEUR));
    employeur.setCompanyName("MusicCorp");
    employeur.setField("Entertainment");

    assertEquals(20L, employeur.getId());
    assertEquals("Bob", employeur.getFirstName());
    assertEquals("Marley", employeur.getLastName());
    assertEquals("bob.marley@example.com", employeur.getEmail());
    assertEquals("bobPass", employeur.getPassword());
    assertEquals(Role.EMPLOYEUR, employeur.getRole());
    assertEquals("MusicCorp", employeur.getCompanyName());
    assertEquals("Entertainment", employeur.getField());
  }
}
