package ca.cal.leandrose.presentation;

import ca.cal.leandrose.service.AuthService;
import ca.cal.leandrose.service.UserAppService;
import ca.cal.leandrose.service.dto.JWTAuthResponse;
import ca.cal.leandrose.service.dto.LoginDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UserControllerTest {

    private AuthService authService;
    private UserAppService userAppService; // mock mais non utilisé ici
    private UserController userController;

    @BeforeEach
    void setUp() {
        authService = mock(AuthService.class);
        userAppService = mock(UserAppService.class);
        userController = new UserController(authService, userAppService);
    }

    @Test
    void authenticateUser_ShouldReturnOk_WhenLoginIsSuccessful() {
        // Arrange
        LoginDTO loginDto = new LoginDTO("user@test.com", "password");
        when(authService.login(any(LoginDTO.class))).thenReturn("jwt-token");

        // Act
        ResponseEntity<?> response = userController.authenticateUser(loginDto);

        // Assert
        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody() instanceof JWTAuthResponse);
        assertEquals("jwt-token", ((JWTAuthResponse) response.getBody()).getAccessToken());
        verify(authService, times(1)).login(any(LoginDTO.class));
    }

    @Test
    void authenticateUser_ShouldReturnUnauthorized_WhenLoginFails() {
        // Arrange
        LoginDTO loginDto = new LoginDTO("user@test.com", "wrong");
        when(authService.login(any(LoginDTO.class)))
                .thenThrow(new RuntimeException("Invalid credentials"));

        // Act
        ResponseEntity<?> response = userController.authenticateUser(loginDto);

        // Assert
        assertEquals(401, response.getStatusCodeValue());
        assertEquals("{\"error\":\"Invalid credentials\"}", response.getBody());
        verify(authService, times(1)).login(any(LoginDTO.class));
    }
}
