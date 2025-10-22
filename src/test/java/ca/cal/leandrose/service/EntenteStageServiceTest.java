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

    @BeforeEach
    void setUp() {
        // Setup Student
        Student student = Student.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@test.com")
                .password("password")
                .studentNumber("STU001")
                .program(Program.COMPUTER_SCIENCE.getTranslationKey())
                .build();

        // Setup Employeur
        Employeur employeur = Employeur.builder()
                .id(1L)
                .firstName("Jane")
                .lastName("Smith")
                .email("jane@company.com")
                .password("password")
                .companyName("TechCorp")
                .field("Software")
                .build();

        // Setup CV - ✅ Corrigé
        // ✅ Changé de fileName à pdfPath
        Cv cv = Cv.builder()
                .id(1L)
                .student(student)
                .pdfPath("/path/to/cv.pdf")  // ✅ Changé de fileName à pdfPath
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

        EntenteStageDto result = ententeStageService.creerEntente(ententeDto);

        assertNotNull(result);
        assertEquals(EntenteStage.StatutEntente.BROUILLON, result.getStatut());
        verify(candidatureRepository).findById(1L);
        verify(ententeRepository).save(any(EntenteStage.class));
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
}