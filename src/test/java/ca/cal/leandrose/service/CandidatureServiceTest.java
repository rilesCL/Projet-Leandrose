package ca.cal.leandrose.service;

import ca.cal.leandrose.model.*;
import ca.cal.leandrose.repository.*;
import ca.cal.leandrose.service.dto.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class CandidatureServiceTest {

  @Autowired private CandidatureService candidatureService;

  @Autowired private StudentRepository studentRepository;

  @Autowired private InternshipOfferRepository offerRepository;

  @Autowired private CvRepository cvRepository;

  @Autowired private EmployeurRepository employeurRepository;

  private Student testStudent;
  private InternshipOffer testOffer;
  private Cv testCv;

  @BeforeEach
  void setUp() {
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
                .startDate(LocalDate.of(2025, 6, 1))
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
  }

  @Test
  void postuler_ShouldCreateCandidature_WhenAllConditionsAreMet() {
    CandidatureDto result =
        candidatureService.postuler(testStudent.getId(), testOffer.getId(), testCv.getId());

    assertNotNull(result);
    assertNotNull(result.getId());
    assertEquals(testStudent.getId(), result.getStudent().getId());
    assertEquals(testOffer.getId(), result.getInternshipOffer().getId());
    assertEquals(testCv.getId(), result.getCv().getId());
    assertEquals(Candidature.Status.PENDING, result.getStatus());
    assertEquals("Alice", result.getStudent().getFirstName());
    assertEquals("Martin", result.getStudent().getLastName());
    assertEquals("Stage développement web", result.getInternshipOffer().getDescription());
    assertEquals("TechCorp", result.getInternshipOffer().getCompanyName());
  }

  @Test
  void getCandidaturesByStudent_ShouldReturnAllCandidatures() {
    candidatureService.postuler(testStudent.getId(), testOffer.getId(), testCv.getId());

    InternshipOffer offer2 =
        offerRepository.save(
            InternshipOffer.builder()
                .description("Stage analyse de données")
                .startDate(LocalDate.of(2025, 9, 1))
                .durationInWeeks(10)
                .address("789 Rue Test")
                .employeur(testOffer.getEmployeur())
                .pdfPath("/path/to/offer2.pdf")
                .status(InternshipOffer.Status.PUBLISHED)
                .build());

    candidatureService.postuler(testStudent.getId(), offer2.getId(), testCv.getId());

    List<CandidatureDto> candidatures =
        candidatureService.getCandidaturesByStudent(testStudent.getId());

    assertEquals(2, candidatures.size());
    assertTrue(
        candidatures.stream()
            .anyMatch(
                c -> c.getInternshipOffer().getDescription().equals("Stage développement web")));
    assertTrue(
        candidatures.stream()
            .anyMatch(
                c -> c.getInternshipOffer().getDescription().equals("Stage analyse de données")));
  }

  @Test
  void candidatureEmployeurDto_ShouldContainAllNecessaryFields() {
    CandidatureDto candidature =
        candidatureService.postuler(testStudent.getId(), testOffer.getId(), testCv.getId());

    List<CandidatureEmployeurDto> candidatures =
        candidatureService.getCandidaturesByOffer(testOffer.getId());

    CandidatureEmployeurDto dto = candidatures.get(0);
    assertNotNull(dto.getId());
    assertEquals("Alice", dto.getStudentFirstName());
    assertEquals("Martin", dto.getStudentLastName());
    assertEquals("Computer Science", dto.getStudentProgram());
    assertEquals(Candidature.Status.PENDING, dto.getStatus());
    assertNotNull(dto.getCvId());
    assertEquals("APPROVED", dto.getCvStatus());
    assertEquals(testOffer.getId(), dto.getOfferId());
    assertEquals("Stage développement web", dto.getOfferDescription());
  }

  @Test
  void completeFlow_EmployeurAcceptsAndStudentAccepts_ShouldResultInAccepted() {
    CandidatureDto candidature =
        candidatureService.postuler(testStudent.getId(), testOffer.getId(), testCv.getId());

    CandidatureDto afterEmployeur = candidatureService.acceptByEmployeur(candidature.getId());
    CandidatureDto afterStudent =
        candidatureService.acceptByStudent(candidature.getId(), testStudent.getId());

    assertEquals(Candidature.Status.PENDING, candidature.getStatus());
    assertEquals(Candidature.Status.ACCEPTEDBYEMPLOYEUR, afterEmployeur.getStatus());
    assertEquals(Candidature.Status.ACCEPTED, afterStudent.getStatus());
  }
}