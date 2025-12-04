package ca.cal.leandrose.presentation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import ca.cal.leandrose.presentation.request.RegisterEmployeur;
import ca.cal.leandrose.presentation.request.RegisterStudent;
import ca.cal.leandrose.service.EmployeurService;
import ca.cal.leandrose.service.GestionnaireService;
import ca.cal.leandrose.service.StudentService;
import ca.cal.leandrose.service.dto.EmployeurDto;
import ca.cal.leandrose.service.dto.ProgramDto;
import ca.cal.leandrose.service.dto.StudentDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@ActiveProfiles("test")
@WebMvcTest(controllers = RegisterController.class)
@AutoConfigureMockMvc(addFilters = false)
class RegisterControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockitoBean private EmployeurService employeurService;
    @MockitoBean private StudentService studentService;
    @MockitoBean private GestionnaireService gestionnaireService;

    
    
    
    @Test
    @DisplayName("POST /api/register/employeur returns 201 and employer details on success")
    void testRegisterEmployeurSuccess() throws Exception {
        RegisterEmployeur request = new RegisterEmployeur();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setEmail("john.doe@example.com");
        request.setPassword("Password123!"); 
        request.setCompanyName("TechCorp");
        request.setField("IT");

        EmployeurDto dto = new EmployeurDto();
        dto.setId(1L);
        dto.setFirstName("John");
        dto.setLastName("Doe");
        dto.setEmail("john.doe@example.com");
        dto.setCompanyName("TechCorp");
        dto.setField("IT");

        when(employeurService.createEmployeur(
                anyString(), anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(dto);

        mockMvc.perform(post("/api/register/employeur")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"));

        verify(employeurService, times(1))
                .createEmployeur("John", "Doe", "john.doe@example.com", "Password123!", "TechCorp", "IT");
    }

    @Test
    @DisplayName("POST /api/register/employeur returns 400 for invalid input")
    void testRegisterEmployeurInvalidInput() throws Exception {
        RegisterEmployeur request = new RegisterEmployeur();
        request.setFirstName("");
        request.setLastName("Doe");
        request.setEmail("invalid-email");
        request.setPassword("pass"); 
        request.setCompanyName("TechCorp");
        request.setField("IT");

        mockMvc.perform(post("/api/register/employeur")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(employeurService);
    }

    @Test
    @DisplayName("POST /api/register/employeur returns 409 when email already exists")
    void testRegisterEmployeurEmailConflict() throws Exception {
        RegisterEmployeur request = new RegisterEmployeur();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setEmail("john@example.com");
        request.setPassword("Password123!");
        request.setCompanyName("TechCorp");
        request.setField("IT");

        when(employeurService.createEmployeur(any(), any(), any(), any(), any(), any()))
                .thenThrow(new org.springframework.dao.DataIntegrityViolationException("duplicate"));

        mockMvc.perform(post("/api/register/employeur")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Cet email est déjà utilisé"));    }

    @Test
    @DisplayName("POST /api/register/employeur returns 500 on server error")
    void testRegisterEmployeurServerError() throws Exception {
        RegisterEmployeur request = new RegisterEmployeur();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setEmail("john@example.com");
        request.setPassword("Password123!");
        request.setCompanyName("TechCorp");
        request.setField("IT");

        when(employeurService.createEmployeur(any(), any(), any(), any(), any(), any()))
                .thenThrow(new RuntimeException("server error"));

        mockMvc.perform(post("/api/register/employeur")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Erreur serveur"));
    }

    
    
    
    @Test
    @DisplayName("POST /api/register/student returns 201 and student details on success")
    void testRegisterStudentSuccess() throws Exception {
        RegisterStudent request = new RegisterStudent();
        request.setFirstName("Marie");
        request.setLastName("Dupont");
        request.setEmail("marie.dupont@student.com");
        request.setPassword("Password123!");
        request.setStudentNumber("STU12345");
        request.setProgram("Informatique");

        StudentDto dto = new StudentDto();
        dto.setId(1L);
        dto.setFirstName("Marie");
        dto.setLastName("Dupont");
        dto.setEmail("marie.dupont@student.com");
        dto.setStudentNumber("STU12345");
        dto.setProgram("Informatique");

        when(studentService.createStudent(any(), any(), any(), any(), any(), any()))
                .thenReturn(dto);

        mockMvc.perform(post("/api/register/student")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.firstName").value("Marie"))
                .andExpect(jsonPath("$.email").value("marie.dupont@student.com"))
                .andExpect(jsonPath("$.studentNumber").value("STU12345"));

        verify(studentService, times(1))
                .createStudent("Marie", "Dupont", "marie.dupont@student.com",
                        "Password123!", "STU12345", "Informatique");
    }

    @Test
    @DisplayName("POST /api/register/student returns 400 for invalid input")
    void testRegisterStudentInvalidInput() throws Exception {
        RegisterStudent request = new RegisterStudent();
        request.setFirstName("");
        request.setLastName("Dupont");
        request.setEmail("invalid-email");
        request.setPassword("pass"); 
        request.setStudentNumber("");
        request.setProgram("");

        mockMvc.perform(post("/api/register/student")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(studentService);
    }

    @Test
    @DisplayName("POST /api/register/student returns 409 when email already exists")
    void testRegisterStudentEmailConflict() throws Exception {
        RegisterStudent request = new RegisterStudent();
        request.setFirstName("Marie");
        request.setLastName("Dupont");
        request.setEmail("marie@example.com");
        request.setPassword("Password123!");
        request.setStudentNumber("STU12345");
        request.setProgram("Informatique");

        when(studentService.createStudent(any(), any(), any(), any(), any(), any()))
                .thenThrow(new org.springframework.dao.DataIntegrityViolationException("duplicate"));

        mockMvc.perform(post("/api/register/student")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.error").value("Cet email est déjà utilisé"));
    }

    @Test
    @DisplayName("POST /api/register/student returns 500 on server error")
    void testRegisterStudentServerError() throws Exception {
        RegisterStudent request = new RegisterStudent();
        request.setFirstName("Marie");
        request.setLastName("Dupont");
        request.setEmail("marie@example.com");
        request.setPassword("Password123!");
        request.setStudentNumber("STU12345");
        request.setProgram("Informatique");

        when(studentService.createStudent(any(), any(), any(), any(), any(), any()))
                .thenThrow(new RuntimeException("unexpected"));

        mockMvc.perform(post("/api/register/student")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error.error").value("Erreur serveur"));
    }

    
    
    
    @Test
    @DisplayName("GET /api/register/programs returns list of programs")
    void testGetPrograms() throws Exception {
        var list = List.of(new ProgramDto("Informatique", ""), new ProgramDto("Finance", ""));
        when(gestionnaireService.getAllPrograms()).thenReturn(list);

        mockMvc.perform(get("/api/register/programs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].code").value("Informatique"))
                .andExpect(jsonPath("$[1].code").value("Finance"));

        verify(gestionnaireService).getAllPrograms();
    }
}
