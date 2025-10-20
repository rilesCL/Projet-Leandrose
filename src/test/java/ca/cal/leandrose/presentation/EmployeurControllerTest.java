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

        ca.cal.leandrose.service.dto.ConvocationDto convocationDto = ca.cal.leandrose.service.dto.ConvocationDto.builder()
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

    ca.cal.leandrose.service.dto.ConvocationDto request = new ca.cal.leandrose.service.dto.ConvocationDto();
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

    ca.cal.leandrose.service.dto.ConvocationDto request = new ca.cal.leandrose.service.dto.ConvocationDto();
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


// ===== TESTS POUR ACCEPTATION PAR L'EMPLOYEUR =====

    @Test
    void acceptCandidature_asEmployeur_returnsAcceptedByEmployeur() throws Exception {
        UserDTO employeurDto = EmployeurDto.builder()
                .id(1L)
                .role(Role.EMPLOYEUR)
                .build();

        CandidatureDto candidatureDto = CandidatureDto.builder()
                .id(100L)
                .employeurId(1L)
                .status(Candidature.Status.ACCEPTEDBYEMPLOYEUR)
                .build();

        when(userAppService.getMe(anyString())).thenReturn(employeurDto);
        when(candidatureService.getCandidatureById(100L)).thenReturn(candidatureDto);
        when(candidatureService.acceptByEmployeur(100L)).thenReturn(candidatureDto);

        mockMvc.perform(post("/employeur/candidatures/100/accept")
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.status").value("ACCEPTEDBYEMPLOYEUR"));

        verify(candidatureService).acceptByEmployeur(100L);
    }

    @Test
    void acceptCandidature_notEmployeur_returnsForbidden() throws Exception {
        UserDTO studentDto = EmployeurDto.builder()
                .id(2L)
                .role(Role.STUDENT)
                .build();

        when(userAppService.getMe(anyString())).thenReturn(studentDto);

        mockMvc.perform(post("/employeur/candidatures/100/accept")
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isForbidden());

        verify(candidatureService, never()).acceptByEmployeur(anyLong());
    }

    @Test
    void acceptCandidature_notOwnCandidature_returnsForbidden() throws Exception {
        UserDTO employeurDto = EmployeurDto.builder()
                .id(1L)
                .role(Role.EMPLOYEUR)
                .build();

        CandidatureDto candidatureDto = CandidatureDto.builder()
                .id(100L)
                .employeurId(2L) // Différent employeur
                .build();

        when(userAppService.getMe(anyString())).thenReturn(employeurDto);
        when(candidatureService.getCandidatureById(100L)).thenReturn(candidatureDto);

        mockMvc.perform(post("/employeur/candidatures/100/accept")
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isForbidden());

        verify(candidatureService, never()).acceptByEmployeur(anyLong());
    }

    @Test
    void acceptCandidature_alreadyRejected_returnsBadRequest() throws Exception {
        UserDTO employeurDto = EmployeurDto.builder()
                .id(1L)
                .role(Role.EMPLOYEUR)
                .build();

        CandidatureDto candidatureDto = CandidatureDto.builder()
                .id(100L)
                .employeurId(1L)
                .build();

        when(userAppService.getMe(anyString())).thenReturn(employeurDto);
        when(candidatureService.getCandidatureById(100L)).thenReturn(candidatureDto);
        when(candidatureService.acceptByEmployeur(100L))
                .thenThrow(new IllegalStateException("Impossible d'accepter une candidature déjà rejetée"));

        mockMvc.perform(post("/employeur/candidatures/100/accept")
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Impossible d'accepter une candidature déjà rejetée"));
    }

    @Test
    void acceptCandidature_alreadyAccepted_returnsBadRequest() throws Exception {
        UserDTO employeurDto = EmployeurDto.builder()
                .id(1L)
                .role(Role.EMPLOYEUR)
                .build();

        CandidatureDto candidatureDto = CandidatureDto.builder()
                .id(100L)
                .employeurId(1L)
                .build();

        when(userAppService.getMe(anyString())).thenReturn(employeurDto);
        when(candidatureService.getCandidatureById(100L)).thenReturn(candidatureDto);
        when(candidatureService.acceptByEmployeur(100L))
                .thenThrow(new IllegalStateException("Cette candidature est déjà entièrement acceptée"));

        mockMvc.perform(post("/employeur/candidatures/100/accept")
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Cette candidature est déjà entièrement acceptée"));
    }

    @Test
    void acceptCandidature_alreadyAcceptedByEmployeur_returnsBadRequest() throws Exception {
        UserDTO employeurDto = EmployeurDto.builder()
                .id(1L)
                .role(Role.EMPLOYEUR)
                .build();

        CandidatureDto candidatureDto = CandidatureDto.builder()
                .id(100L)
                .employeurId(1L)
                .build();

        when(userAppService.getMe(anyString())).thenReturn(employeurDto);
        when(candidatureService.getCandidatureById(100L)).thenReturn(candidatureDto);
        when(candidatureService.acceptByEmployeur(100L))
                .thenThrow(new IllegalStateException("Vous avez déjà accepté cette candidature, en attente de la réponse de l'étudiant"));

        mockMvc.perform(post("/employeur/candidatures/100/accept")
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Vous avez déjà accepté cette candidature, en attente de la réponse de l'étudiant"));
    }

    @Test
    void acceptCandidature_candidatureNotFound_returnsNotFound() throws Exception {
        UserDTO employeurDto = EmployeurDto.builder()
                .id(1L)
                .role(Role.EMPLOYEUR)
                .build();

        when(userAppService.getMe(anyString())).thenReturn(employeurDto);
        when(candidatureService.getCandidatureById(999L))
                .thenThrow(new RuntimeException("Candidature introuvable"));

        mockMvc.perform(post("/employeur/candidatures/999/accept")
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Candidature non trouvée"));
    }

// ===== TESTS POUR REJET PAR L'EMPLOYEUR =====

    @Test
    void rejectCandidature_asEmployeur_returnsRejected() throws Exception {
        UserDTO employeurDto = EmployeurDto.builder()
                .id(1L)
                .role(Role.EMPLOYEUR)
                .build();

        CandidatureDto candidatureDto = CandidatureDto.builder()
                .id(100L)
                .employeurId(1L)
                .status(Candidature.Status.REJECTED)
                .build();

        when(userAppService.getMe(anyString())).thenReturn(employeurDto);
        when(candidatureService.getCandidatureById(100L)).thenReturn(candidatureDto);
        when(candidatureService.rejectByEmployeur(100L)).thenReturn(candidatureDto);

        mockMvc.perform(post("/employeur/candidatures/100/reject")
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.status").value("REJECTED"));

        verify(candidatureService).rejectByEmployeur(100L);
    }

    @Test
    void rejectCandidature_notEmployeur_returnsForbidden() throws Exception {
        UserDTO studentDto = EmployeurDto.builder()
                .id(2L)
                .role(Role.STUDENT)
                .build();

        when(userAppService.getMe(anyString())).thenReturn(studentDto);

        mockMvc.perform(post("/employeur/candidatures/100/reject")
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isForbidden());

        verify(candidatureService, never()).rejectByEmployeur(anyLong());
    }

    @Test
    void rejectCandidature_notOwnCandidature_returnsForbidden() throws Exception {
        UserDTO employeurDto = EmployeurDto.builder()
                .id(1L)
                .role(Role.EMPLOYEUR)
                .build();

        CandidatureDto candidatureDto = CandidatureDto.builder()
                .id(100L)
                .employeurId(2L) // Différent employeur
                .build();

        when(userAppService.getMe(anyString())).thenReturn(employeurDto);
        when(candidatureService.getCandidatureById(100L)).thenReturn(candidatureDto);

        mockMvc.perform(post("/employeur/candidatures/100/reject")
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isForbidden());

        verify(candidatureService, never()).rejectByEmployeur(anyLong());
    }

    @Test
    void rejectCandidature_alreadyAcceptedByBothParties_returnsBadRequest() throws Exception {
        UserDTO employeurDto = EmployeurDto.builder()
                .id(1L)
                .role(Role.EMPLOYEUR)
                .build();

        CandidatureDto candidatureDto = CandidatureDto.builder()
                .id(100L)
                .employeurId(1L)
                .build();

        when(userAppService.getMe(anyString())).thenReturn(employeurDto);
        when(candidatureService.getCandidatureById(100L)).thenReturn(candidatureDto);
        when(candidatureService.rejectByEmployeur(100L))
                .thenThrow(new IllegalStateException("Impossible de rejeter une candidature déjà acceptée par les deux parties"));

        mockMvc.perform(post("/employeur/candidatures/100/reject")
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Impossible de rejeter une candidature déjà acceptée par les deux parties"));
    }

    @Test
    void rejectCandidature_alreadyRejected_returnsBadRequest() throws Exception {
        UserDTO employeurDto = EmployeurDto.builder()
                .id(1L)
                .role(Role.EMPLOYEUR)
                .build();

        CandidatureDto candidatureDto = CandidatureDto.builder()
                .id(100L)
                .employeurId(1L)
                .build();

        when(userAppService.getMe(anyString())).thenReturn(employeurDto);
        when(candidatureService.getCandidatureById(100L)).thenReturn(candidatureDto);
        when(candidatureService.rejectByEmployeur(100L))
                .thenThrow(new IllegalStateException("Cette candidature est déjà rejetée"));

        mockMvc.perform(post("/employeur/candidatures/100/reject")
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Cette candidature est déjà rejetée"));
    }

    @Test
    void rejectCandidature_candidatureNotFound_returnsNotFound() throws Exception {
        UserDTO employeurDto = EmployeurDto.builder()
                .id(1L)
                .role(Role.EMPLOYEUR)
                .build();

        when(userAppService.getMe(anyString())).thenReturn(employeurDto);
        when(candidatureService.getCandidatureById(999L))
                .thenThrow(new RuntimeException("Candidature introuvable"));

        mockMvc.perform(post("/employeur/candidatures/999/reject")
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Candidature non trouvée"));
    }

// ===== TESTS POUR RÉCUPÉRATION DES CANDIDATURES =====

    @Test
    void getCandidaturesForOffer_asEmployeur_returnsList() throws Exception {
        UserDTO employeurDto = EmployeurDto.builder()
                .id(1L)
                .role(Role.EMPLOYEUR)
                .build();

        InternshipOffer offer = InternshipOffer.builder()
                .id(100L)
                .employeur(Employeur.builder().id(1L).build())
                .build();

        CandidatureEmployeurDto candidature = CandidatureEmployeurDto.builder()
                .id(50L)
                .studentFirstName("Alice")
                .studentLastName("Martin")
                .status(Candidature.Status.PENDING)
                .build();

        when(userAppService.getMe(anyString())).thenReturn(employeurDto);
        when(internshipOfferService.getOffer(100L)).thenReturn(offer);
        when(candidatureService.getCandidaturesByOffer(100L)).thenReturn(List.of(candidature));

        mockMvc.perform(get("/employeur/offers/100/candidatures")
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(50))
                .andExpect(jsonPath("$[0].studentFirstName").value("Alice"))
                .andExpect(jsonPath("$[0].studentLastName").value("Martin"));

        verify(candidatureService).getCandidaturesByOffer(100L);
    }

    @Test
    void getCandidaturesForOffer_notOwnOffer_returnsForbidden() throws Exception {
        UserDTO employeurDto = EmployeurDto.builder()
                .id(1L)
                .role(Role.EMPLOYEUR)
                .build();

        InternshipOffer offer = InternshipOffer.builder()
                .id(100L)
                .employeur(Employeur.builder().id(2L).build()) // Différent employeur
                .build();

        when(userAppService.getMe(anyString())).thenReturn(employeurDto);
        when(internshipOfferService.getOffer(100L)).thenReturn(offer);

        mockMvc.perform(get("/employeur/offers/100/candidatures")
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isForbidden());

        verify(candidatureService, never()).getCandidaturesByOffer(anyLong());
    }

    @Test
    void getAllMyCandidatures_asEmployeur_returnsList() throws Exception {
        UserDTO employeurDto = EmployeurDto.builder()
                .id(1L)
                .role(Role.EMPLOYEUR)
                .build();

        CandidatureEmployeurDto candidature1 = CandidatureEmployeurDto.builder()
                .id(50L)
                .studentFirstName("Alice")
                .build();

        CandidatureEmployeurDto candidature2 = CandidatureEmployeurDto.builder()
                .id(51L)
                .studentFirstName("Bob")
                .build();

        when(userAppService.getMe(anyString())).thenReturn(employeurDto);
        when(candidatureService.getCandidaturesByEmployeur(1L))
                .thenReturn(List.of(candidature1, candidature2));

        mockMvc.perform(get("/employeur/candidatures")
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(50))
                .andExpect(jsonPath("$[1].id").value(51));

        verify(candidatureService).getCandidaturesByEmployeur(1L);
    }

    @Test
    void getAllMyCandidatures_notEmployeur_returnsForbidden() throws Exception {
        UserDTO studentDto = EmployeurDto.builder()
                .id(2L)
                .role(Role.STUDENT)
                .build();

        when(userAppService.getMe(anyString())).thenReturn(studentDto);

        mockMvc.perform(get("/employeur/candidatures")
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isForbidden());

        verify(candidatureService, never()).getCandidaturesByEmployeur(anyLong());
    }
}
