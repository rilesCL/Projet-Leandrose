package ca.cal.leandrose.presentation;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import ca.cal.leandrose.model.Candidature;
import ca.cal.leandrose.model.EntenteStage;
import ca.cal.leandrose.model.InternshipOffer;
import ca.cal.leandrose.presentation.request.UpdateStudentInfoRequest;
import ca.cal.leandrose.repository.StudentRepository;
import ca.cal.leandrose.security.TestSecurityConfiguration;
import ca.cal.leandrose.service.*;
import ca.cal.leandrose.service.dto.*;
import ca.cal.leandrose.service.mapper.InternshipOfferMapper;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@WebMvcTest(controllers = StudentController.class)
@ActiveProfiles("test")
@Import(TestSecurityConfiguration.class)
class StudentControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private CvService cvService;

  @MockitoBean private StudentRepository studentRepository;

  @MockitoBean private UserAppService userAppService;

  @MockitoBean private CandidatureService candidatureService;

  @MockitoBean private InternshipOfferService internshipOfferService;

  @MockitoBean private StudentService studentService;

  @MockitoBean private ConvocationService convocationService;

  @MockitoBean private EntenteStageService ententeStageService;

  private StudentDto studentDto;
  private InternshipOfferDto internshipOfferDto;
  private CandidatureDto candidatureDto;
  @BeforeEach
  void setup() {
    studentDto =
        StudentDto.builder()
            .id(1L)
            .firstName("John")
            .lastName("Doe")
            .email("john@example.com")
            .role(ca.cal.leandrose.model.auth.Role.STUDENT)
            .program("COMPUTER_SCIENCE")
            .internshipTerm("WINTER 2026")
            .build();

      internshipOfferDto = InternshipOfferDto.builder()
              .id(1L)
              .description("Software Developer Internship")
              .employeurId(1L)
              .companyName("Tech Corp")
              .build();

      candidatureDto = CandidatureDto.builder()
              .id(100L)
              .student(studentDto)
              .internshipOffer(internshipOfferDto)
              .build();
  }

  @Test
  void getCv_missingAuthorization_returnsUnauthorized() throws Exception {
    mockMvc.perform(get("/student/cv")).andExpect(status().isUnauthorized());
  }

  @Test
  void uploadCv_asStudent_returnsCvDto() throws Exception {
    CvDto cvDto = CvDto.builder().id(1L).pdfPath("path/to/cv.pdf").build();

    when(userAppService.getMe(anyString())).thenReturn(studentDto);
    when(studentService.getStudentById(1L)).thenReturn(studentDto);
    when(cvService.uploadCv(eq(1L), any())).thenReturn(cvDto);

    MockMultipartFile file =
        new MockMultipartFile("pdfFile", "cv.pdf", "application/pdf", "PDF_CONTENT".getBytes());

    mockMvc
        .perform(multipart("/student/cv").file(file).header("Authorization", "Bearer token"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1));
  }

  @Test
  void uploadCv_notStudent_returnsForbidden() throws Exception {
    UserDTO dto = new UserDTO(2L, null, null, null, ca.cal.leandrose.model.auth.Role.EMPLOYEUR);
    when(userAppService.getMe(anyString())).thenReturn(dto);

    MockMultipartFile file =
        new MockMultipartFile("pdfFile", "cv.pdf", "application/pdf", "PDF_CONTENT".getBytes());

    mockMvc
        .perform(multipart("/student/cv").file(file).header("Authorization", "Bearer token"))
        .andExpect(status().isForbidden());
  }

  @Test
  void getCv_asStudent_returnsCvDto() throws Exception {
    CvDto cvDto = CvDto.builder().id(1L).pdfPath("path/to/cv.pdf").build();

    when(userAppService.getMe(anyString())).thenReturn(studentDto);
    when(studentService.getStudentById(1L)).thenReturn(studentDto);
    when(cvService.getCvByStudentId(1L)).thenReturn(cvDto);

    mockMvc
        .perform(get("/student/cv").header("Authorization", "Bearer token"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1));
  }

  @Test
  void getCv_notStudent_returnsForbidden() throws Exception {
    UserDTO dto = new UserDTO(1L, null, null, null, ca.cal.leandrose.model.auth.Role.EMPLOYEUR);
    when(userAppService.getMe(anyString())).thenReturn(dto);

    mockMvc
        .perform(get("/student/cv").header("Authorization", "Bearer token"))
        .andExpect(status().isForbidden());
  }

  @Test
  void getPublishedOffers_returnsList() throws Exception {
    InternshipOfferDto offer = InternshipOfferDto.builder().id(10L).description("Stage A").build();

    when(userAppService.getMe(anyString())).thenReturn(studentDto);
    when(studentService.getStudentById(1L)).thenReturn(studentDto);
    when(internshipOfferService.getPublishedOffersForStudents(anyString(), anyString()))
        .thenReturn(List.of(offer));

    mockMvc
        .perform(get("/student/offers").header("Authorization", "Bearer token"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].id").value(10));

    verify(internshipOfferService).getPublishedOffersForStudents(anyString(), anyString());
  }

  @Test
  void getPublishedOffers_notStudent_returnsForbidden() throws Exception {
    UserDTO dto = new UserDTO(2L, null, null, null, ca.cal.leandrose.model.auth.Role.EMPLOYEUR);
    when(userAppService.getMe(anyString())).thenReturn(dto);

    mockMvc
        .perform(get("/student/offers").header("Authorization", "Bearer token"))
        .andExpect(status().isForbidden());

    verify(internshipOfferService, never()).getPublishedOffersForStudents(anyString(), anyString());
  }

  @Test
  void getOfferDetails_publishedOffer_returnsOk() throws Exception {
    InternshipOfferDto offer =
        InternshipOfferMapper.toDto(
            InternshipOffer.builder().id(20L).status(InternshipOffer.Status.PUBLISHED).build());
    when(internshipOfferService.getOffer(20L)).thenReturn(offer);

    mockMvc
        .perform(get("/student/offers/20"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(20));
  }

  @Test
  void getOfferDetails_notPublished_returnsForbidden() throws Exception {
    InternshipOfferDto offer =
        InternshipOfferMapper.toDto(
            InternshipOffer.builder().id(21L).status(InternshipOffer.Status.REJECTED).build());
    when(internshipOfferService.getOffer(21L)).thenReturn(offer);

    mockMvc.perform(get("/student/offers/21")).andExpect(status().isForbidden());
  }

  @Test
  void downloadOfferPdf_published_returnsPdf() throws Exception {
    InternshipOfferDto offer =
        InternshipOfferMapper.toDto(
            InternshipOffer.builder().id(100L).status(InternshipOffer.Status.PUBLISHED).build());
    when(internshipOfferService.getOffer(100L)).thenReturn(offer);
    when(internshipOfferService.getOfferPdf(100L)).thenReturn("PDF_CONTENT".getBytes());

    mockMvc
        .perform(get("/student/offers/100/pdf"))
        .andExpect(status().isOk())
        .andExpect(header().string("Content-Type", "application/pdf"));
  }

  @Test
  void downloadOfferPdf_notPublished_returnsForbidden() throws Exception {
    InternshipOfferDto offer =
        InternshipOfferMapper.toDto(
            InternshipOffer.builder()
                .id(101L)
                .status(InternshipOffer.Status.PENDING_VALIDATION)
                .build());
    when(internshipOfferService.getOffer(101L)).thenReturn(offer);

    mockMvc.perform(get("/student/offers/101/pdf")).andExpect(status().isForbidden());
  }

  @Test
  void applyToOffer_asStudent_returnsCandidature() throws Exception {
    UserDTO studentDto =
        new UserDTO(1L, null, null, null, ca.cal.leandrose.model.auth.Role.STUDENT);

    StudentDto sDto =
        StudentDto.builder()
            .id(1L)
            .firstName("John")
            .lastName("Doe")
            .email("john@example.com")
            .build();
    InternshipOfferDto offerDto =
        InternshipOfferDto.builder()
            .id(200L)
            .description("Stage en Java")
            .companyName("TechCorp")
            .build();
    CvDto cvDto = CvDto.builder().id(300L).pdfPath("/cv/path.pdf").build();

    CandidatureDto candidature =
        CandidatureDto.builder()
            .id(100L)
            .student(sDto)
            .internshipOffer(offerDto)
            .cv(cvDto)
            .status(Candidature.Status.PENDING)
            .build();

    when(userAppService.getMe(anyString())).thenReturn(studentDto);
    when(candidatureService.postuler(1L, 200L, 300L)).thenReturn(candidature);

    mockMvc
        .perform(
            post("/student/offers/200/apply")
                .param("cvId", "300")
                .header("Authorization", "Bearer token"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(100));
  }

  @Test
  void applyToOffer_notStudent_returnsForbidden() throws Exception {
    UserDTO dto = new UserDTO(2L, null, null, null, ca.cal.leandrose.model.auth.Role.EMPLOYEUR);
    when(userAppService.getMe(anyString())).thenReturn(dto);

    mockMvc
        .perform(
            post("/student/offers/200/apply")
                .param("cvId", "300")
                .header("Authorization", "Bearer token"))
        .andExpect(status().isForbidden());
  }

  @Test
  void getMyCandidatures_asStudent_returnsList() throws Exception {
    UserDTO studentDto =
        new UserDTO(1L, null, null, null, ca.cal.leandrose.model.auth.Role.STUDENT);

    StudentDto sDto =
        StudentDto.builder()
            .id(1L)
            .firstName("John")
            .lastName("Doe")
            .email("john@example.com")
            .build();
    InternshipOfferDto offerDto =
        InternshipOfferDto.builder()
            .id(200L)
            .description("Stage A")
            .companyName("TechCorp")
            .build();
    CvDto cvDto = CvDto.builder().id(300L).pdfPath("/cv/path.pdf").build();

    CandidatureDto c =
        CandidatureDto.builder()
            .id(500L)
            .student(sDto)
            .internshipOffer(offerDto)
            .cv(cvDto)
            .status(Candidature.Status.PENDING)
            .build();

    when(userAppService.getMe(anyString())).thenReturn(studentDto);
    when(candidatureService.getCandidaturesByStudent(1L)).thenReturn(List.of(c));

    mockMvc
        .perform(get("/student/applications").header("Authorization", "Bearer token"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].id").value(500));
  }

  @Test
  void getMyCandidatures_notStudent_returnsForbidden() throws Exception {
    UserDTO dto = new UserDTO(2L, null, null, null, ca.cal.leandrose.model.auth.Role.EMPLOYEUR);
    when(userAppService.getMe(anyString())).thenReturn(dto);

    mockMvc
        .perform(get("/student/applications").header("Authorization", "Bearer token"))
        .andExpect(status().isForbidden());
  }

  @Test
  void acceptCandidature_asStudent_returnsAcceptedCandidature() throws Exception {
    UserDTO studentDto =
        new UserDTO(1L, null, null, null, ca.cal.leandrose.model.auth.Role.STUDENT);

    CandidatureDto candidature =
        CandidatureDto.builder()
            .id(100L)
            .status(Candidature.Status.ACCEPTED)
            .student(StudentDto.builder().id(1L).firstName("John").lastName("Doe").build())
            .internshipOffer(InternshipOfferDto.builder().id(200L).companyName("TechCorp").build())
            .build();

    when(userAppService.getMe(anyString())).thenReturn(studentDto);
    when(candidatureService.acceptByStudent(100L, 1L)).thenReturn(candidature);

    mockMvc
        .perform(post("/student/applications/100/accept").header("Authorization", "Bearer token"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(100))
        .andExpect(jsonPath("$.status").value("ACCEPTED"));

    verify(candidatureService).acceptByStudent(100L, 1L);
  }

  @Test
  void acceptCandidature_notStudent_returnsForbidden() throws Exception {
    UserDTO dto = new UserDTO(2L, null, null, null, ca.cal.leandrose.model.auth.Role.EMPLOYEUR);
    when(userAppService.getMe(anyString())).thenReturn(dto);

    mockMvc
        .perform(post("/student/applications/100/accept").header("Authorization", "Bearer token"))
        .andExpect(status().isForbidden());

    verify(candidatureService, never()).acceptByStudent(anyLong(), anyLong());
  }

  @Test
  void acceptCandidature_notAcceptedByEmployeur_returnsBadRequest() throws Exception {
    UserDTO studentDto =
        new UserDTO(1L, null, null, null, ca.cal.leandrose.model.auth.Role.STUDENT);

    when(userAppService.getMe(anyString())).thenReturn(studentDto);
    when(candidatureService.acceptByStudent(100L, 1L))
        .thenThrow(
            new IllegalStateException("L'employeur doit d'abord accepter cette candidature"));

    mockMvc
        .perform(post("/student/applications/100/accept").header("Authorization", "Bearer token"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error.message").value("L'employeur doit d'abord accepter cette candidature"));
  }

  @Test
  void acceptCandidature_candidatureNotFound_returnsNotFound() throws Exception {
    UserDTO studentDto =
        new UserDTO(1L, null, null, null, ca.cal.leandrose.model.auth.Role.STUDENT);

    when(userAppService.getMe(anyString())).thenReturn(studentDto);
    when(candidatureService.acceptByStudent(999L, 1L))
        .thenThrow(new RuntimeException("Candidature introuvable"));

    mockMvc
        .perform(post("/student/applications/999/accept").header("Authorization", "Bearer token"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.error.message").value("Candidature non trouvée"));
  }

  @Test
  void acceptCandidature_notOwnCandidature_returnsBadRequest() throws Exception {
    UserDTO studentDto =
        new UserDTO(1L, null, null, null, ca.cal.leandrose.model.auth.Role.STUDENT);

    when(userAppService.getMe(anyString())).thenReturn(studentDto);
    when(candidatureService.acceptByStudent(100L, 1L))
        .thenThrow(new IllegalStateException("Cette candidature ne vous appartient pas"));

    mockMvc
        .perform(post("/student/applications/100/accept").header("Authorization", "Bearer token"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error.message").value("Cette candidature ne vous appartient pas"));
  }

  @Test
  void rejectCandidature_asStudent_returnsRejectedCandidature() throws Exception {
    UserDTO studentDto =
        new UserDTO(1L, null, null, null, ca.cal.leandrose.model.auth.Role.STUDENT);
    CandidatureDto candidature =
        CandidatureDto.builder()
            .id(100L)
            .status(Candidature.Status.REJECTED)
            .student(StudentDto.builder().id(1L).firstName("John").lastName("Doe").build())
            .internshipOffer(InternshipOfferDto.builder().id(200L).companyName("TechCorp").build())
            .build();

    when(userAppService.getMe(anyString())).thenReturn(studentDto);
    when(candidatureService.rejectByStudent(100L, 1L)).thenReturn(candidature);

    mockMvc
        .perform(post("/student/applications/100/reject").header("Authorization", "Bearer token"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(100))
        .andExpect(jsonPath("$.status").value("REJECTED"));

    verify(candidatureService).rejectByStudent(100L, 1L);
  }

  @Test
  void rejectCandidature_notStudent_returnsForbidden() throws Exception {
    UserDTO dto = new UserDTO(2L, null, null, null, ca.cal.leandrose.model.auth.Role.EMPLOYEUR);
    when(userAppService.getMe(anyString())).thenReturn(dto);

    mockMvc
        .perform(post("/student/applications/100/reject").header("Authorization", "Bearer token"))
        .andExpect(status().isForbidden());

    verify(candidatureService, never()).rejectByStudent(anyLong(), anyLong());
  }

  @Test
  void rejectCandidature_notAcceptedByEmployeur_returnsBadRequest() throws Exception {
    UserDTO studentDto =
        new UserDTO(1L, null, null, null, ca.cal.leandrose.model.auth.Role.STUDENT);

    when(userAppService.getMe(anyString())).thenReturn(studentDto);
    when(candidatureService.rejectByStudent(100L, 1L))
        .thenThrow(
            new IllegalStateException(
                "Vous ne pouvez refuser que les candidatures acceptées par l'employeur"));

    mockMvc
        .perform(post("/student/applications/100/reject").header("Authorization", "Bearer token"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error.message").value(
                "Vous ne pouvez refuser que les candidatures acceptées par l'employeur"));
  }

  @Test
  void rejectCandidature_candidatureNotFound_returnsNotFound() throws Exception {
    UserDTO studentDto =
        new UserDTO(1L, null, null, null, ca.cal.leandrose.model.auth.Role.STUDENT);

    when(userAppService.getMe(anyString())).thenReturn(studentDto);
    when(candidatureService.rejectByStudent(999L, 1L))
        .thenThrow(new RuntimeException("Candidature introuvable"));

    mockMvc
        .perform(post("/student/applications/999/reject").header("Authorization", "Bearer token"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.error.message").value("Candidature non trouvée"));

  }

  @Test
  void rejectCandidature_notOwnCandidature_returnsBadRequest() throws Exception {
    UserDTO studentDto =
        new UserDTO(1L, null, null, null, ca.cal.leandrose.model.auth.Role.STUDENT);

    when(userAppService.getMe(anyString())).thenReturn(studentDto);
    when(candidatureService.rejectByStudent(100L, 1L))
        .thenThrow(new IllegalStateException("Cette candidature ne vous appartient pas"));

    mockMvc
        .perform(post("/student/applications/100/reject").header("Authorization", "Bearer token"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error.message").value("Cette candidature ne vous appartient pas"));

  }

  @Test
  void signerEntente_asStudent_returnsOk() throws Exception {
    UserDTO student = new UserDTO(1L, null, null, null, ca.cal.leandrose.model.auth.Role.STUDENT);
    EntenteStageDto ententeDto =
        EntenteStageDto.builder()
            .id(10L)
            .candidatureId(20L)
            .statut(EntenteStage.StatutEntente.EN_ATTENTE_SIGNATURE)
            .dateSignatureEtudiant(java.time.LocalDateTime.now())
            .build();

    when(userAppService.getMe(anyString())).thenReturn(student);
    when(ententeStageService.signerParEtudiant(10L, 1L)).thenReturn(ententeDto);

    mockMvc
        .perform(post("/student/ententes/10/signer").header("Authorization", "Bearer token"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(10))
        .andExpect(jsonPath("$.statut").value("EN_ATTENTE_SIGNATURE"));

    verify(ententeStageService).signerParEtudiant(10L, 1L);
  }

  @Test
  void signerEntente_notStudent_returnsForbidden() throws Exception {
    UserDTO employeur =
        new UserDTO(2L, null, null, null, ca.cal.leandrose.model.auth.Role.EMPLOYEUR);
    when(userAppService.getMe(anyString())).thenReturn(employeur);

    mockMvc
        .perform(post("/student/ententes/10/signer").header("Authorization", "Bearer token"))
        .andExpect(status().isForbidden());

    verify(ententeStageService, never()).signerParEtudiant(anyLong(), anyLong());
  }

  @Test
  void signerEntente_ententeNotFound_returnsNotFound() throws Exception {
    UserDTO student = new UserDTO(1L, null, null, null, ca.cal.leandrose.model.auth.Role.STUDENT);

    when(userAppService.getMe(anyString())).thenReturn(student);
    when(ententeStageService.signerParEtudiant(10L, 1L))
        .thenThrow(new jakarta.persistence.EntityNotFoundException("Entente non trouvée"));

    mockMvc
        .perform(post("/student/ententes/10/signer").header("Authorization", "Bearer token"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.error.message").value("Entente non trouvée"));
  }

  @Test
  void signerEntente_illegalArgument_returnsBadRequest() throws Exception {
    UserDTO student = new UserDTO(1L, null, null, null, ca.cal.leandrose.model.auth.Role.STUDENT);

    when(userAppService.getMe(anyString())).thenReturn(student);
    when(ententeStageService.signerParEtudiant(10L, 1L))
        .thenThrow(
            new IllegalArgumentException(
                "Cet étudiant n'est pas autorisé à signer cette entente."));

    mockMvc
        .perform(post("/student/ententes/10/signer").header("Authorization", "Bearer token"))
        .andExpect(status().isBadRequest())
        .andExpect(
            jsonPath("$.error.message")
                .value("Cet étudiant n'est pas autorisé à signer cette entente."));
  }

  @Test
  void signerEntente_illegalState_returnsBadRequest() throws Exception {
    UserDTO student = new UserDTO(1L, null, null, null, ca.cal.leandrose.model.auth.Role.STUDENT);

    when(userAppService.getMe(anyString())).thenReturn(student);
    when(ententeStageService.signerParEtudiant(10L, 1L))
        .thenThrow(new IllegalStateException("L'entente doit être en attente de signature."));

    mockMvc
        .perform(post("/student/ententes/10/signer").header("Authorization", "Bearer token"))
        .andExpect(status().isBadRequest())
        .andExpect(
            jsonPath("$.error.message").value("L'entente doit être en attente de signature."));
  }

  @Test
  void getEntentesPourEtudiant_asStudent_returnsFilteredList() throws Exception {
    UserDTO student =
        new UserDTO(
            1L, "John", "Doe", "john@example.com", ca.cal.leandrose.model.auth.Role.STUDENT);

    StudentDto studentDto =
        StudentDto.builder()
            .id(1L)
            .firstName("John")
            .lastName("Doe")
            .email("john@example.com")
            .build();

    EntenteStageDto entente1 =
        EntenteStageDto.builder()
            .id(10L)
            .candidatureId(20L)
            .student(studentDto)
            .statut(EntenteStage.StatutEntente.EN_ATTENTE_SIGNATURE)
            .build();

    when(userAppService.getMe(anyString())).thenReturn(student);
    when(ententeStageService.getEntentesByStudentId(1L)).thenReturn(List.of(entente1));

    mockMvc
        .perform(get("/student/ententes").header("Authorization", "Bearer token"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(1))
        .andExpect(jsonPath("$[0].id").value(10));

    verify(ententeStageService).getEntentesByStudentId(1L);
  }

  @Test
  void getEntentesPourEtudiant_notStudent_returnsForbidden() throws Exception {
    UserDTO employeur =
        new UserDTO(2L, null, null, null, ca.cal.leandrose.model.auth.Role.EMPLOYEUR);
    when(userAppService.getMe(anyString())).thenReturn(employeur);

    mockMvc
        .perform(get("/student/ententes").header("Authorization", "Bearer token"))
        .andExpect(status().isForbidden());

    verify(ententeStageService, never()).getEntentesByStudentId(anyLong());
  }

  @Test
  void getEntentesPourEtudiant_noEntentes_returnsEmptyList() throws Exception {
    UserDTO student =
        new UserDTO(
            1L, "John", "Doe", "john@example.com", ca.cal.leandrose.model.auth.Role.STUDENT);

    when(userAppService.getMe(anyString())).thenReturn(student);
    when(ententeStageService.getEntentesByStudentId(1L)).thenReturn(List.of());

    mockMvc
        .perform(get("/student/ententes").header("Authorization", "Bearer token"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(0));
  }

  @Test
  void getEntentesPourEtudiant_serviceThrowsException_returnsInternalServerError()
      throws Exception {
    UserDTO student =
        new UserDTO(
            1L, "John", "Doe", "john@example.com", ca.cal.leandrose.model.auth.Role.STUDENT);

    when(userAppService.getMe(anyString())).thenReturn(student);
    when(ententeStageService.getEntentesByStudentId(1L))
        .thenThrow(new RuntimeException("Database error"));

    mockMvc
        .perform(get("/student/ententes").header("Authorization", "Bearer token"))
        .andExpect(status().isInternalServerError());
  }

  @Test
  void getEntentePdf_asStudent_ownEntente_returnsPdf() throws Exception {
    UserDTO student =
        new UserDTO(
            1L, "John", "Doe", "john@example.com", ca.cal.leandrose.model.auth.Role.STUDENT);

    StudentDto studentDto =
        StudentDto.builder()
            .id(1L)
            .firstName("John")
            .lastName("Doe")
            .email("john@example.com")
            .build();

    EntenteStageDto entente =
        EntenteStageDto.builder()
            .id(10L)
            .candidatureId(20L)
            .student(studentDto)
            .cheminDocumentPDF("uploads/ententes/entente_10.pdf")
            .statut(EntenteStage.StatutEntente.EN_ATTENTE_SIGNATURE)
            .build();

    when(userAppService.getMe(anyString())).thenReturn(student);
    when(ententeStageService.getEntenteById(10L)).thenReturn(entente);

    mockMvc
        .perform(get("/student/ententes/10/pdf").header("Authorization", "Bearer token"))
        .andExpect(status().isNotFound());

    verify(ententeStageService).getEntenteById(10L);
  }

  @Test
  void getEntentePdf_notStudent_returnsForbidden() throws Exception {
    UserDTO employeur =
        new UserDTO(2L, null, null, null, ca.cal.leandrose.model.auth.Role.EMPLOYEUR);
    when(userAppService.getMe(anyString())).thenReturn(employeur);

    mockMvc
        .perform(get("/student/ententes/10/pdf").header("Authorization", "Bearer token"))
        .andExpect(status().isForbidden());

    verify(ententeStageService, never()).getEntenteById(anyLong());
  }

  @Test
  void getEntentePdf_notOwnEntente_returnsForbidden() throws Exception {
    UserDTO student =
        new UserDTO(
            1L, "John", "Doe", "john@example.com", ca.cal.leandrose.model.auth.Role.STUDENT);

    StudentDto otherStudent =
        StudentDto.builder()
            .id(2L)
            .firstName("Jane")
            .lastName("Smith")
            .email("jane@example.com")
            .build();

    EntenteStageDto entente =
        EntenteStageDto.builder()
            .id(10L)
            .candidatureId(20L)
            .student(otherStudent)
            .cheminDocumentPDF("uploads/ententes/entente_10.pdf")
            .statut(EntenteStage.StatutEntente.EN_ATTENTE_SIGNATURE)
            .build();

    when(userAppService.getMe(anyString())).thenReturn(student);
    when(ententeStageService.getEntenteById(10L)).thenReturn(entente);

    mockMvc
        .perform(get("/student/ententes/10/pdf").header("Authorization", "Bearer token"))
        .andExpect(status().isForbidden());
  }

  @Test
  void getEntentePdf_ententeNotFound_returnsNotFound() throws Exception {
    UserDTO student =
        new UserDTO(
            1L, "John", "Doe", "john@example.com", ca.cal.leandrose.model.auth.Role.STUDENT);

    when(userAppService.getMe(anyString())).thenReturn(student);
    when(ententeStageService.getEntenteById(999L))
        .thenThrow(new jakarta.persistence.EntityNotFoundException("Entente non trouvée"));

    mockMvc
        .perform(get("/student/ententes/999/pdf").header("Authorization", "Bearer token"))
        .andExpect(status().isNotFound());
  }

  @Test
  void getEntentePdf_noPdfPath_returnsNotFound() throws Exception {
    UserDTO student =
        new UserDTO(
            1L, "John", "Doe", "john@example.com", ca.cal.leandrose.model.auth.Role.STUDENT);

    StudentDto studentDto =
        StudentDto.builder()
            .id(1L)
            .firstName("John")
            .lastName("Doe")
            .email("john@example.com")
            .build();

    EntenteStageDto entente =
        EntenteStageDto.builder()
            .id(10L)
            .candidatureId(20L)
            .student(studentDto)
            .cheminDocumentPDF(null)
            .statut(EntenteStage.StatutEntente.BROUILLON)
            .build();

    when(userAppService.getMe(anyString())).thenReturn(student);
    when(ententeStageService.getEntenteById(10L)).thenReturn(entente);

    mockMvc
        .perform(get("/student/ententes/10/pdf").header("Authorization", "Bearer token"))
        .andExpect(status().isNotFound());
  }

  @Test
  void getEntentePdf_emptyPdfPath_returnsNotFound() throws Exception {
    UserDTO student =
        new UserDTO(
            1L, "John", "Doe", "john@example.com", ca.cal.leandrose.model.auth.Role.STUDENT);

    StudentDto studentDto =
        StudentDto.builder()
            .id(1L)
            .firstName("John")
            .lastName("Doe")
            .email("john@example.com")
            .build();

    EntenteStageDto entente =
        EntenteStageDto.builder()
            .id(10L)
            .candidatureId(20L)
            .student(studentDto)
            .cheminDocumentPDF("")
            .statut(EntenteStage.StatutEntente.BROUILLON)
            .build();

    when(userAppService.getMe(anyString())).thenReturn(student);
    when(ententeStageService.getEntenteById(10L)).thenReturn(entente);

    mockMvc
        .perform(get("/student/ententes/10/pdf").header("Authorization", "Bearer token"))
        .andExpect(status().isNotFound());
  }

  @Test
  void getEntentePdf_studentIsNull_returnsForbidden() throws Exception {
    UserDTO student =
        new UserDTO(
            1L, "John", "Doe", "john@example.com", ca.cal.leandrose.model.auth.Role.STUDENT);

    EntenteStageDto entente =
        EntenteStageDto.builder()
            .id(10L)
            .candidatureId(20L)
            .student(null)
            .cheminDocumentPDF("uploads/ententes/entente_10.pdf")
            .statut(EntenteStage.StatutEntente.EN_ATTENTE_SIGNATURE)
            .build();

    when(userAppService.getMe(anyString())).thenReturn(student);
    when(ententeStageService.getEntenteById(10L)).thenReturn(entente);

    mockMvc
        .perform(get("/student/ententes/10/pdf").header("Authorization", "Bearer token"))
        .andExpect(status().isForbidden());
  }

  @Test
  void getEntentePdf_malformedUrl_returnsInternalServerError() throws Exception {
    UserDTO student =
        new UserDTO(
            1L, "John", "Doe", "john@example.com", ca.cal.leandrose.model.auth.Role.STUDENT);

    StudentDto studentDto =
        StudentDto.builder()
            .id(1L)
            .firstName("John")
            .lastName("Doe")
            .email("john@example.com")
            .build();

    EntenteStageDto entente =
        EntenteStageDto.builder()
            .id(10L)
            .candidatureId(20L)
            .student(studentDto)
            .cheminDocumentPDF("://invalid-url")
            .statut(EntenteStage.StatutEntente.EN_ATTENTE_SIGNATURE)
            .build();

    when(userAppService.getMe(anyString())).thenReturn(student);
    when(ententeStageService.getEntenteById(10L)).thenReturn(entente);

    mockMvc
        .perform(get("/student/ententes/10/pdf").header("Authorization", "Bearer token"))
        .andExpect(status().isInternalServerError());
  }

  @Test
  void getMyProf_asStudent_withProf_returnsProfDto() throws Exception {
    UserDTO student =
        new UserDTO(
            1L, "John", "Doe", "john@example.com", ca.cal.leandrose.model.auth.Role.STUDENT);

    ProfDto profDto =
        ProfDto.builder()
            .id(10L)
            .firstName("Prof")
            .lastName("Smith")
            .email("prof@college.com")
            .role(ca.cal.leandrose.model.auth.Role.PROF)
            .employeeNumber("EMP001")
            .nameCollege("College Test")
            .department("Informatique")
            .build();

    when(userAppService.getMe(anyString())).thenReturn(student);
    when(studentService.getProfByStudentId(1L)).thenReturn(Optional.of(profDto));

    mockMvc
        .perform(get("/student/prof").header("Authorization", "Bearer token"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(10))
        .andExpect(jsonPath("$.firstName").value("Prof"))
        .andExpect(jsonPath("$.lastName").value("Smith"))
        .andExpect(jsonPath("$.email").value("prof@college.com"))
        .andExpect(jsonPath("$.employeeNumber").value("EMP001"));

    verify(studentService).getProfByStudentId(1L);
  }

  @Test
  void getMyProf_asStudent_noProf_returnsNotFound() throws Exception {
    UserDTO student =
        new UserDTO(
            1L, "John", "Doe", "john@example.com", ca.cal.leandrose.model.auth.Role.STUDENT);

    when(userAppService.getMe(anyString())).thenReturn(student);
    when(studentService.getProfByStudentId(1L)).thenReturn(Optional.empty());

    mockMvc
        .perform(get("/student/prof").header("Authorization", "Bearer token"))
        .andExpect(status().isNotFound());

    verify(studentService).getProfByStudentId(1L);
  }

  @Test
  void getMyProf_missingAuthorization_returnsUnauthorized() throws Exception {
    mockMvc.perform(get("/student/prof")).andExpect(status().isUnauthorized());
  }

  @Test
  void getMyProf_notStudent_returnsForbidden() throws Exception {
    UserDTO employeur =
        new UserDTO(2L, null, null, null, ca.cal.leandrose.model.auth.Role.EMPLOYEUR);
    when(userAppService.getMe(anyString())).thenReturn(employeur);

    mockMvc
        .perform(get("/student/prof").header("Authorization", "Bearer token"))
        .andExpect(status().isForbidden());

    verify(studentService, never()).getProfByStudentId(anyLong());
  }

  @Test
  void getMyProf_serviceThrowsException_returnsInternalServerError() throws Exception {
    UserDTO student =
        new UserDTO(
            1L, "John", "Doe", "john@example.com", ca.cal.leandrose.model.auth.Role.STUDENT);

    when(userAppService.getMe(anyString())).thenReturn(student);
    when(studentService.getProfByStudentId(1L)).thenThrow(new RuntimeException("Database error"));

    mockMvc
        .perform(get("/student/prof").header("Authorization", "Bearer token"))
        .andExpect(status().isInternalServerError());
  }

  @Test
  void getMyGestionnaire_asStudent_withGestionnaire_returnsGestionnaireDto() throws Exception {
    UserDTO student =
        new UserDTO(
            1L, "John", "Doe", "john@example.com", ca.cal.leandrose.model.auth.Role.STUDENT);

    GestionnaireDto gestionnaireDto =
        GestionnaireDto.builder()
            .id(5L)
            .firstName("Gest")
            .lastname("Manager")
            .email("gest@college.com")
            .role(ca.cal.leandrose.model.auth.Role.GESTIONNAIRE)
            .phoneNumber("514-123-4567")
            .build();

    when(userAppService.getMe(anyString())).thenReturn(student);
    when(studentService.getGestionnaireByStudentId(1L)).thenReturn(Optional.of(gestionnaireDto));

    mockMvc
        .perform(get("/student/gestionnaire").header("Authorization", "Bearer token"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(5))
        .andExpect(jsonPath("$.firstName").value("Gest"))
        .andExpect(jsonPath("$.lastName").value("Manager"))
        .andExpect(jsonPath("$.email").value("gest@college.com"))
        .andExpect(jsonPath("$.phoneNumber").value("514-123-4567"));

    verify(studentService).getGestionnaireByStudentId(1L);
  }

  @Test
  void getMyGestionnaire_asStudent_noGestionnaire_returnsNotFound() throws Exception {
    UserDTO student =
        new UserDTO(
            1L, "John", "Doe", "john@example.com", ca.cal.leandrose.model.auth.Role.STUDENT);

    when(userAppService.getMe(anyString())).thenReturn(student);
    when(studentService.getGestionnaireByStudentId(1L)).thenReturn(Optional.empty());

    mockMvc
        .perform(get("/student/gestionnaire").header("Authorization", "Bearer token"))
        .andExpect(status().isNotFound());

    verify(studentService).getGestionnaireByStudentId(1L);
  }

  @Test
  void getMyGestionnaire_missingAuthorization_returnsUnauthorized() throws Exception {
    mockMvc.perform(get("/student/gestionnaire")).andExpect(status().isUnauthorized());
  }

  @Test
  void getMyGestionnaire_notStudent_returnsForbidden() throws Exception {
    UserDTO employeur =
        new UserDTO(2L, null, null, null, ca.cal.leandrose.model.auth.Role.EMPLOYEUR);
    when(userAppService.getMe(anyString())).thenReturn(employeur);

    mockMvc
        .perform(get("/student/gestionnaire").header("Authorization", "Bearer token"))
        .andExpect(status().isForbidden());

    verify(studentService, never()).getGestionnaireByStudentId(anyLong());
  }

  @Test
  void getMyGestionnaire_serviceThrowsException_returnsInternalServerError() throws Exception {
    UserDTO student =
        new UserDTO(
            1L, "John", "Doe", "john@example.com", ca.cal.leandrose.model.auth.Role.STUDENT);

    when(userAppService.getMe(anyString())).thenReturn(student);
    when(studentService.getGestionnaireByStudentId(1L))
        .thenThrow(new RuntimeException("Database error"));

    mockMvc
        .perform(get("/student/gestionnaire").header("Authorization", "Bearer token"))
        .andExpect(status().isInternalServerError());
  }

  @Test
  void getMyEmployeurs_asStudent_withEmployeurs_returnsList() throws Exception {
    UserDTO student =
        new UserDTO(
            1L, "John", "Doe", "john@example.com", ca.cal.leandrose.model.auth.Role.STUDENT);

    EmployeurDto employeur1 =
        EmployeurDto.builder()
            .id(20L)
            .firstName("Employeur1")
            .lastname("One")
            .email("emp1@company.com")
            .role(ca.cal.leandrose.model.auth.Role.EMPLOYEUR)
            .companyName("TechCorp")
            .field("IT")
            .build();

    EmployeurDto employeur2 =
        EmployeurDto.builder()
            .id(21L)
            .firstName("Employeur2")
            .lastname("Two")
            .email("emp2@company.com")
            .role(ca.cal.leandrose.model.auth.Role.EMPLOYEUR)
            .companyName("DevCorp")
            .field("Software")
            .build();

    when(userAppService.getMe(anyString())).thenReturn(student);
    when(studentService.getEmployeursByStudentId(1L)).thenReturn(List.of(employeur1, employeur2));

    mockMvc
        .perform(get("/student/employeurs").header("Authorization", "Bearer token"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(2))
        .andExpect(jsonPath("$[0].id").value(20))
        .andExpect(jsonPath("$[0].companyName").value("TechCorp"))
        .andExpect(jsonPath("$[1].id").value(21))
        .andExpect(jsonPath("$[1].companyName").value("DevCorp"));

    verify(studentService).getEmployeursByStudentId(1L);
  }

  @Test
  void getMyEmployeurs_asStudent_noEmployeurs_returnsEmptyList() throws Exception {
    UserDTO student =
        new UserDTO(
            1L, "John", "Doe", "john@example.com", ca.cal.leandrose.model.auth.Role.STUDENT);

    when(userAppService.getMe(anyString())).thenReturn(student);
    when(studentService.getEmployeursByStudentId(1L)).thenReturn(List.of());

    mockMvc
        .perform(get("/student/employeurs").header("Authorization", "Bearer token"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(0));

    verify(studentService).getEmployeursByStudentId(1L);
  }

  @Test
  void getMyEmployeurs_missingAuthorization_returnsUnauthorized() throws Exception {
    mockMvc.perform(get("/student/employeurs")).andExpect(status().isUnauthorized());
  }

  @Test
  void getMyEmployeurs_notStudent_returnsForbidden() throws Exception {
    UserDTO employeur =
        new UserDTO(2L, null, null, null, ca.cal.leandrose.model.auth.Role.EMPLOYEUR);
    when(userAppService.getMe(anyString())).thenReturn(employeur);

    mockMvc
        .perform(get("/student/employeurs").header("Authorization", "Bearer token"))
        .andExpect(status().isForbidden());

    verify(studentService, never()).getEmployeursByStudentId(anyLong());
  }

  @Test
  void getMyEmployeurs_serviceThrowsException_returnsInternalServerError() throws Exception {
    UserDTO student =
        new UserDTO(
            1L, "John", "Doe", "john@example.com", ca.cal.leandrose.model.auth.Role.STUDENT);

    when(userAppService.getMe(anyString())).thenReturn(student);
    when(studentService.getEmployeursByStudentId(1L))
        .thenThrow(new RuntimeException("Database error"));

    mockMvc
        .perform(get("/student/employeurs").header("Authorization", "Bearer token"))
        .andExpect(status().isInternalServerError());
  }

  @Test
  void getCurrentStudent_asStudent_returnsStudentDto() throws Exception {
    when(userAppService.getMe(anyString())).thenReturn(studentDto);
    when(studentService.getStudentById(1L)).thenReturn(studentDto);

    mockMvc
        .perform(get("/student/me").header("Authorization", "Bearer token"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.firstName").value("John"));
  }

  @Test
  void getCurrentStudent_missingAuthorization_returnsUnauthorized() throws Exception {
    mockMvc.perform(get("/student/me")).andExpect(status().isUnauthorized());
  }

  @Test
  void getCurrentStudent_notStudent_returnsForbidden() throws Exception {
    UserDTO dto = new UserDTO(2L, null, null, null, ca.cal.leandrose.model.auth.Role.EMPLOYEUR);
    when(userAppService.getMe(anyString())).thenReturn(dto);

    mockMvc
        .perform(get("/student/me").header("Authorization", "Bearer token"))
        .andExpect(status().isForbidden());
  }

  @Test
  void getCurrentStudent_serviceThrowsException_returnsNotFound() throws Exception {
    when(userAppService.getMe(anyString())).thenReturn(studentDto);
    when(studentService.getStudentById(1L)).thenThrow(new RuntimeException("Student not found"));

    mockMvc
        .perform(get("/student/me").header("Authorization", "Bearer token"))
        .andExpect(status().isNotFound());
  }

  @Test
  void updateStudentInfo_asStudent_returnsUpdatedStudent() throws Exception {
    UpdateStudentInfoRequest request = new UpdateStudentInfoRequest();
    request.setProgram("SOFTWARE_ENGINEERING");

    StudentDto updated = StudentDto.builder()
        .id(1L)
        .firstName("John")
        .lastName("Doe")
        .program("SOFTWARE_ENGINEERING")
        .build();

    when(userAppService.getMe(anyString())).thenReturn(studentDto);
    when(studentService.updateStudentInfo(1L, "SOFTWARE_ENGINEERING")).thenReturn(updated);

    mockMvc
        .perform(put("/student/update-info")
            .header("Authorization", "Bearer token")
            .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
            .content("{\"program\":\"SOFTWARE_ENGINEERING\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.program").value("SOFTWARE_ENGINEERING"));
  }

  @Test
  void updateStudentInfo_notStudent_returnsForbidden() throws Exception {
    UserDTO dto = new UserDTO(2L, null, null, null, ca.cal.leandrose.model.auth.Role.EMPLOYEUR);
    when(userAppService.getMe(anyString())).thenReturn(dto);

    mockMvc
        .perform(put("/student/update-info")
            .header("Authorization", "Bearer token")
            .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
            .content("{\"program\":\"SOFTWARE_ENGINEERING\"}"))
        .andExpect(status().isForbidden());
  }

  @Test
  void updateStudentInfo_illegalArgument_returnsBadRequest() throws Exception {
    when(userAppService.getMe(anyString())).thenReturn(studentDto);
    when(studentService.updateStudentInfo(1L, "INVALID"))
        .thenThrow(new IllegalArgumentException("Invalid program"));

    mockMvc
        .perform(put("/student/update-info")
            .header("Authorization", "Bearer token")
            .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
            .content("{\"program\":\"INVALID\"}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error.error").value("Invalid program"));
  }

  @Test
  void updateStudentInfo_generalException_returnsInternalServerError() throws Exception {
    when(userAppService.getMe(anyString())).thenReturn(studentDto);
    when(studentService.updateStudentInfo(1L, "SOFTWARE_ENGINEERING"))
        .thenThrow(new RuntimeException("Database error"));

    mockMvc
        .perform(put("/student/update-info")
            .header("Authorization", "Bearer token")
            .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
            .content("{\"program\":\"SOFTWARE_ENGINEERING\"}"))
        .andExpect(status().isInternalServerError())
        .andExpect(jsonPath("$.error.error").value("Une erreur est survenue"));
  }

  @Test
  void getMyConvocations_asStudent_returnsList() throws Exception {
    ConvocationDto convocation = ConvocationDto.builder()
        .id(1L)
        .location("Bureau 301")
        .build();

    when(userAppService.getMe(anyString())).thenReturn(studentDto);
    when(convocationService.getConvocationsByStudentId(1L)).thenReturn(List.of(convocation));

    mockMvc
        .perform(get("/student/convocations").header("Authorization", "Bearer token"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].id").value(1))
        .andExpect(jsonPath("$[0].location").value("Bureau 301"));
  }

  @Test
  void getMyConvocations_notStudent_returnsForbidden() throws Exception {
    UserDTO dto = new UserDTO(2L, null, null, null, ca.cal.leandrose.model.auth.Role.EMPLOYEUR);
    when(userAppService.getMe(anyString())).thenReturn(dto);

    mockMvc
        .perform(get("/student/convocations").header("Authorization", "Bearer token"))
        .andExpect(status().isForbidden());
  }

  @Test
  void downloadCv_asStudent_returnsPdf() throws Exception {
    CvDto cvDto = CvDto.builder().id(1L).pdfPath("/path/to/cv.pdf").build();

    when(userAppService.getMe(anyString())).thenReturn(studentDto);
    when(studentService.getStudentById(1L)).thenReturn(studentDto);
    when(cvService.getCvByStudentId(1L)).thenReturn(cvDto);

    mockMvc
        .perform(get("/student/cv/download").header("Authorization", "Bearer token"))
        .andExpect(status().isNotFound());
  }

  @Test
  void downloadCv_missingAuthorization_returnsUnauthorized() throws Exception {
    mockMvc.perform(get("/student/cv/download")).andExpect(status().isUnauthorized());
  }

  @Test
  void downloadCv_notStudent_returnsForbidden() throws Exception {
    UserDTO dto = new UserDTO(2L, null, null, null, ca.cal.leandrose.model.auth.Role.EMPLOYEUR);
    when(userAppService.getMe(anyString())).thenReturn(dto);

    mockMvc
        .perform(get("/student/cv/download").header("Authorization", "Bearer token"))
        .andExpect(status().isForbidden());
  }

  @Test
  void downloadCv_cvNotFound_returnsNotFound() throws Exception {
    when(userAppService.getMe(anyString())).thenReturn(studentDto);
    when(studentService.getStudentById(1L)).thenReturn(studentDto);
    when(cvService.getCvByStudentId(1L)).thenThrow(new RuntimeException("CV not found"));

    mockMvc
        .perform(get("/student/cv/download").header("Authorization", "Bearer token"))
        .andExpect(status().isNotFound());
  }

  @Test
  void getCv_cvNotFound_returnsNotFound() throws Exception {
    when(userAppService.getMe(anyString())).thenReturn(studentDto);
    when(studentService.getStudentById(1L)).thenReturn(studentDto);
    when(cvService.getCvByStudentId(1L)).thenThrow(new RuntimeException("CV not found"));

    mockMvc
        .perform(get("/student/cv").header("Authorization", "Bearer token"))
        .andExpect(status().isNotFound());
  }

  @Test
  void applyToOffer_illegalStateException_returnsBadRequest() throws Exception {
    when(userAppService.getMe(anyString())).thenReturn(studentDto);
    when(candidatureService.postuler(1L, 200L, 300L))
        .thenThrow(new IllegalStateException("CV not approved"));

    mockMvc
        .perform(post("/student/offers/200/apply")
            .param("cvId", "300")
            .header("Authorization", "Bearer token"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void downloadOfferPdf_exception_returnsNotFound() throws Exception {
    InternshipOfferDto offer = InternshipOfferMapper.toDto(
        InternshipOffer.builder().id(100L).status(InternshipOffer.Status.PUBLISHED).build());
    when(internshipOfferService.getOffer(100L)).thenReturn(offer);
    when(internshipOfferService.getOfferPdf(100L))
        .thenThrow(new RuntimeException("File not found"));

    mockMvc
        .perform(get("/student/offers/100/pdf"))
        .andExpect(status().isNotFound());
  }
}
