package ca.cal.leandrose.presentation;

import ca.cal.leandrose.model.EntenteStage;
import ca.cal.leandrose.security.TestSecurityConfiguration;
import ca.cal.leandrose.service.CvService;
import ca.cal.leandrose.service.EntenteStageService;
import ca.cal.leandrose.service.GestionnaireService;
import ca.cal.leandrose.service.InternshipOfferService;
import ca.cal.leandrose.service.dto.*;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(GestionnaireController.class)
@ActiveProfiles("test")
@Import(TestSecurityConfiguration.class)
class GestionnaireControllerEntenteTest {

    @Autowired private MockMvc mockMvc;

    @Autowired private ObjectMapper objectMapper;

    @MockitoBean private EntenteStageService ententeStageService;

    @MockitoBean private InternshipOfferService internshipOfferService;

    @MockitoBean private GestionnaireService gestionnaireService;

    @MockitoBean private CvService cvService;

    private EntenteStageDto ententeDto;
    private CandidatureDto candidatureDto;
    private StudentDto studentDto;
    private EmployeurDto employeurDto;
    private InternshipOfferDto offerDto;

    @BeforeEach
    void setUp() {
        objectMapper.registerModule(new JavaTimeModule());

        studentDto =
                StudentDto.builder()
                        .id(1L)
                        .firstName("John")
                        .lastName("Doe")
                        .email("john.doe@etu.ca")
                        .studentNumber("12345")
                        .program("Informatique")
                        .build();

        employeurDto =
                EmployeurDto.builder()
                        .id(2L)
                        .firstName("Marie")
                        .lastname("Dupont")
                        .email("marie@techcorp.com")
                        .companyName("TechCorp")
                        .field("Développement logiciel")
                        .build();

        offerDto =
                InternshipOfferDto.builder()
                        .id(10L)
                        .description("Stage développement web")
                        .startDate(LocalDate.of(2025, 6, 1))
                        .durationInWeeks(12)
                        .address("Montréal")
                        .remuneration(30f)
                        .status("ACCEPTEE")
                        .employeurDto(employeurDto)
                        .companyName(employeurDto.getCompanyName())
                        .build();

        ententeDto =
                EntenteStageDto.builder()
                        .id(1L)
                        .candidatureId(100L)
                        .student(studentDto)
                        .internshipOffer(offerDto)
                        .missionsObjectifs("Développement web")
                        .statut(EntenteStage.StatutEntente.BROUILLON)
                        .dateDebut(offerDto.getStartDate())
                        .duree(offerDto.getDurationInWeeks())
                        .lieu(offerDto.getAddress())
                        .remuneration(offerDto.getRemuneration())
                        .dateCreation(LocalDateTime.now())
                        .build();

        // ✅ Correction ici : structure imbriquée cohérente avec ton DTO
        candidatureDto =
                CandidatureDto.builder()
                        .id(1L)
                        .student(studentDto)
                        .internshipOffer(offerDto)
                        .status(ca.cal.leandrose.model.Candidature.Status.ACCEPTEDBYEMPLOYEUR)
                        .applicationDate(LocalDateTime.now())
                        .build();
    }

    @Test
    void getCandidaturesAcceptees_ShouldReturnListOfCandidatures() throws Exception {
        List<CandidatureDto> candidatures = List.of(candidatureDto);
        when(ententeStageService.getCandidaturesAcceptees()).thenReturn(candidatures);

        mockMvc
                .perform(get("/gestionnaire/ententes/candidatures/accepted"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].student.firstName").value("John"))
                .andExpect(jsonPath("$[0].internshipOffer.companyName").value("TechCorp"));

        verify(ententeStageService).getCandidaturesAcceptees();
    }

    @Test
    void creerEntente_ShouldReturnCreatedEntente_WhenValidData() throws Exception {
        when(ententeStageService.creerEntente(any(EntenteStageDto.class))).thenReturn(ententeDto);

        mockMvc
                .perform(
                        post("/gestionnaire/ententes")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(ententeDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.student.firstName").value("John"))
                .andExpect(jsonPath("$.student.lastName").value("Doe"))
                .andExpect(jsonPath("$.internshipOffer.companyName").value("TechCorp"))
                .andExpect(jsonPath("$.internshipOffer.employeurDto.email").value("marie@techcorp.com"))
                .andExpect(jsonPath("$.remuneration").value(30.0));

        verify(ententeStageService).creerEntente(any(EntenteStageDto.class));
    }

    @Test
    void modifierEntente_ShouldReturnUpdatedEntente_WhenValidData() throws Exception {
        ententeDto.setMissionsObjectifs("Mise à jour des objectifs");
        when(ententeStageService.modifierEntente(eq(1L), any(EntenteStageDto.class)))
                .thenReturn(ententeDto);

        mockMvc
                .perform(
                        put("/gestionnaire/ententes/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(ententeDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.missionsObjectifs").value("Mise à jour des objectifs"));

        verify(ententeStageService).modifierEntente(eq(1L), any(EntenteStageDto.class));
    }

    @Test
    void getEntente_ShouldReturnNestedStudentAndOfferInfos() throws Exception {
        when(ententeStageService.getEntenteById(1L)).thenReturn(ententeDto);

        mockMvc
                .perform(get("/gestionnaire/ententes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.student.firstName").value("John"))
                .andExpect(jsonPath("$.student.lastName").value("Doe"))
                .andExpect(jsonPath("$.internshipOffer.employeurDto.companyName").value("TechCorp"))
                .andExpect(jsonPath("$.internshipOffer.employeurDto.email").value("marie@techcorp.com"));
    }

    @Test
    void getAllEntentes_ShouldReturnListOfNestedDtos() throws Exception {
        EntenteStageDto entente2 =
                EntenteStageDto.builder()
                        .id(2L)
                        .student(StudentDto.builder().firstName("Jane").lastName("Smith").build())
                        .internshipOffer(InternshipOfferDto.builder().companyName("DevCorp").build())
                        .statut(EntenteStage.StatutEntente.VALIDEE)
                        .build();

        when(ententeStageService.getAllEntentes()).thenReturn(Arrays.asList(ententeDto, entente2));

        mockMvc
                .perform(get("/gestionnaire/ententes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(2))
                .andExpect(jsonPath("$[0].student.firstName").value("John"))
                .andExpect(jsonPath("$[1].internshipOffer.companyName").value("DevCorp"));
    }

    @Test
    void supprimerEntente_ShouldReturnNoContent() throws Exception {
        doNothing().when(ententeStageService).supprimerEntente(1L);

        mockMvc.perform(delete("/gestionnaire/ententes/1")).andExpect(status().isNoContent());

        verify(ententeStageService).supprimerEntente(1L);
    }
}
