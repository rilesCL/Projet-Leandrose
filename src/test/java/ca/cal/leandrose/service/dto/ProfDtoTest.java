package ca.cal.leandrose.service.dto;

import static org.junit.jupiter.api.Assertions.*;

import ca.cal.leandrose.model.Prof;
import ca.cal.leandrose.model.auth.Role;
import org.junit.jupiter.api.Test;

class ProfDtoTest {

  @Test
  void testProfDtoBuilder() {
    ProfDto dto =
        ProfDto.builder()
            .id(1L)
            .firstName("Prof")
            .lastName("Test")
            .email("prof@college.com")
            .role(Role.PROF)
            .employeeNumber("EMP001")
            .nameCollege("College Test")
            .address("123 Test St")
            .fax_machine("514-123-4567")
            .department("Informatique")
            .phoneNumber("514-987-6543")
            .build();

    assertEquals(1L, dto.getId());
    assertEquals("Prof", dto.getFirstName());
    assertEquals("Test", dto.getLastName());
    assertEquals("prof@college.com", dto.getEmail());
    assertEquals(Role.PROF, dto.getRole());
    assertEquals("EMP001", dto.getEmployeeNumber());
    assertEquals("College Test", dto.getNameCollege());
    assertEquals("123 Test St", dto.getAddress());
    assertEquals("514-123-4567", dto.getFax_machine());
    assertEquals("Informatique", dto.getDepartment());
    assertEquals("514-987-6543", dto.getPhoneNumber());
  }

  @Test
  void testProfDtoNoArgsConstructor() {
    ProfDto dto = new ProfDto();

    assertNotNull(dto);
    assertNull(dto.getId());
    assertNull(dto.getFirstName());
    assertNull(dto.getEmployeeNumber());
  }

  @Test
  void testProfDtoWithError() {
    ProfDto dto = new ProfDto("Test error message");

    assertNotNull(dto);
    assertNotNull(dto.getError());
    assertEquals("Test error message", dto.getError().get("error"));
  }

  @Test
  void testCreate_WithCompleteProf() {
    Prof prof =
        Prof.builder()
            .id(1L)
            .firstName("Prof")
            .lastName("Test")
            .email("prof@college.com")
            .password("password")
            .employeeNumber("EMP001")
            .nameCollege("College Test")
            .address("123 Test St")
            .fax_machine("514-123-4567")
            .department("Informatique")
            .phoneNumber("514-987-6543")
            .build();

    ProfDto dto = ProfDto.create(prof);

    assertNotNull(dto);
    assertEquals(1L, dto.getId());
    assertEquals("Prof", dto.getFirstName());
    assertEquals("Test", dto.getLastName());
    assertEquals("prof@college.com", dto.getEmail());
    assertEquals(Role.PROF, dto.getRole());
    assertEquals("EMP001", dto.getEmployeeNumber());
    assertEquals("College Test", dto.getNameCollege());
    assertEquals("123 Test St", dto.getAddress());
    assertEquals("514-123-4567", dto.getFax_machine());
    assertEquals("Informatique", dto.getDepartment());
    assertEquals("514-987-6543", dto.getPhoneNumber());
  }

  @Test
  void testCreate_WithNullFields() {
    Prof prof =
        Prof.builder()
            .id(1L)
            .firstName("Prof")
            .lastName("Test")
            .email("prof@college.com")
            .password("password")
            .employeeNumber(null)
            .nameCollege(null)
            .address(null)
            .fax_machine(null)
            .department(null)
            .phoneNumber(null)
            .build();

    ProfDto dto = ProfDto.create(prof);

    assertNotNull(dto);
    assertNull(dto.getEmployeeNumber());
    assertNull(dto.getNameCollege());
    assertNull(dto.getAddress());
    assertNull(dto.getFax_machine());
    assertNull(dto.getDepartment());
    assertNull(dto.getPhoneNumber());
  }

  @Test
  void testEmpty() {
    ProfDto dto = ProfDto.empty();

    assertNotNull(dto);
    assertNull(dto.getId());
    assertNull(dto.getFirstName());
    assertNull(dto.getEmployeeNumber());
  }

  @Test
  void testProfDtoSetters() {
    ProfDto dto = new ProfDto();
    dto.setEmployeeNumber("EMP002");
    dto.setNameCollege("New College");
    dto.setAddress("New Address");
    dto.setFax_machine("514-111-1111");
    dto.setDepartment("New Department");
    dto.setPhoneNumber("514-222-2222");
    dto.setError(java.util.Map.of("error", "Test error"));

    assertEquals("EMP002", dto.getEmployeeNumber());
    assertEquals("New College", dto.getNameCollege());
    assertEquals("New Address", dto.getAddress());
    assertEquals("514-111-1111", dto.getFax_machine());
    assertEquals("New Department", dto.getDepartment());
    assertEquals("514-222-2222", dto.getPhoneNumber());
    assertNotNull(dto.getError());
    assertEquals("Test error", dto.getError().get("error"));
  }
}
