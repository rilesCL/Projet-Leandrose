package ca.cal.leandrose.presentation;

import ca.cal.leandrose.model.EntenteStage;
import ca.cal.leandrose.security.TestSecurityConfiguration;
import ca.cal.leandrose.service.CvService;
import ca.cal.leandrose.service.EntenteStageService;
import ca.cal.leandrose.service.GestionnaireService;
import ca.cal.leandrose.service.InternshipOfferService;
import ca.cal.leandrose.service.dto.CandidatureDto;
import ca.cal.leandrose.service.dto.EntenteStageDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(GestionnaireController.class)
@ActiveProfiles("test")
@Import({TestSecurityConfiguration.class})
class GestionnaireControllerEntenteTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private EntenteStageService ententeStageService;

    @MockitoBean
    private InternshipOfferService internshipOfferService;

    @MockitoBean
    private GestionnaireService gestionnaireService;

    @MockitoBean
    private CvService cvService;

    private EntenteStageDto ententeDto;
    private CandidatureDto candidatureDto;

    @BeforeEach
    void setUp() {
        objectMapper.registerModule(new JavaTimeModule());

        ententeDto = EntenteStageDto.builder()
                .id(1L)
                .candidatureId(1L)
                .studentId(1L)
                .studentNom("Doe")
                .studentPrenom("John")
                .internshipOfferId(1L)
                .internshipOfferDescription("Stage développement")
                .nomEntreprise("TechCorp")
                .contactEntreprise("contact@techcorp.com")
                .dateDebut(LocalDate.of(2025, 6, 1))
                .dateFin(LocalDate.of(2025, 8, 31))
                .duree("12 semaines")
                .horaires("9h-17h")
                .lieu("Montreal")
                .modalitesTeletravail("2 jours/semaine")
                .remuneration(new BigDecimal("3000.00"))
                .missionsObjectifs("Développement web")
                .statut(EntenteStage.StatutEntente.BROUILLON)
                .dateCreation(LocalDateTime.now())
                .build();

        candidatureDto = CandidatureDto.builder()
                .id(1L)
                .studentName("John Doe")
                .offerDescription("Stage développement")
                .build();
    }

    @Test
    void getCandidaturesAcceptees_ShouldReturnListOfCandidatures() throws Exception {
        List<CandidatureDto> candidatures = Arrays.asList(candidatureDto);
        when(ententeStageService.getCandidaturesAcceptees()).thenReturn(candidatures);

        mockMvc.perform(get("/gestionnaire/ententes/candidatures-acceptees"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].studentName").value("John Doe"))
                .andExpect(jsonPath("$[0].offerDescription").value("Stage développement"));

        verify(ententeStageService, times(1)).getCandidaturesAcceptees();
    }

    @Test
    void getCandidaturesAcceptees_ShouldReturnEmptyList_WhenNoAcceptedCandidatures() throws Exception {
        when(ententeStageService.getCandidaturesAcceptees()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/gestionnaire/ententes/candidatures-acceptees"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.size()").value(0));

        verify(ententeStageService, times(1)).getCandidaturesAcceptees();
    }

    @Test
    void creerEntente_ShouldReturnCreatedEntente_WhenValidData() throws Exception {
        when(ententeStageService.creerEntente(any(EntenteStageDto.class))).thenReturn(ententeDto);

        mockMvc.perform(post("/gestionnaire/ententes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ententeDto)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.duree").value("12 semaines"))
                .andExpect(jsonPath("$.statut").value("BROUILLON"))
                .andExpect(jsonPath("$.nomEntreprise").value("TechCorp"))
                .andExpect(jsonPath("$.studentNom").value("Doe"));

        verify(ententeStageService, times(1)).creerEntente(any(EntenteStageDto.class));
    }

    @Test
    void modifierEntente_ShouldReturnUpdatedEntente_WhenValidData() throws Exception {
        EntenteStageDto modificationDto = EntenteStageDto.builder()
                .remuneration(new BigDecimal("3500.00"))
                .modalitesTeletravail("3 jours/semaine")
                .build();

        ententeDto.setRemuneration(new BigDecimal("3500.00"));
        ententeDto.setModalitesTeletravail("3 jours/semaine");

        when(ententeStageService.modifierEntente(eq(1L), any(EntenteStageDto.class)))
                .thenReturn(ententeDto);

        mockMvc.perform(put("/gestionnaire/ententes/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(modificationDto)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.remuneration").value(3500.00))
                .andExpect(jsonPath("$.modalitesTeletravail").value("3 jours/semaine"));

        verify(ententeStageService, times(1)).modifierEntente(eq(1L), any(EntenteStageDto.class));
    }

    @Test
    void validerEntente_ShouldReturnValidatedEntente_WhenEntenteExists() throws Exception {
        ententeDto.setStatut(EntenteStage.StatutEntente.EN_ATTENTE_SIGNATURE);
        ententeDto.setDateModification(LocalDateTime.now());

        when(ententeStageService.validerEtGenererEntente(1L)).thenReturn(ententeDto);

        mockMvc.perform(post("/gestionnaire/ententes/1/valider"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.statut").value("EN_ATTENTE_SIGNATURE"));

        verify(ententeStageService, times(1)).validerEtGenererEntente(1L);
    }

    @Test
    void getAllEntentes_ShouldReturnListOfEntentes() throws Exception {
        EntenteStageDto entente2 = EntenteStageDto.builder()
                .id(2L)
                .candidatureId(2L)
                .studentNom("Smith")
                .studentPrenom("Jane")
                .nomEntreprise("DevCorp")
                .duree("16 semaines")
                .horaires("10h-18h")
                .statut(EntenteStage.StatutEntente.VALIDEE)
                .dateDebut(LocalDate.of(2025, 7, 1))
                .dateFin(LocalDate.of(2025, 10, 31))
                .missionsObjectifs("Backend dev")
                .build();

        List<EntenteStageDto> ententes = Arrays.asList(ententeDto, entente2);
        when(ententeStageService.getAllEntentes()).thenReturn(ententes);

        mockMvc.perform(get("/gestionnaire/ententes"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.size()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].duree").value("12 semaines"))
                .andExpect(jsonPath("$[0].nomEntreprise").value("TechCorp"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].duree").value("16 semaines"))
                .andExpect(jsonPath("$[1].statut").value("VALIDEE"));

        verify(ententeStageService, times(1)).getAllEntentes();
    }

    @Test
    void getAllEntentes_ShouldReturnEmptyList_WhenNoEntentes() throws Exception {
        when(ententeStageService.getAllEntentes()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/gestionnaire/ententes"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.size()").value(0));

        verify(ententeStageService, times(1)).getAllEntentes();
    }

    @Test
    void getEntente_ShouldReturnEntente_WhenEntenteExists() throws Exception {
        when(ententeStageService.getEntenteById(1L)).thenReturn(ententeDto);

        mockMvc.perform(get("/gestionnaire/ententes/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.lieu").value("Montreal"))
                .andExpect(jsonPath("$.studentNom").value("Doe"))
                .andExpect(jsonPath("$.studentPrenom").value("John"))
                .andExpect(jsonPath("$.horaires").value("9h-17h"));

        verify(ententeStageService, times(1)).getEntenteById(1L);
    }

    @Test
    void getEntente_ShouldAcceptValidPathVariable() throws Exception {
        Long ententeId = 123L;
        when(ententeStageService.getEntenteById(ententeId)).thenReturn(ententeDto);

        mockMvc.perform(get("/gestionnaire/ententes/{ententeId}", ententeId))
                .andExpect(status().isOk());

        verify(ententeStageService, times(1)).getEntenteById(ententeId);
    }

    @Test
    void telechargerPDFEntente_ShouldReturnPdfFile_WhenEntenteExists() throws Exception {
        byte[] pdfBytes = new byte[]{1, 2, 3, 4, 5};
        when(ententeStageService.telechargerPDF(1L)).thenReturn(pdfBytes);

        mockMvc.perform(get("/gestionnaire/ententes/1/telecharger"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andExpect(header().exists("Content-Disposition"))
                .andExpect(content().bytes(pdfBytes));

        verify(ententeStageService, times(1)).telechargerPDF(1L);
    }

    @Test
    void supprimerEntente_ShouldReturnNoContent_WhenEntenteDeleted() throws Exception {
        doNothing().when(ententeStageService).supprimerEntente(1L);

        mockMvc.perform(delete("/gestionnaire/ententes/1"))
                .andExpect(status().isNoContent());

        verify(ententeStageService, times(1)).supprimerEntente(1L);
    }

    @Test
    void creerEntente_ShouldHandleValidationError_WhenInvalidData() throws Exception {
        EntenteStageDto invalidDto = EntenteStageDto.builder()
                .candidatureId(null)
                .build();

        when(ententeStageService.creerEntente(any(EntenteStageDto.class)))
                .thenThrow(new IllegalArgumentException("La candidature est obligatoire"));

        mockMvc.perform(post("/gestionnaire/ententes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.message").value("La candidature est obligatoire"));

        verify(ententeStageService, times(1)).creerEntente(any(EntenteStageDto.class));
    }

    @Test
    void modifierEntente_ShouldHandleNotFound_WhenEntenteDoesNotExist() throws Exception {
        when(ententeStageService.modifierEntente(eq(999L), any(EntenteStageDto.class)))
                .thenThrow(new jakarta.persistence.EntityNotFoundException("Entente non trouvée"));

        mockMvc.perform(put("/gestionnaire/ententes/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ententeDto)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.message").value("Entente non trouvée"));

        verify(ententeStageService, times(1)).modifierEntente(eq(999L), any(EntenteStageDto.class));
    }

    @Test
    void getEntente_ShouldHandleNotFound_WhenEntenteDoesNotExist() throws Exception {
        when(ententeStageService.getEntenteById(999L))
                .thenThrow(new jakarta.persistence.EntityNotFoundException("Entente non trouvée"));

        mockMvc.perform(get("/gestionnaire/ententes/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.message").value("Entente non trouvée"));

        verify(ententeStageService, times(1)).getEntenteById(999L);
    }

    @Test
    void modifierEntente_ShouldHandleConflict_WhenEntenteNotBrouillon() throws Exception {
        when(ententeStageService.modifierEntente(eq(1L), any(EntenteStageDto.class)))
                .thenThrow(new IllegalStateException("Impossible de modifier une entente qui n'est pas en brouillon"));

        mockMvc.perform(put("/gestionnaire/ententes/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ententeDto)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.message").value("Impossible de modifier une entente qui n'est pas en brouillon"));

        verify(ententeStageService, times(1)).modifierEntente(eq(1L), any(EntenteStageDto.class));
    }

    @Test
    void shouldMapPostRequestToCreerEntenteEndpoint() throws Exception {
        when(ententeStageService.creerEntente(any(EntenteStageDto.class))).thenReturn(ententeDto);

        mockMvc.perform(post("/gestionnaire/ententes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ententeDto)))
                .andExpect(status().isCreated());

        verify(ententeStageService, times(1)).creerEntente(any(EntenteStageDto.class));
    }

    @Test
    void shouldMapPutRequestToModifierEntenteEndpoint() throws Exception {
        when(ententeStageService.modifierEntente(eq(1L), any(EntenteStageDto.class))).thenReturn(ententeDto);

        mockMvc.perform(put("/gestionnaire/ententes/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ententeDto)))
                .andExpect(status().isOk());

        verify(ententeStageService, times(1)).modifierEntente(eq(1L), any(EntenteStageDto.class));
    }

    @Test
    void shouldMapGetRequestToGetAllEntentesEndpoint() throws Exception {
        when(ententeStageService.getAllEntentes()).thenReturn(Collections.singletonList(ententeDto));

        mockMvc.perform(get("/gestionnaire/ententes"))
                .andExpect(status().isOk());

        verify(ententeStageService, times(1)).getAllEntentes();
    }

    @Test
    void shouldMapDeleteRequestToSupprimerEntenteEndpoint() throws Exception {
        doNothing().when(ententeStageService).supprimerEntente(1L);

        mockMvc.perform(delete("/gestionnaire/ententes/1"))
                .andExpect(status().isNoContent());

        verify(ententeStageService, times(1)).supprimerEntente(1L);
    }

    @Test
    void modifierEntente_ShouldHandleMultipleModifications() throws Exception {
        Long ententeId1 = 1L;
        Long ententeId2 = 2L;

        EntenteStageDto modifiedEntente1 = EntenteStageDto.builder()
                .id(ententeId1)
                .remuneration(new BigDecimal("3500.00"))
                .build();

        EntenteStageDto modifiedEntente2 = EntenteStageDto.builder()
                .id(ententeId2)
                .remuneration(new BigDecimal("4000.00"))
                .build();

        when(ententeStageService.modifierEntente(eq(ententeId1), any(EntenteStageDto.class)))
                .thenReturn(modifiedEntente1);
        when(ententeStageService.modifierEntente(eq(ententeId2), any(EntenteStageDto.class)))
                .thenReturn(modifiedEntente2);

        mockMvc.perform(put("/gestionnaire/ententes/{ententeId}", ententeId1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(modifiedEntente1)))
                .andExpect(status().isOk());

        mockMvc.perform(put("/gestionnaire/ententes/{ententeId}", ententeId2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(modifiedEntente2)))
                .andExpect(status().isOk());

        verify(ententeStageService, times(1)).modifierEntente(eq(ententeId1), any(EntenteStageDto.class));
        verify(ententeStageService, times(1)).modifierEntente(eq(ententeId2), any(EntenteStageDto.class));
    }

}