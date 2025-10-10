package ca.cal.leandrose.presentation;

import ca.cal.leandrose.model.InternshipOffer;
import ca.cal.leandrose.model.Program;
import ca.cal.leandrose.model.Student;
import ca.cal.leandrose.repository.StudentRepository;
import ca.cal.leandrose.security.TestSecurityConfiguration;
import ca.cal.leandrose.service.CandidatureService;
import ca.cal.leandrose.service.CvService;
import ca.cal.leandrose.service.InternshipOfferService;
import ca.cal.leandrose.service.UserAppService;
import ca.cal.leandrose.service.dto.CandidatureDto;
import ca.cal.leandrose.service.dto.CvDto;
import ca.cal.leandrose.service.dto.InternshipOfferDto;
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
import java.util.Optional;

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

    @Test
    void getCv_missingAuthorization_returnsUnauthorized() throws Exception {
        mockMvc.perform(get("/student/cv"))
                .andExpect(status().isUnauthorized());
    }

//    @Test
//    void getPublishedOffers_returnsList() throws Exception {
//        UserDTO studentDto = new UserDTO(1L, null, null, null, ca.cal.leandrose.model.auth.Role.STUDENT);
//        Student student = Student.builder()
//                .id(1L)
//                .program(String.valueOf(Program.COMPUTER_SCIENCE))
//                .build();
//        InternshipOffer offer = InternshipOffer.builder()
//                .id(10L)
//                .description("Stage A")
//                .build();
//
//        when(userAppService.getMe(anyString())).thenReturn(studentDto);
//        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
//        when(internshipOfferService.getPublishedOffersForStudents(String.valueOf(Program.COMPUTER_SCIENCE)))
//                .thenReturn(List.of(offer));
//
//        mockMvc.perform(get("/student/offers")
//                        .header("Authorization", "Bearer token"))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$[0].id").value(10));
//
//        verify(internshipOfferService).getPublishedOffersForStudents(String.valueOf(Program.COMPUTER_SCIENCE));
//    }

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
        InternshipOfferDto offer = InternshipOfferDto.toDto(InternshipOffer.builder()
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
        InternshipOfferDto offer = InternshipOfferDto.toDto(InternshipOffer.builder()
                .id(21L)
                .status(InternshipOffer.Status.REJECTED)
                .build());
        when(internshipOfferService.getOffer(21L)).thenReturn(offer);

        mockMvc.perform(get("/student/offers/21"))
                .andExpect(status().isForbidden());
    }

    @Test
    void uploadCv_asStudent_returnsCvDto() throws Exception {
        UserDTO studentDto = new UserDTO(1L, null, null, null, ca.cal.leandrose.model.auth.Role.STUDENT);
        Student student = Student.builder().id(1L).build();
        CvDto cvDto = CvDto.builder().id(1L).pdfPath("path/to/cv.pdf").build();

        when(userAppService.getMe(anyString())).thenReturn(studentDto);
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(cvService.uploadCv(eq(1L), any())).thenReturn(cvDto);

        MockMultipartFile file = new MockMultipartFile("pdfFile", "cv.pdf", "application/pdf", "PDF_CONTENT".getBytes());

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
        UserDTO studentDto = new UserDTO(1L, null, null, null, ca.cal.leandrose.model.auth.Role.STUDENT);
        Student student = Student.builder().id(1L).build();
        CvDto cvDto = CvDto.builder().id(1L).pdfPath("path/to/cv.pdf").build();

        when(userAppService.getMe(anyString())).thenReturn(studentDto);
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
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
        InternshipOfferDto offer = InternshipOfferDto.toDto(InternshipOffer.builder()
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
        InternshipOfferDto offer = InternshipOfferDto.toDto(InternshipOffer.builder()
                .id(101L)
                .status(InternshipOffer.Status.PENDING_VALIDATION)
                .build());
        when(internshipOfferService.getOffer(101L)).thenReturn(offer);

        mockMvc.perform(get("/student/offers/101/pdf"))
                .andExpect(status().isForbidden());
    }
}