package ca.cal.leandrose.service;

import ca.cal.leandrose.model.Employeur;
import ca.cal.leandrose.model.Gestionnaire;
import ca.cal.leandrose.model.Student;
import ca.cal.leandrose.security.JwtTokenProvider;
import ca.cal.leandrose.service.dto.LoginDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    private AuthenticationManager authenticationManager;
    private JwtTokenProvider jwtTokenProvider;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        authenticationManager = mock(AuthenticationManager.class);
        jwtTokenProvider = mock(JwtTokenProvider.class);
        authService = new AuthService(authenticationManager, jwtTokenProvider);
    }

    @Test
    void login_ShouldReturnToken_ForEmployeur_WithRole() {
        // Arrange
        LoginDTO loginDto = new LoginDTO("emp@test.com", "password");
        Employeur employeur = Employeur.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .email("emp@test.com")
                .password("encoded-pass")
                .companyName("TechCorp")
                .field("IT")
                .build();

        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_EMPLOYEUR"));
        Authentication authentication = new UsernamePasswordAuthenticationToken(employeur, null, authorities);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(jwtTokenProvider.generateToken(authentication)).thenReturn("jwt-employeur");

        // Act
        String token = authService.login(loginDto);

        // Assert
        assertEquals("jwt-employeur", token);
        assertTrue(authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_EMPLOYEUR")));

        verify(authenticationManager).authenticate(any());
        verify(jwtTokenProvider).generateToken(authentication);
    }

    @Test
    void login_ShouldReturnToken_ForStudent_WithRole() {
        // Arrange
        LoginDTO loginDto = new LoginDTO("student@test.com", "password");
        Student student = Student.builder()
                .id(2L)
                .firstName("Jane")
                .lastName("Smith")
                .email("student@test.com")
                .password("encoded-pass")
                .studentNumber("STU123")
                .program("CS")
                .build();

        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_STUDENT"));
        Authentication authentication = new UsernamePasswordAuthenticationToken(student, null, authorities);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(jwtTokenProvider.generateToken(authentication)).thenReturn("jwt-student");

        // Act
        String token = authService.login(loginDto);

        // Assert
        assertEquals("jwt-student", token);
        assertTrue(authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_STUDENT")));

        verify(authenticationManager).authenticate(any());
        verify(jwtTokenProvider).generateToken(authentication);
    }

    @Test
    void login_ShouldReturnToken_ForGestionnaire_WithRole() {
        // Arrange
        LoginDTO loginDto = new LoginDTO("manager@test.com", "password");
        Gestionnaire gestionnaire = Gestionnaire.builder()
                .id(3L)
                .firstName("Alice")
                .lastName("Manager")
                .email("manager@test.com")
                .password("encoded-pass")
                .matricule("GSNumber")
                .build();

        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_GESTIONNAIRE"));
        Authentication authentication = new UsernamePasswordAuthenticationToken(gestionnaire, null, authorities);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(jwtTokenProvider.generateToken(authentication)).thenReturn("jwt-gestionnaire");

        // Act
        String token = authService.login(loginDto);

        // Assert
        assertEquals("jwt-gestionnaire", token);
        assertTrue(authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_GESTIONNAIRE")));

        verify(authenticationManager).authenticate(any());
        verify(jwtTokenProvider).generateToken(authentication);
    }

    @Test
    void login_ShouldThrowException_WhenAuthenticationFails() {
        // Arrange
        LoginDTO loginDto = new LoginDTO("bad@test.com", "wrong");
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new RuntimeException("Bad credentials"));

        // Act + Assert
        assertThrows(RuntimeException.class, () -> authService.login(loginDto));
        verify(authenticationManager).authenticate(any());
        verify(jwtTokenProvider, never()).generateToken(any());
    }
}
