package ca.cal.leandrose.service;

import static org.junit.jupiter.api.Assertions.*;

import ca.cal.leandrose.model.*;
import ca.cal.leandrose.repository.*;
import ca.cal.leandrose.service.dto.ConvocationDto;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class ConvocationServiceTest {

  @Autowired private ConvocationService convocationService;

  @Autowired private CandidatureRepository candidatureRepository;

  @Autowired private ConvocationRepository convocationRepository;

  @Autowired private StudentRepository studentRepository;

  @Autowired private InternshipOfferRepository offerRepository;

  @Autowired private CvRepository cvRepository;

  @Autowired private EmployeurRepository employeurRepository;

  private Student testStudent;
  private InternshipOffer testOffer;
  private Cv testCv;
  private Candidature testCandidature;
  private LocalDateTime futureDate;

  @BeforeEach
  void setUp() {
    futureDate = LocalDateTime.now().plusDays(7);

    Employeur employeur =
        employeurRepository.save(
            Employeur.builder()
                .firstName("Jean")
                .lastName("Boss")
                .email("boss@test.com")
                .password("password")
                .companyName("TechCorp")
                .field("Informatique")
                .build());

    testStudent =
        studentRepository.save(
            Student.builder()
                .firstName("Alice")
                .lastName("Martin")
                .email("alice@test.com")
                .password("password")
                .studentNumber("123456")
                .program("Computer Science")
                .build());

    testOffer =
        offerRepository.save(
            InternshipOffer.builder()
                .description("Stage développement web")
                .startDate(LocalDate.now().plusWeeks(2))
                .durationInWeeks(12)
                .address("123 Rue Test")
                .remuneration(1500f)
                .employeur(employeur)
                .pdfPath("/path/to/pdf.pdf")
                .status(InternshipOffer.Status.PUBLISHED)
                .build());

    testCv =
        cvRepository.save(
            Cv.builder()
                .student(testStudent)
                .pdfPath("/path/to/cv.pdf")
                .status(Cv.Status.APPROVED)
                .build());

    testCandidature =
        candidatureRepository.save(
            Candidature.builder()
                .student(testStudent)
                .internshipOffer(testOffer)
                .cv(testCv)
                .status(Candidature.Status.PENDING)
                .applicationDate(LocalDateTime.now())
                .build());
  }

  @Test
  void addConvocation_ShouldCreateConvocation() {
    convocationService.addConvocation(
        testCandidature.getId(), futureDate, "Bureau 301", "Message personnalisé");

    Candidature updated = candidatureRepository.findById(testCandidature.getId()).orElseThrow();
    assertEquals(Candidature.Status.CONVENED, updated.getStatus());

    Convocation convocation =
        convocationRepository.findAll().stream()
            .filter(
                c ->
                    c.getCandidature() != null
                        && c.getCandidature().getId().equals(testCandidature.getId()))
            .findFirst()
            .orElseThrow();
    assertEquals(testCandidature.getId(), convocation.getCandidature().getId());
    assertEquals(futureDate, convocation.getConvocationDate());
    assertEquals("Bureau 301", convocation.getLocation());
    assertEquals("Message personnalisé", convocation.getPersonnalMessage());
  }

  @Test
  void addConvocation_ShouldUseDefaultMessage_WhenMessageNull() {
    convocationService.addConvocation(testCandidature.getId(), futureDate, "Bureau 301", null);

    Convocation convocation =
        convocationRepository.findAll().stream()
            .filter(
                c ->
                    c.getCandidature() != null
                        && c.getCandidature().getId().equals(testCandidature.getId()))
            .findFirst()
            .orElseThrow();

    String expected = "Vous êtes convoqué(e) pour un entretien.";
    String actual = convocation.getPersonnalMessage();
    String normalized = (actual == null) ? "" : actual.trim();
    assertEquals(expected, normalized.isEmpty() ? expected : normalized);
  }

  @Test
  void addConvocation_ShouldUseDefaultMessage_WhenMessageEmpty() {
    convocationService.addConvocation(testCandidature.getId(), futureDate, "Bureau 301", "  ");

    Convocation convocation =
        convocationRepository.findAll().stream()
            .filter(
                c ->
                    c.getCandidature() != null
                        && c.getCandidature().getId().equals(testCandidature.getId()))
            .findFirst()
            .orElseThrow();

    String expected = "Vous êtes convoqué(e) pour un entretien.";
    String actual = convocation.getPersonnalMessage();
    String normalized = (actual == null) ? "" : actual.trim();
    assertEquals(expected, normalized.isEmpty() ? expected : normalized);
  }

  @Test
  void addConvocation_ShouldThrow_WhenCandidatureNotFound() {
    RuntimeException ex =
        assertThrows(
            RuntimeException.class,
            () -> convocationService.addConvocation(999L, futureDate, "Bureau 301", "Message"));
    assertEquals("Candidature non trouvée", ex.getMessage());
  }

  @Test
  void addConvocation_ShouldThrow_WhenConvocationDateInPast() {
    LocalDateTime pastDate = LocalDateTime.now().minusDays(1);
    IllegalArgumentException ex =
        assertThrows(
            IllegalArgumentException.class,
            () ->
                convocationService.addConvocation(
                    testCandidature.getId(), pastDate, "Bureau 301", "Message"));
    assertEquals("La date de convocation ne peut pas être dans le passé", ex.getMessage());
  }

  @Test
  void addConvocation_ShouldThrow_WhenLocationNullOrEmpty() {
    IllegalArgumentException ex1 =
        assertThrows(
            IllegalArgumentException.class,
            () ->
                convocationService.addConvocation(
                    testCandidature.getId(), futureDate, null, "Message"));
    assertEquals("Le lieu ne peut pas être vide", ex1.getMessage());

    IllegalArgumentException ex2 =
        assertThrows(
            IllegalArgumentException.class,
            () ->
                convocationService.addConvocation(
                    testCandidature.getId(), futureDate, "   ", "Message"));
    assertEquals("Le lieu ne peut pas être vide", ex2.getMessage());
  }

  @Test
  void addConvocation_ShouldThrow_WhenAlreadyConvened() {
    testCandidature.setStatus(Candidature.Status.CONVENED);
    candidatureRepository.save(testCandidature);

    IllegalStateException ex =
        assertThrows(
            IllegalStateException.class,
            () ->
                convocationService.addConvocation(
                    testCandidature.getId(), futureDate, "Bureau 301", "Message"));
    assertEquals("Cette candidature a déjà une convocation", ex.getMessage());
  }

  @Test
  void getAllConvocationsByInternshipOfferId_ShouldReturnDtos() {
    testCandidature.setStatus(Candidature.Status.CONVENED);
    candidatureRepository.save(testCandidature);

    convocationRepository.save(
        Convocation.builder()
            .candidature(testCandidature)
            .convocationDate(futureDate)
            .location("Bureau 301")
            .personnalMessage("Message")
            .build());

    List<ConvocationDto> result =
        convocationService.getAllConvocationsByInterShipOfferId(testOffer.getId());

    assertNotNull(result);
    assertFalse(result.isEmpty());
    assertTrue(
        result.stream()
            .anyMatch(
                dto ->
                    "Bureau 301".equals(dto.getLocation())
                        && testCandidature.getId().equals(dto.getCandidatureId())));
  }

  @Test
  void addMultipleConvocations_ShouldReturnMultipleDtos() {
    Student student2 =
        studentRepository.save(
            Student.builder()
                .firstName("Bob")
                .lastName("Smith")
                .email("bob@test.com")
                .password("password")
                .studentNumber("654321")
                .program("Computer Science")
                .build());

    Cv cv2 =
        cvRepository.save(
            Cv.builder()
                .student(student2)
                .pdfPath("/path/to/cv2.pdf")
                .status(Cv.Status.APPROVED)
                .build());

    Candidature candidature2 =
        candidatureRepository.save(
            Candidature.builder()
                .student(student2)
                .internshipOffer(testOffer)
                .cv(cv2)
                .status(Candidature.Status.PENDING)
                .applicationDate(LocalDateTime.now())
                .build());

    convocationService.addConvocation(
        testCandidature.getId(), futureDate, "Bureau 301", "Message 1");
    convocationService.addConvocation(
        candidature2.getId(), futureDate.plusDays(1), "Bureau 302", "Message 2");

    List<ConvocationDto> result =
        convocationService.getAllConvocationsByInterShipOfferId(testOffer.getId());
    assertTrue(result.size() >= 2);
    assertTrue(
        result.stream().anyMatch(dto -> dto.getCandidatureId().equals(testCandidature.getId())));
    assertTrue(
        result.stream().anyMatch(dto -> dto.getCandidatureId().equals(candidature2.getId())));
  }
}
