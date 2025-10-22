package ca.cal.leandrose.presentation;

import ca.cal.leandrose.model.Candidature;
import ca.cal.leandrose.model.auth.Role;
import ca.cal.leandrose.repository.EmployeurRepository;
import ca.cal.leandrose.security.TestSecurityConfiguration;
import ca.cal.leandrose.service.*;
import ca.cal.leandrose.service.dto.*;
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
    private EmployeurService employeurService;

    // Méthode utilitaire pour générer des CandidatureDto imbriqués
    private CandidatureDto createCandidatureDto(Long id, Long employeurId, String studentFirstName, String studentLastName) {
        EmployeurDto employeurDto = EmployeurDto.builder()
                .id(employeurId)
                .role(Role.EMPLOYEUR)
                .firstName("Employeur")
                .lastname("Test")
                .email("employeur@test.com")
                .companyName("TechCorp")
                .build();

        return CandidatureDto.builder()
                .id(id)
                .student(StudentDto.builder()
                        .id(1L)
                        .firstName(studentFirstName)
                        .lastName(studentLastName)
                        .build())
                .internshipOffer(InternshipOfferDto.builder()
                        .id(100L)
                        .description("Stage Java")
                        .employeurDto(employeurDto)
                        .pdfPath("dummy.pdf")
                        .build())
                .cv(CvDto.builder()
                        .id(10L)
                        .pdfPath("dummy.pdf")
                        .build())
                .status(Candidature.Status.PENDING)
                .applicationDate(LocalDateTime.now())
                .build();
    }

    // ================= TESTS DE TÉLÉCHARGEMENT OFFRE =================

    @Test
    void downloadOffer_asEmployeur_returnsPdf() throws Exception {
        EmployeurDto employeurDto = EmployeurDto.builder().id(1L).role(Role.EMPLOYEUR).build();
        InternshipOfferDto offer = InternshipOfferDto.builder()
                .id(100L)
                .description("Stage Java")
                .employeurDto(employeurDto)
                .pdfPath("dummy.pdf")
                .build();

        when(userAppService.getMe(anyString())).thenReturn(employeurDto);
        when(internshipOfferService.getOffer(100L)).thenReturn(offer);

        mockMvc.perform(get("/employeur/offers/100/download")
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isOk());
    }

    @Test
    void downloadOffer_notEmployeur_returnsForbidden() throws Exception {
        UserDTO studentDto = StudentDto.builder().id(2L).role(Role.STUDENT).build();
        when(userAppService.getMe(anyString())).thenReturn(studentDto);

        mockMvc.perform(get("/employeur/offers/100/download")
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isForbidden());
    }

    // ================= TESTS CONVOCATIONS =================

    @Test
    void getConvocationsByOffer_asEmployeur_returnsList() throws Exception {
        EmployeurDto employeurDto = EmployeurDto.builder().id(1L).role(Role.EMPLOYEUR).build();
        InternshipOfferDto offer = InternshipOfferDto.builder().id(100L).employeurDto(employeurDto).build();

        ConvocationDto convocationDto = ConvocationDto.builder().id(10L).location("Bureau 301").build();

        when(userAppService.getMe(anyString())).thenReturn(employeurDto);
        when(internshipOfferService.getOffer(100L)).thenReturn(offer);
        when(convocationService.getAllConvocationsByInterShipOfferId(100L)).thenReturn(List.of(convocationDto));

        mockMvc.perform(get("/employeur/offers/100/convocations")
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10))
                .andExpect(jsonPath("$[0].location").value("Bureau 301"));
    }

    @Test
    void getConvocationsByOffer_notEmployeur_returnsForbidden() throws Exception {
        UserDTO studentDto = StudentDto.builder().id(2L).role(Role.STUDENT).build();
        when(userAppService.getMe(anyString())).thenReturn(studentDto);

        mockMvc.perform(get("/employeur/offers/100/convocations")
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isForbidden());
    }

    @Test
    void createConvocation_asEmployeur_createsSuccessfully() throws Exception {
        EmployeurDto employeurDto = EmployeurDto.builder().id(1L).role(Role.EMPLOYEUR).build();
        ConvocationDto request = new ConvocationDto();
        request.setConvocationDate(LocalDateTime.now().plusDays(5));
        request.setLocation("Bureau 301");
        request.setMessage("Message perso");

        CandidatureDto candidatureDto = createCandidatureDto(50L, 1L, "Alice", "Martin");

        when(userAppService.getMe(anyString())).thenReturn(employeurDto);
        when(candidatureService.getCandidatureById(50L)).thenReturn(candidatureDto);
        doNothing().when(convocationService).addConvocation(anyLong(), any(), anyString(), anyString());

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
        UserDTO studentDto = StudentDto.builder().id(2L).role(Role.STUDENT).build();
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
        EmployeurDto employeurDto = EmployeurDto.builder().id(1L).role(Role.EMPLOYEUR).build();
        ConvocationDto request = new ConvocationDto();
        request.setConvocationDate(LocalDateTime.now().plusDays(5));
        request.setLocation("Bureau 301");
        request.setMessage("Message perso");

        CandidatureDto candidatureDto = createCandidatureDto(50L, 1L, "Alice", "Martin");

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

    // ================= TESTS ACCEPTATION =================

    @Test
    void acceptCandidature_asEmployeur_returnsAcceptedByEmployeur() throws Exception {
        EmployeurDto employeurDto = EmployeurDto.builder().id(1L).role(Role.EMPLOYEUR).build();
        CandidatureDto candidatureDto = createCandidatureDto(100L, 1L, "Alice", "Martin");
        candidatureDto.setStatus(Candidature.Status.ACCEPTEDBYEMPLOYEUR);

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
        UserDTO studentDto = StudentDto.builder().id(2L).role(Role.STUDENT).build();
        when(userAppService.getMe(anyString())).thenReturn(studentDto);

        mockMvc.perform(post("/employeur/candidatures/100/accept")
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isForbidden());

        verify(candidatureService, never()).acceptByEmployeur(anyLong());
    }

    // ================= TESTS REJET =================

    @Test
    void rejectCandidature_asEmployeur_returnsRejected() throws Exception {
        EmployeurDto employeurDto = EmployeurDto.builder().id(1L).role(Role.EMPLOYEUR).build();
        CandidatureDto candidatureDto = createCandidatureDto(100L, 1L, "Alice", "Martin");
        candidatureDto.setStatus(Candidature.Status.REJECTED);

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

    // ================= TESTS RÉCUPÉRATION DES CANDIDATURES =================

    @Test
    void getCandidaturesForOffer_asEmployeur_returnsList() throws Exception {
        EmployeurDto employeurDto = EmployeurDto.builder().id(1L).role(Role.EMPLOYEUR).build();
        InternshipOfferDto offer = InternshipOfferDto.builder()
                .id(100L)
                .employeurDto(employeurDto)
                .build();
        CandidatureDto candidatureDto = createCandidatureDto(50L, 1L, "Alice", "Martin");

        CandidatureEmployeurDto candidatureEmployeurDto = CandidatureEmployeurDto.builder()
                .id(candidatureDto.getId())
                .studentFirstName(candidatureDto.getStudent().getFirstName())
                .studentLastName(candidatureDto.getStudent().getLastName())
                .status(candidatureDto.getStatus())
                .build();

        when(userAppService.getMe(anyString())).thenReturn(employeurDto);
        when(internshipOfferService.getOffer(100L)).thenReturn(offer);
        when(candidatureService.getCandidaturesByOffer(100L)).thenReturn(List.of(candidatureEmployeurDto));

        mockMvc.perform(get("/employeur/offres/100/candidatures")
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(50))
                .andExpect(jsonPath("$[0].studentFirstName").value("Alice"))
                .andExpect(jsonPath("$[0].studentLastName").value("Martin"));
    }

    @Test
    void getAllMyCandidatures_asEmployeur_returnsList() throws Exception {
        EmployeurDto employeurDto = EmployeurDto.builder().id(1L).role(Role.EMPLOYEUR).build();
        CandidatureDto cand1 = createCandidatureDto(50L, 1L, "Alice", "Martin");
        CandidatureDto cand2 = createCandidatureDto(51L, 1L, "Bob", "Dupont");

        CandidatureEmployeurDto candDto1 = CandidatureEmployeurDto.builder()
                .id(cand1.getId())
                .studentFirstName(cand1.getStudent().getFirstName())
                .studentLastName(cand1.getStudent().getLastName())
                .status(cand1.getStatus())
                .build();

        CandidatureEmployeurDto candDto2 = CandidatureEmployeurDto.builder()
                .id(cand2.getId())
                .studentFirstName(cand2.getStudent().getFirstName())
                .studentLastName(cand2.getStudent().getLastName())
                .status(cand2.getStatus())
                .build();

        when(userAppService.getMe(anyString())).thenReturn(employeurDto);
        when(candidatureService.getCandidaturesByEmployeur(1L)).thenReturn(List.of(candDto1, candDto2));

        mockMvc.perform(get("/employeur/candidatures")
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(50))
                .andExpect(jsonPath("$[1].id").value(51));
    }

}
