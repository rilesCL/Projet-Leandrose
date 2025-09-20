package ca.cal.leandrose.presentation;

import ca.cal.leandrose.presentation.request.RegisterEmployeur;
import ca.cal.leandrose.presentation.request.RegisterStudent;
import ca.cal.leandrose.service.EmployeurService;
import ca.cal.leandrose.service.StudentService;
import ca.cal.leandrose.service.dto.EmployeurDto;
import ca.cal.leandrose.service.dto.StudentDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@WebMvcTest(controllers = RegisterController.class)
@AutoConfigureMockMvc(addFilters = false)
class RegisterControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private EmployeurService employeurService;

    @MockitoBean
    private StudentService studentService;

    @Test
    @DisplayName("POST /api/register/employeur returns 201 and employer details on success")
    void testRegisterEmployeurSuccess() throws Exception {
        // Arrange
        RegisterEmployeur request = new RegisterEmployeur();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setEmail("john.doe@example.com");
        request.setPassword("password123");
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
                anyString(), anyString(), anyString(), anyString(), anyString(), anyString()
        )).thenReturn(dto);

        // Act + Assert
        mockMvc.perform(post("/api/register/employeur")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"));

        verify(employeurService, times(1))
                .createEmployeur("John", "Doe", "john.doe@example.com", "password123", "TechCorp", "IT");
    }

    @Test
    @DisplayName("POST /api/register/employeur returns 400 for invalid input")
    void testRegisterEmployeurInvalidInput() throws Exception {
        //Arrange
        RegisterEmployeur request = new RegisterEmployeur();
        request.setFirstName("");
        request.setLastName("Doe");
        request.setEmail("invalid-email");
        request.setPassword("pass");
        request.setCompanyName("TechCorp");
        request.setField("IT");

        // Act
        mockMvc.perform(post("/api/register/employeur")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(employeurService);
    }


    @Test
    @DisplayName("POST /api/register/student returns 201 and student details on success")
    void testRegisterStudentSuccess() throws Exception {
        // Arrange
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

        when(studentService.createStudent(
                anyString(), anyString(), anyString(), anyString(), anyString(), anyString()
        )).thenReturn(dto);

        // Act + Assert
        mockMvc.perform(post("/api/register/student")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.firstName").value("Marie"))
                .andExpect(jsonPath("$.email").value("marie.dupont@student.com"))
                .andExpect(jsonPath("$.studentNumber").value("STU12345"));

        verify(studentService, times(1))
                .createStudent("Marie", "Dupont", "marie.dupont@student.com", "Password123!", "STU12345", "Informatique");
    }

    @Test
    @DisplayName("POST /api/register/student returns 400 for invalid input")
    void testRegisterStudentInvalidInput() throws Exception {
        // Arrange invalid request
        RegisterStudent request = new RegisterStudent();
        request.setFirstName(""); // Invalid
        request.setLastName("Dupont");
        request.setEmail("invalid-email");
        request.setPassword("weak"); // too weak
        request.setStudentNumber("STU123");
        request.setProgram("");

        // Act + Assert
        mockMvc.perform(post("/api/register/student")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(studentService);
    }
}
