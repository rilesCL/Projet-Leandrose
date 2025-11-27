package ca.cal.leandrose.model;

import static org.junit.jupiter.api.Assertions.*;

import ca.cal.leandrose.model.auth.Credentials;
import ca.cal.leandrose.model.auth.Role;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class InternshipOfferTest {

  private Employeur employeur;
  private Gestionnaire gestionnaire;
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

    gestionnaire =
        Gestionnaire.builder()
            .id(1L)
            .firstName("Manager")
            .lastName("Test")
            .email("manager@college.com")
            .password("password")
            .phoneNumber("514-123-4567")
            .build();

    schoolTerm = new SchoolTerm(SchoolTerm.Season.FALL, 2025);
  }

  @Test
  void testInternshipOfferBuilder() {
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
            .build();

    assertEquals(1L, offer.getId());
    assertEquals("Stage développement Java", offer.getDescription());
    assertEquals(LocalDate.of(2025, 9, 1), offer.getStartDate());
    assertEquals(12, offer.getDurationInWeeks());
    assertEquals("123 Rue Test, Montréal", offer.getAddress());
    assertEquals(500.0f, offer.getRemuneration());
    assertEquals(employeur, offer.getEmployeur());
    assertEquals(InternshipOffer.Status.PUBLISHED, offer.getStatus());
    assertEquals("/uploads/offer.pdf", offer.getPdfPath());
    assertEquals(schoolTerm, offer.getSchoolTerm());
  }

  @Test
  void testInternshipOfferNoArgsConstructorAndSetters() {
    InternshipOffer offer = new InternshipOffer();
    offer.setId(2L);
    offer.setDescription("Stage test");
    offer.setStartDate(LocalDate.of(2025, 1, 1));
    offer.setDurationInWeeks(16);
    offer.setAddress("456 Test St");
    offer.setRemuneration(600.0f);
    offer.setEmployeur(employeur);
    offer.setStatus(InternshipOffer.Status.PENDING_VALIDATION);
    offer.setPdfPath("/uploads/offer2.pdf");
    offer.setSchoolTerm(schoolTerm);

    assertEquals(2L, offer.getId());
    assertEquals("Stage test", offer.getDescription());
    assertEquals(LocalDate.of(2025, 1, 1), offer.getStartDate());
    assertEquals(16, offer.getDurationInWeeks());
    assertEquals("456 Test St", offer.getAddress());
    assertEquals(600.0f, offer.getRemuneration());
    assertEquals(employeur, offer.getEmployeur());
    assertEquals(InternshipOffer.Status.PENDING_VALIDATION, offer.getStatus());
  }

  @Test
  void testGetEmployeurId_WithEmployeur() {
    InternshipOffer offer =
        InternshipOffer.builder()
            .id(1L)
            .description("Stage")
            .startDate(LocalDate.now())
            .durationInWeeks(12)
            .address("Test")
            .employeur(employeur)
            .status(InternshipOffer.Status.PUBLISHED)
            .build();

    assertEquals(1L, offer.getEmployeurId());
  }

  @Test
  void testGetEmployeurId_WithNullEmployeur() {
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

    assertNull(offer.getEmployeurId());
  }

  @Test
  void testGetCompanyName_WithEmployeur() {
    InternshipOffer offer =
        InternshipOffer.builder()
            .id(1L)
            .description("Stage")
            .startDate(LocalDate.now())
            .durationInWeeks(12)
            .address("Test")
            .employeur(employeur)
            .status(InternshipOffer.Status.PUBLISHED)
            .build();

    assertEquals("TechCorp", offer.getCompanyName());
  }

  @Test
  void testGetCompanyName_WithNullEmployeur() {
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

    assertNull(offer.getCompanyName());
  }

  @Test
  void testGetEmployeurFirstName_WithEmployeur() {
    InternshipOffer offer =
        InternshipOffer.builder()
            .id(1L)
            .description("Stage")
            .startDate(LocalDate.now())
            .durationInWeeks(12)
            .address("Test")
            .employeur(employeur)
            .status(InternshipOffer.Status.PUBLISHED)
            .build();

    assertEquals("Jane", offer.getEmployeurFirstName());
  }

  @Test
  void testGetEmployeurLastName_WithEmployeur() {
    InternshipOffer offer =
        InternshipOffer.builder()
            .id(1L)
            .description("Stage")
            .startDate(LocalDate.now())
            .durationInWeeks(12)
            .address("Test")
            .employeur(employeur)
            .status(InternshipOffer.Status.PUBLISHED)
            .build();

    assertEquals("Smith", offer.getEmployeurLastName());
  }

  @Test
  void testGetEmployeurEmail_WithEmployeur() {
    InternshipOffer offer =
        InternshipOffer.builder()
            .id(1L)
            .description("Stage")
            .startDate(LocalDate.now())
            .durationInWeeks(12)
            .address("Test")
            .employeur(employeur)
            .status(InternshipOffer.Status.PUBLISHED)
            .build();

    assertEquals("jane@company.com", offer.getEmployeurEmail());
  }

  @Test
  void testGetEmployeurEmail_WithNullEmployeur() {
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

    assertNull(offer.getEmployeurEmail());
  }

  @Test
  void testGetTerm_WithSchoolTerm() {
    InternshipOffer offer =
        InternshipOffer.builder()
            .id(1L)
            .description("Stage")
            .startDate(LocalDate.now())
            .durationInWeeks(12)
            .address("Test")
            .employeur(employeur)
            .status(InternshipOffer.Status.PUBLISHED)
            .schoolTerm(schoolTerm)
            .build();

    assertEquals("FALL 2025", offer.getTerm());
  }

  @Test
  void testGetTerm_WithNullSchoolTerm() {
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

    assertNull(offer.getTerm());
  }

  @Test
  void testInternshipOfferStatusEnum() {
    assertEquals(
        InternshipOffer.Status.PENDING_VALIDATION,
        InternshipOffer.Status.valueOf("PENDING_VALIDATION"));
    assertEquals(InternshipOffer.Status.PUBLISHED, InternshipOffer.Status.valueOf("PUBLISHED"));
    assertEquals(InternshipOffer.Status.ASSIGNED, InternshipOffer.Status.valueOf("ASSIGNED"));
    assertEquals(InternshipOffer.Status.ARCHIVED, InternshipOffer.Status.valueOf("ARCHIVED"));
    assertEquals(InternshipOffer.Status.REJECTED, InternshipOffer.Status.valueOf("REJECTED"));
    assertEquals(InternshipOffer.Status.DISABLED, InternshipOffer.Status.valueOf("DISABLED"));
  }
}



