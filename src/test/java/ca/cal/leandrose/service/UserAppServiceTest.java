package ca.cal.leandrose.service;

import ca.cal.leandrose.model.*;
import ca.cal.leandrose.model.auth.Credentials;
import ca.cal.leandrose.model.auth.Role;
import ca.cal.leandrose.presentation.request.UpdateUserRequest;
import ca.cal.leandrose.repository.*;
import ca.cal.leandrose.security.JwtTokenProvider;
import ca.cal.leandrose.security.exception.UserNotFoundException;
import ca.cal.leandrose.service.dto.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserAppServiceTest {

    @Mock private JwtTokenProvider jwtTokenProvider;

    @Mock private UserAppRepository userAppRepository;

    @Mock private StudentRepository studentRepository;

    @Mock private EmployeurRepository employeurRepository;

    @Mock private GestionnaireRepository gestionnaireRepository;

    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks private UserAppService userAppService;

    private final String token = "Bearer faketoken";
    private final String email = "test@example.com";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(jwtTokenProvider.getEmailFromJWT("faketoken")).thenReturn(email);
    }

    @Test
    void shouldReturnEmployeurDtoWhenUserIsEmployeur() {
        Employeur employeur =
                Employeur.builder()
                        .id(1L)
                        .firstName("John")
                        .lastName("Doe")
                        .email(email)
                        .password("pass")
                        .companyName("TechCorp")
                        .field("IT")
                        .build();

        when(userAppRepository.findUserAppByEmail(email)).thenReturn(Optional.of(employeur));
        when(employeurRepository.findById(1L)).thenReturn(Optional.of(employeur));

        UserDTO result = userAppService.getMe(token);

        assertThat(result).isInstanceOf(EmployeurDto.class);
        assertThat(((EmployeurDto) result).getCompanyName()).isEqualTo("TechCorp");
    }

    @Test
    void shouldReturnStudentDtoWhenUserIsStudent() {
        Student student =
                Student.builder()
                        .id(2L)
                        .firstName("Alice")
                        .lastName("Smith")
                        .email(email)
                        .password("pass")
                        .studentNumber("STU123")
                        .program("CS")
                        .build();

        when(userAppRepository.findUserAppByEmail(email)).thenReturn(Optional.of(student));
        when(studentRepository.findById(2L)).thenReturn(Optional.of(student));

        UserDTO result = userAppService.getMe(token);

        assertThat(result).isInstanceOf(StudentDto.class);
        assertThat(((StudentDto) result).getProgram()).isEqualTo("CS");
    }

    @Test
    void shouldReturnGestionnaireDtoWhenUserIsGestionnaire() {
        Gestionnaire gestionnaire =
                Gestionnaire.builder()
                        .id(3L)
                        .firstName("Admin")
                        .lastName("Boss")
                        .email(email)
                        .password("pass")
                        .phoneNumber("555-555")
                        .build();

        when(userAppRepository.findUserAppByEmail(email)).thenReturn(Optional.of(gestionnaire));
        when(gestionnaireRepository.findById(3L)).thenReturn(Optional.of(gestionnaire));

        UserDTO result = userAppService.getMe(token);

        assertThat(result).isInstanceOf(GestionnaireDto.class);
        assertThat((result).getFirstName()).isEqualTo("Admin");
    }

    @Test
    void shouldThrowExceptionWhenUserNotFound() {
        when(userAppRepository.findUserAppByEmail(email)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userAppService.getMe(token))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void shouldReturnEmptyEmployeurDtoIfNotFoundInRepo() {
        Employeur employeur =
                Employeur.builder()
                        .id(1L)
                        .firstName("John")
                        .lastName("Doe")
                        .email(email)
                        .password("pass")
                        .companyName("TechCorp")
                        .field("IT")
                        .build();

        when(userAppRepository.findUserAppByEmail(email)).thenReturn(Optional.of(employeur));
        when(employeurRepository.findById(1L)).thenReturn(Optional.empty());

        UserDTO result = userAppService.getMe(token);

        assertThat(result).isInstanceOf(EmployeurDto.class);
        assertThat(((EmployeurDto) result).getCompanyName()).isNull();
    }

    @Test
    void verifyPassword_shouldReturnTrueWhenPasswordMatches() {

        Credentials creds = Credentials.builder()
                .email(email)
                .password("encodedPass")
                .role(Role.STUDENT)
                .build();

        Student user = new Student();
        user.setId(10L);
        user.setCredentials(creds);

        when(userAppRepository.findUserAppByEmail(email)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("mypassword", "encodedPass")).thenReturn(true);

        boolean result = userAppService.verifyPassword(token, "mypassword");

        assertThat(result).isTrue();
    }

    @Test
    void verifyPassword_shouldReturnFalseWhenPasswordDoesNotMatch() {

        Credentials creds = Credentials.builder()
                .email(email)
                .password("encodedPass")
                .role(Role.STUDENT)
                .build();

        Student user = new Student();
        user.setId(10L);
        user.setCredentials(creds);

        when(userAppRepository.findUserAppByEmail(email)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongpass", "encodedPass")).thenReturn(false);

        boolean result = userAppService.verifyPassword(token, "wrongpass");

        assertThat(result).isFalse();
    }

    @Test
    void updateProfile_shouldUpdateFirstAndLastName() {

        Credentials creds = Credentials.builder()
                .email(email)
                .password("encodedPass")
                .role(Role.STUDENT)
                .build();

        Student user = new Student();
        user.setId(5L);
        user.setCredentials(creds);

        when(userAppRepository.findUserAppByEmail(email)).thenReturn(Optional.of(user));
        when(userAppRepository.save(user)).thenReturn(user);

        UpdateUserRequest req = new UpdateUserRequest();
        req.setFirstName("NewFirst");
        req.setLastName("NewLast");

        userAppService.updateProfile(token, req);

        assertThat(user.getFirstName()).isEqualTo("NewFirst");
        assertThat(user.getLastName()).isEqualTo("NewLast");
    }

    @Test
    void updateProfile_shouldEncodePasswordWhenProvided() {

        Credentials creds = Credentials.builder()
                .email(email)
                .password("oldPass")
                .role(Role.STUDENT)
                .build();

        Student user = new Student();
        user.setId(100L);
        user.setCredentials(creds);

        when(userAppRepository.findUserAppByEmail(email)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("newpass")).thenReturn("encodedNewPass");

        UpdateUserRequest req = new UpdateUserRequest();
        req.setNewPassword("newpass");

        userAppService.updateProfile(token, req);

        assertThat(creds.getPassword()).isEqualTo("encodedNewPass");
    }

    @Test
    void updateProfile_shouldUpdatePhoneNumberForGestionnaire() {

        Gestionnaire g =
                Gestionnaire.builder()
                        .id(9L)
                        .firstName("Jean")
                        .lastName("Dupont")
                        .email(email)
                        .password("pass")
                        .phoneNumber("111")
                        .build();

        when(userAppRepository.findUserAppByEmail(email)).thenReturn(Optional.of(g));
        when(gestionnaireRepository.findById(9L)).thenReturn(Optional.of(g));

        UpdateUserRequest req = new UpdateUserRequest();
        req.setPhoneNumber("999-999-9999");

        userAppService.updateProfile(token, req);

        assertThat(g.getPhoneNumber()).isEqualTo("999-999-9999");
    }
}
