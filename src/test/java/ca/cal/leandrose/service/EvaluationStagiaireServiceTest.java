package ca.cal.leandrose.service;

import ca.cal.leandrose.model.*;
import ca.cal.leandrose.repository.*;
import ca.cal.leandrose.service.dto.evaluation.CreatorTypeEvaluation;
import ca.cal.leandrose.service.dto.evaluation.employer.EvaluationEmployerInfoDto;
import ca.cal.leandrose.service.dto.evaluation.EvaluationStagiaireDto;
import ca.cal.leandrose.service.dto.evaluation.prof.EvaluationTeacherInfoDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

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
        // Setup common test data
        employeur = Employeur.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .companyName("Test Company")
                .build();
        professeur = Prof.builder()
                .id(1L)
                .firstName("Marie")
                .lastName("Beauchamp")
                .email("marie-claude.beauchamp@college.ca")
                .nameCollege("Collège Mainsonneuve")
                .address("3800 R. Sherbrooke E, Montréal, QC H1X 2A2")
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
                .gestionnaire(null) // or create a Gestionnaire if needed
                .missionsObjectifs("Développement d'applications Java, participation aux réunions d'équipe")
                .statut(EntenteStage.StatutEntente.VALIDEE) // or whatever status is appropriate
                .dateCreation(LocalDateTime.now())
                .dateModification(LocalDateTime.now())
                .cheminDocumentPDF("/path/to/entente.pdf")
                .dateSignatureEtudiant(LocalDateTime.now())
                .dateSignatureEmployeur(LocalDateTime.now())
                .dateSignatureGestionnaire(LocalDateTime.now())
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
                .build();
    }

    // Test for createEvaluation method
    @Test
    void createEvaluationByEmployer_Success() {
        // Given
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
        // When
        EvaluationStagiaireDto result = evaluationStagiaireService.createEvaluationByEmployer(1L, 2L, 3L);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals(1L, result.employeurId());
        assertEquals(2L, result.studentId());
        assertEquals(3L, result.internshipOfferId());
        assertFalse(result.submittedByEmployer());

        verify(employeurRepository).findById(1L);
        verify(studentRepository).findById(2L);
        verify(internshipOfferRepository).findById(3L);
        verify(evaluationStagiaireRepository).save(any(EvaluationStagiaire.class));
    }

    @Test
    void createEvaluationByProf_Success() {
        // Given
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
        // When
        EvaluationStagiaireDto result = evaluationStagiaireService.createEvaluationByProf(1L, 2L, 3L);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals(1L, result.professeurId());
        assertEquals(2L, result.studentId());
        assertEquals(3L, result.internshipOfferId());
        assertFalse(result.submittedByProfessor());

        verify(profRepository).findById(1L);
        verify(studentRepository).findById(2L);
        verify(internshipOfferRepository).findById(3L);
        verify(evaluationStagiaireRepository).save(any(EvaluationStagiaire.class));
    }

    @Test
    void createEvaluation_EvaluationAlreadyExists_ThrowsException() {
        // Given
        EvaluationStagiaire existingEvaluation = EvaluationStagiaire.builder()
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
                .thenReturn(Optional.of(existingEvaluation));

        // When & Then
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> evaluationStagiaireService.createEvaluationByEmployer(1L, 2L, 3L));

        assertEquals("L'évaluation complète existe déjà pour ce stagiaire.", exception.getMessage());

        verify(ententeStageRepository).findByCandidature_Student_IdAndCandidature_InternshipOffer_Id(2L, 3L);
        verify(studentRepository).findById(2L);
        verify(internshipOfferRepository).findById(3L);
        verify(evaluationStagiaireRepository).findByStudentIdAndInternshipOfferId(2L, 3L);
        verify(evaluationStagiaireRepository, never()).save(any(EvaluationStagiaire.class));
    }

    @Test
    void createEvaluation_EmployeurNotFound_ThrowsException() {
        // Given
        when(ententeStageRepository.findByCandidature_Student_IdAndCandidature_InternshipOffer_Id(2L, 3L))
                .thenReturn(Optional.of(ententeStage));
        when(studentRepository.findById(2L)).thenReturn(Optional.of(student));
        when(internshipOfferRepository.findById(3L)).thenReturn(Optional.of(internshipOffer));
        when(employeurRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> evaluationStagiaireService.createEvaluationByEmployer(1L, 2L, 3L));

        assertEquals("Employeur non trouvé", exception.getMessage());

        verify(ententeStageRepository).findByCandidature_Student_IdAndCandidature_InternshipOffer_Id(2L, 3L);
        verify(studentRepository).findById(2L);
        verify(internshipOfferRepository).findById(3L);
        verify(employeurRepository).findById(1L);
        verify(evaluationStagiaireRepository, never()).save(any(EvaluationStagiaire.class));
    }
    @Test
    void createEvaluation_ProfNotFound_ThrowsException() {
        // Given
        when(ententeStageRepository.findByCandidature_Student_IdAndCandidature_InternshipOffer_Id(2L, 3L))
                .thenReturn(Optional.of(ententeStage));
        when(studentRepository.findById(2L)).thenReturn(Optional.of(student));
        when(internshipOfferRepository.findById(3L)).thenReturn(Optional.of(internshipOffer));
        when(profRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> evaluationStagiaireService.createEvaluationByProf(1L, 2L, 3L));

        assertEquals("Professeur non trouvé", exception.getMessage());

        verify(ententeStageRepository).findByCandidature_Student_IdAndCandidature_InternshipOffer_Id(2L, 3L);
        verify(studentRepository).findById(2L);
        verify(internshipOfferRepository).findById(3L);
        verify(profRepository).findById(1L);
        verify(evaluationStagiaireRepository, never()).save(any(EvaluationStagiaire.class));
    }

    @Test
    void createEvaluation_StudentNotFound_ThrowsException() {
        // Given
        when(studentRepository.findById(2L)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> evaluationStagiaireService.createEvaluationByEmployer(1L, 2L, 3L));

        assertEquals("Étudiant non trouvé", exception.getMessage());
        verify(studentRepository).findById(2L);
        verify(internshipOfferRepository, never()).findById(anyLong());
    }

    @Test
    void createEvaluation_InternshipOfferNotFound_ThrowsException() {
        // Given
        when(studentRepository.findById(2L)).thenReturn(Optional.of(student));
        when(internshipOfferRepository.findById(3L)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> evaluationStagiaireService.createEvaluationByEmployer(1L, 2L, 3L));

        assertEquals("Offre de stage non trouvée", exception.getMessage());
        verify(internshipOfferRepository).findById(3L);
    }

    @Test
    void getEvaluationInfo_ForEmployer_Success() {
        // Given
        EntenteStage ententeStage = EntenteStage.builder()
                .id(1L)
                .statut(EntenteStage.StatutEntente.VALIDEE)
                .candidature(candidature)
                .build();

        when(ententeStageRepository.findByCandidature_Student_IdAndCandidature_InternshipOffer_IdAndStatut(2L, 3L, EntenteStage.StatutEntente.VALIDEE))
                .thenReturn(Optional.of(ententeStage));

        EvaluationStagiaire eval = new EvaluationStagiaire();
        eval.setSubmittedByEmployer(false);

        when(evaluationStagiaireRepository.findByStudentIdAndInternshipOfferId(2L, 3L))
                .thenReturn(Optional.of(eval));

        when(studentRepository.findById(2L)).thenReturn(Optional.of(student));
        when(internshipOfferRepository.findById(3L)).thenReturn(Optional.of(internshipOffer));

        // When
        EvaluationEmployerInfoDto result = evaluationStagiaireService.getEvaluationInfoForEmployer(1L, 2L, 3L);

        // Then
        assertNotNull(result);
        assertNotNull(result.studentInfo());
        assertNotNull(result.internshipInfo());
        assertEquals("Alice", result.studentInfo().firstName());
        assertEquals("Smith", result.studentInfo().lastName());
        assertEquals("Computer Science", result.studentInfo().program());
        assertEquals("Java Developer Internship", result.internshipInfo().description());
        assertEquals("Test Company", result.internshipInfo().companyName());

        verify(ententeStageRepository).findByCandidature_Student_IdAndCandidature_InternshipOffer_IdAndStatut(2L, 3L, EntenteStage.StatutEntente.VALIDEE);
        verify(studentRepository).findById(2L);
        verify(evaluationStagiaireRepository).findByStudentIdAndInternshipOfferId(2L, 3L);
        verify(internshipOfferRepository).findById(3L);
    }
    @Test
    void getEvaluationInfo_ForProf_Success() {
        // Given
        EntenteStage ententeStage = EntenteStage.builder()
                .id(1L)
                .statut(EntenteStage.StatutEntente.VALIDEE)
                .candidature(candidature)
                .prof(professeur)
                .build();

        when(ententeStageRepository.findByProf_IdAndCandidature_Student_IdAndCandidature_InternshipOffer_Id(
                1L, 2L, 3L))
                .thenReturn(Optional.of(ententeStage));

        EvaluationStagiaire eval = new EvaluationStagiaire();
        eval.setSubmittedByEmployer(false);
        eval.setSubmittedByProfessor(false);

        when(evaluationStagiaireRepository.findByStudentIdAndInternshipOfferId(2L, 3L))
                .thenReturn(Optional.of(eval));

        // When
        EvaluationTeacherInfoDto result = evaluationStagiaireService.getEvaluationInfoForTeacher(1L, 2L, 3L);

        // Then
        assertNotNull(result);
        assertNotNull(result.entrepriseTeacherDto());
        assertNotNull(result.studentTeacherDto());
        assertEquals("Alice Smith", result.studentTeacherDto().fullname());
        assertEquals("Test Company", result.entrepriseTeacherDto().companyName());
        assertEquals("John Doe", result.entrepriseTeacherDto().contactName());


        verify(ententeStageRepository).findByProf_IdAndCandidature_Student_IdAndCandidature_InternshipOffer_Id(1L, 2L, 3L);
        verify(evaluationStagiaireRepository).findByStudentIdAndInternshipOfferId(2L, 3L);
    }

    @Test
    void getEvaluationInfo_ForEmployer_NotEligible_ThrowsException() {
        // Given
        when(ententeStageRepository.findByCandidature_Student_IdAndCandidature_InternshipOffer_IdAndStatut(2L, 3L, EntenteStage.StatutEntente.VALIDEE))
                .thenReturn(Optional.empty());

        // When & Then
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> evaluationStagiaireService.getEvaluationInfoForEmployer(1L, 2L, 3L));

        assertEquals("Evaluation not allowed - agreement not validated or not found", exception.getMessage());
        verify(ententeStageRepository).findByCandidature_Student_IdAndCandidature_InternshipOffer_IdAndStatut(2L, 3L, EntenteStage.StatutEntente.VALIDEE);
        verify(evaluationStagiaireRepository, never()).existsByInternshipOfferIdAndStudentId(anyLong(), anyLong());
    }
    @Test
    void getEvaluationInfo_ForProf_NotEligible_ThrowsException() {
        // Given
        when(ententeStageRepository.findByProf_IdAndCandidature_Student_IdAndCandidature_InternshipOffer_Id(1L, 2L, 3L))
                .thenReturn(Optional.empty());

        // When & Then
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> evaluationStagiaireService.getEvaluationInfoForTeacher(1L, 2L, 3L));

        assertEquals("Aucune entente de stage associée à ce professeur pour cet étudiant et ce stage.", exception.getMessage());

        verify(ententeStageRepository).findByProf_IdAndCandidature_Student_IdAndCandidature_InternshipOffer_Id(1L, 2L, 3L);
        verify(evaluationStagiaireRepository, never()).findByStudentIdAndInternshipOfferId(anyLong(), anyLong());
    }

    @Test
    void getEvaluationInfo_EvaluationAlreadyExists_ThrowsExceptionForEmployer() {
        //Given
        EntenteStage ententeStage = EntenteStage.builder()
                .id(1L)
                .statut(EntenteStage.StatutEntente.VALIDEE)
                .candidature(candidature)
                .build();

        doReturn(true)
                .when(evaluationStagiaireService)
                .isEvaluationEligible(CreatorTypeEvaluation.EMPLOYER, 1L, 2L, 3L);

        EvaluationStagiaire existing = new EvaluationStagiaire();
        existing.setSubmittedByEmployer(true);

        when(evaluationStagiaireRepository.findByStudentIdAndInternshipOfferId(2L, 3L))
                .thenReturn(Optional.of(existing));

        // When + Then
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> evaluationStagiaireService.getEvaluationInfoForEmployer(1L, 2L, 3L));

        assertEquals("L'employeur a déjà complété cette formulaire.", ex.getMessage());

        verify(studentRepository, never()).findById(any());
    }

    @Test
    void getEvaluationInfo_EvaluationAlreadyExists_ThrowsExceptionForProf() {
        //Given
        EntenteStage ententeStage = EntenteStage.builder()
                .id(1L)
                .statut(EntenteStage.StatutEntente.VALIDEE)
                .candidature(candidature)
                .prof(professeur)
                .build();

        when(ententeStageRepository.findByProf_IdAndCandidature_Student_IdAndCandidature_InternshipOffer_Id(1L, 2L, 3L))
                .thenReturn(Optional.of(ententeStage));

        EvaluationStagiaire existing = new EvaluationStagiaire();
        existing.setSubmittedByProfessor(true);

        when(evaluationStagiaireRepository.findByStudentIdAndInternshipOfferId(2L, 3L))
                .thenReturn(Optional.of(existing));

        // When + Then
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> evaluationStagiaireService.getEvaluationInfoForTeacher(1L, 2L, 3L));

        assertEquals("le profeseur a déjà complété cette formulaire", ex.getMessage());

        verify(studentRepository, never()).findById(any());
    }

    @Test
    void getEvaluationInfo_ForEmployer_StudentNotFound_ThrowsException() {
        // Given
        EntenteStage ententeStage = EntenteStage.builder()
                .id(1L)
                .statut(EntenteStage.StatutEntente.VALIDEE)
                .candidature(candidature)
                .build();

        when(ententeStageRepository.findByCandidature_Student_IdAndCandidature_InternshipOffer_IdAndStatut(2L, 3L, EntenteStage.StatutEntente.VALIDEE))
                .thenReturn(Optional.of(ententeStage));
        when(studentRepository.findById(2L)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> evaluationStagiaireService.getEvaluationInfoForEmployer(1L, 2L, 3L));

        assertEquals("Étudiant non trouvé", exception.getMessage());
        verify(studentRepository).findById(2L);
    }
    @Test
    void getEvaluationInfo_ForProf_NoEntenteStage_ThrowsException() {
        // Given
        when(ententeStageRepository.findByProf_IdAndCandidature_Student_IdAndCandidature_InternshipOffer_Id(1L, 2L, 3L))
                .thenReturn(Optional.empty());

        // When & Then
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> evaluationStagiaireService.getEvaluationInfoForTeacher(1L, 2L, 3L));

        assertEquals("Aucune entente de stage associée à ce professeur pour cet étudiant et ce stage.", exception.getMessage());
    }

    // Test for getEvaluationById method
    @Test
    void getEvaluationById_Success() {
        // Given
        when(evaluationStagiaireRepository.findById(1L)).thenReturn(Optional.of(evaluationStagiaire));

        // When
        EvaluationStagiaireDto result = evaluationStagiaireService.getEvaluationById(1L);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals(1L, result.employeurId());
        assertEquals(2L, result.studentId());
        assertEquals(3L, result.internshipOfferId());

        verify(evaluationStagiaireRepository).findById(1L);
    }

    @Test
    void getEvaluationById_NotFound_ThrowsException() {
        // Given
        when(evaluationStagiaireRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> evaluationStagiaireService.getEvaluationById(1L));

        assertEquals("Évaluation non trouvée", exception.getMessage());
        verify(evaluationStagiaireRepository).findById(1L);
    }

    // Test for getEvaluationsByEmployeur method
    @Test
    void getEvaluationsByEmployeur_Success() {
        // Given
        EvaluationStagiaire evaluation2 = EvaluationStagiaire.builder()
                .id(2L)
                .dateEvaluation(LocalDate.now())
                .employeur(employeur)
                .student(student)
                .professeur(professeur)
                .internshipOffer(internshipOffer)
                .submittedByEmployer(true)
                .build();

        when(evaluationStagiaireRepository.findByEmployeurId(1L))
                .thenReturn(List.of(evaluationStagiaire, evaluation2));

        // When
        List<EvaluationStagiaireDto> result = evaluationStagiaireService.getEvaluationsByEmployeur(1L);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());

        EvaluationStagiaireDto firstResult = result.get(0);
        assertEquals(1L, firstResult.id());
        assertEquals(1L, firstResult.employeurId());
        assertFalse(firstResult.submittedByEmployer());

        EvaluationStagiaireDto secondResult = result.get(1);
        assertEquals(2L, secondResult.id());
        assertTrue(secondResult.submittedByEmployer());

        verify(evaluationStagiaireRepository).findByEmployeurId(1L);
    }
    @Test
    void getEvaluationsByProf_Success() {
        // Given
        EvaluationStagiaire evaluation2 = EvaluationStagiaire.builder()
                .id(2L)
                .dateEvaluation(LocalDate.now())
                .employeur(employeur)
                .student(student)
                .professeur(professeur)
                .internshipOffer(internshipOffer)
                .submittedByEmployer(true)
                .build();

        when(evaluationStagiaireRepository.findByProfesseurId(1L))
                .thenReturn(List.of(evaluationStagiaire, evaluation2));

        // When
        List<EvaluationStagiaireDto> result = evaluationStagiaireService.getEvaluationsByProfesseur(1L);
        System.out.println("Resultat: " + result);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());

        EvaluationStagiaireDto firstResult = result.get(0);
        assertEquals(1L, firstResult.id());
        assertEquals(1L, firstResult.employeurId());
        assertFalse(firstResult.submittedByEmployer());

        EvaluationStagiaireDto secondResult = result.get(1);
        assertEquals(2L, secondResult.id());
        assertTrue(secondResult.submittedByEmployer());

        verify(evaluationStagiaireRepository).findByProfesseurId(1L);
    }

    @Test
    void getEvaluationsByEmployeur_EmptyList() {
        // Given
        when(evaluationStagiaireRepository.findByEmployeurId(1L)).thenReturn(List.of());

        // When
        List<EvaluationStagiaireDto> result = evaluationStagiaireService.getEvaluationsByEmployeur(1L);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(evaluationStagiaireRepository).findByEmployeurId(1L);
    }
    @Test
    void getEvaluationsByProf_EmptyList() {
        // Given
        when(evaluationStagiaireRepository.findByProfesseurId(1L)).thenReturn(List.of());

        // When
        List<EvaluationStagiaireDto> result = evaluationStagiaireService.getEvaluationsByProfesseur(1L);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(evaluationStagiaireRepository).findByProfesseurId(1L);
    }
}