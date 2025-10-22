package ca.cal.leandrose.service;

import ca.cal.leandrose.model.*;
import ca.cal.leandrose.repository.*;
import ca.cal.leandrose.service.dto.CandidatureDto;
import ca.cal.leandrose.service.dto.CandidatureEmployeurDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
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


    @Test
    void getCandidaturesByOffer_ShouldReturnCandidaturesForSpecificOffer() {
        // Arrange
        candidatureService.postuler(testStudent.getId(), testOffer.getId(), testCv.getId());


        Student student2 = studentRepository.save(Student.builder()
                .firstName("Bob")
                .lastName("Smith")
                .email("bob@test.com")
                .password("password")
                .studentNumber("654321")
                .program("Computer Science")
                .build());

        Cv cv2 = cvRepository.save(Cv.builder()
                .student(student2)
                .pdfPath("/path/to/cv2.pdf")
                .status(Cv.Status.APPROVED)
                .build());

        candidatureService.postuler(student2.getId(), testOffer.getId(), cv2.getId());

        // Act
        List<CandidatureEmployeurDto> candidatures =
                candidatureService.getCandidaturesByOffer(testOffer.getId());

        // Assert
        assertEquals(2, candidatures.size());
        assertTrue(candidatures.stream()
                .anyMatch(c -> c.getStudentLastName().equals("Martin")));
        assertTrue(candidatures.stream()
                .anyMatch(c -> c.getStudentLastName().equals("Smith")));
        assertTrue(candidatures.stream()
                .allMatch(c -> c.getOfferId().equals(testOffer.getId())));
    }

    @Test
    void getCandidaturesByOffer_ShouldReturnEmptyList_WhenNoCandidatures() {
        // Act
        List<CandidatureEmployeurDto> candidatures =
                candidatureService.getCandidaturesByOffer(testOffer.getId());

        // Assert
        assertNotNull(candidatures);
        assertTrue(candidatures.isEmpty());
    }

    @Test
    void getCandidaturesByEmployeur_ShouldReturnAllCandidaturesForAllOffers() {
        // Arrange
        candidatureService.postuler(testStudent.getId(), testOffer.getId(), testCv.getId());

        InternshipOffer offer2 = offerRepository.save(InternshipOffer.builder()
                .description("Stage Data Science")
                .startDate(LocalDate.of(2025, 9, 1))
                .durationInWeeks(10)
                .address("789 Rue Test")
                .employeur(testOffer.getEmployeur())
                .pdfPath("/path/to/offer2.pdf")
                .status(InternshipOffer.Status.PUBLISHED)
                .build());

        Student student2 = studentRepository.save(Student.builder()
                .firstName("Charlie")
                .lastName("Brown")
                .email("charlie@test.com")
                .password("password")
                .studentNumber("111222")
                .program("Data Science")
                .build());

        Cv cv2 = cvRepository.save(Cv.builder()
                .student(student2)
                .pdfPath("/path/to/cv2.pdf")
                .status(Cv.Status.APPROVED)
                .build());

        candidatureService.postuler(student2.getId(), offer2.getId(), cv2.getId());

        // Act
        List<CandidatureEmployeurDto> candidatures =
                candidatureService.getCandidaturesByEmployeur(testOffer.getEmployeur().getId());

        // Assert
        assertEquals(2, candidatures.size());
        assertTrue(candidatures.stream()
                .anyMatch(c -> c.getOfferDescription().equals("Stage développement web")));
        assertTrue(candidatures.stream()
                .anyMatch(c -> c.getOfferDescription().equals("Stage Data Science")));
    }

    @Test
    void getCandidaturesByEmployeur_ShouldNotReturnOtherEmployeursCandidatures() {
        // Arrange
        candidatureService.postuler(testStudent.getId(), testOffer.getId(), testCv.getId());

        Employeur employeur2 = employeurRepository.save(Employeur.builder()
                .firstName("Jane")
                .lastName("Manager")
                .email("jane@company.com")
                .password("password")
                .companyName("OtherCorp")
                .field("Finance")
                .build());

        InternshipOffer otherOffer = offerRepository.save(InternshipOffer.builder()
                .description("Stage Finance")
                .startDate(LocalDate.of(2025, 7, 1))
                .durationInWeeks(8)
                .address("999 Rue Autre")
                .employeur(employeur2)
                .pdfPath("/path/to/other.pdf")
                .status(InternshipOffer.Status.PUBLISHED)
                .build());

        Student student2 = studentRepository.save(Student.builder()
                .firstName("David")
                .lastName("Lee")
                .email("david@test.com")
                .password("password")
                .studentNumber("333444")
                .program("Finance")
                .build());

        Cv cv2 = cvRepository.save(Cv.builder()
                .student(student2)
                .pdfPath("/path/to/cv3.pdf")
                .status(Cv.Status.APPROVED)
                .build());

        candidatureService.postuler(student2.getId(), otherOffer.getId(), cv2.getId());

        // Act
        List<CandidatureEmployeurDto> candidatures1 =
                candidatureService.getCandidaturesByEmployeur(testOffer.getEmployeur().getId());
        List<CandidatureEmployeurDto> candidatures2 =
                candidatureService.getCandidaturesByEmployeur(employeur2.getId());

        // Assert
        assertEquals(1, candidatures1.size());
        assertEquals("Stage développement web", candidatures1.get(0).getOfferDescription());

        assertEquals(1, candidatures2.size());
        assertEquals("Stage Finance", candidatures2.get(0).getOfferDescription());
    }

    @Test
    void candidatureEmployeurDto_ShouldContainAllNecessaryFields() {
        // Arrange
        CandidatureDto candidature = candidatureService.postuler(
                testStudent.getId(), testOffer.getId(), testCv.getId());

        // Act
        List<CandidatureEmployeurDto> candidatures =
                candidatureService.getCandidaturesByOffer(testOffer.getId());

        // Assert
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

// ===== TESTS POUR ACCEPTATION PAR L'EMPLOYEUR =====

    @Test
    void acceptByEmployeur_ShouldChangeStatusToAcceptedByEmployeur_WhenPending() {
        // Arrange
        CandidatureDto candidature = candidatureService.postuler(
                testStudent.getId(), testOffer.getId(), testCv.getId());

        // Act
        CandidatureDto result = candidatureService.acceptByEmployeur(candidature.getId());

        // Assert
        assertNotNull(result);
        assertEquals(Candidature.Status.ACCEPTEDBYEMPLOYEUR, result.getStatus());
        assertEquals(candidature.getId(), result.getId());
    }

    @Test
    void acceptByEmployeur_ShouldThrowException_WhenAlreadyRejected() {
        // Arrange
        CandidatureDto candidature = candidatureService.postuler(
                testStudent.getId(), testOffer.getId(), testCv.getId());
        candidatureService.rejectByEmployeur(candidature.getId());

        // Act & Assert
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> candidatureService.acceptByEmployeur(candidature.getId())
        );

        assertEquals("Impossible d'accepter une candidature déjà rejetée", exception.getMessage());
    }

    @Test
    void acceptByEmployeur_ShouldThrowException_WhenAlreadyAccepted() {
        // Arrange
        CandidatureDto candidature = candidatureService.postuler(
                testStudent.getId(), testOffer.getId(), testCv.getId());
        candidatureService.acceptByEmployeur(candidature.getId());
        candidatureService.acceptByStudent(candidature.getId(), testStudent.getId());

        // Act & Assert
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> candidatureService.acceptByEmployeur(candidature.getId())
        );

        assertEquals("Cette candidature est déjà entièrement acceptée", exception.getMessage());
    }

    @Test
    void acceptByEmployeur_ShouldThrowException_WhenAlreadyAcceptedByEmployeur() {
        // Arrange
        CandidatureDto candidature = candidatureService.postuler(
                testStudent.getId(), testOffer.getId(), testCv.getId());
        candidatureService.acceptByEmployeur(candidature.getId());

        // Act & Assert
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> candidatureService.acceptByEmployeur(candidature.getId())
        );

        assertEquals("Vous avez déjà accepté cette candidature, en attente de la réponse de l'étudiant",
                exception.getMessage());
    }

    @Test
    void acceptByEmployeur_ShouldThrowException_WhenCandidatureNotFound() {
        // Act & Assert
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> candidatureService.acceptByEmployeur(999L)
        );

        assertEquals("Candidature introuvable", exception.getMessage());
    }

