package ca.cal.leandrose.presentation;

import ca.cal.leandrose.model.Cv;
import ca.cal.leandrose.model.EntenteStage;
import ca.cal.leandrose.security.TestSecurityConfiguration;
import ca.cal.leandrose.service.*;
import ca.cal.leandrose.service.dto.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(GestionnaireController.class)
@ActiveProfiles("test")
@Import(TestSecurityConfiguration.class)
class GestionnaireControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private GestionnaireService gestionnaireService;

    @MockitoBean
    private InternshipOfferService internshipOfferService;

    @MockitoBean
    private CvService cvService;

    @MockitoBean
    private EntenteStageService ententeStageService;

    @MockitoBean
    private UserAppService userAppService;

    private CvDto sampleCvDto;
    private Cv sampleCv;
    private InternshipOfferDto internshipOfferDto;

    @BeforeEach
    void setUp() {
        sampleCvDto = CvDto.builder()
                .id(1L)
                .studentId(1L)
                .studentName("John Doe")
                .pdfPath("/path/to/test-cv.pdf")
                .status(Cv.Status.APPROVED)
                .build();

        sampleCv = Cv.builder()
                .id(1L)
                .pdfPath("/path/to/test-cv.pdf")
                .status(Cv.Status.PENDING)
                .build();
        internshipOfferDto = InternshipOfferDto.builder()
                .id(1L)
                .description("Test offer")
                .startDate(LocalDate.of(2025, 1, 1))
                .durationInWeeks(12)
                .address("123 Main ST")
                .remuneration(15f)
                .status("PUBLISHED")
                .employeurId(20L)
                .companyName("TechCorp")
                .pdfPath("/docs/offer.pdf")
                .build();
    }

    // === TESTS CV ===

    @Test
    void approveCv_ShouldReturnApprovedCvDto_WhenCvExists() throws Exception {
        Long cvId = 1L;
        CvDto approvedCvDto = CvDto.builder()
                .id(cvId)
                .studentId(1L)
                .pdfPath("/path/to/test-cv.pdf")
                .status(Cv.Status.APPROVED)
                .build();

        when(gestionnaireService.approveCv(cvId)).thenReturn(approvedCvDto);

        mockMvc.perform(post("/gestionnaire/cv/{cvId}/approve", cvId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(cvId))
                .andExpect(jsonPath("$.studentId").value(1L))
                .andExpect(jsonPath("$.pdfPath").value("/path/to/test-cv.pdf"))
                .andExpect(jsonPath("$.status").value("APPROVED"));

        verify(gestionnaireService, times(1)).approveCv(cvId);
    }

    @Test
    void rejectCv_ShouldReturnRejectedCvDto_WhenCvExists() throws Exception {
        Long cvId = 1L;
        String rejectionReason = "cv non professionnel";

        CvDto rejectedCvDto = CvDto.builder()
                .id(cvId)
                .studentId(1L)
                .pdfPath("/path/to/test-cv.pdf")
                .status(Cv.Status.REJECTED)
                .build();

        when(gestionnaireService.rejectCv(eq(cvId), anyString())).thenReturn(rejectedCvDto);

        mockMvc.perform(post("/gestionnaire/cv/{cvId}/reject", cvId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rejectionReason)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(cvId))
                .andExpect(jsonPath("$.studentId").value(1L))
                .andExpect(jsonPath("$.pdfPath").value("/path/to/test-cv.pdf"))
                .andExpect(jsonPath("$.status").value("REJECTED"));

        verify(gestionnaireService, times(1)).rejectCv(eq(cvId), anyString());
    }

    @Test
    void getPendingCvs_ShouldReturnListOfPendingCvs() throws Exception {
        CvDto pendingCv1 = CvDto.builder()
                .id(1L)
                .studentId(1L)
                .pdfPath("/path/to/cv1.pdf")
                .status(Cv.Status.PENDING)
                .build();

        CvDto pendingCv2 = CvDto.builder()
                .id(2L)
                .studentId(2L)
                .pdfPath("/path/to/cv2.pdf")
                .status(Cv.Status.PENDING)
                .build();

        List<CvDto> pendingCvs = Arrays.asList(pendingCv1, pendingCv2);

        when(gestionnaireService.getPendingCvs()).thenReturn(pendingCvs);

        mockMvc.perform(get("/gestionnaire/cvs/pending"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.size()").value(2))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[1].id").value(2L));

        verify(gestionnaireService, times(1)).getPendingCvs();
    }

    // === TESTS OFFRES ===

    @Test
    void getApprovedOffers_returnList() throws Exception {
        when(gestionnaireService.getApprovedOffers()).thenReturn(List.of(internshipOfferDto));

        mockMvc.perform(get("/gestionnaire/offers/approved"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].companyName").value("TechCorp"));
    }

    @Test
    void getRejectedOffers_returnedList() throws Exception {
        internshipOfferDto.setStatus("REJECTED");

        when(gestionnaireService.getRejectedoffers()).thenReturn(List.of(internshipOfferDto));

        mockMvc.perform(get("/gestionnaire/offers/reject"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("REJECTED"));
    }

    @Test
    void getOfferDetails_returnOffers() throws Exception {
        when(internshipOfferService.getOffer(1L)).thenReturn(internshipOfferDto);

        mockMvc.perform(get("/gestionnaire/offers/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.companyName").value("TechCorp"))
                .andExpect(jsonPath("$.description").value("Test offer"));
    }

    @Test
    void signerEntente_Success_ReturnsSignedEntente() throws Exception {
        when(userAppService.getMe(anyString())).thenReturn(
                new UserDTO(1L, "Jean", "Dupont", "gestionnaire@test.com", ca.cal.leandrose.model.auth.Role.GESTIONNAIRE)
        );

        EntenteStageDto ententeSigned = EntenteStageDto.builder()
                .id(1L)
                .statut(EntenteStage.StatutEntente.VALIDEE)
                .dateSignatureGestionnaire(LocalDateTime.now())
                .build();

        when(ententeStageService.signerParGestionnaire(eq(1L), anyLong())).thenReturn(ententeSigned);

        mockMvc.perform(post("/gestionnaire/ententes/1/signer")
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.statut").value("VALIDEE"))
                .andExpect(jsonPath("$.dateSignatureGestionnaire").exists());

        verify(ententeStageService, times(1)).signerParGestionnaire(eq(1L), anyLong());
    }

    @Test
    void signerEntente_EntenteNotFound_ReturnsNotFound() throws Exception {
        when(userAppService.getMe(anyString())).thenReturn(
                new UserDTO(1L, "Jean", "Dupont", "gestionnaire@test.com", ca.cal.leandrose.model.auth.Role.GESTIONNAIRE)
        );

        when(ententeStageService.signerParGestionnaire(eq(999L), anyLong()))
                .thenThrow(new jakarta.persistence.EntityNotFoundException("Entente non trouvée"));

        mockMvc.perform(post("/gestionnaire/ententes/999/signer")
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.message").value("Entente non trouvée"));

        verify(ententeStageService, times(1)).signerParGestionnaire(eq(999L), anyLong());
    }

    @Test
    void signerEntente_InvalidStatus_ReturnsBadRequest() throws Exception {
        when(userAppService.getMe(anyString())).thenReturn(
                new UserDTO(1L, "Jean", "Dupont", "gestionnaire@test.com", ca.cal.leandrose.model.auth.Role.GESTIONNAIRE)
        );

        when(ententeStageService.signerParGestionnaire(eq(1L), anyLong()))
                .thenThrow(new IllegalStateException("L'entente doit être en attente de signature."));

        mockMvc.perform(post("/gestionnaire/ententes/1/signer")
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.message").value("L'entente doit être en attente de signature."));

        verify(ententeStageService, times(1)).signerParGestionnaire(eq(1L), anyLong());
    }

    @Test
    void signerEntente_AlreadySigned_ReturnsConflict() throws Exception {
        when(userAppService.getMe(anyString())).thenReturn(
                new UserDTO(1L, "Jean", "Dupont", "gestionnaire@test.com", ca.cal.leandrose.model.auth.Role.GESTIONNAIRE)
        );

        when(ententeStageService.signerParGestionnaire(eq(1L), anyLong()))
                .thenThrow(new IllegalStateException("Le gestionnaire a déjà signé cette entente."));

        mockMvc.perform(post("/gestionnaire/ententes/1/signer")
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.message").value("Le gestionnaire a déjà signé cette entente."));

        verify(ententeStageService, times(1)).signerParGestionnaire(eq(1L), anyLong());
    }
}
