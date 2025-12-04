package ca.cal.leandrose.service.dto;

import static org.junit.jupiter.api.Assertions.*;

import ca.cal.leandrose.model.Gestionnaire;
import ca.cal.leandrose.model.auth.Role;
import org.junit.jupiter.api.Test;

class GestionnaireDtoTest {

  @Test
  void testGestionnaireDtoBuilder() {
    GestionnaireDto dto =
        GestionnaireDto.builder()
            .id(1L)
            .firstName("Manager")
            .lastname("Test")
            .email("manager@college.com")
            .role(Role.GESTIONNAIRE)
            .phoneNumber("514-123-4567")
            .build();

    assertEquals(1L, dto.getId());
    assertEquals("Manager", dto.getFirstName());
    assertEquals("Test", dto.getLastName());
    assertEquals("manager@college.com", dto.getEmail());
    assertEquals(Role.GESTIONNAIRE, dto.getRole());
    assertEquals("514-123-4567", dto.getPhoneNumber());
  }

  @Test
  void testGestionnaireDtoNoArgsConstructor() {
    GestionnaireDto dto = new GestionnaireDto();

    assertNotNull(dto);
    assertNull(dto.getId());
    assertNull(dto.getFirstName());
    assertNull(dto.getPhoneNumber());
  }

  @Test
  void testCreate_WithCompleteGestionnaire() {
    Gestionnaire gestionnaire =
        Gestionnaire.builder()
            .id(1L)
            .firstName("Manager")
            .lastName("Test")
            .email("manager@college.com")
            .password("password")
            .phoneNumber("514-123-4567")
            .build();

    GestionnaireDto dto = GestionnaireDto.create(gestionnaire);

    assertNotNull(dto);
    assertEquals(1L, dto.getId());
    assertEquals("Manager", dto.getFirstName());
    assertEquals("Test", dto.getLastName());
    assertEquals("manager@college.com", dto.getEmail());
    assertEquals(Role.GESTIONNAIRE, dto.getRole());
    assertEquals("514-123-4567", dto.getPhoneNumber());
  }

  @Test
  void testEmpty() {
    GestionnaireDto dto = GestionnaireDto.empty();

    assertNotNull(dto);
    assertNull(dto.getId());
    assertNull(dto.getFirstName());
    assertNull(dto.getPhoneNumber());
  }

  @Test
  void testCreate_WithNullPhoneNumber() {
    Gestionnaire gestionnaire =
        Gestionnaire.builder()
            .id(1L)
            .firstName("Manager")
            .lastName("Test")
            .email("manager@college.com")
            .password("password")
            .phoneNumber(null)
            .build();

    GestionnaireDto dto = GestionnaireDto.create(gestionnaire);

    assertNotNull(dto);
    assertNull(dto.getPhoneNumber());
  }
}
