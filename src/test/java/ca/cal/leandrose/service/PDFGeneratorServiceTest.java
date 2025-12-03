package ca.cal.leandrose.service;

import static org.junit.jupiter.api.Assertions.*;

import ca.cal.leandrose.model.*;
import ca.cal.leandrose.model.auth.Credentials;
import ca.cal.leandrose.model.auth.Role;
import ca.cal.leandrose.model.auth.Role;
import ca.cal.leandrose.service.dto.ProfDto;
import ca.cal.leandrose.service.dto.evaluation.*;
import ca.cal.leandrose.service.dto.evaluation.employer.EvaluationEmployerFormData;
import ca.cal.leandrose.service.dto.evaluation.employer.EmployerQuestionResponse;
import ca.cal.leandrose.service.dto.evaluation.prof.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.*;

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

    // ==================== File Operations Tests ====================

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

    // ==================== Entente PDF Tests ====================

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

    @Test
    void testGenererEntentePDF_WithNullAddress() {
        EntenteStage entente = createTestEntente();
        entente.getCandidature().getInternshipOffer().setAddress(null);

        String result = pdfGeneratorService.genererEntentePDF(entente);

        assertNotNull(result);
        assertTrue(Files.exists(Path.of(result)));
    }

    @Test
    void testGenererEntentePDF_WithBlankAddress() {
        EntenteStage entente = createTestEntente();
        entente.getCandidature().getInternshipOffer().setAddress("   ");

        String result = pdfGeneratorService.genererEntentePDF(entente);

        assertNotNull(result);
        assertTrue(Files.exists(Path.of(result)));
    }

    // ==================== Employer Evaluation Tests ====================

    @Test
    void testGeneratedEvaluationByEmployer_FrenchLanguage() {
        EvaluationStagiaire evaluation = createTestEvaluation();
        EvaluationEmployerFormData formData = createTestEmployerFormData();

        String result = pdfGeneratorService.generatedEvaluationByEmployer(
                evaluation, formData, "fr",
                "Jean", "Dupont",
                "Collège Test", "123 rue Test", "514-555-1234"
        );

        assertNotNull(result);
        assertTrue(result.endsWith(".pdf"));
        assertTrue(Files.exists(Path.of(result)));
    }

    @Test
    void testGeneratedEvaluationByEmployer_EnglishLanguage() {
        EvaluationStagiaire evaluation = createTestEvaluation();
        EvaluationEmployerFormData formData = createTestEmployerFormData();

        String result = pdfGeneratorService.generatedEvaluationByEmployer(
                evaluation, formData, "en",
                "John", "Smith",
                "Test College", "123 Test St", "514-555-1234"
        );

        assertNotNull(result);
        assertTrue(result.endsWith(".pdf"));
        assertTrue(Files.exists(Path.of(result)));
    }

    @Test
    void testGeneratedEvaluationByEmployer_WithAllRatings() {
        EvaluationStagiaire evaluation = createTestEvaluation();
        EvaluationEmployerFormData formData = createTestEmployerFormDataWithAllRatings();

        String result = pdfGeneratorService.generatedEvaluationByEmployer(
                evaluation, formData, "fr",
                "Jean", "Dupont",
                "Collège Test", "123 rue Test", "514-555-1234"
        );

        assertNotNull(result);
        assertTrue(Files.exists(Path.of(result)));
    }

    @Test
    void testGeneratedEvaluationByEmployer_WithComments() {
        EvaluationStagiaire evaluation = createTestEvaluation();
        EvaluationEmployerFormData formData = createTestEmployerFormDataWithComments();

        String result = pdfGeneratorService.generatedEvaluationByEmployer(
                evaluation, formData, "fr",
                "Jean", "Dupont",
                "Collège Test", "123 rue Test", "514-555-1234"
        );

        assertNotNull(result);
        assertTrue(Files.exists(Path.of(result)));
    }

    @Test
    void testGeneratedEvaluationByEmployer_GlobalAssessmentExcellent() {
        EvaluationStagiaire evaluation = createTestEvaluation();
        Map<String, List<EmployerQuestionResponse>> categories = new HashMap<>();
        categories.put("productivity", createEmployerQuestionResponses(5));
        categories.put("quality", createEmployerQuestionResponses(5));
        categories.put("relationships", createEmployerQuestionResponses(6));
        categories.put("skills", createEmployerQuestionResponses(6));

        EvaluationEmployerFormData formData = new EvaluationEmployerFormData(
                categories,
                null,
                0,
                null,
                false,
                15,
                "NO",
                false
        );

        String result = pdfGeneratorService.generatedEvaluationByEmployer(
                evaluation, formData, "fr",
                "Jean", "Dupont",
                "Collège Test", "123 rue Test", "514-555-1234"
        );

        assertNotNull(result);
        assertTrue(Files.exists(Path.of(result)));
    }

    @Test
    void testGeneratedEvaluationByEmployer_GlobalAssessmentSatisfaisant() {
        EvaluationStagiaire evaluation = createTestEvaluation();
        Map<String, List<EmployerQuestionResponse>> categories = new HashMap<>();
        categories.put("productivity", createEmployerQuestionResponses(5));
        categories.put("quality", createEmployerQuestionResponses(5));
        categories.put("relationships", createEmployerQuestionResponses(6));
        categories.put("skills", createEmployerQuestionResponses(6));

        EvaluationEmployerFormData formData = new EvaluationEmployerFormData(
                categories,
                null,
                3,
                null,
                false,
                15,
                "NO",
                false
        );

        String result = pdfGeneratorService.generatedEvaluationByEmployer(
                evaluation, formData, "fr",
                "Jean", "Dupont",
                "Collège Test", "123 rue Test", "514-555-1234"
        );

        assertNotNull(result);
        assertTrue(Files.exists(Path.of(result)));
    }

    @Test
    void testGeneratedEvaluationByEmployer_DiscussedWithTrainee() {
        EvaluationStagiaire evaluation = createTestEvaluation();
        Map<String, List<EmployerQuestionResponse>> categories = new HashMap<>();
        categories.put("productivity", createEmployerQuestionResponses(5));
        categories.put("quality", createEmployerQuestionResponses(5));
        categories.put("relationships", createEmployerQuestionResponses(6));
        categories.put("skills", createEmployerQuestionResponses(6));

        EvaluationEmployerFormData formData = new EvaluationEmployerFormData(
                categories,
                null,
                1,
                null,
                true,
                15,
                "NO",
                false
        );

        String result = pdfGeneratorService.generatedEvaluationByEmployer(
                evaluation, formData, "fr",
                "Jean", "Dupont",
                "Collège Test", "123 rue Test", "514-555-1234"
        );

        assertNotNull(result);
        assertTrue(Files.exists(Path.of(result)));
    }

    @Test
    void testGeneratedEvaluationByEmployer_WelcomeNextInternshipYes() {
        EvaluationStagiaire evaluation = createTestEvaluation();
        Map<String, List<EmployerQuestionResponse>> categories = new HashMap<>();
        categories.put("productivity", createEmployerQuestionResponses(5));
        categories.put("quality", createEmployerQuestionResponses(5));
        categories.put("relationships", createEmployerQuestionResponses(6));
        categories.put("skills", createEmployerQuestionResponses(6));

        EvaluationEmployerFormData formData = new EvaluationEmployerFormData(
                categories,
                null,
                1,
                null,
                false,
                15,
                "YES",
                false
        );

        String result = pdfGeneratorService.generatedEvaluationByEmployer(
                evaluation, formData, "fr",
                "Jean", "Dupont",
                "Collège Test", "123 rue Test", "514-555-1234"
        );

        assertNotNull(result);
        assertTrue(Files.exists(Path.of(result)));
    }

    @Test
    void testGeneratedEvaluationByEmployer_WelcomeNextInternshipMaybe() {
        EvaluationStagiaire evaluation = createTestEvaluation();
        Map<String, List<EmployerQuestionResponse>> categories = new HashMap<>();
        categories.put("productivity", createEmployerQuestionResponses(5));
        categories.put("quality", createEmployerQuestionResponses(5));
        categories.put("relationships", createEmployerQuestionResponses(6));
        categories.put("skills", createEmployerQuestionResponses(6));

        EvaluationEmployerFormData formData = new EvaluationEmployerFormData(
                categories,
                null,
                1,
                null,
                false,
                15,
                "MAYBE",
                false
        );

        String result = pdfGeneratorService.generatedEvaluationByEmployer(
                evaluation, formData, "fr",
                "Jean", "Dupont",
                "Collège Test", "123 rue Test", "514-555-1234"
        );

        assertNotNull(result);
        assertTrue(Files.exists(Path.of(result)));
    }

    @Test
    void testGeneratedEvaluationByEmployer_TechnicalTrainingSufficient() {
        EvaluationStagiaire evaluation = createTestEvaluation();
        Map<String, List<EmployerQuestionResponse>> categories = new HashMap<>();
        categories.put("productivity", createEmployerQuestionResponses(5));
        categories.put("quality", createEmployerQuestionResponses(5));
        categories.put("relationships", createEmployerQuestionResponses(6));
        categories.put("skills", createEmployerQuestionResponses(6));

        EvaluationEmployerFormData formData = new EvaluationEmployerFormData(
                categories,
                null,
                1,
                null,
                false,
                15,
                "NO",
                true
        );

        String result = pdfGeneratorService.generatedEvaluationByEmployer(
                evaluation, formData, "fr",
                "Jean", "Dupont",
                "Collège Test", "123 rue Test", "514-555-1234"
        );

        assertNotNull(result);
        assertTrue(Files.exists(Path.of(result)));
    }

    @Test
    void testGeneratedEvaluationByEmployer_WithSupervisionHours() {
        EvaluationStagiaire evaluation = createTestEvaluation();
        Map<String, List<EmployerQuestionResponse>> categories = new HashMap<>();
        categories.put("productivity", createEmployerQuestionResponses(5));
        categories.put("quality", createEmployerQuestionResponses(5));
        categories.put("relationships", createEmployerQuestionResponses(6));
        categories.put("skills", createEmployerQuestionResponses(6));

        EvaluationEmployerFormData formData = new EvaluationEmployerFormData(
                categories,
                null,
                1,
                null,
                false,
                20,
                "NO",
                false
        );

        String result = pdfGeneratorService.generatedEvaluationByEmployer(
                evaluation, formData, "fr",
                "Jean", "Dupont",
                "Collège Test", "123 rue Test", "514-555-1234"
        );

        assertNotNull(result);
        assertTrue(Files.exists(Path.of(result)));
    }

    // ==================== Teacher Evaluation Tests ====================

    @Test
    void testGeneratedEvaluationByTeacher_FrenchLanguage() {
        EvaluationStagiaire evaluation = createTestEvaluation();
        EvaluationProfFormDto formData = createTestTeacherFormData();
        EvaluationTeacherInfoDto teacherInfo = createTestTeacherInfo();

        String result = pdfGeneratorService.generatedEvaluationByTeacher(
                evaluation, formData, teacherInfo, "fr"
        );

        assertNotNull(result);
        assertTrue(result.endsWith(".pdf"));
        assertTrue(Files.exists(Path.of(result)));
    }

    @Test
    void testGeneratedEvaluationByTeacher_EnglishLanguage() {
        EvaluationStagiaire evaluation = createTestEvaluation();
        EvaluationProfFormDto formData = createTestTeacherFormData();
        EvaluationTeacherInfoDto teacherInfo = createTestTeacherInfo();

        String result = pdfGeneratorService.generatedEvaluationByTeacher(
                evaluation, formData, teacherInfo, "en"
        );

        assertNotNull(result);
        assertTrue(Files.exists(Path.of(result)));
    }

    @Test
    void testGeneratedEvaluationByTeacher_WithAllCategories() {
        EvaluationStagiaire evaluation = createTestEvaluation();
        EvaluationProfFormDto formData = createTestTeacherFormDataWithAllCategories();
        EvaluationTeacherInfoDto teacherInfo = createTestTeacherInfo();

        String result = pdfGeneratorService.generatedEvaluationByTeacher(
                evaluation, formData, teacherInfo, "fr"
        );

        assertNotNull(result);
        assertTrue(Files.exists(Path.of(result)));
    }

    @Test
    void testGeneratedEvaluationByTeacher_PreferredStageFirst() {
        EvaluationStagiaire evaluation = createTestEvaluation();
        Map<String, List<QuestionResponseTeacher>> categories = new HashMap<>();
        categories.put("conformity", createTeacherQuestionResponses(3));
        categories.put("environment", createTeacherQuestionResponses(2));
        categories.put("general", createTeacherQuestionResponses(5));

        EvaluationProfFormDto formData = new EvaluationProfFormDto(
                categories, 1, 2, false, false, new ArrayList<>()
        );

        EvaluationTeacherInfoDto teacherInfo = createTestTeacherInfo();

        String result = pdfGeneratorService.generatedEvaluationByTeacher(
                evaluation, formData, teacherInfo, "fr"
        );

        assertNotNull(result);
        assertTrue(Files.exists(Path.of(result)));
    }

    @Test
    void testGeneratedEvaluationByTeacher_PreferredStageSecond() {
        EvaluationStagiaire evaluation = createTestEvaluation();
        Map<String, List<QuestionResponseTeacher>> categories = new HashMap<>();
        categories.put("conformity", createTeacherQuestionResponses(3));
        categories.put("environment", createTeacherQuestionResponses(2));
        categories.put("general", createTeacherQuestionResponses(5));

        EvaluationProfFormDto formData = new EvaluationProfFormDto(
                categories, 2, 2, false, false, new ArrayList<>()
        );

        EvaluationTeacherInfoDto teacherInfo = createTestTeacherInfo();

        String result = pdfGeneratorService.generatedEvaluationByTeacher(
                evaluation, formData, teacherInfo, "fr"
        );

        assertNotNull(result);
        assertTrue(Files.exists(Path.of(result)));
    }

    @Test
    void testGeneratedEvaluationByTeacher_CapacityOne() {
        EvaluationStagiaire evaluation = createTestEvaluation();
        Map<String, List<QuestionResponseTeacher>> categories = new HashMap<>();
        categories.put("conformity", createTeacherQuestionResponses(3));
        categories.put("environment", createTeacherQuestionResponses(2));
        categories.put("general", createTeacherQuestionResponses(5));

        EvaluationProfFormDto formData = new EvaluationProfFormDto(
                categories, 1, 1, false, false, new ArrayList<>()
        );

        EvaluationTeacherInfoDto teacherInfo = createTestTeacherInfo();

        String result = pdfGeneratorService.generatedEvaluationByTeacher(
                evaluation, formData, teacherInfo, "fr"
        );

        assertNotNull(result);
        assertTrue(Files.exists(Path.of(result)));
    }

    @Test
    void testGeneratedEvaluationByTeacher_CapacityMultiple() {
        EvaluationStagiaire evaluation = createTestEvaluation();
        Map<String, List<QuestionResponseTeacher>> categories = new HashMap<>();
        categories.put("conformity", createTeacherQuestionResponses(3));
        categories.put("environment", createTeacherQuestionResponses(2));
        categories.put("general", createTeacherQuestionResponses(5));

        EvaluationProfFormDto formData = new EvaluationProfFormDto(
                categories, 1, 3, false, false, new ArrayList<>()
        );

        EvaluationTeacherInfoDto teacherInfo = createTestTeacherInfo();

        String result = pdfGeneratorService.generatedEvaluationByTeacher(
                evaluation, formData, teacherInfo, "fr"
        );

        assertNotNull(result);
        assertTrue(Files.exists(Path.of(result)));
    }

    @Test
    void testGeneratedEvaluationByTeacher_SameTraineeNextStage() {
        EvaluationStagiaire evaluation = createTestEvaluation();
        Map<String, List<QuestionResponseTeacher>> categories = new HashMap<>();
        categories.put("conformity", createTeacherQuestionResponses(3));
        categories.put("environment", createTeacherQuestionResponses(2));
        categories.put("general", createTeacherQuestionResponses(5));

        EvaluationProfFormDto formData = new EvaluationProfFormDto(
                categories, 1, 2, true, false, new ArrayList<>()
        );

        EvaluationTeacherInfoDto teacherInfo = createTestTeacherInfo();

        String result = pdfGeneratorService.generatedEvaluationByTeacher(
                evaluation, formData, teacherInfo, "fr"
        );

        assertNotNull(result);
        assertTrue(Files.exists(Path.of(result)));
    }

    @Test
    void testGeneratedEvaluationByTeacher_WithWorkShifts() {
        EvaluationStagiaire evaluation = createTestEvaluation();
        Map<String, List<QuestionResponseTeacher>> categories = new HashMap<>();
        categories.put("conformity", createTeacherQuestionResponses(3));
        categories.put("environment", createTeacherQuestionResponses(2));
        categories.put("general", createTeacherQuestionResponses(5));

        List<WorkShiftRange> shifts = List.of(
                new WorkShiftRange("8:00", "16:00"),
                new WorkShiftRange("16:00", "00:00"),
                new WorkShiftRange("00:00", "8:00")
        );

        EvaluationProfFormDto formData = new EvaluationProfFormDto(
                categories, 1, 2, false, true, shifts
        );

        EvaluationTeacherInfoDto teacherInfo = createTestTeacherInfo();

        String result = pdfGeneratorService.generatedEvaluationByTeacher(
                evaluation, formData, teacherInfo, "fr"
        );

        assertNotNull(result);
        assertTrue(Files.exists(Path.of(result)));
    }

    @Test
    void testGeneratedEvaluationByTeacher_CreatesDirectoryIfNotExists() {
        Path customDir = tempDir.resolve("custom_evaluations");
        ReflectionTestUtils.setField(pdfGeneratorService, "baseEvaluationsDir", customDir.toString());

        EvaluationStagiaire evaluation = createTestEvaluation();
        EvaluationProfFormDto formData = createTestTeacherFormData();
        EvaluationTeacherInfoDto teacherInfo = createTestTeacherInfo();

        pdfGeneratorService.generatedEvaluationByTeacher(evaluation, formData, teacherInfo, "fr");

        assertTrue(Files.exists(customDir));
    }

    // ==================== Helper Methods ====================

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
                .program(Program.COMPUTER_SCIENCE.getTranslationKey())
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
                .description("Test Offer Description")
                .startDate(LocalDate.now())
                .durationInWeeks(12)
                .address("123 Test St")
                .remuneration(500.0f)
                .employeur(employeur)
                .status(InternshipOffer.Status.PUBLISHED)
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
                .missionsObjectifs("Test missions and objectives")
                .statut(EntenteStage.StatutEntente.BROUILLON)
                .dateCreation(java.time.LocalDateTime.now())
                .build();

        return entente;
    }

    private EvaluationStagiaire createTestEvaluation() {
        Student student = Student.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .program(Program.COMPUTER_SCIENCE.getTranslationKey())
                .build();

        Employeur employeur = Employeur.builder()
                .id(1L)
                .firstName("Jane")
                .lastName("Smith")
                .companyName("Test Company")
                .build();

        InternshipOffer offer = InternshipOffer.builder()
                .id(1L)
                .description("Test Offer Description")
                .startDate(LocalDate.now())
                .durationInWeeks(12)
                .address("123 Test St")
                .remuneration(500.0f)
                .employeur(employeur)
                .status(InternshipOffer.Status.PUBLISHED)
                .build();

        EvaluationStagiaire evaluation = EvaluationStagiaire.builder()
                .id(1L)
                .student(student)
                .employeur(employeur)
                .internshipOffer(offer)
                .dateEvaluation(LocalDate.now())
                .build();

        return evaluation;
    }

    private EvaluationEmployerFormData createTestEmployerFormData() {
        Map<String, List<EmployerQuestionResponse>> categories = new HashMap<>();

        categories.put("productivity", createEmployerQuestionResponses(5));
        categories.put("quality", createEmployerQuestionResponses(5));
        categories.put("relationships", createEmployerQuestionResponses(6));
        categories.put("skills", createEmployerQuestionResponses(6));

        return new EvaluationEmployerFormData(
                categories,
                "Good performance",
                1,
                "Good performance",
                false,
                15,
                "NO",
                false
        );
    }

    private EvaluationEmployerFormData createTestEmployerFormDataWithAllRatings() {
        Map<String, List<EmployerQuestionResponse>> categories = new HashMap<>();

        List<EmployerQuestionResponse> productivity = List.of(
                new EmployerQuestionResponse("EXCELLENT", false, "Great job"),
                new EmployerQuestionResponse("TRES_BIEN", false,"Very good"),
                new EmployerQuestionResponse("SATISFAISANT",false, "Acceptable"),
                new EmployerQuestionResponse("A_AMELIORER",false, "Needs work"),
                new EmployerQuestionResponse("EXCELLENT", false,null)
        );
        categories.put("productivity", productivity);

        categories.put("quality", createEmployerQuestionResponses(5));
        categories.put("relationships", createEmployerQuestionResponses(6));
        categories.put("skills", createEmployerQuestionResponses(6));

        return new EvaluationEmployerFormData(
                categories,
                null,
                1,
                null,
                false,
                15,
                "NO",
                false
        );
    }

    private EvaluationEmployerFormData createTestEmployerFormDataWithComments() {
        Map<String, List<EmployerQuestionResponse>> categories = new HashMap<>();

        List<EmployerQuestionResponse> responses = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            responses.add(new EmployerQuestionResponse("TRES_BIEN", false,"Comment " + i));
        }
        categories.put("productivity", responses);
        categories.put("quality", createEmployerQuestionResponses(5));
        categories.put("relationships", createEmployerQuestionResponses(6));
        categories.put("skills", createEmployerQuestionResponses(6));

        return new EvaluationEmployerFormData(
                categories,
                "Overall excellent internship experience",
                1,
                "Overall excellent internship experience",
                false,
                15,
                "NO",
                false
        );
    }

    private List<EmployerQuestionResponse> createEmployerQuestionResponses(int count) {
        List<EmployerQuestionResponse> responses = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            responses.add(new EmployerQuestionResponse("TRES_BIEN", false,null));
        }
        return responses;
    }

    private EvaluationProfFormDto createTestTeacherFormData() {
        Map<String, List<QuestionResponseTeacher>> categories = new HashMap<>();

        categories.put("conformity", createTeacherQuestionResponses(3));
        categories.put("environment", createTeacherQuestionResponses(2));
        categories.put("general", createTeacherQuestionResponses(5));

        return new EvaluationProfFormDto(
                categories,
                1,
                2,
                false,
                false,
                new ArrayList<>()
        );
    }

    private EvaluationProfFormDto createTestTeacherFormDataWithAllCategories() {
        Map<String, List<QuestionResponseTeacher>> categories = new HashMap<>();

        List<QuestionResponseTeacher> conformity = List.of(
                new QuestionResponseTeacher("EXCELLENT"),
                new QuestionResponseTeacher("TRES_BIEN"),
                new QuestionResponseTeacher("SATISFAISANT")
        );
        categories.put("conformity", conformity);

        List<QuestionResponseTeacher> environment = List.of(
                new QuestionResponseTeacher("TRES_BIEN"),
                new QuestionResponseTeacher("EXCELLENT")
        );
        categories.put("environment", environment);

        List<QuestionResponseTeacher> general = List.of(
                new QuestionResponseTeacher("SATISFAISANT"),
                new QuestionResponseTeacher("TRES_BIEN"),
                new QuestionResponseTeacher("EXCELLENT"),
                new QuestionResponseTeacher("A_AMELIORER"),
                new QuestionResponseTeacher("TRES_BIEN")
        );
        categories.put("general", general);

        return new EvaluationProfFormDto(
                categories,
                1,
                2,
                false,
                false,
                new ArrayList<>()
        );
    }

    private List<QuestionResponseTeacher> createTeacherQuestionResponses(int count) {
        List<QuestionResponseTeacher> responses = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            responses.add(new QuestionResponseTeacher("TRES_BIEN"));
        }
        return responses;
    }

    private EvaluationTeacherInfoDto createTestTeacherInfo() {
        ProfDto profDto = ProfDto.builder()
                .id(1L)
                .firstName("Jean")
                .lastName("Dupont")
                .email("jean.dupont@college.com")
                .role(Role.PROF)
                .employeeNumber("EMP123")
                .nameCollege("Collège Test")
                .address("123 rue Test")
                .fax_machine("514-555-9999")
                .department("Informatique")
                .phoneNumber("514-555-1234")
                .build();

        EntrepriseTeacherDto entrepriseDto = new EntrepriseTeacherDto(
                "Test Company",
                "Jane Smith",
                "123 Test St, Montreal",
                "jane.smith@company.com"
        );

        StudentTeacherDto studentDto = new StudentTeacherDto(
                "John Doe",
                LocalDate.now()
        );

        return new EvaluationTeacherInfoDto(entrepriseDto, studentDto, profDto);
    }
}