// ===== TESTS POUR REJET PAR L'EMPLOYEUR =====

    @Test
    void rejectByEmployeur_ShouldChangeStatusToRejected_WhenPending() {
        // Arrange
        CandidatureDto candidature = candidatureService.postuler(
                testStudent.getId(), testOffer.getId(), testCv.getId());

        // Act
        CandidatureDto result = candidatureService.rejectByEmployeur(candidature.getId());

        // Assert
        assertNotNull(result);
        assertEquals(Candidature.Status.REJECTED, result.getStatus());
        assertEquals(candidature.getId(), result.getId());
    }

    @Test
    void rejectByEmployeur_ShouldChangeStatusToRejected_WhenAcceptedByEmployeur() {
        // Arrange
        CandidatureDto candidature = candidatureService.postuler(
                testStudent.getId(), testOffer.getId(), testCv.getId());
        candidatureService.acceptByEmployeur(candidature.getId());

        // Act
        CandidatureDto result = candidatureService.rejectByEmployeur(candidature.getId());

        // Assert
        assertNotNull(result);
        assertEquals(Candidature.Status.REJECTED, result.getStatus());
    }

    @Test
    void rejectByEmployeur_ShouldThrowException_WhenAlreadyAcceptedByBothParties() {
        // Arrange
        CandidatureDto candidature = candidatureService.postuler(
                testStudent.getId(), testOffer.getId(), testCv.getId());
        candidatureService.acceptByEmployeur(candidature.getId());
        candidatureService.acceptByStudent(candidature.getId(), testStudent.getId());

        // Act & Assert
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> candidatureService.rejectByEmployeur(candidature.getId())
        );

        assertEquals("Impossible de rejeter une candidature déjà acceptée par les deux parties",
                exception.getMessage());
    }

    @Test
    void rejectByEmployeur_ShouldThrowException_WhenAlreadyRejected() {
        // Arrange
        CandidatureDto candidature = candidatureService.postuler(
                testStudent.getId(), testOffer.getId(), testCv.getId());
        candidatureService.rejectByEmployeur(candidature.getId());

        // Act & Assert
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> candidatureService.rejectByEmployeur(candidature.getId())
        );

        assertEquals("Cette candidature est déjà rejetée", exception.getMessage());
    }

