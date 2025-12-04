package ca.cal.leandrose.service.dto;

import static org.junit.jupiter.api.Assertions.*;

import ca.cal.leandrose.model.Employeur;
import ca.cal.leandrose.model.auth.Role;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class EmployeurDtoServiceTest {

  @Test
  void testEmployeurDtoBuilder() {
    EmployeurDto dto =
        EmployeurDto.builder()
            .id(1L)
            .firstName("Jane")
            .lastname("Smith")
            .email("jane@company.com")
            .role(Role.EMPLOYEUR)
            .companyName("TechCorp")
            .field("Software")
            .phoneNumber("514-123-4567")
            .build();

    assertEquals(1L, dto.getId());
    assertEquals("Jane", dto.getFirstName());
    assertEquals("Smith", dto.getLastName());
    assertEquals("jane@company.com", dto.getEmail());
    assertEquals(Role.EMPLOYEUR, dto.getRole());
    assertEquals("TechCorp", dto.getCompanyName());
    assertEquals("Software", dto.getField());
    assertEquals("514-123-4567", dto.getPhoneNumber());
  }

  @Test
  void testEmployeurDtoNoArgsConstructor() {
    EmployeurDto dto = new EmployeurDto();

    assertNotNull(dto);
    assertNull(dto.getId());
    assertNull(dto.getFirstName());
    assertNull(dto.getCompanyName());
    assertNull(dto.getField());
    assertNull(dto.getPhoneNumber());
  }

  @Test
  void testCreate_WithCompleteEmployeur() {
    Employeur employeur =
        Employeur.builder()
            .id(1L)
            .firstName("Jane")
            .lastName("Smith")
            .email("jane@company.com")
            .password("password")
            .companyName("TechCorp")
            .field("Software")
            .phoneNumber("514-123-4567")
            .build();

    EmployeurDto dto = EmployeurDto.create(employeur);

    assertNotNull(dto);
    assertEquals(1L, dto.getId());
    assertEquals("Jane", dto.getFirstName());
    assertEquals("Smith", dto.getLastName());
    assertEquals("jane@company.com", dto.getEmail());
    assertEquals(Role.EMPLOYEUR, dto.getRole());
    assertEquals("TechCorp", dto.getCompanyName());
    assertEquals("Software", dto.getField());
    assertEquals("514-123-4567", dto.getPhoneNumber());
  }

  @Test
  void testCreate_WithNullFields() {
    Employeur employeur =
        Employeur.builder()
            .id(1L)
            .firstName("Test")
            .lastName("User")
            .email("test@example.com")
            .password("password")
            .companyName(null)
            .field(null)
            .phoneNumber(null)
            .build();

    EmployeurDto dto = EmployeurDto.create(employeur);

    assertNotNull(dto);
    assertNull(dto.getCompanyName());
    assertNull(dto.getField());
    assertNull(dto.getPhoneNumber());
  }

  @Test
  void testEmpty() {
    EmployeurDto dto = EmployeurDto.empty();

    assertNotNull(dto);
    assertNull(dto.getId());
    assertNull(dto.getFirstName());
    assertNull(dto.getCompanyName());
    assertNull(dto.getField());
    assertNull(dto.getPhoneNumber());
  }

  @Test
  void testEmployeurDtoWithError() {
    EmployeurDto dto = new EmployeurDto();
    Map<String, String> errorMap = new HashMap<>();
    errorMap.put("message", "Test error");
    dto.setError(errorMap);

    assertNotNull(dto.getError());
    assertEquals("Test error", dto.getError().get("message"));
  }

  @Test
  void testEmployeurDtoSetters() {
    EmployeurDto dto = new EmployeurDto();
    dto.setId(2L);
    dto.setFirstName("New");
    dto.setLastName("Name");
    dto.setEmail("new@example.com");
    dto.setRole(Role.EMPLOYEUR);
    dto.setCompanyName("NewCorp");
    dto.setField("IT");
    dto.setPhoneNumber("514-999-9999");

    assertEquals(2L, dto.getId());
    assertEquals("New", dto.getFirstName());
    assertEquals("Name", dto.getLastName());
    assertEquals("new@example.com", dto.getEmail());
    assertEquals(Role.EMPLOYEUR, dto.getRole());
    assertEquals("NewCorp", dto.getCompanyName());
    assertEquals("IT", dto.getField());
    assertEquals("514-999-9999", dto.getPhoneNumber());
  }
}
