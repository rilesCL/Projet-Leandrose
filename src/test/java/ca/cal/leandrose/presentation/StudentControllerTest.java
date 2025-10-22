package ca.cal.leandrose.presentation;

import ca.cal.leandrose.model.Candidature;
import ca.cal.leandrose.model.EntenteStage;
import ca.cal.leandrose.model.InternshipOffer;
import ca.cal.leandrose.model.Student;
import ca.cal.leandrose.repository.StudentRepository;
import ca.cal.leandrose.security.TestSecurityConfiguration;
import ca.cal.leandrose.service.*;
import ca.cal.leandrose.service.dto.*;
import ca.cal.leandrose.service.mapper.InternshipOfferMapper;
import org.junit.jupiter.api.BeforeEach;
import ca.cal.leandrose.service.*;
import ca.cal.leandrose.service.dto.CandidatureDto;
import ca.cal.leandrose.service.dto.CvDto;
import ca.cal.leandrose.service.dto.UserDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = StudentController.class)
@ActiveProfiles("test")
@Import(TestSecurityConfiguration.class)
class StudentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CvService cvService;

    @MockitoBean
    private StudentRepository studentRepository;

    @MockitoBean
    private UserAppService userAppService;

    @MockitoBean
    private CandidatureService candidatureService;

    @MockitoBean
    private InternshipOfferService internshipOfferService;

    @MockitoBean
    private StudentService studentService;

    @MockitoBean
    private ConvocationService convocationService;
    private StudentDto studentDto;

    @BeforeEach
    void setup(){
        studentDto = StudentDto.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .role(ca.cal.leandrose.model.auth.Role.STUDENT)
                .program("COMPUTER_SCIENCE")
                .build();
    }

    @Test
    void getCv_missingAuthorization_returnsUnauthorized() throws Exception {
        mockMvc.perform(get("/student/cv"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getPublishedOffers_returnsList() throws Exception {
        InternshipOfferDto offer = InternshipOfferDto.builder()
                .id(10L)
                .description("Stage A")
                .build();

        when(userAppService.getMe(anyString())).thenReturn(studentDto);
        when(studentService.getStudentById(1L)).thenReturn(studentDto);
        when(internshipOfferService.getPublishedOffersForStudents(anyString()))
                .thenReturn(List.of(offer));

        mockMvc.perform(get("/student/offers")
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10));

        verify(internshipOfferService).getPublishedOffersForStudents(anyString());
    }

    @Test
    void getPublishedOffers_notStudent_returnsForbidden() throws Exception {
        UserDTO dto = new UserDTO(2L, null, null, null, ca.cal.leandrose.model.auth.Role.EMPLOYEUR);
        when(userAppService.getMe(anyString())).thenReturn(dto);

        mockMvc.perform(get("/student/offers")
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isForbidden());

        verify(internshipOfferService, never()).getPublishedOffersForStudents(any());
    }

    @Test
    void getOfferDetails_publishedOffer_returnsOk() throws Exception {
        InternshipOfferDto offer = InternshipOfferMapper.toDto(InternshipOffer.builder()
                .id(20L)
                .status(InternshipOffer.Status.PUBLISHED)
                .build());
        when(internshipOfferService.getOffer(20L)).thenReturn(offer);

        mockMvc.perform(get("/student/offers/20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(20));
    }

    @Test
    void getOfferDetails_notPublished_returnsForbidden() throws Exception {
        InternshipOfferDto offer = InternshipOfferMapper.toDto(InternshipOffer.builder()
                .id(21L)
                .status(InternshipOffer.Status.REJECTED)
                .build());
        when(internshipOfferService.getOffer(21L)).thenReturn(offer);

        mockMvc.perform(get("/student/offers/21"))
                .andExpect(status().isForbidden());
    }

    @Test
    void uploadCv_asStudent_returnsCvDto() throws Exception {
        CvDto cvDto = CvDto.builder()
                .id(1L)
                .pdfPath("path/to/cv.pdf")
                .build();

        when(userAppService.getMe(anyString())).thenReturn(studentDto);
        when(studentService.getStudentById(1L)).thenReturn(studentDto);
        when(cvService.uploadCv(eq(1L), any())).thenReturn(cvDto);

        MockMultipartFile file = new MockMultipartFile(
                "pdfFile", "cv.pdf", "application/pdf", "PDF_CONTENT".getBytes()
        );

        // Act + Assert
        mockMvc.perform(multipart("/student/cv")
                        .file(file)
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void uploadCv_notStudent_returnsForbidden() throws Exception {
        UserDTO dto = new UserDTO(2L, null, null, null, ca.cal.leandrose.model.auth.Role.EMPLOYEUR);
        when(userAppService.getMe(anyString())).thenReturn(dto);

        MockMultipartFile file = new MockMultipartFile("pdfFile", "cv.pdf", "application/pdf", "PDF_CONTENT".getBytes());

        mockMvc.perform(multipart("/student/cv")
                        .file(file)
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getCv_asStudent_returnsCvDto() throws Exception {

        Student student = Student.builder().id(1L).build();
        CvDto cvDto = CvDto.builder().id(1L).pdfPath("path/to/cv.pdf").build();

        when(userAppService.getMe(anyString())).thenReturn(studentDto);
        when(studentService.getStudentById(1L)).thenReturn(studentDto);

        when(cvService.getCvByStudentId(1L)).thenReturn(cvDto);

        mockMvc.perform(get("/student/cv")
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void getCv_notStudent_returnsForbidden() throws Exception {
        UserDTO dto = new UserDTO(1L, null, null, null, ca.cal.leandrose.model.auth.Role.EMPLOYEUR);
        when(userAppService.getMe(anyString())).thenReturn(dto);

        mockMvc.perform(get("/student/cv")
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isForbidden());
    }

    @Test
    void applyToOffer_asStudent_returnsCandidature() throws Exception {
        UserDTO studentDto = new UserDTO(1L, null, null, null, ca.cal.leandrose.model.auth.Role.STUDENT);
        CandidatureDto candidature = CandidatureDto.builder().id(100L).build();

        when(userAppService.getMe(anyString())).thenReturn(studentDto);
        when(candidatureService.postuler(1L, 200L, 300L)).thenReturn(candidature);

        mockMvc.perform(post("/student/offers/200/apply")
                        .param("cvId", "300")
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100));
    }

    @Test
    void applyToOffer_notStudent_returnsForbidden() throws Exception {
        UserDTO dto = new UserDTO(2L, null, null, null, ca.cal.leandrose.model.auth.Role.EMPLOYEUR);
        when(userAppService.getMe(anyString())).thenReturn(dto);

        mockMvc.perform(post("/student/offers/200/apply")
                        .param("cvId", "300")
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getMyCandidatures_asStudent_returnsList() throws Exception {
        UserDTO studentDto = new UserDTO(1L, null, null, null, ca.cal.leandrose.model.auth.Role.STUDENT);
        CandidatureDto c = CandidatureDto.builder().id(500L).build();

        when(userAppService.getMe(anyString())).thenReturn(studentDto);
        when(candidatureService.getCandidaturesByStudent(1L)).thenReturn(List.of(c));

        mockMvc.perform(get("/student/applications")
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(500));
    }

    @Test
    void getMyCandidatures_notStudent_returnsForbidden() throws Exception {
        UserDTO dto = new UserDTO(2L, null, null, null, ca.cal.leandrose.model.auth.Role.EMPLOYEUR);
        when(userAppService.getMe(anyString())).thenReturn(dto);

        mockMvc.perform(get("/student/applications")
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isForbidden());
    }

    @Test
    void downloadOfferPdf_published_returnsPdf() throws Exception {
        InternshipOfferDto offer = InternshipOfferMapper.toDto(InternshipOffer.builder()
                .id(100L)
                .status(InternshipOffer.Status.PUBLISHED)
                .build());
        when(internshipOfferService.getOffer(100L)).thenReturn(offer);
        when(internshipOfferService.getOfferPdf(100L)).thenReturn("PDF_CONTENT".getBytes());

        mockMvc.perform(get("/student/offers/100/pdf"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/pdf"));
    }

    @Test
    void downloadOfferPdf_notPublished_returnsForbidden() throws Exception {
        InternshipOfferDto offer = InternshipOfferMapper.toDto(InternshipOffer.builder()
                .id(101L)
                .status(InternshipOffer.Status.PENDING_VALIDATION)
                .build());
        when(internshipOfferService.getOffer(101L)).thenReturn(offer);

        mockMvc.perform(get("/student/offers/101/pdf"))
                .andExpect(status().isForbidden());
    }


// ===== TESTS POUR ACCEPTATION DE CANDIDATURE PAR L'ÉTUDIANT =====

    @Test
    void acceptCandidature_asStudent_returnsAcceptedCandidature() throws Exception {
        UserDTO studentDto = new UserDTO(1L, null, null, null, ca.cal.leandrose.model.auth.Role.STUDENT);
        CandidatureDto candidature = CandidatureDto.builder()
                .id(100L)
                .status(Candidature.Status.ACCEPTED)
                .build();

        when(userAppService.getMe(anyString())).thenReturn(studentDto);
        when(candidatureService.acceptByStudent(100L, 1L)).thenReturn(candidature);

        mockMvc.perform(post("/student/applications/100/accept")
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.status").value("ACCEPTED"));

        verify(candidatureService).acceptByStudent(100L, 1L);
    }

    @Test
    void acceptCandidature_notStudent_returnsForbidden() throws Exception {
        UserDTO dto = new UserDTO(2L, null, null, null, ca.cal.leandrose.model.auth.Role.EMPLOYEUR);
        when(userAppService.getMe(anyString())).thenReturn(dto);

        mockMvc.perform(post("/student/applications/100/accept")
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isForbidden());

        verify(candidatureService, never()).acceptByStudent(anyLong(), anyLong());
    }

    @Test
    void acceptCandidature_notAcceptedByEmployeur_returnsBadRequest() throws Exception {
        UserDTO studentDto = new UserDTO(1L, null, null, null, ca.cal.leandrose.model.auth.Role.STUDENT);

        when(userAppService.getMe(anyString())).thenReturn(studentDto);
        when(candidatureService.acceptByStudent(100L, 1L))
                .thenThrow(new IllegalStateException("L'employeur doit d'abord accepter cette candidature"));

        mockMvc.perform(post("/student/applications/100/accept")
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("L'employeur doit d'abord accepter cette candidature"));
    }

    @Test
    void acceptCandidature_candidatureNotFound_returnsNotFound() throws Exception {
        UserDTO studentDto = new UserDTO(1L, null, null, null, ca.cal.leandrose.model.auth.Role.STUDENT);

        when(userAppService.getMe(anyString())).thenReturn(studentDto);
        when(candidatureService.acceptByStudent(999L, 1L))
                .thenThrow(new RuntimeException("Candidature introuvable"));

        mockMvc.perform(post("/student/applications/999/accept")
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Candidature non trouvée"));
    }

    @Test
    void acceptCandidature_notOwnCandidature_returnsBadRequest() throws Exception {
        UserDTO studentDto = new UserDTO(1L, null, null, null, ca.cal.leandrose.model.auth.Role.STUDENT);

        when(userAppService.getMe(anyString())).thenReturn(studentDto);
        when(candidatureService.acceptByStudent(100L, 1L))
                .thenThrow(new IllegalStateException("Cette candidature ne vous appartient pas"));

        mockMvc.perform(post("/student/applications/100/accept")
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Cette candidature ne vous appartient pas"));
    }

// ===== TESTS POUR REJET DE CANDIDATURE PAR L'ÉTUDIANT =====

    @Test
    void rejectCandidature_asStudent_returnsRejectedCandidature() throws Exception {
        UserDTO studentDto = new UserDTO(1L, null, null, null, ca.cal.leandrose.model.auth.Role.STUDENT);
        CandidatureDto candidature = CandidatureDto.builder()
                .id(100L)
                .status(Candidature.Status.REJECTED)
                .build();

        when(userAppService.getMe(anyString())).thenReturn(studentDto);
        when(candidatureService.rejectByStudent(100L, 1L)).thenReturn(candidature);

        mockMvc.perform(post("/student/applications/100/reject")
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.status").value("REJECTED"));

        verify(candidatureService).rejectByStudent(100L, 1L);
    }

    @Test
    void rejectCandidature_notStudent_returnsForbidden() throws Exception {
        UserDTO dto = new UserDTO(2L, null, null, null, ca.cal.leandrose.model.auth.Role.EMPLOYEUR);
        when(userAppService.getMe(anyString())).thenReturn(dto);

        mockMvc.perform(post("/student/applications/100/reject")
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isForbidden());

        verify(candidatureService, never()).rejectByStudent(anyLong(), anyLong());
    }

    @Test
    void rejectCandidature_notAcceptedByEmployeur_returnsBadRequest() throws Exception {
        UserDTO studentDto = new UserDTO(1L, null, null, null, ca.cal.leandrose.model.auth.Role.STUDENT);

        when(userAppService.getMe(anyString())).thenReturn(studentDto);
        when(candidatureService.rejectByStudent(100L, 1L))
                .thenThrow(new IllegalStateException("Vous ne pouvez refuser que les candidatures acceptées par l'employeur"));

        mockMvc.perform(post("/student/applications/100/reject")
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Vous ne pouvez refuser que les candidatures acceptées par l'employeur"));
    }

    @Test
    void rejectCandidature_candidatureNotFound_returnsNotFound() throws Exception {
        UserDTO studentDto = new UserDTO(1L, null, null, null, ca.cal.leandrose.model.auth.Role.STUDENT);

        when(userAppService.getMe(anyString())).thenReturn(studentDto);
        when(candidatureService.rejectByStudent(999L, 1L))
                .thenThrow(new RuntimeException("Candidature introuvable"));

        mockMvc.perform(post("/student/applications/999/reject")
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Candidature non trouvée"));
    }

    @Test
    void rejectCandidature_notOwnCandidature_returnsBadRequest() throws Exception {
        UserDTO studentDto = new UserDTO(1L, null, null, null, ca.cal.leandrose.model.auth.Role.STUDENT);

        when(userAppService.getMe(anyString())).thenReturn(studentDto);
        when(candidatureService.rejectByStudent(100L, 1L))
                .thenThrow(new IllegalStateException("Cette candidature ne vous appartient pas"));

        mockMvc.perform(post("/student/applications/100/reject")
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Cette candidature ne vous appartient pas"));
    }
    @MockitoBean
    private EntenteStageService ententeStageService;

    @Test
    void signerEntente_asStudent_returnsOk() throws Exception {
        UserDTO student = new UserDTO(1L, null, null, null, ca.cal.leandrose.model.auth.Role.STUDENT);
        EntenteStageDto ententeDto = EntenteStageDto.builder()
                .id(10L)
                .candidatureId(20L)
                .statut(EntenteStage.StatutEntente.EN_ATTENTE_SIGNATURE)
                .dateSignatureEtudiant(java.time.LocalDateTime.now())
                .build();

        when(userAppService.getMe(anyString())).thenReturn(student);
        when(ententeStageService.signerParEtudiant(10L, 1L)).thenReturn(ententeDto);

        mockMvc.perform(post("/student/ententes/10/signer")
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.statut").value("EN_ATTENTE_SIGNATURE"));

        verify(ententeStageService).signerParEtudiant(10L, 1L);
    }

    @Test
    void signerEntente_notStudent_returnsForbidden() throws Exception {
        UserDTO employeur = new UserDTO(2L, null, null, null, ca.cal.leandrose.model.auth.Role.EMPLOYEUR);
        when(userAppService.getMe(anyString())).thenReturn(employeur);

        mockMvc.perform(post("/student/ententes/10/signer")
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isForbidden());

        verify(ententeStageService, never()).signerParEtudiant(anyLong(), anyLong());
    }

    @Test
    void signerEntente_ententeNotFound_returnsNotFound() throws Exception {
        UserDTO student = new UserDTO(1L, null, null, null, ca.cal.leandrose.model.auth.Role.STUDENT);

        when(userAppService.getMe(anyString())).thenReturn(student);
        when(ententeStageService.signerParEtudiant(10L, 1L))
                .thenThrow(new jakarta.persistence.EntityNotFoundException("Entente non trouvée"));

        mockMvc.perform(post("/student/ententes/10/signer")
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.message").value("Entente non trouvée"));
    }

    @Test
    void signerEntente_illegalArgument_returnsBadRequest() throws Exception {
        UserDTO student = new UserDTO(1L, null, null, null, ca.cal.leandrose.model.auth.Role.STUDENT);

        when(userAppService.getMe(anyString())).thenReturn(student);
        when(ententeStageService.signerParEtudiant(10L, 1L))
                .thenThrow(new IllegalArgumentException("Cet étudiant n'est pas autorisé à signer cette entente."));

        mockMvc.perform(post("/student/ententes/10/signer")
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.message").value("Cet étudiant n'est pas autorisé à signer cette entente."));
    }

    @Test
    void signerEntente_illegalState_returnsBadRequest() throws Exception {
        UserDTO student = new UserDTO(1L, null, null, null, ca.cal.leandrose.model.auth.Role.STUDENT);

        when(userAppService.getMe(anyString())).thenReturn(student);
        when(ententeStageService.signerParEtudiant(10L, 1L))
                .thenThrow(new IllegalStateException("L'entente doit être en attente de signature."));

        mockMvc.perform(post("/student/ententes/10/signer")
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.message").value("L'entente doit être en attente de signature."));
    }
}
