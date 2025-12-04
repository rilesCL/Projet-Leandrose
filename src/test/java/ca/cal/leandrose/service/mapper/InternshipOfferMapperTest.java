package ca.cal.leandrose.service.mapper;

import static org.junit.jupiter.api.Assertions.*;

import ca.cal.leandrose.model.Employeur;
import ca.cal.leandrose.model.InternshipOffer;
import ca.cal.leandrose.model.SchoolTerm;
import ca.cal.leandrose.service.dto.InternshipOfferDto;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class InternshipOfferMapperTest {

  private Employeur employeur;
  private SchoolTerm schoolTerm;

  @BeforeEach
  void setUp() {
    employeur =
        Employeur.builder()
            .id(1L)
            .firstName("Jane")
            .lastName("Smith")
            .email("jane@company.com")
            .password("password")
            .companyName("TechCorp")
            .field("Software")
            .build();

    schoolTerm = new SchoolTerm(SchoolTerm.Season.FALL, 2025);
  }

  @Test
  void testToDto_WithCompleteOffer() {
    InternshipOffer offer =
        InternshipOffer.builder()
            .id(1L)
            .description("Stage développement Java")
            .startDate(LocalDate.of(2025, 9, 1))
            .durationInWeeks(12)
            .address("123 Rue Test, Montréal")
            .remuneration(500.0f)
            .employeur(employeur)
            .status(InternshipOffer.Status.PUBLISHED)
            .pdfPath("/uploads/offer.pdf")
            .schoolTerm(schoolTerm)
            .rejectionComment(null)
            .build();

    InternshipOfferDto dto = InternshipOfferMapper.toDto(offer);

    assertNotNull(dto);
    assertEquals(1L, dto.getId());
    assertEquals("Stage développement Java", dto.getDescription());
    assertEquals(LocalDate.of(2025, 9, 1), dto.getStartDate());
    assertEquals(12, dto.getDurationInWeeks());
    assertEquals("123 Rue Test, Montréal", dto.getAddress());
    assertEquals(500.0f, dto.getRemuneration());
    assertEquals("PUBLISHED", dto.getStatus());
    assertEquals("/uploads/offer.pdf", dto.getPdfPath());
    assertEquals("FALL 2025", dto.getSchoolTerm());
    assertEquals(1L, dto.getEmployeurId());
    assertEquals("TechCorp", dto.getCompanyName());
    assertNotNull(dto.getEmployeurDto());
  }

  @Test
  void testToDto_WithNullOffer() {
    InternshipOfferDto dto = InternshipOfferMapper.toDto(null);

    assertNull(dto);
  }

  @Test
  void testToDto_WithNullStatus() {
    InternshipOffer offer =
        InternshipOffer.builder()
            .id(1L)
            .description("Stage")
            .startDate(LocalDate.now())
            .durationInWeeks(12)
            .address("Test")
            .employeur(employeur)
            .status(null)
            .build();

    InternshipOfferDto dto = InternshipOfferMapper.toDto(offer);

    assertEquals("PENDING_VALIDATION", dto.getStatus());
  }

  @Test
  void testToDto_WithNullEmployeur() {
    InternshipOffer offer =
        InternshipOffer.builder()
            .id(1L)
            .description("Stage")
            .startDate(LocalDate.now())
            .durationInWeeks(12)
            .address("Test")
            .employeur(null)
            .status(InternshipOffer.Status.PUBLISHED)
            .build();

    InternshipOfferDto dto = InternshipOfferMapper.toDto(offer);

    assertNull(dto.getEmployeurId());
    assertNull(dto.getCompanyName());
    assertNull(dto.getEmployeurDto());
  }

  @Test
  void testToDto_WithNullSchoolTerm() {
    InternshipOffer offer =
        InternshipOffer.builder()
            .id(1L)
            .description("Stage")
            .startDate(LocalDate.now())
            .durationInWeeks(12)
            .address("Test")
            .employeur(employeur)
            .status(InternshipOffer.Status.PUBLISHED)
            .schoolTerm(null)
            .build();

    InternshipOfferDto dto = InternshipOfferMapper.toDto(offer);

    assertNull(dto.getSchoolTerm());
  }

  @Test
  void testToDto_WithRejectionComment() {
    InternshipOffer offer =
        InternshipOffer.builder()
            .id(1L)
            .description("Stage")
            .startDate(LocalDate.now())
            .durationInWeeks(12)
            .address("Test")
            .employeur(employeur)
            .status(InternshipOffer.Status.REJECTED)
            .rejectionComment("Does not meet requirements")
            .build();

    InternshipOfferDto dto = InternshipOfferMapper.toDto(offer);

    assertEquals("Does not meet requirements", dto.getRejectionComment());
  }

  @Test
  void testToDto_WithValidationDate() {
    LocalDate validationDate = LocalDate.of(2025, 8, 15);
    InternshipOffer offer =
        InternshipOffer.builder()
            .id(1L)
            .description("Stage")
            .startDate(LocalDate.now())
            .durationInWeeks(12)
            .address("Test")
            .employeur(employeur)
            .status(InternshipOffer.Status.PUBLISHED)
            .validationDate(validationDate)
            .build();

    InternshipOfferDto dto = InternshipOfferMapper.toDto(offer);

    assertEquals(validationDate, dto.getValidationDate());
  }
}