// ===== TESTS POUR ACCEPTATION PAR L'ÉTUDIANT =====

    @Test
    void acceptByStudent_ShouldChangeStatusToAccepted_WhenAcceptedByEmployeur() {
        // Arrange
        CandidatureDto candidature = candidatureService.postuler(
                testStudent.getId(), testOffer.getId(), testCv.getId());
        candidatureService.acceptByEmployeur(candidature.getId());

        // Act
        CandidatureDto result = candidatureService.acceptByStudent(
                candidature.getId(), testStudent.getId());

        // Assert
        assertNotNull(result);
        assertEquals(Candidature.Status.ACCEPTED, result.getStatus());
        assertEquals(candidature.getId(), result.getId());
    }

    @Test
    void acceptByStudent_ShouldThrowException_WhenNotAcceptedByEmployeur() {
        // Arrange
        CandidatureDto candidature = candidatureService.postuler(
                testStudent.getId(), testOffer.getId(), testCv.getId());

        // Act & Assert
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> candidatureService.acceptByStudent(candidature.getId(), testStudent.getId())
        );

        assertEquals("L'employeur doit d'abord accepter cette candidature", exception.getMessage());
    }

    @Test
    void acceptByStudent_ShouldThrowException_WhenNotOwnCandidature() {
        // Arrange
        CandidatureDto candidature = candidatureService.postuler(
                testStudent.getId(), testOffer.getId(), testCv.getId());
        candidatureService.acceptByEmployeur(candidature.getId());

        // Créer un autre étudiant
        Student otherStudent = studentRepository.save(Student.builder()
                .firstName("Bob")
                .lastName("Jones")
                .email("bob@test.com")
                .password("password")
                .studentNumber("999999")
                .program("Computer Science")
                .build());

        // Act & Assert
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> candidatureService.acceptByStudent(candidature.getId(), otherStudent.getId())
        );

        assertEquals("Cette candidature ne vous appartient pas", exception.getMessage());
    }

    @Test
    void acceptByStudent_ShouldThrowException_WhenCandidatureNotFound() {
        // Act & Assert
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> candidatureService.acceptByStudent(999L, testStudent.getId())
        );

        assertEquals("Candidature introuvable", exception.getMessage());
    }

