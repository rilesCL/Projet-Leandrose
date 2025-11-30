package ca.cal.leandrose.service;

import static org.junit.jupiter.api.Assertions.*;

import ca.cal.leandrose.model.*;
import ca.cal.leandrose.model.auth.Credentials;
import ca.cal.leandrose.model.auth.Role;
import ca.cal.leandrose.service.dto.evaluation.employer.EvaluationEmployerFormData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

class PDFGeneratorServiceTest {

    private PDFGeneratorService pdfGeneratorService;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        pdfGeneratorService = new PDFGeneratorService();
        ReflectionTestUtils.setField(pdfGeneratorService, "baseUploadDir", tempDir.resolve("ententes").toString());
        ReflectionTestUtils.setField(pdfGeneratorService, "baseEvaluationsDir", tempDir.resolve("evaluations").toString());
    }

    @Test
    void testLireFichierPDF_FileExists() throws Exception {
        Path testFile = tempDir.resolve("test.pdf");
        byte[] testContent = "test content".getBytes();
        Files.write(testFile, testContent);

        byte[] result = pdfGeneratorService.lireFichierPDF(testFile.toString());

        assertNotNull(result);
        assertArrayEquals(testContent, result);
    }

    @Test
    void testLireFichierPDF_FileNotFound() {
        String nonExistentPath = tempDir.resolve("nonexistent.pdf").toString();

        assertThrows(RuntimeException.class, () -> {
            pdfGeneratorService.lireFichierPDF(nonExistentPath);
        });
    }

    @Test
    void testSupprimerFichierPDF_FileExists() throws Exception {
        Path testFile = tempDir.resolve("test.pdf");
        Files.createFile(testFile);

        pdfGeneratorService.supprimerFichierPDF(testFile.toString());

        assertFalse(Files.exists(testFile));
    }

    @Test
    void testSupprimerFichierPDF_FileNotExists() {
        String nonExistentPath = tempDir.resolve("nonexistent.pdf").toString();

        assertDoesNotThrow(() -> {
            pdfGeneratorService.supprimerFichierPDF(nonExistentPath);
        });
    }

    @Test
    void testGenererEntentePDF_Success() {
        EntenteStage entente = createTestEntente();

        String result = pdfGeneratorService.genererEntentePDF(entente);

        assertNotNull(result);
        assertTrue(result.endsWith(".pdf"));
        assertTrue(Files.exists(Path.of(result)));
    }

    @Test
    void testGenererEntentePDF_CreatesDirectoryIfNotExists() {
        Path customDir = tempDir.resolve("custom_ententes");
        ReflectionTestUtils.setField(pdfGeneratorService, "baseUploadDir", customDir.toString());

        EntenteStage entente = createTestEntente();
        pdfGeneratorService.genererEntentePDF(entente);

        assertTrue(Files.exists(customDir));
    }

    private EntenteStage createTestEntente() {
        Credentials studentCreds = Credentials.builder()
                .email("student@test.com")
                .password("pass")
                .role(Role.STUDENT)
                .build();

        Student student = Student.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .email("student@test.com")
                .password("pass")
                .build();
        student.setCredentials(studentCreds);

        Employeur employeur = Employeur.builder()
                .id(1L)
                .firstName("Jane")
                .lastName("Smith")
                .email("employer@test.com")
                .password("pass")
                .companyName("Test Company")
                .build();

        InternshipOffer offer = InternshipOffer.builder()
                .id(1L)
                .description("Test Offer")
                .startDate(LocalDate.now())
                .durationInWeeks(12)
                .address("123 Test St")
                .remuneration(500.0f)
                .employeur(employeur)
                .build();

        Candidature candidature = Candidature.builder()
                .id(1L)
                .student(student)
                .internshipOffer(offer)
                .status(Candidature.Status.ACCEPTED)
                .build();

        EntenteStage entente = EntenteStage.builder()
                .id(1L)
                .candidature(candidature)
                .missionsObjectifs("Test missions")
                .statut(EntenteStage.StatutEntente.BROUILLON)
                .dateCreation(java.time.LocalDateTime.now())
                .build();

        return entente;
    }
}

