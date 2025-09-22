package ca.cal.leandrose.presentation;

import ca.cal.leandrose.service.AuthService;
import ca.cal.leandrose.service.UserAppService;
import ca.cal.leandrose.service.dto.LoginDTO;
import ca.cal.leandrose.service.dto.UserDTO;
import ca.cal.leandrose.model.auth.Role;
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


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@WebMvcTest(controllers = UserController.class)
@AutoConfigureMockMvc(addFilters = false) // disable security
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private UserAppService userService;



    @Test
    @DisplayName("POST /user/login returns 200 and JWT token on success")
    void authenticateUser_success_returnsToken() throws Exception {
        LoginDTO loginDto = new LoginDTO("user@example.com", "password");
        when(authService.login(any(LoginDTO.class))).thenReturn("jwt-token-123");

        mockMvc.perform(post("/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDto)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.accessToken").value("jwt-token-123"));
    }

    @Test
    @DisplayName("POST /user/login returns 401 on invalid credentials")
    void authenticateUser_failure_returnsUnauthorized() throws Exception {
        LoginDTO loginDto = new LoginDTO("user@example.com", "wrongpass");
        when(authService.login(any(LoginDTO.class))).thenThrow(new RuntimeException("Invalid credentials"));

        mockMvc.perform(post("/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDto)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("Invalid credentials"));
    }

    @Test
    @DisplayName("GET /user/me returns UserDTO")
    void getMe_returnsUserDto() throws Exception {
        String token = "Bearer jwt-token";
        UserDTO dto = new UserDTO();
        dto.setId(1L);
        dto.setEmail("user@example.com");
        dto.setRole(Role.EMPLOYEUR);

        when(userService.getMe(token)).thenReturn(dto);

        mockMvc.perform(get("/user/me")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.email").value("user@example.com"))
                .andExpect(jsonPath("$.role").value("EMPLOYEUR"));
    }
}
