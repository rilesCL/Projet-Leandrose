package ca.cal.leandrose.service.dto;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class ProfStudentItemDtoTest {

  @Test
  void testProfStudentItemDtoBuilder() {
    ProfStudentItemDto dto =
        ProfStudentItemDto.builder()
            .ententeId(1L)
            .studentId(10L)
            .studentFirstName("John")
            .studentLastName("Doe")
            .companyName("TechCorp")
            .offerTitle("Stage développement")
            .startDate(LocalDate.of(2025, 9, 1))
            .endDate(LocalDate.of(2025, 12, 1))
            .stageStatus("EN_COURS")
            .evaluationStatus("A_FAIRE")
            .build();

    assertEquals(1L, dto.getEntenteId());
    assertEquals(10L, dto.getStudentId());
    assertEquals("John", dto.getStudentFirstName());
    assertEquals("Doe", dto.getStudentLastName());
    assertEquals("TechCorp", dto.getCompanyName());
    assertEquals("Stage développement", dto.getOfferTitle());
    assertEquals(LocalDate.of(2025, 9, 1), dto.getStartDate());
    assertEquals(LocalDate.of(2025, 12, 1), dto.getEndDate());
    assertEquals("EN_COURS", dto.getStageStatus());
    assertEquals("A_FAIRE", dto.getEvaluationStatus());
  }

  @Test
  void testProfStudentItemDtoNoArgsConstructorAndSetters() {
    ProfStudentItemDto dto = new ProfStudentItemDto();
    dto.setEntenteId(2L);
    dto.setStudentId(20L);
    dto.setStudentFirstName("Jane");
    dto.setStudentLastName("Smith");
    dto.setCompanyName("DevCorp");
    dto.setOfferTitle("Stage Java");
    dto.setStartDate(LocalDate.of(2025, 1, 1));
    dto.setEndDate(LocalDate.of(2025, 4, 1));
    dto.setStageStatus("TERMINE");
    dto.setEvaluationStatus("COMPLETE");

    assertEquals(2L, dto.getEntenteId());
    assertEquals(20L, dto.getStudentId());
    assertEquals("Jane", dto.getStudentFirstName());
    assertEquals("Smith", dto.getStudentLastName());
    assertEquals("DevCorp", dto.getCompanyName());
    assertEquals("Stage Java", dto.getOfferTitle());
    assertEquals(LocalDate.of(2025, 1, 1), dto.getStartDate());
    assertEquals(LocalDate.of(2025, 4, 1), dto.getEndDate());
    assertEquals("TERMINE", dto.getStageStatus());
    assertEquals("COMPLETE", dto.getEvaluationStatus());
  }

  @Test
  void testProfStudentItemDtoAllArgsConstructor() {
    ProfStudentItemDto dto =
        new ProfStudentItemDto(
            1L,
            10L,
            "John",
            "Doe",
            "TechCorp",
            "Stage développement",
            LocalDate.of(2025, 9, 1),
            LocalDate.of(2025, 12, 1),
            "EN_COURS",
            "A_FAIRE");

    assertEquals(1L, dto.getEntenteId());
    assertEquals(10L, dto.getStudentId());
    assertEquals("John", dto.getStudentFirstName());
    assertEquals("Doe", dto.getStudentLastName());
    assertEquals("TechCorp", dto.getCompanyName());
    assertEquals("Stage développement", dto.getOfferTitle());
    assertEquals(LocalDate.of(2025, 9, 1), dto.getStartDate());
    assertEquals(LocalDate.of(2025, 12, 1), dto.getEndDate());
    assertEquals("EN_COURS", dto.getStageStatus());
    assertEquals("A_FAIRE", dto.getEvaluationStatus());
  }

  @Test
  void testProfStudentItemDtoWithNullValues() {
    ProfStudentItemDto dto = new ProfStudentItemDto();
    dto.setEntenteId(null);
    dto.setStudentId(null);
    dto.setStudentFirstName(null);
    dto.setStudentLastName(null);
    dto.setCompanyName(null);
    dto.setOfferTitle(null);
    dto.setStartDate(null);
    dto.setEndDate(null);
    dto.setStageStatus(null);
    dto.setEvaluationStatus(null);

    assertNull(dto.getEntenteId());
    assertNull(dto.getStudentId());
    assertNull(dto.getStudentFirstName());
    assertNull(dto.getStudentLastName());
    assertNull(dto.getCompanyName());
    assertNull(dto.getOfferTitle());
    assertNull(dto.getStartDate());
    assertNull(dto.getEndDate());
    assertNull(dto.getStageStatus());
    assertNull(dto.getEvaluationStatus());
  }
}
