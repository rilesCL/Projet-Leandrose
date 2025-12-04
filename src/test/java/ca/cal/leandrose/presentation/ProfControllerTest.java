package ca.cal.leandrose.presentation;

import ca.cal.leandrose.model.EvaluationStatus;
import ca.cal.leandrose.model.auth.Role;
import ca.cal.leandrose.security.TestSecurityConfiguration;
import ca.cal.leandrose.service.EntenteStageService;
import ca.cal.leandrose.service.EvaluationStagiaireService;
import ca.cal.leandrose.service.UserAppService;
import ca.cal.leandrose.service.dto.*;
import ca.cal.leandrose.service.dto.evaluation.CreateEvaluationRequest;
import ca.cal.leandrose.service.dto.evaluation.CreatorTypeEvaluation;
import ca.cal.leandrose.service.dto.evaluation.EvaluationStagiaireDto;
import ca.cal.leandrose.service.dto.evaluation.prof.EvaluationProfFormDto;
import ca.cal.leandrose.service.dto.evaluation.prof.EvaluationTeacherInfoDto;
import ca.cal.leandrose.service.dto.evaluation.prof.QuestionResponseTeacher;
import ca.cal.leandrose.service.dto.evaluation.prof.WorkShiftRange;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.file.AccessDeniedException;
import java.time.LocalDate;
import java.util.*;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ProfController.class)
@ActiveProfiles("test")
@Import(TestSecurityConfiguration.class)
class ProfControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockitoBean
    private EntenteStageService ententeStageService;

    @MockitoBean
    private EvaluationStagiaireService evaluationStagiaireService;

    @MockitoBean
    private UserAppService userAppService;

    private ProfDto profDto;
    private StudentDto studentDto;
    private InternshipOfferDto internshipOfferDto;
    private EvaluationStagiaireDto evaluationDto;
    private MockHttpServletRequest request;
    private EvaluationProfFormDto formData;
    private EmployeurDto employeurDto;

    private ProfStudentItemDto dto(long ententeId, long studentId, String fn, String ln,
                                   String company, String title,
                                   LocalDate start, LocalDate end,
                                   String stageStatus, String evaluationStatus) {
        return ProfStudentItemDto.builder()
                .ententeId(ententeId)
                .studentId(studentId)
                .studentFirstName(fn)
                .studentLastName(ln)
                .companyName(company)
                .offerTitle(title)
                .startDate(start)
                .endDate(end)
                .stageStatus(stageStatus)
                .evaluationStatus(evaluationStatus)
                .build();
    }

    @BeforeEach
    void setup(){
        request = new MockHttpServletRequest();
        objectMapper = new ObjectMapper();
        profDto = ProfDto.builder()
                .id(2L)
                .role(Role.PROF)
                .firstName("Jean")
                .lastName("Dupont")
                .email("prof@gmail.com")
                .employeeNumber("dakdadas")
                .nameCollege("College Mainsonneuve")
                .address("Rue Debois")
                .fax_machine("437893434")
                .department("Informatique")
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
        employeurDto =  EmployeurDto.builder()
                .id(1L)
                .role(Role.EMPLOYEUR)
                .firstName("Employeur")
                .lastname("Test")
                .email("employeur@test.com")
                .companyName("TechCorp")
                .build();
        internshipOfferDto = InternshipOfferDto.builder()
                .id(100L)
                .description("Stage Java")
                .employeurDto(employeurDto)
                .pdfPath("dummy.pdf")
                .employeurId(1L)
                .build();

        evaluationDto = new EvaluationStagiaireDto(
                3L, LocalDate.now(), 2L, 1L, 2L,  100L,"/path/to/pdf", null, true, false, EvaluationStatus.EN_COURS
        );
        formData = createEvaluationProfFormData();
    }

    private EvaluationProfFormDto createEvaluationProfFormData(){
        QuestionResponseTeacher question1 = new QuestionResponseTeacher(
                "EXCELLENT"
        );

        QuestionResponseTeacher question2 = new QuestionResponseTeacher(
                "SATISFACTORY"
        );

        QuestionResponseTeacher question3 = new QuestionResponseTeacher(
                "NEED IMPROVEMENT"
        );

        Map<String, List<QuestionResponseTeacher>> categories = new HashMap<>();
        categories.put("Technical Skills", Arrays.asList(question1, question2));
        categories.put("Academic Performance", Arrays.asList(question1, question3));
        categories.put("Professional Development", Arrays.asList(question2));
        categories.put("Communication Skills", Arrays.asList(question1, question2, question3));

        List<WorkShiftRange> workShifts = Arrays.asList(
                new WorkShiftRange("09:00", "12:00"),
                new WorkShiftRange( "13:00", "17:00")
        );

        return new EvaluationProfFormDto(
                categories,
                4,
                5,                                    true,
                true,
                workShifts
        );
    }

    @Test
    void getEtudiantsAttribues_DefaultParams() throws Exception {
        var item = dto(10L, 5L, "John", "Doe", "TechCorp", "Stage dev",
                LocalDate.of(2025, 6, 1), LocalDate.of(2025, 8, 24), "EN_COURS", "A_FAIRE");

        when(ententeStageService.getEtudiantsPourProf(1L, null, null, null, null, null, "name", true))
                .thenReturn(List.of(item));

        mockMvc.perform(get("/prof/1/etudiants").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].ententeId", is(10)))
                .andExpect(jsonPath("$[0].studentId", is(5)))
                .andExpect(jsonPath("$[0].studentFirstName", is("John")))
                .andExpect(jsonPath("$[0].studentLastName", is("Doe")))
                .andExpect(jsonPath("$[0].companyName", is("TechCorp")))
                .andExpect(jsonPath("$[0].offerTitle", is("Stage dev")))
                .andExpect(jsonPath("$[0].stageStatus", is("EN_COURS")))
                .andExpect(jsonPath("$[0].evaluationStatus", is("A_FAIRE")));

        verify(ententeStageService, times(1))
                .getEtudiantsPourProf(1L, null, null, null, null, null, "name", true);
    }

    @Test
    void getEtudiantsAttribues_AllFiltersAndSortParams() throws Exception {
        var item = dto(20L, 8L, "Sophie", "Martin", "TechInnovation", "Full-Stack",
                LocalDate.of(2025, 5, 20), LocalDate.of(2025, 9, 7), "EN_COURS", "EN_COURS");

        when(ententeStageService.getEtudiantsPourProf(
                eq(2L),
                eq("sophie"),
                eq("tech"),
                eq(LocalDate.of(2025, 5, 1)),
                eq(LocalDate.of(2025, 9, 30)),
                eq("EN_COURS"),
                eq("company"),
                eq(false)
        )).thenReturn(List.of(item));

        mockMvc.perform(get("/prof/2/etudiants")
                        .param("nom", "sophie")
                        .param("entreprise", "tech")
                        .param("dateFrom", "2025-05-01")
                        .param("dateTo", "2025-09-30")
                        .param("evaluationStatus", "EN_COURS")
                        .param("sortBy", "company")
                        .param("asc", "false")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].ententeId", is(20)))
                .andExpect(jsonPath("$[0].studentId", is(8)))
                .andExpect(jsonPath("$[0].companyName", is("TechInnovation")))
                .andExpect(jsonPath("$[0].evaluationStatus", is("EN_COURS")));

        ArgumentCaptor<Long> profId = ArgumentCaptor.forClass(Long.class);
        verify(ententeStageService, times(1)).getEtudiantsPourProf(
                profId.capture(),
                eq("sophie"),
                eq("tech"),
                eq(LocalDate.of(2025, 5, 1)),
                eq(LocalDate.of(2025, 9, 30)),
                eq("EN_COURS"),
                eq("company"),
                eq(false)
        );
        org.junit.jupiter.api.Assertions.assertEquals(2L, profId.getValue());
    }

    @Test
    void createEvaluation_success_returnsOk() throws Exception {
        when(userAppService.getMe(anyString())).thenReturn(profDto);
        when(evaluationStagiaireService.isEvaluationEligible(CreatorTypeEvaluation.PROF, 2L, 2L, 100L)).thenReturn(true);
        when(evaluationStagiaireService.createEvaluationByProf(2L, 2L, 100L)).thenReturn(evaluationDto);

        CreateEvaluationRequest createRequest = new CreateEvaluationRequest(2L, 100L);

        mockMvc.perform(post("/prof/evaluations")
                        .header("Authorization", "Bearer token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isOk());
    }

    @Test
    void createEvaluation_notProf_returnsForbidden() throws Exception {
        when(userAppService.getMe(anyString())).thenReturn(studentDto);

        CreateEvaluationRequest createRequest = new CreateEvaluationRequest(2L, 3L);

        mockMvc.perform(post("/prof/evaluations")
                        .header("Authorization", "Bearer token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    void createEvaluation_notEligible_returnsBadRequest() throws Exception {
        when(userAppService.getMe(anyString())).thenReturn(profDto);
        when(evaluationStagiaireService.isEvaluationEligible(CreatorTypeEvaluation.PROF, 2L, 2L, 3L)).thenReturn(false);

        CreateEvaluationRequest createRequest = new CreateEvaluationRequest(2L, 3L);

        mockMvc.perform(post("/prof/evaluations")
                        .header("Authorization", "Bearer token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createEvaluation_serviceException_returnsBadRequest() throws Exception {
        when(userAppService.getMe(anyString())).thenReturn(profDto);
        when(evaluationStagiaireService.isEvaluationEligible(CreatorTypeEvaluation.PROF ,2L, 2L, 3L)).thenReturn(true);
        when(evaluationStagiaireService.createEvaluationByProf(2L, 2L, 3L))
                .thenThrow(new RuntimeException("Evaluation error"));

        CreateEvaluationRequest createRequest = new CreateEvaluationRequest(2L, 3L);

        mockMvc.perform(post("/prof/evaluations")
                        .header("Authorization", "Bearer token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void generateEvaluationPdf_success_returnsOk() throws Exception {
        when(userAppService.getMe(anyString())).thenReturn(profDto);

        when(evaluationStagiaireService.getEvaluationById(1L)).thenReturn(evaluationDto);

        EvaluationStagiaireDto evaluationWithPdf = evaluationDto;

        when(evaluationStagiaireService.generateEvaluationByTeacher(eq(1L), any(), eq("fr")))
                .thenReturn(evaluationWithPdf);

        mockMvc.perform(post("/prof/evaluations/1/generate-pdf")
                        .header("Authorization", "Bearer token")
                        .header("Accept-Language", "fr")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(formData)))
                .andExpect(status().isOk());
    }

    @Test
    void generateEvaluationPdf_notProf_returnsForbidden() throws Exception {
        when(userAppService.getMe(anyString())).thenReturn(studentDto);

        mockMvc.perform(post("/prof/evaluations/1/generate-pdf")
                        .header("Authorization", "Bearer token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(formData)))
                .andExpect(status().isForbidden());
    }

    @Test
    void generateEvaluationPdf_notOwner_returnsForbidden() throws Exception {
        EvaluationStagiaireDto otherEvaluation = new EvaluationStagiaireDto(1L, LocalDate.now(), 2L,1L, 2L,
                100L, null, null, false, false, EvaluationStatus.EN_COURS);

        when(userAppService.getMe(anyString())).thenReturn(profDto);
        when(evaluationStagiaireService.getEvaluationById(1L)).thenReturn(otherEvaluation);

        mockMvc.perform(post("/prof/evaluations/1/generate-pdf")
                        .header("Authorization", "Bearer token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(formData)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getEvaluationPdf_success_returnsOk() throws Exception {
        byte[] pdfBytes = "mock-pdf-content".getBytes();

        when(userAppService.getMe(anyString())).thenReturn(profDto);
        when(evaluationStagiaireService.getEvaluationById(1L)).thenReturn(evaluationDto);
        when(evaluationStagiaireService.getEvaluationPdf(1L, CreatorTypeEvaluation.PROF)).thenReturn(pdfBytes);

        mockMvc.perform(get("/prof/evaluations/1/pdf")
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF));
    }

    @Test
    void getEvaluationPdf_notFound_returnsNotFound() throws Exception {
        when(userAppService.getMe(anyString())).thenReturn(profDto);
        when(evaluationStagiaireService.getEvaluationById(1L)).thenReturn(evaluationDto);
        when(evaluationStagiaireService.getEvaluationPdf(1L, CreatorTypeEvaluation.PROF))
                .thenThrow(new RuntimeException("PDF not found"));

        mockMvc.perform(get("/prof/evaluations/1/pdf")
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getMyEvaluations_success_returnsOk() throws Exception {
        when(userAppService.getMe(anyString())).thenReturn(profDto);
        when(evaluationStagiaireService.getEvaluationsByProfesseur(2L)).thenReturn(List.of(evaluationDto));

        mockMvc.perform(get("/prof/evaluations")
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isOk());
    }

    @Test
    void getMyEvaluations_notProf_returnsForbidden() throws Exception {
        when(userAppService.getMe(anyString())).thenReturn(studentDto);

        mockMvc.perform(get("/prof/evaluations")
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getMyEvaluations_serviceException_returnsInternalServerError() throws Exception {
        when(userAppService.getMe(anyString())).thenReturn(profDto);
        when(evaluationStagiaireService.getEvaluationsByProfesseur(2L))
                .thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(get("/prof/evaluations")
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void getEvaluation_success_returnsOk() throws Exception {
        when(userAppService.getMe(anyString())).thenReturn(profDto);
        when(evaluationStagiaireService.getEvaluationById(1L)).thenReturn(evaluationDto);

        mockMvc.perform(get("/prof/evaluation/1")
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isOk());
    }

    @Test
    void getEvaluation_notOwner_returnsForbidden() throws Exception {
        ProfDto differentProf = ProfDto.builder()
                .id(999L)
                .role(Role.PROF)
                .firstName("Different")
                .lastName("professeur")
                .email("different@test.com")
                .nameCollege("Collège Mainsonneuve")
                .build();


        EvaluationStagiaireDto otherEvaluation = new EvaluationStagiaireDto(
                1L, LocalDate.now(), 2L, 1L, 2L, 100L,
                null, null, false, false, EvaluationStatus.EN_COURS
        );

        when(userAppService.getMe(anyString())).thenReturn(differentProf);
        when(evaluationStagiaireService.getEvaluationById(1L)).thenReturn(otherEvaluation);

        mockMvc.perform(get("/prof/evaluation/1")
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getEvaluation_notFound_returnsNotFound() throws Exception {
        when(userAppService.getMe(anyString())).thenReturn(profDto);
        when(evaluationStagiaireService.getEvaluationById(1L))
                .thenThrow(new RuntimeException("Evaluation not found"));

        mockMvc.perform(get("/prof/evaluation/1")
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getEligibleEvaluations_success_returnsOk() throws Exception {
        when(userAppService.getMe(anyString())).thenReturn(profDto);
        when(evaluationStagiaireService.getEligibleEvaluations(CreatorTypeEvaluation.PROF, 2L)).thenReturn(List.of());

        mockMvc.perform(get("/prof/evaluations/eligible")
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isOk());
    }

    @Test
    void getEligibleEvaluations_notProf_returnsForbidden() throws Exception {
        when(userAppService.getMe(anyString())).thenReturn(studentDto);

        mockMvc.perform(get("/prof/evaluations/eligible")
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getEvaluationInfo_success_returnsOk() throws Exception {
        when(userAppService.getMe(anyString())).thenReturn(profDto);
        when(evaluationStagiaireService.getEvaluationInfoForTeacher(2L, 2L, 3L))
                .thenReturn(new EvaluationTeacherInfoDto(null, null, null));

        mockMvc.perform(get("/prof/evaluations/info")
                        .header("Authorization", "Bearer token")
                        .param("studentId", "2")
                        .param("offerId", "3"))
                .andExpect(status().isOk());
    }

    @Test
    void getEvaluationInfo_serviceException_returnsBadRequest() throws Exception {
        when(userAppService.getMe(anyString())).thenReturn(profDto);
        when(evaluationStagiaireService.getEvaluationInfoForTeacher(2L, 2L, 3L))
                .thenThrow(new RuntimeException("Evaluation not allowed"));

        mockMvc.perform(get("/prof/evaluations/info")
                        .header("Authorization", "Bearer token")
                        .param("studentId", "2")
                        .param("offerId", "3"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void checkExistingEvaluation_exists_returnsOk() throws Exception {
        when(userAppService.getMe(anyString())).thenReturn(profDto);
        when(evaluationStagiaireService.getExistingEvaluation(2L, 3L))
                .thenReturn(Optional.of(evaluationDto));

        mockMvc.perform(get("/prof/evaluations/check-existing")
                        .header("Authorization", "Bearer token")
                        .param("studentId", "2")
                        .param("offerId", "3"))
                .andExpect(status().isOk());
    }

    @Test
    void checkExistingEvaluation_notExists_returnsOk() throws Exception {
        when(userAppService.getMe(anyString())).thenReturn(profDto);
        when(evaluationStagiaireService.getExistingEvaluation(2L, 3L))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/prof/evaluations/check-existing")
                        .header("Authorization", "Bearer token")
                        .param("studentId", "2")
                        .param("offerId", "3"))
                .andExpect(status().isOk());
    }

    @Test
    void checkExistingEvaluation_notProf_returnsForbidden() throws Exception {
        when(userAppService.getMe(anyString())).thenReturn(studentDto);

        mockMvc.perform(get("/prof/evaluations/check-existing")
                        .header("Authorization", "Bearer token")
                        .param("studentId", "2")
                        .param("offerId", "3"))
                .andExpect(status().isForbidden());
    }

    @Test
    void checkTeacherAssigned_success_returnsOk() throws Exception {
        when(userAppService.getMe(anyString())).thenReturn(profDto);
        when(ententeStageService.isTeacherAssigned(2L, 100L)).thenReturn(true);

        mockMvc.perform(get("/prof/evaluations/check-teacher-assigned")
                        .header("Authorization", "Bearer token")
                        .param("studentId", "2")
                        .param("offerId", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.teacherAssigned").value(true));
    }

    @Test
    void checkTeacherAssigned_serviceException_returnsBadRequest() throws Exception {
        when(userAppService.getMe(anyString())).thenReturn(profDto);
        when(ententeStageService.isTeacherAssigned(2L, 100L))
                .thenThrow(new RuntimeException("Service error"));

        mockMvc.perform(get("/prof/evaluations/check-teacher-assigned")
                        .header("Authorization", "Bearer token")
                        .param("studentId", "2")
                        .param("offerId", "100"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void generateEvaluationPdf_authenticationException_returnsUnauthorized() throws Exception {
        when(userAppService.getMe(anyString()))
                .thenThrow(new RuntimeException("Authentication failed"));

        mockMvc.perform(post("/prof/evaluations/1/generate-pdf")
                        .header("Authorization", "Bearer token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(formData)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void generateEvaluationPdf_serviceException_returnsBadRequest() throws Exception {
        when(userAppService.getMe(anyString())).thenReturn(profDto);
        when(evaluationStagiaireService.getEvaluationById(1L)).thenReturn(evaluationDto);
        when(evaluationStagiaireService.generateEvaluationByTeacher(eq(1L), any(), eq("fr")))
                .thenThrow(new RuntimeException("PDF generation error"));

        mockMvc.perform(post("/prof/evaluations/1/generate-pdf")
                        .header("Authorization", "Bearer token")
                        .header("Accept-Language", "fr")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(formData)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void generateEvaluationPdf_englishLanguage_returnsOk() throws Exception {
        when(userAppService.getMe(anyString())).thenReturn(profDto);
        when(evaluationStagiaireService.getEvaluationById(1L)).thenReturn(evaluationDto);
        when(evaluationStagiaireService.generateEvaluationByTeacher(eq(1L), any(), eq("en")))
                .thenReturn(evaluationDto);

        mockMvc.perform(post("/prof/evaluations/1/generate-pdf")
                        .header("Authorization", "Bearer token")
                        .header("Accept-Language", "en-US")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(formData)))
                .andExpect(status().isOk());
    }

    @Test
    void getEvaluationPdf_notOwner_returnsForbidden() throws Exception {
        ProfDto differentProf = ProfDto.builder()
                .id(999L)
                .role(Role.PROF)
                .build();

        EvaluationStagiaireDto otherEvaluation = new EvaluationStagiaireDto(
                1L, LocalDate.now(), 2L, 999L, 2L, 100L,
                null, null, false, false, EvaluationStatus.EN_COURS
        );

        when(userAppService.getMe(anyString())).thenReturn(differentProf);
        when(evaluationStagiaireService.getEvaluationById(1L)).thenReturn(otherEvaluation);

        mockMvc.perform(get("/prof/evaluations/1/pdf")
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getEvaluationInfo_notProf_returnsForbidden() throws Exception {
        when(userAppService.getMe(anyString())).thenReturn(studentDto);

        mockMvc.perform(get("/prof/evaluations/info")
                        .header("Authorization", "Bearer token")
                        .param("studentId", "2")
                        .param("offerId", "3"))
                .andExpect(status().isForbidden());
    }
}
