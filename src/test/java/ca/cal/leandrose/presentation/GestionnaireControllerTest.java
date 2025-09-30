package ca.cal.leandrose.presentation;

import ca.cal.leandrose.model.Cv;
import ca.cal.leandrose.repository.CvRepository;
import ca.cal.leandrose.security.TestSecurityConfiguration;
import ca.cal.leandrose.service.GestionnaireService;
import ca.cal.leandrose.service.InternshipOfferService;
import ca.cal.leandrose.service.dto.CvDto;
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
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(GestionnaireController.class)
@ActiveProfiles("test")
@Import(TestSecurityConfiguration.class)
class GestionnaireControllerTest {

    @Autowired
    private MockMvc mockMvc;

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
        CvDto rejectedCvDto = CvDto.builder()
                .id(cvId)
                .studentId(1L)
                .pdfPath("/path/to/test-cv.pdf")
                .status(Cv.Status.REJECTED)
                .build();

        when(gestionnaireService.rejectCv(cvId, "cv non professionnel")).thenReturn(rejectedCvDto);

        mockMvc.perform(post("/gestionnaire/cv/{cvId}/reject", cvId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(cvId))
                .andExpect(jsonPath("$.studentId").value(1L))
                .andExpect(jsonPath("$.pdfPath").value("/path/to/test-cv.pdf"))
                .andExpect(jsonPath("$.status").value("REJECTED"));

        verify(gestionnaireService, times(1)).rejectCv(cvId, "manque de détail");
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
        when(gestionnaireService.getPendingCvs()).thenReturn(Arrays.asList());

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
        String filePath = "/tmp/test-cv.pdf";

        Cv cv = Cv.builder()
                .id(cvId)
                .pdfPath(filePath)
                .build();

        when(cvRepository.findById(cvId)).thenReturn(Optional.of(cv));

        Path tempPath = Paths.get(System.getProperty("java.io.tmpdir"), fileName);
        Files.write(tempPath, "test pdf content".getBytes());

        cv.setPdfPath(tempPath.toString());

        mockMvc.perform(get("/gestionnaire/cv/{cvId}/download", cvId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"" + fileName + "\""));

        verify(cvRepository, times(1)).findById(cvId);

        Files.deleteIfExists(tempPath);
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
        when(gestionnaireService.rejectCv(cvId, "cv trop long")).thenReturn(sampleCvDto);

        mockMvc.perform(post("/gestionnaire/cv/{cvId}/reject", cvId))
                .andExpect(status().isOk());

        verify(gestionnaireService, times(1)).rejectCv(cvId, "cv trop long");
    }

    @Test
    void downloadCv_ShouldAcceptValidPathVariable() throws Exception {
        Long cvId = 123L;
        when(cvRepository.findById(cvId)).thenReturn(Optional.of(sampleCv));

        Path tempPath = Paths.get(System.getProperty("java.io.tmpdir"), "temp-cv.pdf");
        Files.write(tempPath, "test content".getBytes());
        sampleCv.setPdfPath(tempPath.toString());

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
        when(gestionnaireService.rejectCv(anyLong(), anyString())).thenReturn(sampleCvDto);

        mockMvc.perform(post("/gestionnaire/cv/1/reject"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldMapGetRequestToRootEndpoint() throws Exception {
        when(gestionnaireService.getPendingCvs()).thenReturn(Arrays.asList(sampleCvDto));

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
}
