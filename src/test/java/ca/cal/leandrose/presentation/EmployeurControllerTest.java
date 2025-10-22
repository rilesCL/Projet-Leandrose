package ca.cal.leandrose.presentation;

import ca.cal.leandrose.model.*;
import ca.cal.leandrose.model.auth.Role;
import ca.cal.leandrose.repository.CandidatureRepository;
import ca.cal.leandrose.repository.EmployeurRepository;
import ca.cal.leandrose.security.TestSecurityConfiguration;
import ca.cal.leandrose.service.*;
import ca.cal.leandrose.service.dto.*;
import ca.cal.leandrose.service.mapper.InternshipOfferMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.Mockito.never;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(controllers = EmployeurController.class)
@ActiveProfiles("test")
@Import(TestSecurityConfiguration.class)
class EmployeurControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserAppService userAppService;

    @MockitoBean
    private InternshipOfferService internshipOfferService;

    @MockitoBean
    private EmployeurRepository employeurRepository;

    @MockitoBean
    private CandidatureService candidatureService;

    @MockitoBean
    private ConvocationService convocationService;

    @MockitoBean
    CandidatureRepository candidatureRepository;

    @MockitoBean
    private EmployeurService employeurService;

    // ✅ AJOUT STORY 39 : nouveau mock
    @MockitoBean
    private EntenteStageService ententeStageService;


    @Test
    void downloadOffer_asEmployeur_returnsPdf() throws Exception {
        UserDTO employeurDto = EmployeurDto.builder()
                .id(1L)
                .role(Role.EMPLOYEUR)
                .build();

        InternshipOfferDto offer = InternshipOfferMapper.toDto(InternshipOffer.builder()
                .id(100L)
                .description("Stage Java")
                .pdfPath("dummy.pdf")
                .employeur(Employeur.builder().id(1L).build())
                .build());

        when(userAppService.getMe(anyString())).thenReturn(employeurDto);
        when(internshipOfferService.getOffer(100L)).thenReturn(offer);

        mockMvc.perform(get("/employeur/offers/100/download")
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isNotFound());
    }

    @Test
    void downloadOffer_notEmployeur_returnsForbidden() throws Exception {
        UserDTO studentDto = EmployeurDto.builder()
                .id(2L)
                .role(Role.STUDENT)
                .build();
        when(userAppService.getMe(anyString())).thenReturn(studentDto);

        mockMvc.perform(get("/employeur/offers/100/download")
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getConvocationsByOffer_asEmployeur_returnsList() throws Exception {
        UserDTO employeurDto = EmployeurDto.builder()
                .id(1L)
                .role(Role.EMPLOYEUR)
                .build();

        InternshipOfferDto offer = InternshipOfferMapper.toDto(InternshipOffer.builder()
                .id(100L)
                .employeur(Employeur.builder().id(1L).build())
                .build());

        ConvocationDto convocationDto = ConvocationDto.builder()
                .id(10L)
                .location("Bureau 301")
                .build();

        when(userAppService.getMe(anyString())).thenReturn(employeurDto);
        when(internshipOfferService.getOffer(100L)).thenReturn(offer);
        when(convocationService.getAllConvocationsByInterShipOfferId(100L))
                .thenReturn(List.of(convocationDto));

        mockMvc.perform(get("/employeur/offers/100/convocations")
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10))
                .andExpect(jsonPath("$[0].location").value("Bureau 301"));
    }

    @Test
    void getConvocationsByOffer_notEmployeur_returnsForbidden() throws Exception {
        UserDTO studentDto = EmployeurDto.builder()
                .id(2L)
                .role(Role.STUDENT)
                .build();
        when(userAppService.getMe(anyString())).thenReturn(studentDto);

        mockMvc.perform(get("/employeur/offers/100/convocations")
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isForbidden());
    }

    @Test
    void createConvocation_asEmployeur_createsSuccessfully() throws Exception {
        UserDTO employeurDto = EmployeurDto.builder()
                .id(1L)
                .role(Role.EMPLOYEUR)
                .build();

        ConvocationDto request = new ConvocationDto();
        request.setConvocationDate(LocalDateTime.now().plusDays(5));
        request.setLocation("Bureau 301");
        request.setMessage("Message perso");

        CandidatureDto candidatureDto = CandidatureDto.builder()
                .id(50L)
                .employeurId(1L)
                .build();

        when(userAppService.getMe(anyString())).thenReturn(employeurDto);
        when(candidatureService.getCandidatureById(50L)).thenReturn(candidatureDto);

        mockMvc.perform(post("/employeur/candidatures/50/convocations")
                        .header("Authorization", "Bearer token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("Convocation créée avec succès"));

        verify(convocationService).addConvocation(
                eq(50L),
                eq(request.getConvocationDate()),
                eq("Bureau 301"),
                eq("Message perso")
        );
    }

    @Test
    void createConvocation_notEmployeur_returnsForbidden() throws Exception {
        UserDTO studentDto = EmployeurDto.builder()
                .id(2L)
                .role(Role.STUDENT)
                .build();

        ConvocationDto request = new ConvocationDto();
        request.setConvocationDate(LocalDateTime.now().plusDays(5));
        request.setLocation("Bureau 301");
        request.setMessage("Message perso");

        when(userAppService.getMe(anyString())).thenReturn(studentDto);

        mockMvc.perform(post("/employeur/candidatures/50/convocations")
                        .header("Authorization", "Bearer token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());

        verify(convocationService, never()).addConvocation(anyLong(), any(), anyString(), anyString());
    }

    @Test
    void createConvocation_whenConvocationFails_returnsBadRequest() throws Exception {
        UserDTO employeurDto = EmployeurDto.builder()
                .id(1L)
                .role(Role.EMPLOYEUR)
                .build();

        ConvocationDto request = new ConvocationDto();
        request.setConvocationDate(LocalDateTime.now().plusDays(5));
        request.setLocation("Bureau 301");
        request.setMessage("Message perso");

        CandidatureDto candidatureDto = CandidatureDto.builder()
                .id(50L)
                .employeurId(1L)
                .build();

        when(userAppService.getMe(anyString())).thenReturn(employeurDto);
        when(candidatureService.getCandidatureById(50L)).thenReturn(candidatureDto);

        doThrow(new IllegalStateException("Already convened"))
                .when(convocationService).addConvocation(anyLong(), any(), anyString(), anyString());

        mockMvc.perform(post("/employeur/candidatures/50/convocations")
                        .header("Authorization", "Bearer token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Already convened"));
    }

    // (tous tes autres tests existants inchangés…)

    // ====== TESTS AJOUTÉS STORY 39 : SIGNATURE D’ENTENTE ======

    @Test
    void signerEntente_asEmployeur_Success() throws Exception {
        UserDTO employeurDto = EmployeurDto.builder()
                .id(1L)
                .role(Role.EMPLOYEUR)
                .build();

        EntenteStageDto ententeDto = EntenteStageDto.builder()
                .id(1L)
                .statut(EntenteStage.StatutEntente.EN_ATTENTE_SIGNATURE)
                .build();

        when(userAppService.getMe(anyString())).thenReturn(employeurDto);
        when(ententeStageService.signerParEmployeur(1L, 1L)).thenReturn(ententeDto);

        mockMvc.perform(post("/employeur/ententes/1/signer")
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.statut").value("EN_ATTENTE_SIGNATURE"));
    }

    @Test
    void signerEntente_NotEmployeur_ReturnsForbidden() throws Exception {
        UserDTO studentDto = EmployeurDto.builder()
                .id(2L)
                .role(Role.STUDENT)
                .build();

        when(userAppService.getMe(anyString())).thenReturn(studentDto);

        mockMvc.perform(post("/employeur/ententes/1/signer")
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isForbidden());

        verify(ententeStageService, never()).signerParEmployeur(anyLong(), anyLong());
    }

    @Test
    void signerEntente_EntenteNotFound_Returns404() throws Exception {
        UserDTO employeurDto = EmployeurDto.builder()
                .id(1L)
                .role(Role.EMPLOYEUR)
                .build();

        when(userAppService.getMe(anyString())).thenReturn(employeurDto);
        when(ententeStageService.signerParEmployeur(1L, 1L))
                .thenThrow(new jakarta.persistence.EntityNotFoundException("Entente non trouvée"));

        mockMvc.perform(post("/employeur/ententes/1/signer")
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isNotFound());
    }

    @Test
    void signerEntente_IllegalArgument_ReturnsBadRequest() throws Exception {
        UserDTO employeurDto = EmployeurDto.builder()
                .id(1L)
                .role(Role.EMPLOYEUR)
                .build();

        when(userAppService.getMe(anyString())).thenReturn(employeurDto);
        when(ententeStageService.signerParEmployeur(1L, 1L))
                .thenThrow(new IllegalArgumentException("Cet employeur n'est pas autorisé à signer cette entente."));

        mockMvc.perform(post("/employeur/ententes/1/signer")
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.message").value("Cet employeur n'est pas autorisé à signer cette entente."));
    }

    @Test
    void signerEntente_IllegalState_ReturnsBadRequest() throws Exception {
        UserDTO employeurDto = EmployeurDto.builder()
                .id(1L)
                .role(Role.EMPLOYEUR)
                .build();

        when(userAppService.getMe(anyString())).thenReturn(employeurDto);
        when(ententeStageService.signerParEmployeur(1L, 1L))
                .thenThrow(new IllegalStateException("L'entente doit être en attente de signature."));

        mockMvc.perform(post("/employeur/ententes/1/signer")
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.message").value("L'entente doit être en attente de signature."));
    }
}
