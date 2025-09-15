package ca.cal.leandrose.presentation;

import ca.cal.leandrose.presentation.request.RegisterEmployeur;
import ca.cal.leandrose.service.EmployeurService;
import ca.cal.leandrose.service.dto.EmployeurDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class RegisterControllerTest {

    @Mock
    private EmployeurService employeurService;

    @InjectMocks
    private RegisterController registerController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(registerController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void testRegisterEmployeurSuccess() throws Exception {
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
    void testRegisterEmployeurInvalidInput() throws Exception {
        RegisterEmployeur request = new RegisterEmployeur();
        request.setFirstName(""); // Invalid: blank
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
}
