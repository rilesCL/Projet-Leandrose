package ca.cal.leandrose.service.dto;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class InternshipOfferDtoTest {

  @Test
  void testInternshipOfferDtoBuilder() {
    InternshipOfferDto dto =
        InternshipOfferDto.builder()
            .id(1L)
            .description("Stage développement")
            .startDate(LocalDate.of(2025, 9, 1))
            .durationInWeeks(12)
            .address("123 Rue Test")
            .remuneration(500.0f)
            .status("PUBLISHED")
            .employeurId(1L)
            .companyName("TechCorp")
            .pdfPath("/uploads/offer.pdf")
            .schoolTerm("FALL 2025")
            .validationDate(LocalDate.of(2025, 8, 15))
            .rejectionComment(null)
            .build();

    assertEquals(1L, dto.getId());
    assertEquals("Stage développement", dto.getDescription());
    assertEquals(LocalDate.of(2025, 9, 1), dto.getStartDate());
    assertEquals(12, dto.getDurationInWeeks());
    assertEquals("123 Rue Test", dto.getAddress());
    assertEquals(500.0f, dto.getRemuneration());
    assertEquals("PUBLISHED", dto.getStatus());
    assertEquals(1L, dto.getEmployeurId());
    assertEquals("TechCorp", dto.getCompanyName());
    assertEquals("/uploads/offer.pdf", dto.getPdfPath());
    assertEquals("FALL 2025", dto.getSchoolTerm());
    assertEquals(LocalDate.of(2025, 8, 15), dto.getValidationDate());
  }

  @Test
  void testInternshipOfferDtoNoArgsConstructorAndSetters() {
    InternshipOfferDto dto = new InternshipOfferDto();
    dto.setId(2L);
    dto.setDescription("Stage test");
    dto.setStartDate(LocalDate.of(2025, 1, 1));
    dto.setDurationInWeeks(16);
    dto.setAddress("456 Test St");
    dto.setRemuneration(600.0f);
    dto.setStatus("PENDING_VALIDATION");
    dto.setEmployeurId(2L);
    dto.setCompanyName("TestCorp");
    dto.setPdfPath("/uploads/offer2.pdf");
    dto.setSchoolTerm("WINTER 2026");
    dto.setRejectionComment("Test rejection");

    assertEquals(2L, dto.getId());
    assertEquals("Stage test", dto.getDescription());
    assertEquals(LocalDate.of(2025, 1, 1), dto.getStartDate());
    assertEquals(16, dto.getDurationInWeeks());
    assertEquals("456 Test St", dto.getAddress());
    assertEquals(600.0f, dto.getRemuneration());
    assertEquals("PENDING_VALIDATION", dto.getStatus());
    assertEquals(2L, dto.getEmployeurId());
    assertEquals("TestCorp", dto.getCompanyName());
    assertEquals("/uploads/offer2.pdf", dto.getPdfPath());
    assertEquals("WINTER 2026", dto.getSchoolTerm());
    assertEquals("Test rejection", dto.getRejectionComment());
  }

  @Test
  void testInternshipOfferDtoConstructor() {
    EmployeurDto employeurDto =
        EmployeurDto.builder()
            .id(1L)
            .firstName("Jane")
            .lastname("Smith")
            .email("jane@company.com")
            .companyName("TechCorp")
            .field("Software")
            .build();

    InternshipOfferDto dto =
        new InternshipOfferDto(
            1L,
            "Stage développement",
            LocalDate.of(2025, 9, 1),
            12,
            "123 Rue Test",
            500.0f,
            "PUBLISHED",
            employeurDto,
            "/uploads/offer.pdf",
            "FALL 2025");

    assertEquals(1L, dto.getId());
    assertEquals("Stage développement", dto.getDescription());
    assertEquals(LocalDate.of(2025, 9, 1), dto.getStartDate());
    assertEquals(12, dto.getDurationInWeeks());
    assertEquals("123 Rue Test", dto.getAddress());
    assertEquals(500.0f, dto.getRemuneration());
    assertEquals("PUBLISHED", dto.getStatus());
    assertEquals(employeurDto, dto.getEmployeurDto());
    assertEquals("/uploads/offer.pdf", dto.getPdfPath());
    assertEquals("FALL 2025", dto.getSchoolTerm());
    assertNull(dto.getRejectionComment());
  }

  @Test
  void testInternshipOfferDtoWithErrorMessage() {
    InternshipOfferDto dto = new InternshipOfferDto("Test error message");

    assertEquals("Test error message", dto.getErrorMessage());
  }

  @Test
  void testInternshipOfferDtoAllArgsConstructor() {
    InternshipOfferDto dto =
        new InternshipOfferDto(
            1L,
            "Description",
            LocalDate.of(2025, 9, 1),
            12,
            "Address",
            500.0f,
            "PUBLISHED",
            null,
            "/path.pdf",
            "FALL 2025");

    assertEquals(1L, dto.getId());
    assertEquals("Description", dto.getDescription());
    assertEquals(LocalDate.of(2025, 9, 1), dto.getStartDate());
    assertEquals(12, dto.getDurationInWeeks());
    assertEquals("Address", dto.getAddress());
    assertEquals(500.0f, dto.getRemuneration());
    assertEquals("PUBLISHED", dto.getStatus());
    assertEquals("/path.pdf", dto.getPdfPath());
    assertEquals("FALL 2025", dto.getSchoolTerm());
    assertNull(dto.getEmployeurDto());
    assertNull(dto.getRejectionComment());
  }

  @Test
  void testInternshipOfferDtoWithNullValues() {
    InternshipOfferDto dto = new InternshipOfferDto();
    dto.setId(null);
    dto.setDescription(null);
    dto.setStartDate(null);
    dto.setAddress(null);
    dto.setRemuneration(null);
    dto.setStatus(null);
    dto.setEmployeurId(null);
    dto.setCompanyName(null);
    dto.setPdfPath(null);
    dto.setSchoolTerm(null);
    dto.setValidationDate(null);
    dto.setEmployeurDto(null);
    dto.setRejectionComment(null);
    dto.setErrorMessage(null);

    assertNull(dto.getId());
    assertNull(dto.getDescription());
    assertNull(dto.getStartDate());
    assertNull(dto.getAddress());
    assertNull(dto.getRemuneration());
    assertNull(dto.getStatus());
    assertNull(dto.getEmployeurId());
    assertNull(dto.getCompanyName());
    assertNull(dto.getPdfPath());
    assertNull(dto.getSchoolTerm());
    assertNull(dto.getValidationDate());
    assertNull(dto.getEmployeurDto());
    assertNull(dto.getRejectionComment());
    assertNull(dto.getErrorMessage());
  }
}
