package ca.cal.leandrose.service;

import ca.cal.leandrose.model.*;
import ca.cal.leandrose.repository.CandidatureRepository;
import ca.cal.leandrose.repository.EntenteStageRepository;
import ca.cal.leandrose.service.dto.CandidatureDto;
import ca.cal.leandrose.service.dto.EntenteStageDto;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EntenteStageServiceTest {

    @Mock
    private EntenteStageRepository ententeRepository;

    @Mock
    private CandidatureRepository candidatureRepository;

    @Mock
    private PDFGeneratorService pdfGeneratorService;

    @InjectMocks
    private EntenteStageService ententeStageService;

    private Candidature candidature;
    private EntenteStage entente;
    private EntenteStageDto ententeDto;
    private Employeur employeur;
    private Student student;

    @BeforeEach
    void setUp() {
        // Setup Student
        student = Student.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@test.com")
                .password("password")
                .studentNumber("STU001")
                .program(Program.COMPUTER_SCIENCE.getTranslationKey())
                .build();

        // Setup Employeur
        employeur = Employeur.builder()
                .id(1L)
                .firstName("Jane")
                .lastName("Smith")
                .email("jane@company.com")
                .password("password")
                .companyName("TechCorp")
                .field("Software")
                .build();

        // Setup CV
        Cv cv = Cv.builder()
                .id(1L)
                .student(student)
                .pdfPath("/path/to/cv.pdf")
                .status(Cv.Status.APPROVED)
                .build();

        // Setup InternshipOffer
        InternshipOffer offer = InternshipOffer.builder()
                .id(1L)
                .description("Stage développement")
                .startDate(LocalDate.now())
                .durationInWeeks(12)
                .address("123 Rue Test")
                .status(InternshipOffer.Status.PUBLISHED)
                .employeur(employeur)
                .build();

        // Setup Candidature
        candidature = Candidature.builder()
                .id(1L)
                .student(student)
                .internshipOffer(offer)
                .cv(cv)
                .status(Candidature.Status.ACCEPTED)
                .applicationDate(LocalDateTime.now())
                .build();

        // Setup EntenteStage
        entente = EntenteStage.builder()
                .id(1L)
                .candidature(candidature)
                .missionsObjectifs("Développement web")
                .statut(EntenteStage.StatutEntente.BROUILLON)
                .dateCreation(LocalDateTime.now())
                .build();

        // Setup EntenteStageDto
        ententeDto = EntenteStageDto.builder()
                .candidatureId(1L)
                .dateDebut(LocalDate.of(2025, 6, 1))
                .duree(12)
                .lieu("Montreal")
                .remuneration(30f)
                .missionsObjectifs("Développement web")
                .build();
    }

    @Test
    void testGetCandidaturesAcceptees() {
        when(candidatureRepository.findByStatus(Candidature.Status.ACCEPTED))
                .thenReturn(Collections.singletonList(candidature));
        when(ententeRepository.existsByCandidatureId(1L)).thenReturn(false);

        List<CandidatureDto> result = ententeStageService.getCandidaturesAcceptees();

        assertEquals(1, result.size());
        verify(candidatureRepository).findByStatus(Candidature.Status.ACCEPTED);
        verify(ententeRepository).existsByCandidatureId(1L);
    }

    @Test
    void testGetCandidaturesAcceptees_FilterExistingEntentes() {
        when(candidatureRepository.findByStatus(Candidature.Status.ACCEPTED))
                .thenReturn(Collections.singletonList(candidature));
        when(ententeRepository.existsByCandidatureId(1L)).thenReturn(true);

        List<CandidatureDto> result = ententeStageService.getCandidaturesAcceptees();

        assertEquals(0, result.size());
    }

    @Test
    void testCreerEntente_Success() {
        when(candidatureRepository.findById(1L)).thenReturn(Optional.of(candidature));
        when(ententeRepository.existsByCandidatureId(1L)).thenReturn(false);
        when(ententeRepository.save(any(EntenteStage.class))).thenReturn(entente);
        when(pdfGeneratorService.genererEntentePDF(any(EntenteStage.class))).thenReturn("/path/to/pdf");

        EntenteStageDto result = ententeStageService.creerEntente(ententeDto);

        assertNotNull(result);
        assertEquals(EntenteStage.StatutEntente.EN_ATTENTE_SIGNATURE, result.getStatut());
        assertNotNull(result.getCheminDocumentPDF());
        assertEquals("/path/to/pdf", result.getCheminDocumentPDF());

        verify(candidatureRepository).findById(1L);
        verify(ententeRepository, times(2)).save(any(EntenteStage.class));
        verify(pdfGeneratorService).genererEntentePDF(any(EntenteStage.class));
    }

    @Test
    void testCreerEntente_CandidatureIdNull() {
        ententeDto.setCandidatureId(null);

        assertThrows(IllegalArgumentException.class, () -> ententeStageService.creerEntente(ententeDto));
    }

    @Test
    void testCreerEntente_CandidatureNotFound() {
        when(candidatureRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> ententeStageService.creerEntente(ententeDto));
    }

    @Test
    void testCreerEntente_CandidatureNotAccepted() {
        candidature.setStatus(Candidature.Status.PENDING);
        when(candidatureRepository.findById(1L)).thenReturn(Optional.of(candidature));

        assertThrows(IllegalStateException.class, () -> {
            ententeStageService.creerEntente(ententeDto);
        });
    }

    @Test
    void testCreerEntente_EntenteAlreadyExists() {
        when(candidatureRepository.findById(1L)).thenReturn(Optional.of(candidature));
        when(ententeRepository.existsByCandidatureId(1L)).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> ententeStageService.creerEntente(ententeDto));
    }

    @Test
    void testCreerEntente_MissionsObjectifsNull() {
        ententeDto.setMissionsObjectifs(null);
        when(candidatureRepository.findById(1L)).thenReturn(Optional.of(candidature));
        when(ententeRepository.existsByCandidatureId(1L)).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> {
            ententeStageService.creerEntente(ententeDto);
        });
    }

    @Test
    void testCreerEntente_RemunerationNegative() {
        ententeDto.setRemuneration(-100f);
        when(candidatureRepository.findById(1L)).thenReturn(Optional.of(candidature));
        when(ententeRepository.existsByCandidatureId(1L)).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> ententeStageService.creerEntente(ententeDto));
    }

    @Test
    void testValiderEtGenererEntente_Success() {
        when(ententeRepository.findById(1L)).thenReturn(Optional.of(entente));
        when(pdfGeneratorService.genererEntentePDF(entente)).thenReturn("/path/to/pdf");
        when(ententeRepository.save(any(EntenteStage.class))).thenReturn(entente);

        EntenteStageDto result = ententeStageService.validerEtGenererEntente(1L);

        assertNotNull(result);
        verify(pdfGeneratorService).genererEntentePDF(entente);
        verify(ententeRepository).save(any(EntenteStage.class));
    }

    @Test
    void testValiderEtGenererEntente_EntenteNotFound() {
        when(ententeRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> {
            ententeStageService.validerEtGenererEntente(1L);
        });
    }

    @Test
    void testModifierEntente_Success() {
        when(ententeRepository.findById(1L)).thenReturn(Optional.of(entente));
        when(ententeRepository.save(any(EntenteStage.class))).thenReturn(entente);

        EntenteStageDto modificationDto = EntenteStageDto.builder()
                .remuneration(350f)
                .build();

        EntenteStageDto result = ententeStageService.modifierEntente(1L, modificationDto);

        assertNotNull(result);
        verify(ententeRepository).save(any(EntenteStage.class));
    }

    @Test
    void testModifierEntente_NotBrouillon() {
        entente.setStatut(EntenteStage.StatutEntente.EN_ATTENTE_SIGNATURE);
        when(ententeRepository.findById(1L)).thenReturn(Optional.of(entente));

        EntenteStageDto modificationDto = EntenteStageDto.builder()
                .remuneration(350f)
                .build();

        assertThrows(IllegalStateException.class, () -> {
            ententeStageService.modifierEntente(1L, modificationDto);
        });
    }

    @Test
    void testGetAllEntentes() {
        when(ententeRepository.findAll()).thenReturn(Collections.singletonList(entente));

        List<EntenteStageDto> result = ententeStageService.getAllEntentes();

        assertEquals(1, result.size());
        verify(ententeRepository).findAll();
    }

    @Test
    void testGetEntenteById_Success() {
        when(ententeRepository.findById(1L)).thenReturn(Optional.of(entente));

        EntenteStageDto result = ententeStageService.getEntenteById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(ententeRepository).findById(1L);
    }

    @Test
    void testGetEntenteById_NotFound() {
        when(ententeRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> {
            ententeStageService.getEntenteById(1L);
        });
    }

    @Test
    void testTelechargerPDF_Success() {
        entente.setCheminDocumentPDF("/path/to/pdf");
        when(ententeRepository.findById(1L)).thenReturn(Optional.of(entente));
        when(pdfGeneratorService.lireFichierPDF("/path/to/pdf")).thenReturn(new byte[100]);

        byte[] result = ententeStageService.telechargerPDF(1L);

        assertNotNull(result);
        assertEquals(100, result.length);
        verify(pdfGeneratorService).lireFichierPDF("/path/to/pdf");
    }

    @Test
    void testTelechargerPDF_NoPdfGenerated() {
        entente.setCheminDocumentPDF(null);
        when(ententeRepository.findById(1L)).thenReturn(Optional.of(entente));

        assertThrows(IllegalStateException.class, () -> {
            ententeStageService.telechargerPDF(1L);
        });
    }

    @Test
    void testSupprimerEntente_Success() {
        when(ententeRepository.findById(1L)).thenReturn(Optional.of(entente));

        ententeStageService.supprimerEntente(1L);

        verify(ententeRepository).delete(entente);
    }

    @Test
    void testSupprimerEntente_NotBrouillon() {
        entente.setStatut(EntenteStage.StatutEntente.VALIDEE);
        when(ententeRepository.findById(1L)).thenReturn(Optional.of(entente));

        assertThrows(IllegalStateException.class, () -> {
            ententeStageService.supprimerEntente(1L);
        });
    }

    @Test
    void signerParEmployeur_Success() {
        entente.setStatut(EntenteStage.StatutEntente.EN_ATTENTE_SIGNATURE);
        when(ententeRepository.findById(1L)).thenReturn(Optional.of(entente));
        when(ententeRepository.save(any())).thenReturn(entente);

        EntenteStageDto result = ententeStageService.signerParEmployeur(1L, employeur.getId());

        assertNotNull(result);
        assertNotNull(result.getDateSignatureEmployeur());
        verify(ententeRepository).save(any(EntenteStage.class));
    }

    @Test
    void signerParEmployeur_EntenteNotFound() {
        when(ententeRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> ententeStageService.signerParEmployeur(1L, employeur.getId()));
    }

    @Test
    void signerParEmployeur_StatusInvalid_Throws() {
        entente.setStatut(EntenteStage.StatutEntente.BROUILLON);
        when(ententeRepository.findById(1L)).thenReturn(Optional.of(entente));
        assertThrows(IllegalStateException.class, () -> ententeStageService.signerParEmployeur(1L, employeur.getId()));
    }

    @Test
    void signerParEmployeur_WrongEmployeur_Throws() {
        entente.setStatut(EntenteStage.StatutEntente.EN_ATTENTE_SIGNATURE);
        when(ententeRepository.findById(1L)).thenReturn(Optional.of(entente));
        assertThrows(IllegalArgumentException.class, () -> ententeStageService.signerParEmployeur(1L, 99L));
    }

    @Test
    void signerParEmployeur_AlreadySigned_Throws() {
        entente.setStatut(EntenteStage.StatutEntente.EN_ATTENTE_SIGNATURE);
        entente.setDateSignatureEmployeur(LocalDateTime.now());
        when(ententeRepository.findById(1L)).thenReturn(Optional.of(entente));
        assertThrows(IllegalStateException.class, () -> ententeStageService.signerParEmployeur(1L, employeur.getId()));
    }

    @Test
    void signerParEmployeur_AllSigned_SetsValidee() {
        entente.setStatut(EntenteStage.StatutEntente.EN_ATTENTE_SIGNATURE);
        entente.setDateSignatureEtudiant(LocalDateTime.now());
        entente.setDateSignatureGestionnaire(LocalDateTime.now());
        when(ententeRepository.findById(1L)).thenReturn(Optional.of(entente));
        when(ententeRepository.save(any())).thenReturn(entente);

        EntenteStageDto result = ententeStageService.signerParEmployeur(1L, employeur.getId());
        assertEquals(EntenteStage.StatutEntente.VALIDEE, result.getStatut());
    }

    @Test
    void signerParEtudiant_Success() {
        entente.setStatut(EntenteStage.StatutEntente.EN_ATTENTE_SIGNATURE);
        when(ententeRepository.findById(1L)).thenReturn(Optional.of(entente));
        when(ententeRepository.save(any())).thenReturn(entente);

        EntenteStageDto result = ententeStageService.signerParEtudiant(1L, student.getId());

        assertNotNull(result);
        assertNotNull(result.getDateSignatureEtudiant());
        verify(ententeRepository).save(any(EntenteStage.class));
    }

    @Test
    void signerParEtudiant_EntenteNotFound() {
        when(ententeRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () ->
                ententeStageService.signerParEtudiant(1L, student.getId())
        );
    }

    @Test
    void signerParEtudiant_StatusInvalid_Throws() {
        entente.setStatut(EntenteStage.StatutEntente.BROUILLON);
        when(ententeRepository.findById(1L)).thenReturn(Optional.of(entente));

        assertThrows(IllegalStateException.class, () ->
                ententeStageService.signerParEtudiant(1L, student.getId())
        );
    }

    @Test
    void signerParEtudiant_WrongStudent_Throws() {
        entente.setStatut(EntenteStage.StatutEntente.EN_ATTENTE_SIGNATURE);
        when(ententeRepository.findById(1L)).thenReturn(Optional.of(entente));

        assertThrows(IllegalArgumentException.class, () ->
                ententeStageService.signerParEtudiant(1L, 99L)
        );
    }

    @Test
    void signerParEtudiant_AlreadySigned_Throws() {
        entente.setStatut(EntenteStage.StatutEntente.EN_ATTENTE_SIGNATURE);
        entente.setDateSignatureEtudiant(LocalDateTime.now());
        when(ententeRepository.findById(1L)).thenReturn(Optional.of(entente));

        assertThrows(IllegalStateException.class, () ->
                ententeStageService.signerParEtudiant(1L, student.getId())
        );
    }

    @Test
    void signerParEtudiant_AllSigned_SetsValidee() {
        entente.setStatut(EntenteStage.StatutEntente.EN_ATTENTE_SIGNATURE);
        entente.setDateSignatureEmployeur(LocalDateTime.now());
        entente.setDateSignatureGestionnaire(LocalDateTime.now());
        when(ententeRepository.findById(1L)).thenReturn(Optional.of(entente));
        when(ententeRepository.save(any())).thenReturn(entente);

        EntenteStageDto result = ententeStageService.signerParEtudiant(1L, student.getId());
        assertEquals(EntenteStage.StatutEntente.VALIDEE, result.getStatut());
    }


    @Test
    void getEntentesByEmployeurId_Success() {
        when(ententeRepository.findAll()).thenReturn(List.of(entente));

        List<EntenteStageDto> result = ententeStageService.getEntentesByEmployeurId(1L);

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getInternshipOffer().getEmployeurId());
        verify(ententeRepository).findAll();
    }

    @Test
    void getEntentesByEmployeurId_NoEntentes_ReturnsEmptyList() {
        when(ententeRepository.findAll()).thenReturn(List.of());

        List<EntenteStageDto> result = ententeStageService.getEntentesByEmployeurId(1L);

        assertEquals(0, result.size());
    }

    @Test
    void getEntentesByEmployeurId_MultipleEntentes_FiltersCorrectly() {
        Employeur autreEmployeur = Employeur.builder()
                .id(2L)
                .firstName("Bob")
                .lastName("Martin")
                .email("bob@company.com")
                .password("password")
                .companyName("AnotherCorp")
                .field("Tech")
                .build();

        InternshipOffer offer2 = InternshipOffer.builder()
                .id(2L)
                .description("Autre stage")
                .startDate(LocalDate.now())
                .durationInWeeks(10)
                .address("456 Rue Test")
                .status(InternshipOffer.Status.PUBLISHED)
                .employeur(autreEmployeur)
                .build();

        Candidature candidature2 = Candidature.builder()
                .id(2L)
                .student(student)
                .internshipOffer(offer2)
                .cv(candidature.getCv())
                .status(Candidature.Status.ACCEPTED)
                .applicationDate(LocalDateTime.now())
                .build();

        EntenteStage entente2 = EntenteStage.builder()
                .id(2L)
                .candidature(candidature2)
                .missionsObjectifs("Autre mission")
                .statut(EntenteStage.StatutEntente.EN_ATTENTE_SIGNATURE)
                .dateCreation(LocalDateTime.now())
                .build();

        when(ententeRepository.findAll()).thenReturn(List.of(entente, entente2));

        List<EntenteStageDto> result = ententeStageService.getEntentesByEmployeurId(1L);

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getInternshipOffer().getEmployeurId());
    }

    @Test
    void getEntentesByEmployeurId_NullEmployeurId_ReturnsEmptyList() {
        List<EntenteStageDto> result = ententeStageService.getEntentesByEmployeurId(null);

        assertEquals(0, result.size());
    }


    @Test
    void getEntentesByStudentId_Success() {
        when(ententeRepository.findAll()).thenReturn(List.of(entente));

        List<EntenteStageDto> result = ententeStageService.getEntentesByStudentId(1L);

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getStudent().getId());
        verify(ententeRepository).findAll();
    }

    @Test
    void getEntentesByStudentId_NoEntentes_ReturnsEmptyList() {
        when(ententeRepository.findAll()).thenReturn(List.of());

        List<EntenteStageDto> result = ententeStageService.getEntentesByStudentId(1L);

        assertEquals(0, result.size());
    }

    @Test
    void getEntentesByStudentId_MultipleEntentes_FiltersCorrectly() {
        Student autreStudent = Student.builder()
                .id(2L)
                .firstName("Jane")
                .lastName("Smith")
                .email("jane@test.com")
                .password("password")
                .studentNumber("STU002")
                .program(Program.SOFTWARE_ENGINEERING.getTranslationKey())
                .build();

        Cv cv2 = Cv.builder()
                .id(2L)
                .student(autreStudent)
                .pdfPath("/path/to/cv2.pdf")
                .status(Cv.Status.APPROVED)
                .build();

        Candidature candidature2 = Candidature.builder()
                .id(2L)
                .student(autreStudent)
                .internshipOffer(candidature.getInternshipOffer())
                .cv(cv2)
                .status(Candidature.Status.ACCEPTED)
                .applicationDate(LocalDateTime.now())
                .build();

        EntenteStage entente2 = EntenteStage.builder()
                .id(2L)
                .candidature(candidature2)
                .missionsObjectifs("Autre mission")
                .statut(EntenteStage.StatutEntente.EN_ATTENTE_SIGNATURE)
                .dateCreation(LocalDateTime.now())
                .build();

        when(ententeRepository.findAll()).thenReturn(List.of(entente, entente2));

        List<EntenteStageDto> result = ententeStageService.getEntentesByStudentId(1L);

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getStudent().getId());
    }

    @Test
    void getEntentesByStudentId_NullStudentId_ReturnsEmptyList() {
        List<EntenteStageDto> result = ententeStageService.getEntentesByStudentId(null);

        assertEquals(0, result.size());
    }


    @Test
    void signerParEmployeur_OnlyEmployeurSigned_StaysEnAttente() {
        entente.setStatut(EntenteStage.StatutEntente.EN_ATTENTE_SIGNATURE);
        entente.setDateSignatureEtudiant(null);
        entente.setDateSignatureGestionnaire(null);
        when(ententeRepository.findById(1L)).thenReturn(Optional.of(entente));
        when(ententeRepository.save(any())).thenReturn(entente);

        EntenteStageDto result = ententeStageService.signerParEmployeur(1L, employeur.getId());

        assertEquals(EntenteStage.StatutEntente.EN_ATTENTE_SIGNATURE, result.getStatut());
    }

    @Test
    void signerParEmployeur_OnlyEmployeurAndEtudiantSigned_StaysEnAttente() {
        entente.setStatut(EntenteStage.StatutEntente.EN_ATTENTE_SIGNATURE);
        entente.setDateSignatureEtudiant(LocalDateTime.now());
        entente.setDateSignatureGestionnaire(null);
        when(ententeRepository.findById(1L)).thenReturn(Optional.of(entente));
        when(ententeRepository.save(any())).thenReturn(entente);

        EntenteStageDto result = ententeStageService.signerParEmployeur(1L, employeur.getId());

        assertEquals(EntenteStage.StatutEntente.EN_ATTENTE_SIGNATURE, result.getStatut());
    }

    @Test
    void signerParEmployeur_OnlyEmployeurAndGestionnaireSigned_StaysEnAttente() {
        entente.setStatut(EntenteStage.StatutEntente.EN_ATTENTE_SIGNATURE);
        entente.setDateSignatureEtudiant(null);
        entente.setDateSignatureGestionnaire(LocalDateTime.now());
        when(ententeRepository.findById(1L)).thenReturn(Optional.of(entente));
        when(ententeRepository.save(any())).thenReturn(entente);

        EntenteStageDto result = ententeStageService.signerParEmployeur(1L, employeur.getId());

        assertEquals(EntenteStage.StatutEntente.EN_ATTENTE_SIGNATURE, result.getStatut());
    }

    @Test
    void signerParEtudiant_OnlyEtudiantSigned_StaysEnAttente() {
        entente.setStatut(EntenteStage.StatutEntente.EN_ATTENTE_SIGNATURE);
        entente.setDateSignatureEmployeur(null);
        entente.setDateSignatureGestionnaire(null);
        when(ententeRepository.findById(1L)).thenReturn(Optional.of(entente));
        when(ententeRepository.save(any())).thenReturn(entente);

        EntenteStageDto result = ententeStageService.signerParEtudiant(1L, student.getId());

        assertEquals(EntenteStage.StatutEntente.EN_ATTENTE_SIGNATURE, result.getStatut());
    }

    @Test
    void signerParEtudiant_OnlyEtudiantAndEmployeurSigned_StaysEnAttente() {
        entente.setStatut(EntenteStage.StatutEntente.EN_ATTENTE_SIGNATURE);
        entente.setDateSignatureEmployeur(LocalDateTime.now());
        entente.setDateSignatureGestionnaire(null);
        when(ententeRepository.findById(1L)).thenReturn(Optional.of(entente));
        when(ententeRepository.save(any())).thenReturn(entente);

        EntenteStageDto result = ententeStageService.signerParEtudiant(1L, student.getId());

        assertEquals(EntenteStage.StatutEntente.EN_ATTENTE_SIGNATURE, result.getStatut());
    }

    @Test
    void signerParEtudiant_OnlyEtudiantAndGestionnaireSigned_StaysEnAttente() {
        entente.setStatut(EntenteStage.StatutEntente.EN_ATTENTE_SIGNATURE);
        entente.setDateSignatureEmployeur(null);
        entente.setDateSignatureGestionnaire(LocalDateTime.now());
        when(ententeRepository.findById(1L)).thenReturn(Optional.of(entente));
        when(ententeRepository.save(any())).thenReturn(entente);

        EntenteStageDto result = ententeStageService.signerParEtudiant(1L, student.getId());

        assertEquals(EntenteStage.StatutEntente.EN_ATTENTE_SIGNATURE, result.getStatut());
    }


    @Test
    void creerEntente_WithNullDateDebut_ThrowsException() {
        ententeDto.setDateDebut(null);
        when(candidatureRepository.findById(1L)).thenReturn(Optional.of(candidature));
        when(ententeRepository.existsByCandidatureId(1L)).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> {
            ententeStageService.creerEntente(ententeDto);
        });
    }

    @Test
    void creerEntente_WithZeroDuree_ThrowsException() {
        ententeDto.setDuree(0);
        when(candidatureRepository.findById(1L)).thenReturn(Optional.of(candidature));
        when(ententeRepository.existsByCandidatureId(1L)).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> {
            ententeStageService.creerEntente(ententeDto);
        });
    }

    @Test
    void creerEntente_WithNegativeDuree_ThrowsException() {
        ententeDto.setDuree(-5);
        when(candidatureRepository.findById(1L)).thenReturn(Optional.of(candidature));
        when(ententeRepository.existsByCandidatureId(1L)).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> {
            ententeStageService.creerEntente(ententeDto);
        });
    }

    @Test
    void creerEntente_WithEmptyMissionsObjectifs_ThrowsException() {
        ententeDto.setMissionsObjectifs("   ");
        when(candidatureRepository.findById(1L)).thenReturn(Optional.of(candidature));
        when(ententeRepository.existsByCandidatureId(1L)).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> {
            ententeStageService.creerEntente(ententeDto);
        });
    }

    @Test
    void validerEtGenererEntente_NotBrouillon_ThrowsException() {
        entente.setStatut(EntenteStage.StatutEntente.EN_ATTENTE_SIGNATURE);
        when(ententeRepository.findById(1L)).thenReturn(Optional.of(entente));

        assertThrows(IllegalStateException.class, () -> {
            ententeStageService.validerEtGenererEntente(1L);
        });
    }

    @Test
    void telechargerPDF_EmptyCheminPDF_ThrowsException() {
        entente.setCheminDocumentPDF("");
        when(ententeRepository.findById(1L)).thenReturn(Optional.of(entente));

        assertThrows(IllegalStateException.class, () -> {
            ententeStageService.telechargerPDF(1L);
        });
    }

    @Test
    void telechargerPDF_BlankCheminPDF_ThrowsException() {
        entente.setCheminDocumentPDF("   ");
        when(ententeRepository.findById(1L)).thenReturn(Optional.of(entente));

        assertThrows(IllegalStateException.class, () -> {
            ententeStageService.telechargerPDF(1L);
        });
    }
}