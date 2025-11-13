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
            .nameCollege("Collège de Mainsonneuve")
            .address("3800 R. Sherbrooke E, Montréal, QC H1X 2A2")
            .fax_machine("(514) 254-7131")
            .department("Informatique")
            .build();

    assertEquals(1L, prof.getId());
    assertEquals("Jean", prof.getFirstName());
    assertEquals("Dupont", prof.getLastName());
    assertEquals("jean.dupont@college.ca", prof.getEmail());
    assertEquals("password123", prof.getPassword());
    assertEquals(Role.PROF, prof.getRole());
    assertEquals("P12345", prof.getEmployeeNumber());
    assertEquals("Collège de Mainsonneuve", prof.getNameCollege());
    assertEquals("3800 R. Sherbrooke E, Montréal, QC H1X 2A2", prof.getAddress());
    assertEquals("(514) 254-7131", prof.getFax_machine());
    assertEquals("Informatique", prof.getDepartment());
  }

  @Test
  void testProfNoArgsConstructorAndSetters() {
    Prof prof = new Prof();
    prof.setId(2L);
    prof.setFirstName("Marie");
    prof.setLastName("Martin");
    prof.setEmployeeNumber("P54321");
    prof.setNameCollege("Collège Ahuntsic");
    prof.setAddress("9155 Rue St-Hubert, Montréal, QC H2M 1Y8");
    prof.setFax_machine("(514) 389-5921");
    prof.setDepartment("Mathématiques");

    assertEquals(2L, prof.getId());
    assertEquals("Marie", prof.getFirstName());
    assertEquals("Martin", prof.getLastName());
    assertEquals("P54321", prof.getEmployeeNumber());
    assertEquals("Collège Ahuntsic", prof.getNameCollege());
    assertEquals("9155 Rue St-Hubert, Montréal, QC H2M 1Y8", prof.getAddress());
    assertEquals("(514) 389-5921", prof.getFax_machine());
    assertEquals("Mathématiques", prof.getDepartment());
  }
}

