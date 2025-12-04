package ca.cal.leandrose.service.dto;

import static org.junit.jupiter.api.Assertions.*;

import ca.cal.leandrose.model.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class EntenteStageDtoTest {

  private EntenteStage entente;
  private Candidature candidature;
  private Student student;
  private InternshipOffer offer;
  private Employeur employeur;
  private Prof prof;
  private Gestionnaire gestionnaire;
  private SchoolTerm schoolTerm;

  @BeforeEach
  void setUp() {
    student =
        Student.builder()
            .id(1L)
            .firstName("John")
            .lastName("Doe")
            .email("john.doe@student.com")
            .password("password")
            .studentNumber("STU001")
            .program("Computer Science")
            .build();

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

    prof =
        Prof.builder()
            .id(1L)
            .firstName("Prof")
            .lastName("Test")
            .email("prof@college.com")
            .password("password")
            .employeeNumber("EMP001")
            .nameCollege("College Test")
            .department("Informatique")
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

    offer =
        InternshipOffer.builder()
            .id(1L)
            .description("Stage développement")
            .startDate(LocalDate.of(2025, 9, 1))
            .durationInWeeks(12)
            .address("123 Rue Test")
            .remuneration(500.0f)
            .employeur(employeur)
            .status(InternshipOffer.Status.PUBLISHED)
            .schoolTerm(schoolTerm)
            .build();

    Cv cv =
        Cv.builder().id(1L).student(student).pdfPath("/cv.pdf").status(Cv.Status.APPROVED).build();

    candidature =
        Candidature.builder()
            .id(1L)
            .student(student)
            .internshipOffer(offer)
            .cv(cv)
            .status(Candidature.Status.ACCEPTED)
            .applicationDate(LocalDateTime.now())
            .build();

    entente =
        EntenteStage.builder()
            .id(1L)
            .candidature(candidature)
            .prof(prof)
            .gestionnaire(gestionnaire)
            .missionsObjectifs("Développement web")
            .statut(EntenteStage.StatutEntente.VALIDEE)
            .dateCreation(LocalDateTime.now())
            .dateModification(LocalDateTime.now())
            .cheminDocumentPDF("/uploads/entente.pdf")
            .dateSignatureEtudiant(LocalDateTime.now().minusDays(1))
            .dateSignatureEmployeur(LocalDateTime.now().minusDays(1))
            .dateSignatureGestionnaire(LocalDateTime.now())
            .build();
  }

  @Test
  void testEntenteStageDtoBuilder() {
    EntenteStageDto dto =
        EntenteStageDto.builder()
            .id(1L)
            .candidatureId(1L)
            .missionsObjectifs("Test missions")
            .statut(EntenteStage.StatutEntente.BROUILLON)
            .dateDebut(LocalDate.now())
            .duree(12)
            .lieu("Test location")
            .remuneration(500.0f)
            .schoolTerm("FALL 2025")
            .dateCreation(LocalDateTime.now())
            .dateModification(LocalDateTime.now())
            .cheminDocumentPDF("/uploads/test.pdf")
            .build();

    assertEquals(1L, dto.getId());
    assertEquals(1L, dto.getCandidatureId());
    assertEquals("Test missions", dto.getMissionsObjectifs());
    assertEquals(EntenteStage.StatutEntente.BROUILLON, dto.getStatut());
    assertEquals(12, dto.getDuree());
    assertEquals("Test location", dto.getLieu());
    assertEquals(500.0f, dto.getRemuneration());
    assertEquals("FALL 2025", dto.getSchoolTerm());
  }

  @Test
  void testEntenteStageDtoNoArgsConstructorAndSetters() {
    EntenteStageDto dto = new EntenteStageDto();
    dto.setId(2L);
    dto.setCandidatureId(2L);
    dto.setMissionsObjectifs("New missions");
    dto.setStatut(EntenteStage.StatutEntente.EN_ATTENTE_SIGNATURE);
    dto.setDateDebut(LocalDate.now());
    dto.setDuree(16);
    dto.setLieu("New location");
    dto.setRemuneration(600.0f);
    dto.setSchoolTerm("WINTER 2026");
    dto.setEmployeurASigner(true);

    assertEquals(2L, dto.getId());
    assertEquals(2L, dto.getCandidatureId());
    assertEquals("New missions", dto.getMissionsObjectifs());
    assertEquals(EntenteStage.StatutEntente.EN_ATTENTE_SIGNATURE, dto.getStatut());
    assertEquals(16, dto.getDuree());
    assertEquals("New location", dto.getLieu());
    assertEquals(600.0f, dto.getRemuneration());
    assertEquals("WINTER 2026", dto.getSchoolTerm());
    assertTrue(dto.isEmployeurASigner());
  }

  @Test
  void testFromEntity_WithCompleteEntente() {
    EntenteStageDto dto = EntenteStageDto.fromEntity(entente);

    assertNotNull(dto);
    assertEquals(1L, dto.getId());
    assertEquals(1L, dto.getCandidatureId());
    assertEquals("Développement web", dto.getMissionsObjectifs());
    assertEquals(EntenteStage.StatutEntente.VALIDEE, dto.getStatut());
    assertEquals(LocalDate.of(2025, 9, 1), dto.getDateDebut());
    assertEquals(12, dto.getDuree());
    assertEquals("123 Rue Test", dto.getLieu());
    assertEquals(500.0f, dto.getRemuneration());
    assertEquals("FALL 2025", dto.getSchoolTerm());
    assertNotNull(dto.getStudent());
    assertNotNull(dto.getInternshipOffer());
    assertNotNull(dto.getProf());
    assertNotNull(dto.getGestionnaire());
    assertTrue(dto.isEmployeurASigner());
  }

  @Test
  void testFromEntity_WithNullEntente() {
    EntenteStageDto dto = EntenteStageDto.fromEntity(null);

    assertNull(dto);
  }

  @Test
  void testFromEntity_WithNullProf() {
    entente.setProf(null);
    EntenteStageDto dto = EntenteStageDto.fromEntity(entente);

    assertNull(dto.getProf());
  }

  @Test
  void testFromEntity_WithNullGestionnaire() {
    entente.setGestionnaire(null);
    EntenteStageDto dto = EntenteStageDto.fromEntity(entente);

    assertNull(dto.getGestionnaire());
  }

  @Test
  void testFromEntity_WithNullSchoolTerm() {
    offer.setSchoolTerm(null);
    EntenteStageDto dto = EntenteStageDto.fromEntity(entente);

    assertNull(dto.getSchoolTerm());
  }

  @Test
  void testFromEntity_WithNullEmployeur() {
    offer.setEmployeur(null);
    EntenteStageDto dto = EntenteStageDto.fromEntity(entente);

    assertNull(dto.getInternshipOffer().getEmployeurId());
    assertNull(dto.getInternshipOffer().getCompanyName());
    assertNull(dto.getInternshipOffer().getEmployeurDto());
  }

  @Test
  void testWithError() {
    Map<String, String> errorMap = new HashMap<>();
    errorMap.put("message", "Test error message");

    EntenteStageDto dto = EntenteStageDto.withError(errorMap);

    assertNotNull(dto);
    assertEquals(errorMap, dto.getError());
    assertEquals("Test error message", dto.getError().get("message"));
  }

  @Test
  void testWithErrorMessage() {
    EntenteStageDto dto = EntenteStageDto.withErrorMessage("Custom error message");

    assertNotNull(dto);
    assertNotNull(dto.getError());
    assertEquals("Custom error message", dto.getError().get("message"));
  }

  @Test
  void testFromEntity_EmployeurASigner_WhenSigned() {
    entente.setDateSignatureEmployeur(LocalDateTime.now());
    EntenteStageDto dto = EntenteStageDto.fromEntity(entente);

    assertTrue(dto.isEmployeurASigner());
  }

  @Test
  void testFromEntity_EmployeurASigner_WhenNotSigned() {
    entente.setDateSignatureEmployeur(null);
    EntenteStageDto dto = EntenteStageDto.fromEntity(entente);

    assertFalse(dto.isEmployeurASigner());
  }

  @Test
  void testEntenteStageDtoAllArgsConstructor() {
    StudentDto studentDto = StudentDto.empty();
    InternshipOfferDto offerDto = new InternshipOfferDto();
    ProfDto profDto = ProfDto.empty();
    GestionnaireDto gestionnaireDto = GestionnaireDto.empty();

    EntenteStageDto dto =
        new EntenteStageDto(
            1L,
            1L,
            studentDto,
            offerDto,
            profDto,
            gestionnaireDto,
            "Missions",
            EntenteStage.StatutEntente.BROUILLON,
            LocalDate.now(),
            12,
            "Location",
            500.0f,
            "FALL 2025",
            LocalDateTime.now(),
            LocalDateTime.now(),
            "Contact",
            "/path.pdf",
            LocalDateTime.now(),
            LocalDateTime.now(),
            LocalDateTime.now(),
            null,
            false);

    assertEquals(1L, dto.getId());
    assertEquals(1L, dto.getCandidatureId());
    assertEquals("Missions", dto.getMissionsObjectifs());
    assertEquals(EntenteStage.StatutEntente.BROUILLON, dto.getStatut());
    assertEquals(12, dto.getDuree());
    assertEquals("Location", dto.getLieu());
    assertEquals(500.0f, dto.getRemuneration());
    assertEquals("FALL 2025", dto.getSchoolTerm());
    assertFalse(dto.isEmployeurASigner());
  }
}
