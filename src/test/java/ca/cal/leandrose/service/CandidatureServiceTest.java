package ca.cal.leandrose.service;

import ca.cal.leandrose.model.*;
import ca.cal.leandrose.repository.*;
import ca.cal.leandrose.service.dto.CandidatureDto;
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

    @Autowired
    private CandidatureService candidatureService;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private InternshipOfferRepository offerRepository;

    @Autowired
    private CvRepository cvRepository;

    @Autowired
    private EmployeurRepository employeurRepository;

    private Student testStudent;
    private InternshipOffer testOffer;
    private Cv testCv;

    @BeforeEach
    void setUp() {
        Employeur employeur = employeurRepository.save(Employeur.builder()
                .firstName("Jean")
                .lastName("Boss")
                .email("boss@test.com")
                .password("password")
                .companyName("TechCorp")
                .field("Informatique")
                .build());

        testStudent = studentRepository.save(Student.builder()
                .firstName("Alice")
                .lastName("Martin")
                .email("alice@test.com")
                .password("password")
                .studentNumber("123456")
                .program("Computer Science")
                .build());

        testOffer = offerRepository.save(InternshipOffer.builder()
                .description("Stage développement web")
                .startDate(LocalDate.of(2025, 6, 1))
                .durationInWeeks(12)
                .address("123 Rue Test")
                .remuneration(1500f)
                .employeur(employeur)
                .pdfPath("/path/to/pdf.pdf")
                .status(InternshipOffer.Status.PUBLISHED)
                .build());

        testCv = cvRepository.save(Cv.builder()
                .student(testStudent)
                .pdfPath("/path/to/cv.pdf")
                .status(Cv.Status.APPROVED)
                .build());
    }

    @Test
    void postuler_ShouldCreateCandidature_WhenAllConditionsAreMet() {
        // Act
        CandidatureDto result = candidatureService.postuler(
                testStudent.getId(),
                testOffer.getId(),
                testCv.getId()
        );

        // Assert
        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals(testStudent.getId(), result.getStudentId());
        assertEquals(testOffer.getId(), result.getOfferId());
        assertEquals(testCv.getId(), result.getCvId());
        assertEquals(Candidature.Status.PENDING, result.getStatus());
        assertEquals(LocalDate.now(), result.getApplicationDate());
        assertEquals("Alice Martin", result.getStudentName());
        assertEquals("Stage développement web", result.getOfferDescription());
        assertEquals("TechCorp", result.getCompanyName());
    }

    @Test
    void postuler_ShouldThrowException_WhenAlreadyApplied() {
        // Arrange
        candidatureService.postuler(
                testStudent.getId(),
                testOffer.getId(),
                testCv.getId()
        );

        // Act & Assert
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> candidatureService.postuler(
                        testStudent.getId(),
                        testOffer.getId(),
                        testCv.getId()
                )
        );

        assertEquals("Vous avez déjà postulé à cette offre", exception.getMessage());
    }

    @Test
    void postuler_ShouldThrowException_WhenCvNotApproved() {
        // Arrange
        final Cv pendingCv = cvRepository.save(Cv.builder()
                .student(testStudent)
                .pdfPath("/path/to/pending_cv.pdf")
                .status(Cv.Status.PENDING)
                .build());

        // Act & Assert
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> candidatureService.postuler(
                        testStudent.getId(),
                        testOffer.getId(),
                        pendingCv.getId()
                )
        );

        assertEquals("Votre CV doit être approuvé pour postuler", exception.getMessage());
    }

    @Test
    void postuler_ShouldThrowException_WhenCvRejected() {
        // Arrange
        final Cv rejectedCv = cvRepository.save(Cv.builder()
                .student(testStudent)
                .pdfPath("/path/to/rejected_cv.pdf")
                .status(Cv.Status.REJECTED)
                .build());

        // Act & Assert
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> candidatureService.postuler(
                        testStudent.getId(),
                        testOffer.getId(),
                        rejectedCv.getId()
                )
        );

        assertEquals("Votre CV doit être approuvé pour postuler", exception.getMessage());
    }

    @Test
    void postuler_ShouldThrowException_WhenOfferNotPublished() {
        // Arrange
        final InternshipOffer pendingOffer = offerRepository.save(InternshipOffer.builder()
                .description("Stage en attente")
                .startDate(LocalDate.of(2025, 9, 1))
                .durationInWeeks(8)
                .address("456 Rue Test")
                .employeur(testOffer.getEmployeur())
                .pdfPath("/path/to/offer.pdf")
                .status(InternshipOffer.Status.PENDING_VALIDATION)
                .build());

        // Act & Assert
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> candidatureService.postuler(
                        testStudent.getId(),
                        pendingOffer.getId(),
                        testCv.getId()
                )
        );

        assertEquals("Cette offre n'est pas disponible", exception.getMessage());
    }

    @Test
    void postuler_ShouldThrowException_WhenOfferRejected() {
        // Arrange
        final InternshipOffer rejectedOffer = offerRepository.save(InternshipOffer.builder()
                .description("Stage rejeté")
                .startDate(LocalDate.of(2025, 9, 1))
                .durationInWeeks(8)
                .address("456 Rue Test")
                .employeur(testOffer.getEmployeur())
                .pdfPath("/path/to/offer.pdf")
                .status(InternshipOffer.Status.REJECTED)
                .build());

        // Act & Assert
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> candidatureService.postuler(
                        testStudent.getId(),
                        rejectedOffer.getId(),
                        testCv.getId()
                )
        );

        assertEquals("Cette offre n'est pas disponible", exception.getMessage());
    }

    @Test
    void postuler_ShouldThrowException_WhenStudentNotFound() {
        // Act & Assert
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> candidatureService.postuler(
                        999L,
                        testOffer.getId(),
                        testCv.getId()
                )
        );

        assertEquals("Étudiant non trouvé", exception.getMessage());
    }

    @Test
    void postuler_ShouldThrowException_WhenOfferNotFound() {
        // Act & Assert
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> candidatureService.postuler(
                        testStudent.getId(),
                        999L,
                        testCv.getId()
                )
        );

        assertEquals("Offre non trouvée", exception.getMessage());
    }

    @Test
    void postuler_ShouldThrowException_WhenCvNotFound() {
        // Act & Assert
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> candidatureService.postuler(
                        testStudent.getId(),
                        testOffer.getId(),
                        999L
                )
        );

        assertEquals("CV non trouvé", exception.getMessage());
    }

    @Test
    void getCandidaturesByStudent_ShouldReturnAllCandidatures() {
        // Arrange
        candidatureService.postuler(testStudent.getId(), testOffer.getId(), testCv.getId());

        InternshipOffer offer2 = offerRepository.save(InternshipOffer.builder()
                .description("Stage analyse de données")
                .startDate(LocalDate.of(2025, 9, 1))
                .durationInWeeks(10)
                .address("789 Rue Test")
                .employeur(testOffer.getEmployeur())
                .pdfPath("/path/to/offer2.pdf")
                .status(InternshipOffer.Status.PUBLISHED)
                .build());

        candidatureService.postuler(testStudent.getId(), offer2.getId(), testCv.getId());

        // Act
        List<CandidatureDto> candidatures = candidatureService.getCandidaturesByStudent(testStudent.getId());

        // Assert
        assertEquals(2, candidatures.size());
        assertTrue(candidatures.stream()
                .anyMatch(c -> c.getOfferDescription().equals("Stage développement web")));
        assertTrue(candidatures.stream()
                .anyMatch(c -> c.getOfferDescription().equals("Stage analyse de données")));
    }

    @Test
    void getCandidaturesByStudent_ShouldReturnEmptyList_WhenNoCandidatures() {
        // Act
        List<CandidatureDto> candidatures = candidatureService.getCandidaturesByStudent(testStudent.getId());

        // Assert
        assertNotNull(candidatures);
        assertTrue(candidatures.isEmpty());
    }
}