// ===== TESTS POUR REJET PAR L'ÉTUDIANT =====

    @Test
    void rejectByStudent_ShouldChangeStatusToRejected_WhenAcceptedByEmployeur() {
        // Arrange
        CandidatureDto candidature = candidatureService.postuler(
                testStudent.getId(), testOffer.getId(), testCv.getId());
        candidatureService.acceptByEmployeur(candidature.getId());

        // Act
        CandidatureDto result = candidatureService.rejectByStudent(
                candidature.getId(), testStudent.getId());

        // Assert
        assertNotNull(result);
        assertEquals(Candidature.Status.REJECTED, result.getStatus());
        assertEquals(candidature.getId(), result.getId());
    }

    @Test
    void rejectByStudent_ShouldThrowException_WhenNotAcceptedByEmployeur() {
        // Arrange
        CandidatureDto candidature = candidatureService.postuler(
                testStudent.getId(), testOffer.getId(), testCv.getId());

        // Act & Assert
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> candidatureService.rejectByStudent(candidature.getId(), testStudent.getId())
        );

        assertEquals("Vous ne pouvez refuser que les candidatures acceptées par l'employeur",
                exception.getMessage());
    }

    @Test
    void rejectByStudent_ShouldThrowException_WhenNotOwnCandidature() {
        // Arrange
        CandidatureDto candidature = candidatureService.postuler(
                testStudent.getId(), testOffer.getId(), testCv.getId());
        candidatureService.acceptByEmployeur(candidature.getId());

        // Créer un autre étudiant
        Student otherStudent = studentRepository.save(Student.builder()
                .firstName("Charlie")
                .lastName("Wilson")
                .email("charlie@test.com")
                .password("password")
                .studentNumber("888888")
                .program("Computer Science")
                .build());

        // Act & Assert
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> candidatureService.rejectByStudent(candidature.getId(), otherStudent.getId())
        );

        assertEquals("Cette candidature ne vous appartient pas", exception.getMessage());
    }

    @Test
    void rejectByStudent_ShouldThrowException_WhenCandidatureNotFound() {
        // Act & Assert
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> candidatureService.rejectByStudent(999L, testStudent.getId())
        );

        assertEquals("Candidature introuvable", exception.getMessage());
    }

// ===== TESTS DE FLUX COMPLET =====

    @Test
    void completeFlow_EmployeurAcceptsAndStudentAccepts_ShouldResultInAccepted() {
        // Arrange
        CandidatureDto candidature = candidatureService.postuler(
                testStudent.getId(), testOffer.getId(), testCv.getId());

        // Act
        CandidatureDto afterEmployeur = candidatureService.acceptByEmployeur(candidature.getId());
        CandidatureDto afterStudent = candidatureService.acceptByStudent(
                candidature.getId(), testStudent.getId());

        // Assert
        assertEquals(Candidature.Status.PENDING, candidature.getStatus());
        assertEquals(Candidature.Status.ACCEPTEDBYEMPLOYEUR, afterEmployeur.getStatus());
        assertEquals(Candidature.Status.ACCEPTED, afterStudent.getStatus());
    }

    @Test
    void completeFlow_EmployeurAcceptsAndStudentRejects_ShouldResultInRejected() {
        // Arrange
        CandidatureDto candidature = candidatureService.postuler(
                testStudent.getId(), testOffer.getId(), testCv.getId());

        // Act
        CandidatureDto afterEmployeur = candidatureService.acceptByEmployeur(candidature.getId());
        CandidatureDto afterStudent = candidatureService.rejectByStudent(
                candidature.getId(), testStudent.getId());

        // Assert
        assertEquals(Candidature.Status.PENDING, candidature.getStatus());
        assertEquals(Candidature.Status.ACCEPTEDBYEMPLOYEUR, afterEmployeur.getStatus());
        assertEquals(Candidature.Status.REJECTED, afterStudent.getStatus());
    }

    @Test
    void completeFlow_EmployeurRejects_ShouldResultInRejected() {
        // Arrange
        CandidatureDto candidature = candidatureService.postuler(
                testStudent.getId(), testOffer.getId(), testCv.getId());

        // Act
        CandidatureDto result = candidatureService.rejectByEmployeur(candidature.getId());

        // Assert
        assertEquals(Candidature.Status.PENDING, candidature.getStatus());
        assertEquals(Candidature.Status.REJECTED, result.getStatus());
    }

}