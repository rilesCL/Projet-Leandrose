package ca.cal.leandrose.presentation;

import ca.cal.leandrose.model.Cv;
import ca.cal.leandrose.repository.CvRepository;
import ca.cal.leandrose.security.TestSecurityConfiguration;
import ca.cal.leandrose.service.GestionnaireService;
import ca.cal.leandrose.service.InternshipOfferService;
import ca.cal.leandrose.service.dto.CvDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(GestionnaireController.class)
@ActiveProfiles("test")
@Import(TestSecurityConfiguration.class)
class GestionnaireControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private GestionnaireService gestionnaireService;

    @MockitoBean
    private InternshipOfferService internshipOfferService;

    @MockitoBean
    private CvRepository cvRepository;

    private CvDto sampleCvDto;
    private Cv sampleCv;

    @BeforeEach
    void setUp() {
        sampleCvDto = CvDto.builder()
                .id(1L)
                .studentId(1L)
                .pdfPath("/path/to/test-cv.pdf")
                .status(Cv.Status.APPROVED)
                .build();

        sampleCv = Cv.builder()
                .id(1L)
                .pdfPath("/path/to/test-cv.pdf")
                .status(Cv.Status.PENDING)
                .build();
    }

    @Test
    void approveCv_ShouldReturnApprovedCvDto_WhenCvExists() throws Exception {
        Long cvId = 1L;
        CvDto approvedCvDto = CvDto.builder()
                .id(cvId)
                .studentId(1L)
                .pdfPath("/path/to/test-cv.pdf")
                .status(Cv.Status.APPROVED)
                .build();

        when(gestionnaireService.approveCv(cvId)).thenReturn(approvedCvDto);

        mockMvc.perform(post("/gestionnaire/cv/{cvId}/approve", cvId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(cvId))
                .andExpect(jsonPath("$.studentId").value(1L))
                .andExpect(jsonPath("$.pdfPath").value("/path/to/test-cv.pdf"))
                .andExpect(jsonPath("$.status").value("APPROVED"));

        verify(gestionnaireService, times(1)).approveCv(cvId);
    }

    @Test
    void rejectCv_ShouldReturnRejectedCvDto_WhenCvExists() throws Exception {
        Long cvId = 1L;
        String rejectionReason = "cv non professionnel";

        CvDto rejectedCvDto = CvDto.builder()
                .id(cvId)
                .studentId(1L)
                .pdfPath("/path/to/test-cv.pdf")
                .status(Cv.Status.REJECTED)
                .build();

        when(gestionnaireService.rejectCv(eq(cvId), anyString())).thenReturn(rejectedCvDto);

        mockMvc.perform(post("/gestionnaire/cv/{cvId}/reject", cvId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rejectionReason)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(cvId))
                .andExpect(jsonPath("$.studentId").value(1L))
                .andExpect(jsonPath("$.pdfPath").value("/path/to/test-cv.pdf"))
                .andExpect(jsonPath("$.status").value("REJECTED"));

        verify(gestionnaireService, times(1)).rejectCv(eq(cvId), anyString());
    }

    @Test
    void getPendingCvs_ShouldReturnListOfPendingCvs() throws Exception {
        CvDto pendingCv1 = CvDto.builder()
                .id(1L)
                .studentId(1L)
                .pdfPath("/path/to/cv1.pdf")
                .status(Cv.Status.PENDING)
                .build();

        CvDto pendingCv2 = CvDto.builder()
                .id(2L)
                .studentId(2L)
                .pdfPath("/path/to/cv2.pdf")
                .status(Cv.Status.PENDING)
                .build();

        List<CvDto> pendingCvs = Arrays.asList(pendingCv1, pendingCv2);

        when(gestionnaireService.getPendingCvs()).thenReturn(pendingCvs);

        mockMvc.perform(get("/gestionnaire/cvs/pending"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.size()").value(2))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].pdfPath").value("/path/to/cv1.pdf"))
                .andExpect(jsonPath("$[0].status").value("PENDING"))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[1].pdfPath").value("/path/to/cv2.pdf"))
                .andExpect(jsonPath("$[1].status").value("PENDING"));

        verify(gestionnaireService, times(1)).getPendingCvs();
    }

    @Test
    void getPendingCvs_ShouldReturnEmptyList_WhenNoPendingCvs() throws Exception {
        when(gestionnaireService.getPendingCvs()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/gestionnaire/cvs/pending"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.size()").value(0));

        verify(gestionnaireService, times(1)).getPendingCvs();
    }

    @Test
    void downloadCv_ShouldReturnPdfFile_WhenCvExistsAndFileExists() throws Exception {
        Long cvId = 1L;
        String fileName = "test-cv.pdf";

        Path tempPath = Paths.get(System.getProperty("java.io.tmpdir"), fileName);
        Files.write(tempPath, "test pdf content".getBytes());

        Cv cv = Cv.builder()
                .id(cvId)
                .pdfPath(tempPath.toString())
                .build();

        when(cvRepository.findById(cvId)).thenReturn(Optional.of(cv));

        mockMvc.perform(get("/gestionnaire/cv/{cvId}/download", cvId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"" + fileName + "\""));

        verify(cvRepository, times(1)).findById(cvId);

        Files.deleteIfExists(tempPath);
    }

    @Test
    void downloadCv_ShouldThrowException_WhenCvNotFound() throws Exception {
        Long cvId = 999L;
        when(cvRepository.findById(cvId)).thenReturn(Optional.empty());

        try {
            mockMvc.perform(get("/gestionnaire/cv/{cvId}/download", cvId));
        } catch (Exception e) {
            // Verify the root cause is RuntimeException with expected message
            Throwable cause = e.getCause();
            while (cause != null && !(cause instanceof RuntimeException && cause.getMessage().contains("CV introuvable"))) {
                cause = cause.getCause();
            }
            assert cause != null : "Expected RuntimeException with 'CV introuvable' message";
        }

        verify(cvRepository, times(1)).findById(cvId);
    }

    @Test
    void approveCv_ShouldAcceptValidPathVariable() throws Exception {
        Long cvId = 123L;
        when(gestionnaireService.approveCv(cvId)).thenReturn(sampleCvDto);

        mockMvc.perform(post("/gestionnaire/cv/{cvId}/approve", cvId))
                .andExpect(status().isOk());

        verify(gestionnaireService, times(1)).approveCv(cvId);
    }

    @Test
    void rejectCv_ShouldAcceptValidPathVariable() throws Exception {
        Long cvId = 123L;
        String rejectionReason = "cv trop long";

        when(gestionnaireService.rejectCv(eq(cvId), anyString())).thenReturn(sampleCvDto);

        mockMvc.perform(post("/gestionnaire/cv/{cvId}/reject", cvId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rejectionReason)))
                .andExpect(status().isOk());

        verify(gestionnaireService, times(1)).rejectCv(eq(cvId), anyString());
    }

    @Test
    void downloadCv_ShouldAcceptValidPathVariable() throws Exception {
        Long cvId = 123L;
        Path tempPath = Paths.get(System.getProperty("java.io.tmpdir"), "temp-cv.pdf");
        Files.write(tempPath, "test content".getBytes());

        sampleCv.setPdfPath(tempPath.toString());
        when(cvRepository.findById(cvId)).thenReturn(Optional.of(sampleCv));

        mockMvc.perform(get("/gestionnaire/cv/{cvId}/download", cvId))
                .andExpect(status().isOk());

        verify(cvRepository, times(1)).findById(cvId);

        Files.deleteIfExists(tempPath);
    }

    @Test
    void shouldMapPostRequestToApproveEndpoint() throws Exception {
        when(gestionnaireService.approveCv(anyLong())).thenReturn(sampleCvDto);

        mockMvc.perform(post("/gestionnaire/cv/1/approve"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldMapPostRequestToRejectEndpoint() throws Exception {
        String rejectionReason = "manque de détail";

        when(gestionnaireService.rejectCv(anyLong(), anyString())).thenReturn(sampleCvDto);

        mockMvc.perform(post("/gestionnaire/cv/1/reject")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rejectionReason)))
                .andExpect(status().isOk());
    }

    @Test
    void shouldMapGetRequestToRootEndpoint() throws Exception {
        when(gestionnaireService.getPendingCvs()).thenReturn(Collections.singletonList(sampleCvDto));

        mockMvc.perform(get("/gestionnaire/cvs/pending"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldMapGetRequestToDownloadEndpoint() throws Exception {
        Path tempPath = Paths.get(System.getProperty("java.io.tmpdir"), "test.pdf");
        Files.write(tempPath, "content".getBytes());

        sampleCv.setPdfPath(tempPath.toString());
        when(cvRepository.findById(anyLong())).thenReturn(Optional.of(sampleCv));

        mockMvc.perform(get("/gestionnaire/cv/1/download"))
                .andExpect(status().isOk());

        Files.deleteIfExists(tempPath);
    }

    @Test
    void rejectCv_ShouldHandleEmptyComment() throws Exception {
        Long cvId = 1L;
        CvDto rejectedCvDto = CvDto.builder()
                .id(cvId)
                .studentId(1L)
                .pdfPath("/path/to/test-cv.pdf")
                .status(Cv.Status.REJECTED)
                .build();

        when(gestionnaireService.rejectCv(eq(cvId), anyString())).thenReturn(rejectedCvDto);

        mockMvc.perform(post("/gestionnaire/cv/{cvId}/reject", cvId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString("")))
                .andExpect(status().isOk());

        verify(gestionnaireService, times(1)).rejectCv(eq(cvId), anyString());
    }

    @Test
    void approveCv_ShouldHandleMultipleApprovals() throws Exception {
        Long cvId1 = 1L;
        Long cvId2 = 2L;

        CvDto approvedCv1 = CvDto.builder().id(cvId1).status(Cv.Status.APPROVED).build();
        CvDto approvedCv2 = CvDto.builder().id(cvId2).status(Cv.Status.APPROVED).build();

        when(gestionnaireService.approveCv(cvId1)).thenReturn(approvedCv1);
        when(gestionnaireService.approveCv(cvId2)).thenReturn(approvedCv2);

        mockMvc.perform(post("/gestionnaire/cv/{cvId}/approve", cvId1))
                .andExpect(status().isOk());

        mockMvc.perform(post("/gestionnaire/cv/{cvId}/approve", cvId2))
                .andExpect(status().isOk());

        verify(gestionnaireService, times(1)).approveCv(cvId1);
        verify(gestionnaireService, times(1)).approveCv(cvId2);
    }
}