package ca.cal.leandrose.model;

import ca.cal.leandrose.model.auth.Role;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ProfTest {

  @Test
  void testProfBuilder() {
    Prof prof =
        Prof.builder()
            .id(1L)
            .firstName("Jean")
            .lastName("Dupont")
            .email("jean.dupont@college.ca")
            .password("password123")
            .employeeNumber("P12345")
            .department("Informatique")
            .build();

    assertEquals(1L, prof.getId());
    assertEquals("Jean", prof.getFirstName());
    assertEquals("Dupont", prof.getLastName());
    assertEquals("jean.dupont@college.ca", prof.getEmail());
    assertEquals("password123", prof.getPassword());
    assertEquals(Role.PROF, prof.getRole());
    assertEquals("P12345", prof.getEmployeeNumber());
    assertEquals("Informatique", prof.getDepartment());
  }

  @Test
  void testProfNoArgsConstructorAndSetters() {
    Prof prof = new Prof();
    prof.setId(2L);
    prof.setFirstName("Marie");
    prof.setLastName("Martin");
    prof.setEmployeeNumber("P54321");
    prof.setDepartment("Mathématiques");

    assertEquals(2L, prof.getId());
    assertEquals("Marie", prof.getFirstName());
    assertEquals("Martin", prof.getLastName());
    assertEquals("P54321", prof.getEmployeeNumber());
    assertEquals("Mathématiques", prof.getDepartment());
  }
}

