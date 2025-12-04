package ca.cal.leandrose.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import ca.cal.leandrose.model.*;
import ca.cal.leandrose.repository.*;
import ca.cal.leandrose.service.dto.evaluation.*;
import ca.cal.leandrose.service.dto.evaluation.employer.EmployerQuestionResponse;
import ca.cal.leandrose.service.dto.evaluation.employer.EvaluationEmployerFormData;
import ca.cal.leandrose.service.dto.evaluation.employer.EvaluationEmployerInfoDto;
import ca.cal.leandrose.service.dto.evaluation.prof.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EvaluationStagiaireServiceTest {

    @Mock
    private EvaluationStagiaireRepository evaluationStagiaireRepository;

    @Mock
    private EmployeurRepository employeurRepository;

    @Mock
    private ProfRepository profRepository;

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private InternshipOfferRepository internshipOfferRepository;

    @Mock
    private EntenteStageRepository ententeStageRepository;

    @Mock
    private PDFGeneratorService pdfGeneratorService;

    @InjectMocks
    @Spy
    private EvaluationStagiaireService evaluationStagiaireService;

    private Employeur employeur;
    private Student student;
    private InternshipOffer internshipOffer;
    private EvaluationStagiaire evaluationStagiaire;
    private Candidature candidature;
    private Prof professeur;
    private EntenteStage ententeStage;

    @BeforeEach
    void setUp() {
        employeur = Employeur.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@company.com")
                .companyName("Test Company")
                .build();

        professeur = Prof.builder()
                .id(1L)
                .firstName("Marie")
                .lastName("Beauchamp")
                .email("marie-claude.beauchamp@college.ca")
                .nameCollege("Collège Mainsonneuve")
                .address("3800 R. Sherbrooke E, Montréal, QC H1X 2A2")
                .fax_machine("514-555-9999")
                .build();

        student = Student.builder()
                .id(2L)
                .firstName("Alice")
                .lastName("Smith")
                .program("Computer Science")
                .build();

        internshipOffer = InternshipOffer.builder()
                .id(3L)
                .description("Java Developer Internship")
                .address("123 Test St")
                .startDate(LocalDate.now())
                .durationInWeeks(12)
                .employeur(employeur)
                .build();

        candidature = Candidature.builder()
                .id(1L)
                .student(student)
                .internshipOffer(internshipOffer)
                .build();

        ententeStage = EntenteStage.builder()
                .id(1L)
                .candidature(candidature)
                .prof(professeur)
                .missionsObjectifs("Développement d'applications Java")
                .statut(EntenteStage.StatutEntente.VALIDEE)
                .dateCreation(LocalDateTime.now())
                .build();

        evaluationStagiaire = EvaluationStagiaire.builder()
                .id(1L)
                .dateEvaluation(LocalDate.now())
                .employeur(employeur)
                .student(student)
                .professeur(professeur)
                .internshipOffer(internshipOffer)
                .ententeStage(ententeStage)
                .submittedByEmployer(false)
                .submittedByProfessor(false)
                .status(EvaluationStatus.EN_COURS)
                .build();
    }

    

    @Test
    void createEvaluationByEmployer_Success() {
        when(ententeStageRepository.findByCandidature_Student_IdAndCandidature_InternshipOffer_Id(2L, 3L))
                .thenReturn(Optional.of(ententeStage));
        when(studentRepository.findById(2L)).thenReturn(Optional.of(student));
        when(internshipOfferRepository.findById(3L)).thenReturn(Optional.of(internshipOffer));
        when(employeurRepository.findById(1L)).thenReturn(Optional.of(employeur));
        when(evaluationStagiaireRepository.findByStudentIdAndInternshipOfferId(2L, 3L))
                .thenReturn(Optional.empty());
        when(evaluationStagiaireRepository.save(any(EvaluationStagiaire.class)))
                .thenAnswer(invocation -> {
                    EvaluationStagiaire evaluation = invocation.getArgument(0);
                    evaluation.setId(1L);
                    return evaluation;
                });

        EvaluationStagiaireDto result = evaluationStagiaireService.createEvaluationByEmployer(1L, 2L, 3L);

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals(1L, result.employeurId());
        assertEquals(2L, result.studentId());
        assertEquals(3L, result.internshipOfferId());
        assertFalse(result.submittedByEmployer());

        verify(employeurRepository).findById(1L);
        verify(evaluationStagiaireRepository).save(any(EvaluationStagiaire.class));
    }

    @Test
    void createEvaluationByProf_Success() {
        when(ententeStageRepository.findByCandidature_Student_IdAndCandidature_InternshipOffer_Id(2L, 3L))
                .thenReturn(Optional.of(ententeStage));
        when(studentRepository.findById(2L)).thenReturn(Optional.of(student));
        when(internshipOfferRepository.findById(3L)).thenReturn(Optional.of(internshipOffer));
        when(profRepository.findById(1L)).thenReturn(Optional.of(professeur));
        when(evaluationStagiaireRepository.findByStudentIdAndInternshipOfferId(2L, 3L))
                .thenReturn(Optional.empty());
        when(evaluationStagiaireRepository.save(any(EvaluationStagiaire.class)))
                .thenAnswer(invocation -> {
                    EvaluationStagiaire evaluation = invocation.getArgument(0);
                    evaluation.setId(1L);
                    return evaluation;
                });

        EvaluationStagiaireDto result = evaluationStagiaireService.createEvaluationByProf(1L, 2L, 3L);

        assertNotNull(result);
        assertEquals(1L, result.professeurId());
        assertFalse(result.submittedByProfessor());

        verify(profRepository).findById(1L);
    }

    @Test
    void createEvaluationByEmployer_ExistingIncompleteEvaluation_ReturnsExisting() {
        EvaluationStagiaire existing = EvaluationStagiaire.builder()
                .id(5L)
                .student(student)
                .internshipOffer(internshipOffer)
                .employeur(employeur)
                .professeur(professeur)
                .submittedByEmployer(false)
                .submittedByProfessor(false)
                .build();

        when(ententeStageRepository.findByCandidature_Student_IdAndCandidature_InternshipOffer_Id(2L, 3L))
                .thenReturn(Optional.of(ententeStage));
        when(studentRepository.findById(2L)).thenReturn(Optional.of(student));
        when(internshipOfferRepository.findById(3L)).thenReturn(Optional.of(internshipOffer));
        when(evaluationStagiaireRepository.findByStudentIdAndInternshipOfferId(2L, 3L))
                .thenReturn(Optional.of(existing));

        EvaluationStagiaireDto result = evaluationStagiaireService.createEvaluationByEmployer(1L, 2L, 3L);

        assertNotNull(result);
        assertEquals(5L, result.id());
        verify(evaluationStagiaireRepository, never()).save(any());
    }

    @Test
    void createEvaluationByProf_ExistingIncompleteEvaluation_ReturnsExisting() {
        EvaluationStagiaire existing = EvaluationStagiaire.builder()
                .id(5L)
                .student(student)
                .internshipOffer(internshipOffer)
                .employeur(employeur)
                .professeur(professeur)
                .submittedByEmployer(true)
                .submittedByProfessor(false)
                .build();

        when(ententeStageRepository.findByCandidature_Student_IdAndCandidature_InternshipOffer_Id(2L, 3L))
                .thenReturn(Optional.of(ententeStage));
        when(studentRepository.findById(2L)).thenReturn(Optional.of(student));
        when(internshipOfferRepository.findById(3L)).thenReturn(Optional.of(internshipOffer));
        when(evaluationStagiaireRepository.findByStudentIdAndInternshipOfferId(2L, 3L))
                .thenReturn(Optional.of(existing));

        EvaluationStagiaireDto result = evaluationStagiaireService.createEvaluationByProf(1L, 2L, 3L);

        assertNotNull(result);
        assertEquals(5L, result.id());
        verify(evaluationStagiaireRepository, never()).save(any());
    }

    @Test
    void createEvaluationByEmployer_AlreadyComplete_ThrowsException() {
        EvaluationStagiaire existing = EvaluationStagiaire.builder()
                .id(1L)
                .student(student)
                .internshipOffer(internshipOffer)
                .employeur(employeur)
                .professeur(professeur)
                .submittedByEmployer(true)
                .submittedByProfessor(false)
                .build();

        when(ententeStageRepository.findByCandidature_Student_IdAndCandidature_InternshipOffer_Id(2L, 3L))
                .thenReturn(Optional.of(ententeStage));
        when(studentRepository.findById(2L)).thenReturn(Optional.of(student));
        when(internshipOfferRepository.findById(3L)).thenReturn(Optional.of(internshipOffer));
        when(evaluationStagiaireRepository.findByStudentIdAndInternshipOfferId(2L, 3L))
                .thenReturn(Optional.of(existing));

        assertThrows(IllegalStateException.class,
                () -> evaluationStagiaireService.createEvaluationByEmployer(1L, 2L, 3L));
    }

    @Test
    void createEvaluationByProf_AlreadyComplete_ThrowsException() {
        EvaluationStagiaire existing = EvaluationStagiaire.builder()
                .id(1L)
                .student(student)
                .internshipOffer(internshipOffer)
                .employeur(employeur)
                .professeur(professeur)
                .submittedByEmployer(false)
                .submittedByProfessor(true)
                .build();

        when(ententeStageRepository.findByCandidature_Student_IdAndCandidature_InternshipOffer_Id(2L, 3L))
                .thenReturn(Optional.of(ententeStage));
        when(studentRepository.findById(2L)).thenReturn(Optional.of(student));
        when(internshipOfferRepository.findById(3L)).thenReturn(Optional.of(internshipOffer));
        when(evaluationStagiaireRepository.findByStudentIdAndInternshipOfferId(2L, 3L))
                .thenReturn(Optional.of(existing));

        assertThrows(IllegalStateException.class,
                () -> evaluationStagiaireService.createEvaluationByProf(1L, 2L, 3L));
    }

    @Test
    void createEvaluation_StudentNotFound_ThrowsException() {
        when(studentRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> evaluationStagiaireService.createEvaluationByEmployer(1L, 2L, 3L));
    }

    @Test
    void createEvaluation_InternshipOfferNotFound_ThrowsException() {
        when(studentRepository.findById(2L)).thenReturn(Optional.of(student));
        when(internshipOfferRepository.findById(3L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> evaluationStagiaireService.createEvaluationByEmployer(1L, 2L, 3L));
    }

    @Test
    void createEvaluation_EntenteStageNotFound_ThrowsException() {
        when(studentRepository.findById(2L)).thenReturn(Optional.of(student));
        when(internshipOfferRepository.findById(3L)).thenReturn(Optional.of(internshipOffer));
        when(ententeStageRepository.findByCandidature_Student_IdAndCandidature_InternshipOffer_Id(2L, 3L))
                .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> evaluationStagiaireService.createEvaluationByEmployer(1L, 2L, 3L));
    }

    @Test
    void createEvaluationByEmployer_EmployeurNotFound_ThrowsException() {
        when(ententeStageRepository.findByCandidature_Student_IdAndCandidature_InternshipOffer_Id(2L, 3L))
                .thenReturn(Optional.of(ententeStage));
        when(studentRepository.findById(2L)).thenReturn(Optional.of(student));
        when(internshipOfferRepository.findById(3L)).thenReturn(Optional.of(internshipOffer));
        when(employeurRepository.findById(1L)).thenReturn(Optional.empty());
        when(evaluationStagiaireRepository.findByStudentIdAndInternshipOfferId(2L, 3L))
                .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> evaluationStagiaireService.createEvaluationByEmployer(1L, 2L, 3L));
    }

    @Test
    void createEvaluationByProf_ProfNotFound_ThrowsException() {
        when(ententeStageRepository.findByCandidature_Student_IdAndCandidature_InternshipOffer_Id(2L, 3L))
                .thenReturn(Optional.of(ententeStage));
        when(studentRepository.findById(2L)).thenReturn(Optional.of(student));
        when(internshipOfferRepository.findById(3L)).thenReturn(Optional.of(internshipOffer));
        when(profRepository.findById(1L)).thenReturn(Optional.empty());
        when(evaluationStagiaireRepository.findByStudentIdAndInternshipOfferId(2L, 3L))
                .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> evaluationStagiaireService.createEvaluationByProf(1L, 2L, 3L));
    }

    

    @Test
    void getEvaluationInfoForEmployer_Success() {
        when(ententeStageRepository.findByCandidature_Student_IdAndCandidature_InternshipOffer_IdAndStatut(
                2L, 3L, EntenteStage.StatutEntente.VALIDEE))
                .thenReturn(Optional.of(ententeStage));
        when(evaluationStagiaireRepository.findByStudentIdAndInternshipOfferId(2L, 3L))
                .thenReturn(Optional.empty());
        when(studentRepository.findById(2L)).thenReturn(Optional.of(student));
        when(internshipOfferRepository.findById(3L)).thenReturn(Optional.of(internshipOffer));

        EvaluationEmployerInfoDto result = evaluationStagiaireService.getEvaluationInfoForEmployer(1L, 2L, 3L);

        assertNotNull(result);
        assertEquals("Alice", result.studentInfo().firstName());
        assertEquals("Java Developer Internship", result.internshipInfo().description());
    }

    @Test
    void getEvaluationInfoForEmployer_NotEligible_ThrowsException() {
        when(ententeStageRepository.findByCandidature_Student_IdAndCandidature_InternshipOffer_IdAndStatut(
                2L, 3L, EntenteStage.StatutEntente.VALIDEE))
                .thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class,
                () -> evaluationStagiaireService.getEvaluationInfoForEmployer(1L, 2L, 3L));
    }

    @Test
    void getEvaluationInfoForEmployer_AlreadySubmitted_ThrowsException() {
        EvaluationStagiaire submitted = EvaluationStagiaire.builder()
                .submittedByEmployer(true)
                .build();

        doReturn(true).when(evaluationStagiaireService)
                .isEvaluationEligible(CreatorTypeEvaluation.EMPLOYER, 1L, 2L, 3L);
        when(evaluationStagiaireRepository.findByStudentIdAndInternshipOfferId(2L, 3L))
                .thenReturn(Optional.of(submitted));

        assertThrows(IllegalStateException.class,
                () -> evaluationStagiaireService.getEvaluationInfoForEmployer(1L, 2L, 3L));
    }

    @Test
    void getEvaluationInfoForTeacher_Success() {
        when(ententeStageRepository.findByProf_IdAndCandidature_Student_IdAndCandidature_InternshipOffer_Id(
                1L, 2L, 3L))
                .thenReturn(Optional.of(ententeStage));
        when(evaluationStagiaireRepository.findByStudentIdAndInternshipOfferId(2L, 3L))
                .thenReturn(Optional.empty());

        EvaluationTeacherInfoDto result = evaluationStagiaireService.getEvaluationInfoForTeacher(1L, 2L, 3L);

        assertNotNull(result);
        assertEquals("Alice Smith", result.studentTeacherDto().fullname());
        assertEquals("Test Company", result.entrepriseTeacherDto().companyName());
    }

    @Test
    void getEvaluationInfoForTeacher_NoEntente_ThrowsException() {
        when(ententeStageRepository.findByProf_IdAndCandidature_Student_IdAndCandidature_InternshipOffer_Id(
                1L, 2L, 3L))
                .thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class,
                () -> evaluationStagiaireService.getEvaluationInfoForTeacher(1L, 2L, 3L));
    }

    @Test
    void getEvaluationInfoForTeacher_AlreadySubmitted_ThrowsException() {
        EvaluationStagiaire submitted = EvaluationStagiaire.builder()
                .submittedByProfessor(true)
                .build();

        when(ententeStageRepository.findByProf_IdAndCandidature_Student_IdAndCandidature_InternshipOffer_Id(
                1L, 2L, 3L))
                .thenReturn(Optional.of(ententeStage));
        when(evaluationStagiaireRepository.findByStudentIdAndInternshipOfferId(2L, 3L))
                .thenReturn(Optional.of(submitted));

        assertThrows(IllegalStateException.class,
                () -> evaluationStagiaireService.getEvaluationInfoForTeacher(1L, 2L, 3L));
    }

    @Test
    void getEvaluationInfoForTeacher_NoEmployeur_ThrowsException() {
        EntenteStage ententeWithoutEmployeur = EntenteStage.builder()
                .id(1L)
                .candidature(candidature)
                .prof(professeur)
                .statut(EntenteStage.StatutEntente.VALIDEE)
                .build();

        InternshipOffer offerWithoutEmployeur = InternshipOffer.builder()
                .id(3L)
                .description("Test")
                .startDate(LocalDate.now())
                .durationInWeeks(12)
                .address("Test Address")
                .employeur(null)
                .build();

        Candidature candidatureWithoutEmployeur = Candidature.builder()
                .id(1L)
                .student(student)
                .internshipOffer(offerWithoutEmployeur)
                .build();

        ententeWithoutEmployeur.setCandidature(candidatureWithoutEmployeur);

        when(ententeStageRepository.findByProf_IdAndCandidature_Student_IdAndCandidature_InternshipOffer_Id(
                1L, 2L, 3L))
                .thenReturn(Optional.of(ententeWithoutEmployeur));
        when(evaluationStagiaireRepository.findByStudentIdAndInternshipOfferId(2L, 3L))
                .thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class,
                () -> evaluationStagiaireService.getEvaluationInfoForTeacher(1L, 2L, 3L));
    }

    

    @Test
    void generateEvaluationPdfByEmployer_Success() {
        Map<String, List<EmployerQuestionResponse>> categories = createEmployerCategories();
        EvaluationEmployerFormData formData = new EvaluationEmployerFormData(
                categories, "Good work", 1, "Good", false, 15, "YES", true
        );

        when(evaluationStagiaireRepository.findById(1L)).thenReturn(Optional.of(evaluationStagiaire));
        when(ententeStageRepository.findByCandidature_Student_IdAndCandidature_InternshipOffer_Id(2L, 3L))
                .thenReturn(Optional.of(ententeStage));
        when(pdfGeneratorService.generatedEvaluationByEmployer(
                any(), any(), anyString(), anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn("/path/to/pdf");
        when(evaluationStagiaireRepository.save(any())).thenReturn(evaluationStagiaire);

        EvaluationStagiaireDto result = evaluationStagiaireService.generateEvaluationPdfByEmployer(
                1L, formData, "fr");

        assertNotNull(result);
        verify(pdfGeneratorService).generatedEvaluationByEmployer(
                any(), any(), eq("fr"), eq("Marie"), eq("Beauchamp"),
                eq("Collège Mainsonneuve"), anyString(), anyString());
        verify(evaluationStagiaireRepository).save(any());
    }

    @Test
    void generateEvaluationPdfByEmployer_EvaluationNotFound_ThrowsException() {
        Map<String, List<EmployerQuestionResponse>> categories = createEmployerCategories();
        EvaluationEmployerFormData formData = new EvaluationEmployerFormData(
                categories, null, 1, null, false, 15, "NO", false
        );

        when(evaluationStagiaireRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> evaluationStagiaireService.generateEvaluationPdfByEmployer(1L, formData, "fr"));
    }

    @Test
    void generateEvaluationByTeacher_Success() {
        Map<String, List<QuestionResponseTeacher>> categories = createTeacherCategories();
        EvaluationProfFormDto formData = new EvaluationProfFormDto(
                categories, 1, 2, false, false, new ArrayList<>()
        );

        when(evaluationStagiaireRepository.findById(1L)).thenReturn(Optional.of(evaluationStagiaire));
        when(pdfGeneratorService.generatedEvaluationByTeacher(any(), any(), any(), anyString()))
                .thenReturn("/path/to/pdf");
        when(evaluationStagiaireRepository.save(any())).thenReturn(evaluationStagiaire);

        EvaluationStagiaireDto result = evaluationStagiaireService.generateEvaluationByTeacher(
                1L, formData, "fr");

        assertNotNull(result);
        verify(pdfGeneratorService).generatedEvaluationByTeacher(any(), any(), any(), eq("fr"));
        verify(evaluationStagiaireRepository).save(any());
    }

    @Test
    void generateEvaluationByTeacher_EvaluationNotFound_ThrowsException() {
        Map<String, List<QuestionResponseTeacher>> categories = createTeacherCategories();
        EvaluationProfFormDto formData = new EvaluationProfFormDto(
                categories, 1, 2, false, false, new ArrayList<>()
        );

        when(evaluationStagiaireRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> evaluationStagiaireService.generateEvaluationByTeacher(1L, formData, "fr"));
    }

    

    @Test
    void getEvaluationPdf_Employer_Success() {
        evaluationStagiaire.setEmployerPdfFilePath("/path/to/employer.pdf");
        byte[] pdfContent = "PDF content".getBytes();

        when(evaluationStagiaireRepository.findById(1L)).thenReturn(Optional.of(evaluationStagiaire));
        when(pdfGeneratorService.lireFichierPDF("/path/to/employer.pdf")).thenReturn(pdfContent);

        byte[] result = evaluationStagiaireService.getEvaluationPdf(1L, CreatorTypeEvaluation.EMPLOYER);

        assertNotNull(result);
        assertArrayEquals(pdfContent, result);
    }

    @Test
    void getEvaluationPdf_Professor_Success() {
        evaluationStagiaire.setProfessorPdfFilePath("/path/to/professor.pdf");
        byte[] pdfContent = "PDF content".getBytes();

        when(evaluationStagiaireRepository.findById(1L)).thenReturn(Optional.of(evaluationStagiaire));
        when(pdfGeneratorService.lireFichierPDF("/path/to/professor.pdf")).thenReturn(pdfContent);

        byte[] result = evaluationStagiaireService.getEvaluationPdf(1L, CreatorTypeEvaluation.PROF);

        assertNotNull(result);
        assertArrayEquals(pdfContent, result);
    }

    @Test
    void getEvaluationPdf_NoPdfGenerated_ThrowsException() {
        evaluationStagiaire.setEmployerPdfFilePath(null);

        when(evaluationStagiaireRepository.findById(1L)).thenReturn(Optional.of(evaluationStagiaire));

        assertThrows(RuntimeException.class,
                () -> evaluationStagiaireService.getEvaluationPdf(1L, CreatorTypeEvaluation.EMPLOYER));
    }

    @Test
    void getEvaluationPdf_EvaluationNotFound_ThrowsException() {
        when(evaluationStagiaireRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> evaluationStagiaireService.getEvaluationPdf(1L, CreatorTypeEvaluation.EMPLOYER));
    }

    

    @Test
    void getEvaluationById_Success() {
        when(evaluationStagiaireRepository.findById(1L)).thenReturn(Optional.of(evaluationStagiaire));

        EvaluationStagiaireDto result = evaluationStagiaireService.getEvaluationById(1L);

        assertNotNull(result);
        assertEquals(1L, result.id());
    }

    @Test
    void getEvaluationById_NotFound_ThrowsException() {
        when(evaluationStagiaireRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> evaluationStagiaireService.getEvaluationById(1L));
    }

    

    @Test
    void getEvaluationsByEmployeur_Success() {
        EvaluationStagiaire eval2 = EvaluationStagiaire.builder()
                .id(2L)
                .dateEvaluation(LocalDate.now())
                .employeur(employeur)
                .student(student)
                .professeur(professeur)
                .internshipOffer(internshipOffer)
                .submittedByEmployer(true)
                .build();

        when(evaluationStagiaireRepository.findByEmployeurId(1L))
                .thenReturn(List.of(evaluationStagiaire, eval2));

        List<EvaluationStagiaireDto> result = evaluationStagiaireService.getEvaluationsByEmployeur(1L);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).id());
        assertEquals(2L, result.get(1).id());
    }

    @Test
    void getEvaluationsByEmployeur_EmptyList() {
        when(evaluationStagiaireRepository.findByEmployeurId(1L)).thenReturn(List.of());

        List<EvaluationStagiaireDto> result = evaluationStagiaireService.getEvaluationsByEmployeur(1L);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getEvaluationsByProfesseur_Success() {
        when(evaluationStagiaireRepository.findByProfesseurId(1L))
                .thenReturn(List.of(evaluationStagiaire));

        List<EvaluationStagiaireDto> result = evaluationStagiaireService.getEvaluationsByProfesseur(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void getEvaluationsByProfesseur_EmptyList() {
        when(evaluationStagiaireRepository.findByProfesseurId(1L)).thenReturn(List.of());

        List<EvaluationStagiaireDto> result = evaluationStagiaireService.getEvaluationsByProfesseur(1L);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    

    @Test
    void isEvaluationEligible_Employer_Valid_ReturnsTrue() {
        EntenteStage validEntente = EntenteStage.builder()
                .id(1L)
                .candidature(candidature)
                .statut(EntenteStage.StatutEntente.VALIDEE)
                .build();

        when(ententeStageRepository.findByCandidature_Student_IdAndCandidature_InternshipOffer_IdAndStatut(
                2L, 3L, EntenteStage.StatutEntente.VALIDEE))
                .thenReturn(Optional.of(validEntente));

        boolean result = evaluationStagiaireService.isEvaluationEligible(
                CreatorTypeEvaluation.EMPLOYER, 1L, 2L, 3L);

        assertTrue(result);
    }

    @Test
    void isEvaluationEligible_Employer_Invalid_ReturnsFalse() {
        when(ententeStageRepository.findByCandidature_Student_IdAndCandidature_InternshipOffer_IdAndStatut(
                2L, 3L, EntenteStage.StatutEntente.VALIDEE))
                .thenReturn(Optional.empty());

        boolean result = evaluationStagiaireService.isEvaluationEligible(
                CreatorTypeEvaluation.EMPLOYER, 1L, 2L, 3L);

        assertFalse(result);
    }

    @Test
    void isEvaluationEligible_Prof_Valid_ReturnsTrue() {
        when(ententeStageRepository.findByProf_IdAndCandidature_Student_IdAndCandidature_InternshipOffer_IdAndStatut(
                1L, 2L, 3L, EntenteStage.StatutEntente.VALIDEE))
                .thenReturn(Optional.of(ententeStage));

        boolean result = evaluationStagiaireService.isEvaluationEligible(
                CreatorTypeEvaluation.PROF, 1L, 2L, 3L);

        assertTrue(result);
    }

    @Test
    void isEvaluationEligible_Prof_Invalid_ReturnsFalse() {
        when(ententeStageRepository.findByProf_IdAndCandidature_Student_IdAndCandidature_InternshipOffer_IdAndStatut(
                1L, 2L, 3L, EntenteStage.StatutEntente.VALIDEE))
                .thenReturn(Optional.empty());

        boolean result = evaluationStagiaireService.isEvaluationEligible(
                CreatorTypeEvaluation.PROF, 1L, 2L, 3L);

        assertFalse(result);
    }

    

    @Test
    void getEligibleEvaluations_Employer_WithoutExistingEvaluation() {
        when(ententeStageRepository.findByCandidature_InternshipOffer_Employeur_IdAndStatut(
                1L, EntenteStage.StatutEntente.VALIDEE))
                .thenReturn(List.of(ententeStage));
        when(evaluationStagiaireRepository.findByStudentIdAndInternshipOfferId(2L, 3L))
                .thenReturn(Optional.empty());

        List<EligibleEvaluationDto> result = evaluationStagiaireService.getEligibleEvaluations(
                CreatorTypeEvaluation.EMPLOYER, 1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertFalse(result.get(0).hasEvaluation());
        assertNull(result.get(0).evaluationId());
    }

    @Test
    void getEligibleEvaluations_Employer_WithExistingEvaluation() {
        when(ententeStageRepository.findByCandidature_InternshipOffer_Employeur_IdAndStatut(
                1L, EntenteStage.StatutEntente.VALIDEE))
                .thenReturn(List.of(ententeStage));
        when(evaluationStagiaireRepository.findByStudentIdAndInternshipOfferId(2L, 3L))
                .thenReturn(Optional.of(evaluationStagiaire));

        List<EligibleEvaluationDto> result = evaluationStagiaireService.getEligibleEvaluations(
                CreatorTypeEvaluation.EMPLOYER, 1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.get(0).hasEvaluation());
        assertEquals(1L, result.get(0).evaluationId());
    }

    @Test
    void getEligibleEvaluations_Prof_WithSubmittedEvaluation() {
        evaluationStagiaire.setSubmittedByEmployer(true);

        when(ententeStageRepository.findByProf_IdAndStatut(1L, EntenteStage.StatutEntente.VALIDEE))
                .thenReturn(List.of(ententeStage));
        when(evaluationStagiaireRepository.findByStudentIdAndInternshipOfferId(2L, 3L))
                .thenReturn(Optional.of(evaluationStagiaire));

        List<EligibleEvaluationDto> result = evaluationStagiaireService.getEligibleEvaluations(
                CreatorTypeEvaluation.PROF, 1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.get(0).evaluationSubmitted());
    }

    @Test
    void getEligibleEvaluations_Prof_EmptyList() {
        when(ententeStageRepository.findByProf_IdAndStatut(1L, EntenteStage.StatutEntente.VALIDEE))
                .thenReturn(List.of());

        List<EligibleEvaluationDto> result = evaluationStagiaireService.getEligibleEvaluations(
                CreatorTypeEvaluation.PROF, 1L);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    

    @Test
    void getExistingEvaluation_Found() {
        when(evaluationStagiaireRepository.findByStudentIdAndInternshipOfferId(2L, 3L))
                .thenReturn(Optional.of(evaluationStagiaire));

        Optional<EvaluationStagiaireDto> result = evaluationStagiaireService.getExistingEvaluation(2L, 3L);

        assertTrue(result.isPresent());
        assertEquals(1L, result.get().id());
    }

    @Test
    void getExistingEvaluation_NotFound() {
        when(evaluationStagiaireRepository.findByStudentIdAndInternshipOfferId(2L, 3L))
                .thenReturn(Optional.empty());

        Optional<EvaluationStagiaireDto> result = evaluationStagiaireService.getExistingEvaluation(2L, 3L);

        assertFalse(result.isPresent());
    }

    

    private Map<String, List<EmployerQuestionResponse>> createEmployerCategories() {
        Map<String, List<EmployerQuestionResponse>> categories = new HashMap<>();
        categories.put("productivity", createEmployerResponses(5));
        categories.put("quality", createEmployerResponses(5));
        categories.put("relationships", createEmployerResponses(6));
        categories.put("skills", createEmployerResponses(6));
        return categories;
    }

    private List<EmployerQuestionResponse> createEmployerResponses(int count) {
        List<EmployerQuestionResponse> responses = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            responses.add(new EmployerQuestionResponse(null, false, "TRES_BIEN"));
        }
        return responses;
    }

    private Map<String, List<QuestionResponseTeacher>> createTeacherCategories() {
        Map<String, List<QuestionResponseTeacher>> categories = new HashMap<>();
        categories.put("conformity", createTeacherResponses(3));
        categories.put("environment", createTeacherResponses(2));
        categories.put("general", createTeacherResponses(5));
        return categories;
    }

    private List<QuestionResponseTeacher> createTeacherResponses(int count) {
        List<QuestionResponseTeacher> responses = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            responses.add(new QuestionResponseTeacher("TRES_BIEN"));
        }
        return responses;
    }
}