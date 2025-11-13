package ca.cal.leandrose.service;

import ca.cal.leandrose.model.Prof;
import ca.cal.leandrose.repository.ProfRepository;
import ca.cal.leandrose.security.exception.UserNotFoundException;
import ca.cal.leandrose.service.dto.ProfDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProfServiceTest {

  @Mock private ProfRepository profRepository;

  @Mock private PasswordEncoder passwordEncoder;

  @InjectMocks private ProfService profService;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  void testCreateProf() {
    String rawPassword = "password123";
    String encodedPassword = "encodedPass";

    when(passwordEncoder.encode(rawPassword)).thenReturn(encodedPassword);
    when(profRepository.existsByEmail(anyString())).thenReturn(false);
    when(profRepository.existsByEmployeeNumber(anyString())).thenReturn(false);

    Prof savedProf =
        Prof.builder()
            .id(1L)
           .firstName("Jean")
            .lastName("Dupont")
            .email("jean.dupont@college.ca")
            .password(encodedPassword)
            .employeeNumber("P12345")
            .nameCollege("Collège Ahuntsic")
            .address("9155 Rue St-Hubert, Montréal, QC H2M 1Y8")
            .fax_machine("(514) 364-7130")
            .department("Informatique")
            .build();

    when(profRepository.save(any(Prof.class))).thenReturn(savedProf);

    ProfDto dto =
        profService.createProf(
            "Jean",
            "Dupont",
            "jean.dupont@college.ca",
            rawPassword,
            "P12345",
            "Collège Ahuntsic",
             "9155 Rue St-Hubert, Montréal, QC H2M 1Y8",
            "(514) 364-7130",
            "Informatique");

    assertNotNull(dto);
    assertEquals(1L, dto.getId());
    assertEquals("Jean", dto.getFirstName());
    assertEquals("Dupont", dto.getLastName());
    assertEquals("jean.dupont@college.ca", dto.getEmail());
    assertEquals("P12345", dto.getEmployeeNumber());
    assertEquals("Collège Ahuntsic", dto.getNameCollege());
    assertEquals("9155 Rue St-Hubert, Montréal, QC H2M 1Y8", dto.getAddress());
    assertEquals("(514) 364-7130", dto.getFax_machine());
    assertEquals("Informatique", dto.getDepartment());

    verify(profRepository, times(1)).save(any(Prof.class));
  }

  @Test
  void testCreateProf_EmailAlreadyExists() {
    when(profRepository.existsByEmail("jean.dupont@college.ca")).thenReturn(true);

    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () ->
                    profService.createProf(
                            "Jean",
                            "Dupont",
                            "jean.dupont@college.ca",
                            "password123",
                            "P12345",
                            "Collège Ahuntsic",
                            "9155 Rue St-Hubert, Montréal, QC H2M 1Y8",
                            "(514) 364-7130",
                            "Informatique"));

    assertEquals("Cet email est déjà utilisé", exception.getMessage());
    verify(profRepository, never()).save(any(Prof.class));
  }

  @Test
  void testCreateProf_EmployeeNumberAlreadyExists() {
    when(profRepository.existsByEmail(anyString())).thenReturn(false);
    when(profRepository.existsByEmployeeNumber("P12345")).thenReturn(true);

    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () ->
                    profService.createProf(
                            "Jean",
                            "Dupont",
                            "jean.dupont@college.ca",
                            "password123",
                            "P12345",
                            "Collège Ahuntsic",
                            "9155 Rue St-Hubert, Montréal, QC H2M 1Y8",
                            "(514) 364-7130",
                            "Informatique"));

    assertEquals("Ce numéro d'employé est déjà utilisé", exception.getMessage());
    verify(profRepository, never()).save(any(Prof.class));
  }

  @Test
  void testCreateProf_InvalidParameters() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            profService.createProf(
                null, "Dupont", "jean.dupont@college.ca", "password123",
                    "P12345", "Collège Ahuntsic",
                    " 9155 Rue St-Hubert, Montréal, QC H2M 1Y8", "(514) 364-7130",  "Informatique"));

    assertThrows(
        IllegalArgumentException.class,
        () ->
                profService.createProf(
                        "Jean", null, "jean.dupont@college.ca", "password123",
                        "P12345", "Collège Ahuntsic",
                        " 9155 Rue St-Hubert, Montréal, QC H2M 1Y8", "(514) 364-7130",  "Informatique"));
    assertThrows(
        IllegalArgumentException.class,
        () ->
                profService.createProf(
                        "Jean", "Dupont", null, "password123",
                        "P12345", "Collège Ahuntsic",
                        " 9155 Rue St-Hubert, Montréal, QC H2M 1Y8", "(514) 364-7130", "Informatique"));
    assertThrows(
        IllegalArgumentException.class,
        () ->
                profService.createProf(
                        "Jean", "Dupont", "jean.dupont@college.ca", null,
                        "P12345", "Collège Ahuntsic",
                        " 9155 Rue St-Hubert, Montréal, QC H2M 1Y8", "(514) 364-7130", "Informatique"));
    assertThrows(
        IllegalArgumentException.class,
        () ->
                profService.createProf(
                        "Jean", "Dupont", "jean.dupont@college.ca", "password123",
                        null, "Collège Ahuntsic",
                        " 9155 Rue St-Hubert, Montréal, QC H2M 1Y8",  "(514) 364-7130", "Informatique"));
    verify(profRepository, never()).save(any(Prof.class));
  }

  @Test
  void testGetProfById() {
    Prof prof =
        Prof.builder()
                .id(1L)
                .firstName("Jean")
                .lastName("Dupont")
                .email("jean.dupont@college.ca")
                .password("encodedPass")
                .employeeNumber("P12345")
                .nameCollege("Collège Ahuntsic")
                .address("9155 Rue St-Hubert, Montréal, QC H2M 1Y8")
                .fax_machine("(514) 364-7130")
                .department("Informatique")
                .build();

    when(profRepository.findById(1L)).thenReturn(Optional.of(prof));

    ProfDto dto = profService.getProfById(1L);

    assertNotNull(dto);
    assertEquals(1L, dto.getId());
    assertEquals("Jean", dto.getFirstName());
    assertEquals("Dupont", dto.getLastName());
  }

  @Test
  void testGetProfById_NotFound() {
    when(profRepository.findById(1L)).thenReturn(Optional.empty());

    UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> profService.getProfById(1L));
    assertNotNull(exception);
  }

  @Test
  void testGetProfById_InvalidId() {
    assertThrows(IllegalArgumentException.class, () -> profService.getProfById(null));
    assertThrows(IllegalArgumentException.class, () -> profService.getProfById(0L));
    assertThrows(IllegalArgumentException.class, () -> profService.getProfById(-1L));
  }

  @Test
  void testGetAllProfs() {
    Prof prof1 =
        Prof.builder()
                .id(1L)
                .firstName("Jean")
                .lastName("Dupont")
                .email("jean.dupont@college.ca")
                .password("encodedPass")
                .employeeNumber("P12345")
                .nameCollege("Collège Ahuntsic")
                .address("9155 Rue St-Hubert, Montréal, QC H2M 1Y8")
                .fax_machine("(514) 364-7130")
                .department("Informatique")
                .build();

    Prof prof2 =
        Prof.builder()
            .id(2L)
            .firstName("Marie")
            .lastName("Martin")
            .email("marie.martin@college.ca")
            .password("encodedPass")
            .employeeNumber("P54321")
            .nameCollege("College de Mainsonneuve")
            .address("3800 R. Sherbrooke E, Montréal, QC H1X 2A2")
            .fax_machine("(514) 364-7130")
            .department("Mathématiques")
            .build();

    when(profRepository.findAll()).thenReturn(Arrays.asList(prof1, prof2));

    List<ProfDto> profs = profService.getAllProfs();

    assertNotNull(profs);
    assertEquals(2, profs.size());
    assertEquals("Jean", profs.get(0).getFirstName());
    assertEquals("Marie", profs.get(1).getFirstName());
  }
}

