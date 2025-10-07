package ca.cal.leandrose.service;

import ca.cal.leandrose.model.Cv;
import ca.cal.leandrose.model.Student;
import ca.cal.leandrose.repository.CvRepository;
import ca.cal.leandrose.repository.StudentRepository;
import ca.cal.leandrose.service.dto.CvDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class CvServiceTest {

    @Autowired
    private CvService cvService;

    @Autowired
    private CvRepository cvRepository;

    @Autowired
    private StudentRepository studentRepository;

    private Student testStudent;

    @BeforeEach
    void setUp() {
        testStudent = studentRepository.save(Student.builder()
                .firstName("Alice")
                .lastName("Martin")
                .email("alice@test.com")
                .password("password")
                .studentNumber("123456")
                .program("Computer Science")
                .build());
    }

    @Test
    void uploadCv_ShouldSaveCv_WhenValidPdf() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "cv.pdf",
                "application/pdf",
                Files.readAllBytes(Path.of("src/test/resources/test.pdf"))
        );

        CvDto cvDto = cvService.uploadCv(testStudent.getId(), file);

        assertNotNull(cvDto);
        assertEquals(Cv.Status.PENDING, cvDto.getStatus());

        Cv saved = cvRepository.findById(cvDto.getId()).orElseThrow();
        assertTrue(Files.exists(Path.of(saved.getPdfPath())));
    }

    @Test
    void uploadCv_ShouldThrow_WhenFileIsEmpty() {
        MockMultipartFile emptyFile = new MockMultipartFile("file", "empty.pdf", "application/pdf", new byte[0]);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                cvService.uploadCv(testStudent.getId(), emptyFile)
        );
        assertEquals("Fichier vide ou manquant.", ex.getMessage());
    }

    @Test
    void uploadCv_ShouldThrow_WhenFileTooLarge() {
        byte[] largeContent = new byte[6 * 1024 * 1024]; // 6 MB
        MockMultipartFile largeFile = new MockMultipartFile("file", "large.pdf", "application/pdf", largeContent);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                cvService.uploadCv(testStudent.getId(), largeFile)
        );
        assertTrue(ex.getMessage().contains("Fichier trop volumineux"));
    }

    @Test
    void uploadCv_ShouldThrow_WhenInvalidExtension() {
        MockMultipartFile wrongExt = new MockMultipartFile("file", "cv.txt", "text/plain", "content".getBytes());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                cvService.uploadCv(testStudent.getId(), wrongExt)
        );
        assertTrue(ex.getMessage().contains(".pdf"));
    }

    @Test
    void uploadCv_ShouldThrow_WhenInvalidContentType() {
        MockMultipartFile wrongType = new MockMultipartFile("file", "cv.pdf", "text/plain", "content".getBytes());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                cvService.uploadCv(testStudent.getId(), wrongType)
        );
        assertTrue(ex.getMessage().contains("Content type invalide"));
    }

    @Test
    void uploadCv_ShouldReplaceExistingCv() throws Exception {
        // Upload first CV
        MockMultipartFile file1 = new MockMultipartFile(
                "file", "cv1.pdf", "application/pdf", Files.readAllBytes(Path.of("src/test/resources/test.pdf"))
        );
        CvDto first = cvService.uploadCv(testStudent.getId(), file1);
        Path firstPath = Path.of(cvRepository.findById(first.getId()).orElseThrow().getPdfPath());

        // Upload second CV
        MockMultipartFile file2 = new MockMultipartFile(
                "file", "cv2.pdf", "application/pdf", Files.readAllBytes(Path.of("src/test/resources/test.pdf"))
        );
        CvDto second = cvService.uploadCv(testStudent.getId(), file2);

        // First file should be deleted
        assertFalse(Files.exists(firstPath));

        // Only one CV in repository
        assertEquals(1, cvRepository.findAll().size());
        assertEquals(second.getId(), cvRepository.findAll().get(0).getId());
    }

    @Test
    void getCvByStudentId_ShouldReturnCv_WhenExists() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "cv.pdf", "application/pdf", Files.readAllBytes(Path.of("src/test/resources/test.pdf"))
        );
        CvDto uploaded = cvService.uploadCv(testStudent.getId(), file);

        CvDto result = cvService.getCvByStudentId(testStudent.getId());

        assertNotNull(result);
        assertEquals(uploaded.getId(), result.getId());
        assertEquals(Cv.Status.PENDING, result.getStatus());
    }

    @Test
    void getCvByStudentId_ShouldThrow_WhenCvNotFound() {
        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                cvService.getCvByStudentId(999L)
        );
        assertTrue(ex.getMessage().contains("CV non trouvé"));
    }
}
