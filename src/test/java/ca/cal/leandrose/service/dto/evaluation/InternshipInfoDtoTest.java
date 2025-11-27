package ca.cal.leandrose.service.dto.evaluation;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class InternshipInfoDtoTest {

  @Test
  void testInternshipInfoDtoRecord() {
    InternshipInfoDto dto = new InternshipInfoDto(1L, "Stage développement", "TechCorp");

    assertNotNull(dto);
    assertEquals(1L, dto.id());
    assertEquals("Stage développement", dto.description());
    assertEquals("TechCorp", dto.companyName());
  }

  @Test
  void testInternshipInfoDtoWithNullValues() {
    InternshipInfoDto dto = new InternshipInfoDto(null, null, null);

    assertNull(dto.id());
    assertNull(dto.description());
    assertNull(dto.companyName());
  }

  @Test
  void testInternshipInfoDtoEquals() {
    InternshipInfoDto dto1 = new InternshipInfoDto(1L, "Stage développement", "TechCorp");
    InternshipInfoDto dto2 = new InternshipInfoDto(1L, "Stage développement", "TechCorp");
    InternshipInfoDto dto3 = new InternshipInfoDto(2L, "Stage développement", "TechCorp");

    assertEquals(dto1, dto2);
    assertEquals(dto1.hashCode(), dto2.hashCode());
    assertNotEquals(dto1, dto3);
  }

  @Test
  void testInternshipInfoDtoToString() {
    InternshipInfoDto dto = new InternshipInfoDto(1L, "Stage développement", "TechCorp");

    assertNotNull(dto.toString());
    assertTrue(dto.toString().contains("InternshipInfoDto"));
  }

  @Test
  void testInternshipInfoDtoWithEmptyStrings() {
    InternshipInfoDto dto = new InternshipInfoDto(1L, "", "");

    assertEquals(1L, dto.id());
    assertEquals("", dto.description());
    assertEquals("", dto.companyName());
  }
}




