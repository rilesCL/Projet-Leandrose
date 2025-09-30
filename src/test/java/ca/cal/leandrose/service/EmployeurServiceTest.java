package ca.cal.leandrose.service;

import ca.cal.leandrose.model.Employeur;
import ca.cal.leandrose.repository.EmployeurRepository;
import ca.cal.leandrose.security.exception.UserNotFoundException;
import ca.cal.leandrose.service.dto.EmployeurDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EmployeurServiceTest {

    @Mock
    private EmployeurRepository employeurRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private EmployeurService employeurService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateEmployeur() {
        String rawPassword = "password123";
        String encodedPassword = "encodedPass";

        when(passwordEncoder.encode(rawPassword)).thenReturn(encodedPassword);

        Employeur savedEmployeur = Employeur.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .password(encodedPassword)
                .companyName("TechCorp")
                .field("IT")
                .build();

        when(employeurRepository.save(any(Employeur.class))).thenReturn(savedEmployeur);

        EmployeurDto dto = employeurService.createEmployeur(
                "John", "Doe", "john.doe@example.com", rawPassword, "TechCorp", "IT"
        );

        assertNotNull(dto);
        assertEquals(1L, dto.getId());
        assertEquals("John", dto.getFirstName());
        assertEquals("Doe", dto.getLastName());
        assertEquals("john.doe@example.com", dto.getEmail());
        assertEquals("TechCorp", dto.getCompanyName());
        assertEquals("IT", dto.getField());

        verify(passwordEncoder).encode(rawPassword);

        ArgumentCaptor<Employeur> captor = ArgumentCaptor.forClass(Employeur.class);
        verify(employeurRepository).save(captor.capture());
        assertEquals(encodedPassword, captor.getValue().getPassword());
    }

    @Test
    void testGetEmployeurByIdFound() {
        Employeur employeur = Employeur.builder()
                .id(2L)
                .firstName("Alice")
                .lastName("Smith")
                .email("alice.smith@example.com")
                .password("pass")
                .companyName("InnoTech")
                .field("Software")
                .build();

        when(employeurRepository.findById(2L)).thenReturn(Optional.of(employeur));

        EmployeurDto dto = employeurService.getEmployeurById(2L);

        assertNotNull(dto);
        assertEquals(2L, dto.getId());
        assertEquals("Alice", dto.getFirstName());
    }

    @Test
    void testGetEmployeurByIdNotFound() {
        when(employeurRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> employeurService.getEmployeurById(99L));
    }
}
