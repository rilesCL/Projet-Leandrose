package ca.cal.leandrose.service.dto.evaluation;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class StudentInfoDtoTest {

  @Test
  void testStudentInfoDtoRecord() {
    StudentInfoDto dto = new StudentInfoDto(1L, "John", "Doe", "Computer Science");

    assertNotNull(dto);
    assertEquals(1L, dto.id());
    assertEquals("John", dto.firstName());
    assertEquals("Doe", dto.lastName());
    assertEquals("Computer Science", dto.program());
  }

  @Test
  void testStudentInfoDtoWithNullValues() {
    StudentInfoDto dto = new StudentInfoDto(null, null, null, null);

    assertNull(dto.id());
    assertNull(dto.firstName());
    assertNull(dto.lastName());
    assertNull(dto.program());
  }

  @Test
  void testStudentInfoDtoEquals() {
    StudentInfoDto dto1 = new StudentInfoDto(1L, "John", "Doe", "Computer Science");
    StudentInfoDto dto2 = new StudentInfoDto(1L, "John", "Doe", "Computer Science");
    StudentInfoDto dto3 = new StudentInfoDto(2L, "John", "Doe", "Computer Science");

    assertEquals(dto1, dto2);
    assertEquals(dto1.hashCode(), dto2.hashCode());
    assertNotEquals(dto1, dto3);
  }

  @Test
  void testStudentInfoDtoToString() {
    StudentInfoDto dto = new StudentInfoDto(1L, "John", "Doe", "Computer Science");

    assertNotNull(dto.toString());
    assertTrue(dto.toString().contains("StudentInfoDto"));
  }

  @Test
  void testStudentInfoDtoWithEmptyStrings() {
    StudentInfoDto dto = new StudentInfoDto(1L, "", "", "");

    assertEquals(1L, dto.id());
    assertEquals("", dto.firstName());
    assertEquals("", dto.lastName());
    assertEquals("", dto.program());
  }
}




