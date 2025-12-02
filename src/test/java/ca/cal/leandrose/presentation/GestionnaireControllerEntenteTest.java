package ca.cal.leandrose.presentation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import ca.cal.leandrose.model.EntenteStage;
import ca.cal.leandrose.security.TestSecurityConfiguration;
import ca.cal.leandrose.service.*;
import ca.cal.leandrose.service.dto.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

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

  @MockitoBean private ChatService chatService;

  @MockitoBean private UserAppService userAppService;
  @MockitoBean private ProfService profService;

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

  @Test
  void creerEntente_ShouldReturnBadRequest_WhenIllegalArgumentException() throws Exception {
    when(ententeStageService.creerEntente(any(EntenteStageDto.class)))
        .thenThrow(new IllegalArgumentException("Invalid data"));
    mockMvc
        .perform(
            post("/gestionnaire/ententes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(ententeDto)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error.message").value("Invalid data"));
  }

  @Test
  void creerEntente_ShouldReturnNotFound_WhenEntityNotFoundException() throws Exception {
    when(ententeStageService.creerEntente(any(EntenteStageDto.class)))
        .thenThrow(new jakarta.persistence.EntityNotFoundException("Entity not found"));
    mockMvc
        .perform(
            post("/gestionnaire/ententes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(ententeDto)))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.error.message").value("Entity not found"));
  }

  @Test
  void modifierEntente_ShouldReturnNotFound_WhenEntityNotFoundException() throws Exception {
    when(ententeStageService.modifierEntente(eq(1L), any(EntenteStageDto.class)))
        .thenThrow(new jakarta.persistence.EntityNotFoundException("Not found"));
    mockMvc
        .perform(
            put("/gestionnaire/ententes/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(ententeDto)))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.error.message").value("Not found"));
  }

  @Test
  void modifierEntente_ShouldReturnConflict_WhenIllegalStateException() throws Exception {
    when(ententeStageService.modifierEntente(eq(1L), any(EntenteStageDto.class)))
        .thenThrow(new IllegalStateException("Already signed"));
    mockMvc
        .perform(
            put("/gestionnaire/ententes/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(ententeDto)))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.error.message").value("Already signed"));
  }

  @Test
  void modifierEntente_ShouldReturnBadRequest_WhenIllegalArgumentException() throws Exception {
    when(ententeStageService.modifierEntente(eq(1L), any(EntenteStageDto.class)))
        .thenThrow(new IllegalArgumentException("Invalid argument"));
    mockMvc
        .perform(
            put("/gestionnaire/ententes/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(ententeDto)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error.message").value("Invalid argument"));
  }

  @Test
  void validerEntente_ShouldReturnOk_WhenValid() throws Exception {
    ententeDto.setStatut(EntenteStage.StatutEntente.VALIDEE);
    when(ententeStageService.validerEtGenererEntente(1L)).thenReturn(ententeDto);
    mockMvc
        .perform(post("/gestionnaire/ententes/1/valider"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.statut").value("VALIDEE"));
    verify(ententeStageService, times(1)).validerEtGenererEntente(1L);
  }

  @Test
  void validerEntente_ShouldReturnNotFound_WhenEntityNotFoundException() throws Exception {
    when(ententeStageService.validerEtGenererEntente(1L))
        .thenThrow(new jakarta.persistence.EntityNotFoundException("Not found"));
    mockMvc
        .perform(post("/gestionnaire/ententes/1/valider"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.error.message").value("Not found"));
  }

  @Test
  void validerEntente_ShouldReturnConflict_WhenIllegalStateException() throws Exception {
    when(ententeStageService.validerEtGenererEntente(1L))
        .thenThrow(new IllegalStateException("Already signed"));
    mockMvc
        .perform(post("/gestionnaire/ententes/1/valider"))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.error.message").value("Already signed"));
  }

  @Test
  void telechargerPDFEntente_ShouldReturnPdfBytes() throws Exception {
    byte[] pdfBytes = "test pdf content".getBytes();
    when(ententeStageService.telechargerPDF(1L)).thenReturn(pdfBytes);
    mockMvc
        .perform(get("/gestionnaire/ententes/1/telecharger"))
        .andExpect(status().isOk())
        .andExpect(header().string("Content-Type", "application/pdf"))
        .andExpect(
            header()
                .string("Content-Disposition", "attachment; filename=\"entente_stage_1.pdf\""));
    verify(ententeStageService, times(1)).telechargerPDF(1L);
  }

  @Test
  void telechargerPDFEntente_ShouldReturnNotFound_WhenEntityNotFoundException() throws Exception {
    when(ententeStageService.telechargerPDF(1L))
        .thenThrow(new jakarta.persistence.EntityNotFoundException("Not found"));
    mockMvc
        .perform(get("/gestionnaire/ententes/1/telecharger"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.error.message").value("Not found"));
  }

  @Test
  void getEntente_ShouldReturnNotFound_WhenEntityNotFoundException() throws Exception {
    when(ententeStageService.getEntenteById(1L))
        .thenThrow(new jakarta.persistence.EntityNotFoundException("Not found"));
    mockMvc
        .perform(get("/gestionnaire/ententes/1"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.error.message").value("Not found"));
  }

  @Test
  void supprimerEntente_ShouldReturnNotFound_WhenEntityNotFoundException() throws Exception {
    doThrow(new jakarta.persistence.EntityNotFoundException("Not found"))
        .when(ententeStageService)
        .supprimerEntente(1L);
    mockMvc.perform(delete("/gestionnaire/ententes/1")).andExpect(status().isNotFound());
  }

  @Test
  void signerEntenteParGestionnaire_ShouldReturnOk_WhenValid() throws Exception {
    UserDTO gestionnaireDto = new UserDTO();
    gestionnaireDto.setId(1L);
    gestionnaireDto.setRole(ca.cal.leandrose.model.auth.Role.GESTIONNAIRE);
    when(userAppService.getMe(anyString())).thenReturn(gestionnaireDto);
    when(ententeStageService.signerParGestionnaire(1L, 1L)).thenReturn(ententeDto);
    mockMvc
        .perform(
            post("/gestionnaire/ententes/1/signer")
                .header("Authorization", "Bearer token"))
        .andExpect(status().isOk());
    verify(ententeStageService, times(1)).signerParGestionnaire(1L, 1L);
  }

  @Test
  void signerEntenteParGestionnaire_ShouldReturnForbidden_WhenNotGestionnaire() throws Exception {
    UserDTO studentDto = new UserDTO();
    studentDto.setId(1L);
    studentDto.setRole(ca.cal.leandrose.model.auth.Role.STUDENT);
    when(userAppService.getMe(anyString())).thenReturn(studentDto);
    mockMvc
        .perform(
            post("/gestionnaire/ententes/1/signer")
                .header("Authorization", "Bearer token"))
        .andExpect(status().isForbidden());
  }

  @Test
  void signerEntenteParGestionnaire_ShouldReturnNotFound_WhenEntityNotFoundException()
      throws Exception {
    UserDTO gestionnaireDto = new UserDTO();
    gestionnaireDto.setId(1L);
    gestionnaireDto.setRole(ca.cal.leandrose.model.auth.Role.GESTIONNAIRE);
    when(userAppService.getMe(anyString())).thenReturn(gestionnaireDto);
    when(ententeStageService.signerParGestionnaire(1L, 1L))
        .thenThrow(new jakarta.persistence.EntityNotFoundException("Not found"));
    mockMvc
        .perform(
            post("/gestionnaire/ententes/1/signer")
                .header("Authorization", "Bearer token"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.error.message").value("Entente non trouvée"));
  }

  @Test
  void signerEntenteParGestionnaire_ShouldReturnConflict_WhenAlreadySigned() throws Exception {
    UserDTO gestionnaireDto = new UserDTO();
    gestionnaireDto.setId(1L);
    gestionnaireDto.setRole(ca.cal.leandrose.model.auth.Role.GESTIONNAIRE);
    when(userAppService.getMe(anyString())).thenReturn(gestionnaireDto);
    when(ententeStageService.signerParGestionnaire(1L, 1L))
        .thenThrow(new IllegalStateException("déjà signé"));
    mockMvc
        .perform(
            post("/gestionnaire/ententes/1/signer")
                .header("Authorization", "Bearer token"))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.error.message").value("déjà signé"));
  }

  @Test
  void getAllProfs_ShouldReturnListOfProfs() throws Exception {
    ProfDto prof1 = ProfDto.builder().id(1L).firstName("Jean").lastName("Dupont").build();
    ProfDto prof2 = ProfDto.builder().id(2L).firstName("Marie").lastName("Martin").build();
    when(profService.getAllProfs()).thenReturn(List.of(prof1, prof2));
    mockMvc
        .perform(get("/gestionnaire/profs"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.size()").value(2))
        .andExpect(jsonPath("$[0].firstName").value("Jean"))
        .andExpect(jsonPath("$[1].firstName").value("Marie"));
    verify(profService, times(1)).getAllProfs();
  }

  @Test
  void getAllProfs_ShouldReturnInternalServerError_WhenException() throws Exception {
    when(profService.getAllProfs()).thenThrow(new RuntimeException("Database error"));
    mockMvc
        .perform(get("/gestionnaire/profs"))
        .andExpect(status().isInternalServerError())
        .andExpect(jsonPath("$.size()").value(0));
  }

  @Test
  void attribuerProf_ShouldReturnOk_WhenValid() throws Exception {
    when(ententeStageService.attribuerProf(1L, 2L)).thenReturn(ententeDto);
    mockMvc
        .perform(
            post("/gestionnaire/ententes/1/attribuer-prof")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"profId\":2}"))
        .andExpect(status().isOk());
    verify(ententeStageService, times(1)).attribuerProf(1L, 2L);
  }

  @Test
  void attribuerProf_ShouldReturnBadRequest_WhenProfIdIsNull() throws Exception {
    mockMvc
        .perform(
            post("/gestionnaire/ententes/1/attribuer-prof")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error.message").value("L'id du professeur est requis"));
  }

  @Test
  void attribuerProf_ShouldReturnNotFound_WhenEntityNotFoundException() throws Exception {
    when(ententeStageService.attribuerProf(1L, 2L))
        .thenThrow(new jakarta.persistence.EntityNotFoundException("Not found"));
    mockMvc
        .perform(
            post("/gestionnaire/ententes/1/attribuer-prof")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"profId\":2}"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.error.message").value("Not found"));
  }

  @Test
  void attribuerProf_ShouldReturnBadRequest_WhenIllegalArgumentException() throws Exception {
    when(ententeStageService.attribuerProf(1L, 2L))
        .thenThrow(new IllegalArgumentException("Invalid argument"));
    mockMvc
        .perform(
            post("/gestionnaire/ententes/1/attribuer-prof")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"profId\":2}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error.message").value("Invalid argument"));
  }
}
