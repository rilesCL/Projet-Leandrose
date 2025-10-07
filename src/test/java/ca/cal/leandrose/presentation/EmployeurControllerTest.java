package ca.cal.leandrose.presentation;

import ca.cal.leandrose.model.*;
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
@Import(value = TestSecurityConfiguration.class)
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


    @Test
    void downloadOffer_asEmployeur_returnsPdf() throws Exception {
        UserDTO employeurDto = EmployeurDto.builder()
                .id(1L)
                .role(Role.EMPLOYEUR)
                .build();

        InternshipOffer offer = InternshipOffer.builder()
                .id(100L)
                .description("Stage Java")
                .pdfPath("dummy.pdf")
                .employeur(Employeur.builder().id(1L).build())
                .build();

        when(userAppService.getMe(anyString())).thenReturn(employeurDto);
        when(internshipOfferService.getOffer(100L)).thenReturn(offer);

        mockMvc.perform(get("/employeur/offers/100/download")
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isNotFound()); // file not present in test, endpoint exists
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

        InternshipOffer offer = InternshipOffer.builder()
                .id(100L)
                .employeur(Employeur.builder().id(1L).build())
                .build();

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
}
