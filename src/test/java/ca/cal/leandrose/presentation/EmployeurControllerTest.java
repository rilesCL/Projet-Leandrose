package ca.cal.leandrose.presentation;

import ca.cal.leandrose.model.Candidature;
import ca.cal.leandrose.model.auth.Role;
import ca.cal.leandrose.repository.EmployeurRepository;
import ca.cal.leandrose.security.TestSecurityConfiguration;
import ca.cal.leandrose.service.*;
import ca.cal.leandrose.service.dto.*;
import ca.cal.leandrose.service.dto.evaluation.CreateEvaluationRequest;
import ca.cal.leandrose.service.dto.evaluation.EvaluationFormData;
import ca.cal.leandrose.service.dto.evaluation.EvaluationInfoDto;
import ca.cal.leandrose.service.dto.evaluation.EvaluationStagiaireDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = EmployeurController.class)
@ActiveProfiles("test")
@Import(TestSecurityConfiguration.class)
class EmployeurControllerTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @MockitoBean private UserAppService userAppService;

  @MockitoBean private InternshipOfferService internshipOfferService;

  @MockitoBean private EmployeurRepository employeurRepository;

  @MockitoBean private CandidatureService candidatureService;

  @MockitoBean private ConvocationService convocationService;

  @MockitoBean private EmployeurService employeurService;

  @MockitoBean private EntenteStageService ententeStageService;
  @MockitoBean private EvaluationStagiaireService evaluationStagiaireService;


  private EvaluationStagiaireDto evaluationDto;
  private EvaluationFormData formData;
  private EmployeurDto employeurDto;
  private StudentDto studentDto;
  private MockHttpServletRequest request;
  private InternshipOfferDto internshipOfferDto;


  @BeforeEach
  void setUp(){
      request = new MockHttpServletRequest();
      objectMapper = new ObjectMapper();

      employeurDto =  EmployeurDto.builder()
              .id(1L)
              .role(Role.EMPLOYEUR)
              .firstName("Employeur")
              .lastname("Test")
              .email("employeur@test.com")
              .companyName("TechCorp")
              .build();
      studentDto = StudentDto.builder()
              .id(2L)
              .firstName("Alice")
              .lastName("Smith")
              .email("alice@gmail.com")
              .role(Role.STUDENT)
              .studentNumber("fa,masd")
              .program("Computer Science")
              .internshipTerm("12")
              .build();
      internshipOfferDto = InternshipOfferDto.builder()
                      .id(100L)
                      .description("Stage Java")
                      .employeurDto(employeurDto)
                      .pdfPath("dummy.pdf")
                      .employeurId(1L)
                      .build();

      evaluationDto = new EvaluationStagiaireDto(
              3L, LocalDate.now(), 2L, 1L,  100L,"/path/to/pdf", false
      );
      formData = new EvaluationFormData(
              Map.of(), "General comment", 2, "Global appreciation",
              true, 10, "YES", true
      );
  }

  private CandidatureDto createCandidatureDto(
      Long id, Long employeurId, String studentFirstName, String studentLastName) {
    EmployeurDto employeurDto =
        EmployeurDto.builder()
            .id(employeurId)
            .role(Role.EMPLOYEUR)
            .firstName("Employeur")
            .lastname("Test")
            .email("employeur@test.com")
            .companyName("TechCorp")
            .build();

    return CandidatureDto.builder()
        .id(id)
        .student(
            StudentDto.builder()
                .id(1L)
                .firstName(studentFirstName)
                .lastName(studentLastName)
                .build())
        .internshipOffer(
            InternshipOfferDto.builder()
                .id(100L)
                .description("Stage Java")
                .employeurDto(employeurDto)
                .pdfPath("dummy.pdf")
                .employeurId(employeurId)
                .build())
        .cv(CvDto.builder().id(10L).pdfPath("dummy.pdf").build())
        .status(Candidature.Status.PENDING)
        .applicationDate(LocalDateTime.now())
        .build();
  }

  @Test
  void downloadOffer_notEmployeur_returnsForbidden() throws Exception {
    UserDTO studentDto = StudentDto.builder().id(2L).role(Role.STUDENT).build();
    when(userAppService.getMe(anyString())).thenReturn(studentDto);

    mockMvc
        .perform(get("/employeur/offers/100/download").header("Authorization", "Bearer token"))
        .andExpect(status().isForbidden());
  }

  @Test
  void downloadOffer_fileNotFound_returnsNotFound() throws Exception {
    EmployeurDto employeurDto = EmployeurDto.builder().id(1L).role(Role.EMPLOYEUR).build();
    InternshipOfferDto offer =
        InternshipOfferDto.builder()
            .id(100L)
            .description("Stage Java")
            .employeurDto(employeurDto)
            .pdfPath("/nonexistent/file.pdf")
            .build();

    when(userAppService.getMe(anyString())).thenReturn(employeurDto);
    when(internshipOfferService.getOffer(100L)).thenReturn(offer);
    when(internshipOfferService.getOfferPdf(100L))
        .thenThrow(new RuntimeException("File not found"));

    mockMvc
        .perform(get("/employeur/offers/100/download").header("Authorization", "Bearer token"))
        .andExpect(status().isNotFound());
  }

  @Test
  void getConvocationsByOffer_asEmployeur_returnsList() throws Exception {
    EmployeurDto employeurDto = EmployeurDto.builder().id(1L).role(Role.EMPLOYEUR).build();
    InternshipOfferDto offer =
        InternshipOfferDto.builder().id(100L).employeurDto(employeurDto).build();
    ConvocationDto convocationDto = ConvocationDto.builder().id(10L).location("Bureau 301").build();

    when(userAppService.getMe(anyString())).thenReturn(employeurDto);
    when(internshipOfferService.getOffer(100L)).thenReturn(offer);
    when(convocationService.getAllConvocationsByInterShipOfferId(100L))
        .thenReturn(List.of(convocationDto));

    mockMvc
        .perform(get("/employeur/offers/100/convocations").header("Authorization", "Bearer token"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].id").value(10))
        .andExpect(jsonPath("$[0].location").value("Bureau 301"));
  }

  @Test
  void getConvocationsByOffer_notEmployeur_returnsForbidden() throws Exception {
    UserDTO studentDto = StudentDto.builder().id(2L).role(Role.STUDENT).build();
    when(userAppService.getMe(anyString())).thenReturn(studentDto);

    mockMvc
        .perform(get("/employeur/offers/100/convocations").header("Authorization", "Bearer token"))
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

    ObjectMapper testObjectMapper = new ObjectMapper();
    testObjectMapper.registerModule(new JavaTimeModule());
    testObjectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    mockMvc
        .perform(
            post("/employeur/candidatures/50/convocations")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(testObjectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(content().string("Convocation créée avec succès"));
  }

  @Test
  void createConvocation_wrongEmployeur_returnsForbidden() throws Exception {
    EmployeurDto me = EmployeurDto.builder().id(1L).role(Role.EMPLOYEUR).build();
    CandidatureDto candidature = createCandidatureDto(77L, 2L, "Student", "X");

    ConvocationDto conv = new ConvocationDto();
    conv.setLocation("Wrong test");

    when(userAppService.getMe(anyString())).thenReturn(me);
    when(candidatureService.getCandidatureById(77L)).thenReturn(candidature);

    mockMvc
        .perform(
            post("/employeur/candidatures/77/convocations")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(conv)))
        .andExpect(status().isForbidden());
  }

  @Test
  void acceptCandidature_asEmployeur_returnsAcceptedByEmployeur() throws Exception {
    EmployeurDto employeurDto = EmployeurDto.builder().id(1L).role(Role.EMPLOYEUR).build();
    CandidatureDto candidatureDto = createCandidatureDto(100L, 1L, "Alice", "Martin");
    candidatureDto.setStatus(Candidature.Status.ACCEPTEDBYEMPLOYEUR);

    when(userAppService.getMe(anyString())).thenReturn(employeurDto);
    when(candidatureService.getCandidatureById(100L)).thenReturn(candidatureDto);
    when(candidatureService.acceptByEmployeur(100L)).thenReturn(candidatureDto);

    mockMvc
        .perform(post("/employeur/candidatures/100/accept").header("Authorization", "Bearer token"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(100))
        .andExpect(jsonPath("$.status").value("ACCEPTEDBYEMPLOYEUR"));
  }

  @Test
  void rejectCandidature_asEmployeur_returnsRejected() throws Exception {
    EmployeurDto employeurDto = EmployeurDto.builder().id(1L).role(Role.EMPLOYEUR).build();
    CandidatureDto candidatureDto = createCandidatureDto(100L, 1L, "Alice", "Martin");
    candidatureDto.setStatus(Candidature.Status.REJECTED);

    when(userAppService.getMe(anyString())).thenReturn(employeurDto);
    when(candidatureService.getCandidatureById(100L)).thenReturn(candidatureDto);
    when(candidatureService.rejectByEmployeur(100L)).thenReturn(candidatureDto);

    mockMvc
        .perform(post("/employeur/candidatures/100/reject").header("Authorization", "Bearer token"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(100))
        .andExpect(jsonPath("$.status").value("REJECTED"));
  }

  @Test
  void acceptCandidature_wrongEmployeur_returnsForbidden() throws Exception {
    EmployeurDto me = EmployeurDto.builder().id(1L).role(Role.EMPLOYEUR).build();
    CandidatureDto candidature = createCandidatureDto(200L, 2L, "Bob", "WrongEmp");

    when(userAppService.getMe(anyString())).thenReturn(me);
    when(candidatureService.getCandidatureById(200L)).thenReturn(candidature);

    mockMvc
        .perform(post("/employeur/candidatures/200/accept").header("Authorization", "Bearer token"))
        .andExpect(status().isForbidden());
  }

  @Test
  void acceptCandidature_notFound_returnsNotFound() throws Exception {
    EmployeurDto me = EmployeurDto.builder().id(1L).role(Role.EMPLOYEUR).build();
    when(userAppService.getMe(anyString())).thenReturn(me);
    when(candidatureService.getCandidatureById(999L))
        .thenThrow(new RuntimeException("Candidature not found"));

    mockMvc
        .perform(post("/employeur/candidatures/999/accept").header("Authorization", "Bearer token"))
        .andExpect(status().isNotFound());
  }

  @Test
  void getCandidaturesForOffer_asEmployeur_returnsList() throws Exception {
    EmployeurDto employeurDto = EmployeurDto.builder().id(1L).role(Role.EMPLOYEUR).build();
    InternshipOfferDto offer =
        InternshipOfferDto.builder().id(100L).employeurDto(employeurDto).build();
    CandidatureDto candidatureDto = createCandidatureDto(50L, 1L, "Alice", "Martin");

    CandidatureEmployeurDto candidatureEmployeurDto =
        CandidatureEmployeurDto.builder()
            .id(candidatureDto.getId())
            .studentFirstName(candidatureDto.getStudent().getFirstName())
            .studentLastName(candidatureDto.getStudent().getLastName())
            .status(candidatureDto.getStatus())
            .build();

    when(userAppService.getMe(anyString())).thenReturn(employeurDto);
    when(internshipOfferService.getOffer(100L)).thenReturn(offer);
    when(candidatureService.getCandidaturesByOffer(100L))
        .thenReturn(List.of(candidatureEmployeurDto));

    mockMvc
        .perform(get("/employeur/offers/100/candidatures").header("Authorization", "Bearer token"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].studentFirstName").value("Alice"))
        .andExpect(jsonPath("$[0].studentLastName").value("Martin"));
  }

  @Test
  void getAllMyCandidatures_asEmployeur_returnsList() throws Exception {
    EmployeurDto employeurDto = EmployeurDto.builder().id(1L).role(Role.EMPLOYEUR).build();
    CandidatureDto cand1 = createCandidatureDto(50L, 1L, "Alice", "Martin");
    CandidatureDto cand2 = createCandidatureDto(51L, 1L, "Bob", "Dupont");

    CandidatureEmployeurDto candDto1 =
        CandidatureEmployeurDto.builder()
            .id(cand1.getId())
            .studentFirstName(cand1.getStudent().getFirstName())
            .studentLastName(cand1.getStudent().getLastName())
            .status(cand1.getStatus())
            .build();

    CandidatureEmployeurDto candDto2 =
        CandidatureEmployeurDto.builder()
            .id(cand2.getId())
            .studentFirstName(cand2.getStudent().getFirstName())
            .studentLastName(cand2.getStudent().getLastName())
            .status(cand2.getStatus())
            .build();

    when(userAppService.getMe(anyString())).thenReturn(employeurDto);
    when(candidatureService.getCandidaturesByEmployeur(1L)).thenReturn(List.of(candDto1, candDto2));

    mockMvc
        .perform(get("/employeur/candidatures").header("Authorization", "Bearer token"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].id").value(50))
        .andExpect(jsonPath("$[1].id").value(51));
  }

    @Test
    void createEvaluation_success_returnsOk() throws Exception {
        when(userAppService.getMe(anyString())).thenReturn(employeurDto);
        when(evaluationStagiaireService.isEvaluationEligible(1L, 2L, 100L)).thenReturn(true);
        when(evaluationStagiaireService.createEvaluation(1L, 2L, 100L)).thenReturn(evaluationDto);

        CreateEvaluationRequest createRequest = new CreateEvaluationRequest(2L, 100L);

        mockMvc.perform(post("/employeur/evaluations")
                        .header("Authorization", "Bearer token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isOk());
    }

    @Test
    void createEvaluation_notEmployeur_returnsForbidden() throws Exception {
        when(userAppService.getMe(anyString())).thenReturn(studentDto);

        CreateEvaluationRequest createRequest = new CreateEvaluationRequest(2L, 3L);

        mockMvc.perform(post("/employeur/evaluations")
                        .header("Authorization", "Bearer token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    void createEvaluation_notEligible_returnsBadRequest() throws Exception {
        when(userAppService.getMe(anyString())).thenReturn(employeurDto);
        when(evaluationStagiaireService.isEvaluationEligible(1L, 2L, 3L)).thenReturn(false);

        CreateEvaluationRequest createRequest = new CreateEvaluationRequest(2L, 3L);

        mockMvc.perform(post("/employeur/evaluations")
                        .header("Authorization", "Bearer token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createEvaluation_serviceException_returnsBadRequest() throws Exception {
        when(userAppService.getMe(anyString())).thenReturn(employeurDto);
        when(evaluationStagiaireService.isEvaluationEligible(1L, 2L, 3L)).thenReturn(true);
        when(evaluationStagiaireService.createEvaluation(1L, 2L, 3L))
                .thenThrow(new RuntimeException("Evaluation error"));

        CreateEvaluationRequest createRequest = new CreateEvaluationRequest(2L, 3L);

        mockMvc.perform(post("/employeur/evaluations")
                        .header("Authorization", "Bearer token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void generateEvaluationPdf_success_returnsOk() throws Exception {
        when(userAppService.getMe(anyString())).thenReturn(employeurDto);
        when(evaluationStagiaireService.getEvaluationById(1L)).thenReturn(evaluationDto);
        when(evaluationStagiaireService.generateEvaluationPdfByEmployer(anyLong(), any(), anyString()))
                .thenReturn(evaluationDto);

        mockMvc.perform(post("/employeur/evaluations/1/generate-pdf")
                        .header("Authorization", "Bearer token")
                        .header("Accept-Language", "fr")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(formData)))
                .andExpect(status().isOk());
    }

    @Test
    void generateEvaluationPdf_notEmployeur_returnsForbidden() throws Exception {
        when(userAppService.getMe(anyString())).thenReturn(studentDto);

        mockMvc.perform(post("/employeur/evaluations/1/generate-pdf")
                        .header("Authorization", "Bearer token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(formData)))
                .andExpect(status().isForbidden());
    }

    @Test
    void generateEvaluationPdf_notOwner_returnsForbidden() throws Exception {
        EvaluationStagiaireDto otherEvaluation = new EvaluationStagiaireDto(1L, LocalDate.now(), 2L,1L, 100L, null, false);

        when(userAppService.getMe(anyString())).thenReturn(employeurDto);
        when(evaluationStagiaireService.getEvaluationById(1L)).thenReturn(otherEvaluation);

        mockMvc.perform(post("/employeur/evaluations/1/generate-pdf")
                        .header("Authorization", "Bearer token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(formData)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getEvaluationPdf_success_returnsOk() throws Exception {
        byte[] pdfBytes = "mock-pdf-content".getBytes();

        when(userAppService.getMe(anyString())).thenReturn(employeurDto);
        when(evaluationStagiaireService.getEvaluationById(1L)).thenReturn(evaluationDto);
        when(evaluationStagiaireService.getEvaluationPdf(1L)).thenReturn(pdfBytes);

        mockMvc.perform(get("/employeur/evaluations/1/pdf")
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF));
    }

    @Test
    void getEvaluationPdf_notFound_returnsNotFound() throws Exception {
        when(userAppService.getMe(anyString())).thenReturn(employeurDto);
        when(evaluationStagiaireService.getEvaluationById(1L)).thenReturn(evaluationDto);
        when(evaluationStagiaireService.getEvaluationPdf(1L))
                .thenThrow(new RuntimeException("PDF not found"));

        mockMvc.perform(get("/employeur/evaluations/1/pdf")
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getMyEvaluations_success_returnsOk() throws Exception {
        when(userAppService.getMe(anyString())).thenReturn(employeurDto);
        when(evaluationStagiaireService.getEvaluationsByEmployeur(1L)).thenReturn(List.of(evaluationDto));

        mockMvc.perform(get("/employeur/evaluations")
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isOk());
    }

    @Test
    void getMyEvaluations_notEmployeur_returnsForbidden() throws Exception {
        when(userAppService.getMe(anyString())).thenReturn(studentDto);

        mockMvc.perform(get("/employeur/evaluations")
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getMyEvaluations_serviceException_returnsInternalServerError() throws Exception {
        when(userAppService.getMe(anyString())).thenReturn(employeurDto);
        when(evaluationStagiaireService.getEvaluationsByEmployeur(1L))
                .thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(get("/employeur/evaluations")
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void getEvaluation_success_returnsOk() throws Exception {
        when(userAppService.getMe(anyString())).thenReturn(employeurDto);
        when(evaluationStagiaireService.getEvaluationById(1L)).thenReturn(evaluationDto);

        mockMvc.perform(get("/employeur/evaluation/1")
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isOk());
    }

    @Test
    void getEvaluation_notOwner_returnsForbidden() throws Exception {
        EmployeurDto differentEmployeur = EmployeurDto.builder()
                .id(999L)
                .role(Role.EMPLOYEUR)
                .firstName("Different")
                .lastname("Employer")
                .email("different@test.com")
                .companyName("DifferentCorp")
                .build();


        EvaluationStagiaireDto otherEvaluation = new EvaluationStagiaireDto(
                1L, LocalDate.now(), 2L, 1L, 100L, null, false
        );

        when(userAppService.getMe(anyString())).thenReturn(differentEmployeur);
        when(evaluationStagiaireService.getEvaluationById(1L)).thenReturn(otherEvaluation);

        mockMvc.perform(get("/employeur/evaluation/1")
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getEvaluation_notFound_returnsNotFound() throws Exception {
        when(userAppService.getMe(anyString())).thenReturn(employeurDto);
        when(evaluationStagiaireService.getEvaluationById(1L))
                .thenThrow(new RuntimeException("Evaluation not found"));

        mockMvc.perform(get("/employeur/evaluation/1")
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getEligibleEvaluations_success_returnsOk() throws Exception {
        when(userAppService.getMe(anyString())).thenReturn(employeurDto);
        when(evaluationStagiaireService.getEligibleEvaluations(1L)).thenReturn(List.of());

        mockMvc.perform(get("/employeur/evaluations/eligible")
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isOk());
    }

    @Test
    void getEligibleEvaluations_notEmployeur_returnsForbidden() throws Exception {
        when(userAppService.getMe(anyString())).thenReturn(studentDto);

        mockMvc.perform(get("/employeur/evaluations/eligible")
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getEvaluationInfo_success_returnsOk() throws Exception {
        when(userAppService.getMe(anyString())).thenReturn(employeurDto);
        when(evaluationStagiaireService.getEvaluationInfo(1L, 2L, 3L))
                .thenReturn(new EvaluationInfoDto(null, null));

        mockMvc.perform(get("/employeur/evaluations/info")
                        .header("Authorization", "Bearer token")
                        .param("studentId", "2")
                        .param("offerId", "3"))
                .andExpect(status().isOk());
    }

    @Test
    void getEvaluationInfo_serviceException_returnsBadRequest() throws Exception {
        when(userAppService.getMe(anyString())).thenReturn(employeurDto);
        when(evaluationStagiaireService.getEvaluationInfo(1L, 2L, 3L))
                .thenThrow(new RuntimeException("Evaluation not allowed"));

        mockMvc.perform(get("/employeur/evaluations/info")
                        .header("Authorization", "Bearer token")
                        .param("studentId", "2")
                        .param("offerId", "3"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void checkExistingEvaluation_exists_returnsOk() throws Exception {
        when(userAppService.getMe(anyString())).thenReturn(employeurDto);
        when(evaluationStagiaireService.getExistingEvaluation(2L, 3L))
                .thenReturn(Optional.of(evaluationDto));

        mockMvc.perform(get("/employeur/evaluations/check-existing")
                        .header("Authorization", "Bearer token")
                        .param("studentId", "2")
                        .param("offerId", "3"))
                .andExpect(status().isOk());
    }

    @Test
    void checkExistingEvaluation_notExists_returnsOk() throws Exception {
        when(userAppService.getMe(anyString())).thenReturn(employeurDto);
        when(evaluationStagiaireService.getExistingEvaluation(2L, 3L))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/employeur/evaluations/check-existing")
                        .header("Authorization", "Bearer token")
                        .param("studentId", "2")
                        .param("offerId", "3"))
                .andExpect(status().isOk());
    }

    @Test
    void checkExistingEvaluation_notEmployeur_returnsForbidden() throws Exception {
        when(userAppService.getMe(anyString())).thenReturn(studentDto);

        mockMvc.perform(get("/employeur/evaluations/check-existing")
                        .header("Authorization", "Bearer token")
                        .param("studentId", "2")
                        .param("offerId", "3"))
                .andExpect(status().isForbidden());
    }
}
