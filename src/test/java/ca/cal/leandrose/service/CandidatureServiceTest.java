package ca.cal.leandrose.service;

import static org.junit.jupiter.api.Assertions.*;

import ca.cal.leandrose.model.*;
import ca.cal.leandrose.repository.*;
import ca.cal.leandrose.service.dto.*;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

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
        SchoolTerm autumn = new SchoolTerm(SchoolTerm.Season.FALL, 2026);
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
                                .schoolTerm(autumn)
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

    // ------------------- HAPPY PATHS -------------------
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
    }

    @Test
    void getCandidaturesByStudent_ShouldReturnAllCandidatures() {
        candidatureService.postuler(testStudent.getId(), testOffer.getId(), testCv.getId());
        SchoolTerm winter = new SchoolTerm(SchoolTerm.Season.WINTER, 2026);

        InternshipOffer offer2 =
                offerRepository.save(
                        InternshipOffer.builder()
                                .description("Stage analyse de données")
                                .startDate(LocalDate.of(2025, 9, 1))
                                .durationInWeeks(10)
                                .address("789 Rue Test")
                                .employeur(testOffer.getEmployeur())
                                .pdfPath("/path/to/offer2.pdf")
                                .schoolTerm(winter)
                                .status(InternshipOffer.Status.PUBLISHED)
                                .build());

        candidatureService.postuler(testStudent.getId(), offer2.getId(), testCv.getId());

        List<CandidatureDto> candidatures =
                candidatureService.getCandidaturesByStudent(testStudent.getId());

        assertEquals(2, candidatures.size());
    }

    @Test
    void completeFlow_EmployeurAcceptsAndStudentAccepts_ShouldResultInAccepted() {
        CandidatureDto candidature =
                candidatureService.postuler(testStudent.getId(), testOffer.getId(), testCv.getId());

        CandidatureDto afterEmployeur = candidatureService.acceptByEmployeur(candidature.getId());
        CandidatureDto afterStudent =
                candidatureService.acceptByStudent(candidature.getId(), testStudent.getId());

        assertEquals(Candidature.Status.ACCEPTEDBYEMPLOYEUR, afterEmployeur.getStatus());
        assertEquals(Candidature.Status.ACCEPTED, afterStudent.getStatus());
    }

    // ------------------- POSTULER EXCEPTIONS -------------------
    @Test
    void postuler_ShouldThrow_WhenAlreadyApplied() {
        candidatureService.postuler(testStudent.getId(), testOffer.getId(), testCv.getId());

        assertThrows(
                IllegalStateException.class,
                () -> candidatureService.postuler(testStudent.getId(), testOffer.getId(), testCv.getId()));
    }

    @Test
    void postuler_ShouldThrow_WhenStudentNotFound() {
        assertThrows(
                RuntimeException.class,
                () -> candidatureService.postuler(999L, testOffer.getId(), testCv.getId()));
    }

    @Test
    void postuler_ShouldThrow_WhenOfferNotFound() {
        assertThrows(
                RuntimeException.class,
                () -> candidatureService.postuler(testStudent.getId(), 999L, testCv.getId()));
    }

    @Test
    void postuler_ShouldThrow_WhenOfferNotPublished() {
        InternshipOffer offer = offerRepository.save(
                InternshipOffer.builder()
                        .description("Offre non publiée")
                        .startDate(LocalDate.of(2025, 7, 1))
                        .durationInWeeks(8)
                        .address("456 Rue Test")
                        .employeur(testOffer.getEmployeur())
                        .pdfPath("/path/to/pdf.pdf")
                        .schoolTerm(new SchoolTerm(SchoolTerm.Season.FALL, 2026))
                        .status(InternshipOffer.Status.PENDING_VALIDATION)
                        .build()
        );


        assertThrows(
                IllegalStateException.class,
                () -> candidatureService.postuler(testStudent.getId(), offer.getId(), testCv.getId()));
    }

    @Test
    void postuler_ShouldThrow_WhenCvNotFound() {
        assertThrows(
                RuntimeException.class,
                () -> candidatureService.postuler(testStudent.getId(), testOffer.getId(), 999L));
    }

    @Test
    void postuler_ShouldThrow_WhenCvNotApproved() {
        Cv rejectedCv = cvRepository.save(
                Cv.builder().student(testStudent).status(Cv.Status.PENDING).build()
        );

        assertThrows(
                IllegalStateException.class,
                () -> candidatureService.postuler(testStudent.getId(), testOffer.getId(), rejectedCv.getId()));
    }

    // ------------------- ACCEPT BY EMPLOYEUR -------------------
    @Test
    void acceptByEmployeur_ShouldThrow_WhenAlreadyRejected() {
        CandidatureDto c = candidatureService.postuler(testStudent.getId(), testOffer.getId(), testCv.getId());
        candidatureService.rejectByEmployeur(c.getId());

        assertThrows(IllegalStateException.class, () -> candidatureService.acceptByEmployeur(c.getId()));
    }

    @Test
    void acceptByEmployeur_ShouldThrow_WhenAlreadyAccepted() {
        CandidatureDto c = candidatureService.postuler(testStudent.getId(), testOffer.getId(), testCv.getId());
        candidatureService.acceptByEmployeur(c.getId());
        candidatureService.acceptByStudent(c.getId(), testStudent.getId());

        assertThrows(IllegalStateException.class, () -> candidatureService.acceptByEmployeur(c.getId()));
    }

    @Test
    void acceptByEmployeur_ShouldThrow_WhenAlreadyAcceptedByEmployeur() {
        CandidatureDto c = candidatureService.postuler(testStudent.getId(), testOffer.getId(), testCv.getId());
        candidatureService.acceptByEmployeur(c.getId());

        assertThrows(IllegalStateException.class, () -> candidatureService.acceptByEmployeur(c.getId()));
    }

    // ------------------- ACCEPT BY STUDENT -------------------
    @Test
    void acceptByStudent_ShouldThrow_WhenNotOwnedByStudent() {
        CandidatureDto c = candidatureService.postuler(testStudent.getId(), testOffer.getId(), testCv.getId());
        candidatureService.acceptByEmployeur(c.getId());

        assertThrows(IllegalStateException.class, () -> candidatureService.acceptByStudent(c.getId(), 999L));
    }

    @Test
    void acceptByStudent_ShouldThrow_WhenNotAcceptedByEmployeur() {
        CandidatureDto c = candidatureService.postuler(testStudent.getId(), testOffer.getId(), testCv.getId());

        assertThrows(IllegalStateException.class, () -> candidatureService.acceptByStudent(c.getId(), testStudent.getId()));
    }

    // ------------------- REJECT BY STUDENT -------------------
    @Test
    void rejectByStudent_ShouldThrow_WhenNotOwnedByStudent() {
        CandidatureDto c = candidatureService.postuler(testStudent.getId(), testOffer.getId(), testCv.getId());
        candidatureService.acceptByEmployeur(c.getId());

        assertThrows(IllegalStateException.class, () -> candidatureService.rejectByStudent(c.getId(), 999L));
    }

    @Test
    void rejectByStudent_ShouldThrow_WhenStatusInvalid() {
        CandidatureDto c = candidatureService.postuler(testStudent.getId(), testOffer.getId(), testCv.getId());

        assertThrows(IllegalStateException.class, () -> candidatureService.rejectByStudent(c.getId(), testStudent.getId()));
    }

    // ------------------- REJECT BY EMPLOYEUR -------------------
    @Test
    void rejectByEmployeur_ShouldThrow_WhenAlreadyAccepted() {
        CandidatureDto c = candidatureService.postuler(testStudent.getId(), testOffer.getId(), testCv.getId());
        candidatureService.acceptByEmployeur(c.getId());
        candidatureService.acceptByStudent(c.getId(), testStudent.getId());

        assertThrows(IllegalStateException.class, () -> candidatureService.rejectByEmployeur(c.getId()));
    }

    @Test
    void rejectByEmployeur_ShouldThrow_WhenAlreadyRejected() {
        CandidatureDto c = candidatureService.postuler(testStudent.getId(), testOffer.getId(), testCv.getId());
        candidatureService.rejectByEmployeur(c.getId());

        assertThrows(IllegalStateException.class, () -> candidatureService.rejectByEmployeur(c.getId()));
    }
    // Add these inside your existing CandidatureServiceTest class

    // ------------------- GET CANDIDATURE -------------------
    @Test
    void getCandidatureById_ShouldReturnCandidature_WhenExists() {
        CandidatureDto c = candidatureService.postuler(testStudent.getId(), testOffer.getId(), testCv.getId());
        CandidatureDto fetched = candidatureService.getCandidatureById(c.getId());

        assertEquals(c.getId(), fetched.getId());
    }

    @Test
    void getCandidatureById_ShouldThrow_WhenNotExists() {
        assertThrows(RuntimeException.class, () -> candidatureService.getCandidatureById(999L));
    }

    // ------------------- GET CANDIDATURES BY OFFER -------------------
    @Test
    void getCandidaturesByOffer_ShouldReturnEmptyList_WhenNoApplications() {
        List<CandidatureEmployeurDto> list = candidatureService.getCandidaturesByOffer(999L);
        assertTrue(list.isEmpty());
    }

    @Test
    void getCandidaturesByOffer_ShouldReturnApplications_WhenExists() {
        CandidatureDto c = candidatureService.postuler(testStudent.getId(), testOffer.getId(), testCv.getId());
        List<CandidatureEmployeurDto> list = candidatureService.getCandidaturesByOffer(testOffer.getId());

        assertEquals(1, list.size());
        assertEquals(c.getId(), list.get(0).getId());
    }

    // ------------------- GET CANDIDATURES BY EMPLOYEUR -------------------
    @Test
    void getCandidaturesByEmployeur_ShouldReturnEmptyList_WhenNoApplications() {
        List<CandidatureEmployeurDto> list = candidatureService.getCandidaturesByEmployeur(999L);
        assertTrue(list.isEmpty());
    }

    @Test
    void getCandidaturesByEmployeur_ShouldReturnApplications_WhenExists() {
        CandidatureDto c = candidatureService.postuler(testStudent.getId(), testOffer.getId(), testCv.getId());
        List<CandidatureEmployeurDto> list = candidatureService.getCandidaturesByEmployeur(testOffer.getEmployeurId());

        assertEquals(1, list.size());
        assertEquals(c.getId(), list.get(0).getId());
    }

    // ------------------- REJECT BY STUDENT (HAPPY PATH) -------------------
    @Test
    void rejectByStudent_ShouldUpdateStatus_WhenValid() {
        CandidatureDto c = candidatureService.postuler(testStudent.getId(), testOffer.getId(), testCv.getId());
        candidatureService.acceptByEmployeur(c.getId());

        CandidatureDto rejected = candidatureService.rejectByStudent(c.getId(), testStudent.getId());
        assertEquals(Candidature.Status.REJECTED, rejected.getStatus());
    }

    // ------------------- REJECT BY EMPLOYEUR (HAPPY PATH) -------------------
    @Test
    void rejectByEmployeur_ShouldUpdateStatus_WhenValid() {
        CandidatureDto c = candidatureService.postuler(testStudent.getId(), testOffer.getId(), testCv.getId());

        CandidatureDto rejected = candidatureService.rejectByEmployeur(c.getId());
        assertEquals(Candidature.Status.REJECTED, rejected.getStatus());
    }

    // ------------------- MULTIPLE STUDENTS APPLY -------------------
    @Test
    void multipleStudentsApplying_ShouldWorkIndependently() {
        Student student2 = studentRepository.save(
                Student.builder()
                        .firstName("Bob")
                        .lastName("Doe")
                        .email("bob@test.com")
                        .password("password")
                        .studentNumber("654321")
                        .program("Computer Science")
                        .build()
        );

        CandidatureDto c1 = candidatureService.postuler(testStudent.getId(), testOffer.getId(), testCv.getId());
        Cv cv2 = cvRepository.save(Cv.builder().student(student2).status(Cv.Status.APPROVED).build());
        CandidatureDto c2 = candidatureService.postuler(student2.getId(), testOffer.getId(), cv2.getId());

        List<CandidatureDto> list = candidatureService.getCandidaturesByStudent(testStudent.getId());
        assertEquals(1, list.size());
        assertEquals(c1.getId(), list.get(0).getId());

        List<CandidatureDto> list2 = candidatureService.getCandidaturesByStudent(student2.getId());
        assertEquals(1, list2.size());
        assertEquals(c2.getId(), list2.get(0).getId());
    }

}
