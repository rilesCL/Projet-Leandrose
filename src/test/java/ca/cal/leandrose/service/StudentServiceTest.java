package ca.cal.leandrose.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import ca.cal.leandrose.model.*;
import ca.cal.leandrose.repository.EntenteStageRepository;
import ca.cal.leandrose.repository.StudentRepository;
import ca.cal.leandrose.security.exception.UserNotFoundException;
import ca.cal.leandrose.service.dto.EmployeurDto;
import ca.cal.leandrose.service.dto.GestionnaireDto;
import ca.cal.leandrose.service.dto.ProfDto;
import ca.cal.leandrose.service.dto.StudentDto;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

class StudentServiceTest {

  @Mock private StudentRepository studentRepository;

  @Mock private PasswordEncoder passwordEncoder;

  @Mock private EntenteStageRepository ententeStageRepository;

  @InjectMocks private StudentService studentService;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  void testCreateStudent() {
    String rawPassword = "password123";
    String encodedPassword = "encodedPass";

    when(passwordEncoder.encode(rawPassword)).thenReturn(encodedPassword);

    Student savedStudent =
        Student.builder()
            .id(1L)
            .firstName("John")
            .lastName("Doe")
            .email("john.doe@student.com")
            .password(encodedPassword)
            .studentNumber("STU001")
            .program("Computer Science")
            .build();

    when(studentRepository.save(any(Student.class))).thenReturn(savedStudent);

    StudentDto dto =
        studentService.createStudent(
            "John", "Doe", "john.doe@student.com", rawPassword, "STU001", "Computer Science");

    assertNotNull(dto);
    assertEquals(1L, dto.getId());
    assertEquals("John", dto.getFirstName());
    assertEquals("Doe", dto.getLastName());
    assertEquals("john.doe@student.com", dto.getEmail());
    assertEquals("STU001", dto.getStudentNumber());
    assertEquals("Computer Science", dto.getProgram());

    verify(passwordEncoder).encode(rawPassword);

    ArgumentCaptor<Student> captor = ArgumentCaptor.forClass(Student.class);
    verify(studentRepository).save(captor.capture());
    assertEquals(encodedPassword, captor.getValue().getPassword());
  }

  @Test
  void testGetStudentByIdFound() {
    Student student =
        Student.builder()
            .id(2L)
            .firstName("Alice")
            .lastName("Smith")
            .email("alice.smith@student.com")
            .password("pass")
            .studentNumber("STU002")
            .program("Mathematics")
            .build();

    when(studentRepository.findById(2L)).thenReturn(Optional.of(student));

    StudentDto dto = studentService.getStudentById(2L);

    assertNotNull(dto);
    assertEquals(2L, dto.getId());
    assertEquals("Alice", dto.getFirstName());
  }

  @Test
  void testGetStudentByIdNotFound() {
    when(studentRepository.findById(99L)).thenReturn(Optional.empty());

    assertThrows(UserNotFoundException.class, () -> studentService.getStudentById(99L));
  }

  @Test
  void testGetProfByStudentId_WithProf_ReturnsProfDto() {
    // Arrange
    Long studentId = 1L;
    Student student = Student.builder().id(studentId).firstName("John").lastName("Doe").build();
    Prof prof =
        Prof.builder()
            .id(10L)
            .firstName("Prof")
            .lastName("Smith")
            .email("prof@college.com")
            .employeeNumber("EMP001")
            .nameCollege("College Test")
            .department("Informatique")
            .build();
    Gestionnaire gestionnaire =
        Gestionnaire.builder()
            .id(5L)
            .firstName("Gest")
            .lastName("Manager")
            .email("gest@college.com")
            .phoneNumber("514-123-4567")
            .build();
    Employeur employeur =
        Employeur.builder()
            .id(20L)
            .firstName("Employeur")
            .lastName("Test")
            .email("emp@company.com")
            .companyName("TechCorp")
            .field("IT")
            .build();

    Cv cv =
        Cv.builder().id(1L).student(student).pdfPath("/cv.pdf").status(Cv.Status.APPROVED).build();
    InternshipOffer offer =
        InternshipOffer.builder()
            .id(1L)
            .description("Stage")
            .employeur(employeur)
            .status(InternshipOffer.Status.PUBLISHED)
            .build();
    Candidature candidature =
        Candidature.builder()
            .id(1L)
            .student(student)
            .internshipOffer(offer)
            .cv(cv)
            .status(Candidature.Status.ACCEPTED)
            .applicationDate(LocalDateTime.now())
            .build();

    EntenteStage entente =
        EntenteStage.builder()
            .id(1L)
            .candidature(candidature)
            .prof(prof)
            .gestionnaire(gestionnaire)
            .statut(EntenteStage.StatutEntente.VALIDEE)
            .dateCreation(LocalDateTime.now())
            .dateModification(LocalDateTime.now().minusDays(1))
            .build();

    when(ententeStageRepository.findAll()).thenReturn(List.of(entente));

    // Act
    Optional<ProfDto> result = studentService.getProfByStudentId(studentId);

    // Assert
    assertTrue(result.isPresent());
    ProfDto profDto = result.get();
    assertEquals(10L, profDto.getId());
    assertEquals("Prof", profDto.getFirstName());
    assertEquals("Smith", profDto.getLastName());
    assertEquals("prof@college.com", profDto.getEmail());
    assertEquals("EMP001", profDto.getEmployeeNumber());
    verify(ententeStageRepository).findAll();
  }

  @Test
  void testGetProfByStudentId_NoProf_ReturnsEmpty() {
    // Arrange
    Long studentId = 1L;
    Student student = Student.builder().id(studentId).build();
    Cv cv =
        Cv.builder().id(1L).student(student).pdfPath("/cv.pdf").status(Cv.Status.APPROVED).build();
    InternshipOffer offer =
        InternshipOffer.builder().id(1L).status(InternshipOffer.Status.PUBLISHED).build();
    Candidature candidature =
        Candidature.builder()
            .id(1L)
            .student(student)
            .internshipOffer(offer)
            .cv(cv)
            .status(Candidature.Status.ACCEPTED)
            .applicationDate(LocalDateTime.now())
            .build();

    EntenteStage entente =
        EntenteStage.builder()
            .id(1L)
            .candidature(candidature)
            .prof(null)
            .statut(EntenteStage.StatutEntente.EN_ATTENTE_SIGNATURE)
            .dateCreation(LocalDateTime.now())
            .build();

    when(ententeStageRepository.findAll()).thenReturn(List.of(entente));

    // Act
    Optional<ProfDto> result = studentService.getProfByStudentId(studentId);

    // Assert
    assertFalse(result.isPresent());
  }

  @Test
  void testGetProfByStudentId_NoEntentes_ReturnsEmpty() {
    when(ententeStageRepository.findAll()).thenReturn(List.of());

    Optional<ProfDto> result = studentService.getProfByStudentId(1L);

    assertFalse(result.isPresent());
  }

  @Test
  void testGetProfByStudentId_InvalidStudentId_ReturnsEmpty() {
    Optional<ProfDto> result = studentService.getProfByStudentId(null);
    assertFalse(result.isPresent());

    result = studentService.getProfByStudentId(0L);
    assertFalse(result.isPresent());

    result = studentService.getProfByStudentId(-1L);
    assertFalse(result.isPresent());
  }

  @Test
  void testGetProfByStudentId_MultipleEntentes_ReturnsMostRecent() {
    // Arrange
    Long studentId = 1L;
    Student student = Student.builder().id(studentId).build();
    Prof prof1 =
        Prof.builder()
            .id(10L)
            .firstName("Prof1")
            .lastName("One")
            .email("prof1@college.com")
            .employeeNumber("EMP001")
            .build();
    Prof prof2 =
        Prof.builder()
            .id(11L)
            .firstName("Prof2")
            .lastName("Two")
            .email("prof2@college.com")
            .employeeNumber("EMP002")
            .build();

    Cv cv =
        Cv.builder().id(1L).student(student).pdfPath("/cv.pdf").status(Cv.Status.APPROVED).build();
    InternshipOffer offer =
        InternshipOffer.builder().id(1L).status(InternshipOffer.Status.PUBLISHED).build();
    Candidature candidature =
        Candidature.builder()
            .id(1L)
            .student(student)
            .internshipOffer(offer)
            .cv(cv)
            .status(Candidature.Status.ACCEPTED)
            .applicationDate(LocalDateTime.now())
            .build();

    EntenteStage entente1 =
        EntenteStage.builder()
            .id(1L)
            .candidature(candidature)
            .prof(prof1)
            .statut(EntenteStage.StatutEntente.VALIDEE)
            .dateCreation(LocalDateTime.now().minusDays(10))
            .dateModification(LocalDateTime.now().minusDays(5))
            .build();

    EntenteStage entente2 =
        EntenteStage.builder()
            .id(2L)
            .candidature(candidature)
            .prof(prof2)
            .statut(EntenteStage.StatutEntente.VALIDEE)
            .dateCreation(LocalDateTime.now().minusDays(5))
            .dateModification(LocalDateTime.now())
            .build();

    when(ententeStageRepository.findAll()).thenReturn(List.of(entente1, entente2));

    // Act
    Optional<ProfDto> result = studentService.getProfByStudentId(studentId);

    // Assert - Should return prof2 as it's more recent
    assertTrue(result.isPresent());
    assertEquals(11L, result.get().getId());
    assertEquals("Prof2", result.get().getFirstName());
  }

  @Test
  void testGetGestionnaireByStudentId_WithGestionnaire_ReturnsGestionnaireDto() {
    // Arrange
    Long studentId = 1L;
    Student student = Student.builder().id(studentId).build();
    Gestionnaire gestionnaire =
        Gestionnaire.builder()
            .id(5L)
            .firstName("Gest")
            .lastName("Manager")
            .email("gest@college.com")
            .phoneNumber("514-123-4567")
            .build();
    Employeur employeur =
        Employeur.builder()
            .id(20L)
            .firstName("Employeur")
            .lastName("Test")
            .email("emp@company.com")
            .companyName("TechCorp")
            .build();

    Cv cv =
        Cv.builder().id(1L).student(student).pdfPath("/cv.pdf").status(Cv.Status.APPROVED).build();
    InternshipOffer offer =
        InternshipOffer.builder()
            .id(1L)
            .description("Stage")
            .employeur(employeur)
            .status(InternshipOffer.Status.PUBLISHED)
            .build();
    Candidature candidature =
        Candidature.builder()
            .id(1L)
            .student(student)
            .internshipOffer(offer)
            .cv(cv)
            .status(Candidature.Status.ACCEPTED)
            .applicationDate(LocalDateTime.now())
            .build();

    EntenteStage entente =
        EntenteStage.builder()
            .id(1L)
            .candidature(candidature)
            .gestionnaire(gestionnaire)
            .statut(EntenteStage.StatutEntente.VALIDEE)
            .dateCreation(LocalDateTime.now())
            .dateModification(LocalDateTime.now())
            .build();

    when(ententeStageRepository.findAll()).thenReturn(List.of(entente));

    // Act
    Optional<GestionnaireDto> result = studentService.getGestionnaireByStudentId(studentId);

    // Assert
    assertTrue(result.isPresent());
    GestionnaireDto gestionnaireDto = result.get();
    assertEquals(5L, gestionnaireDto.getId());
    assertEquals("Gest", gestionnaireDto.getFirstName());
    assertEquals("Manager", gestionnaireDto.getLastName());
    assertEquals("514-123-4567", gestionnaireDto.getPhoneNumber());
  }

  @Test
  void testGetGestionnaireByStudentId_NoGestionnaire_ReturnsEmpty() {
    Long studentId = 1L;
    Student student = Student.builder().id(studentId).build();
    Cv cv =
        Cv.builder().id(1L).student(student).pdfPath("/cv.pdf").status(Cv.Status.APPROVED).build();
    InternshipOffer offer =
        InternshipOffer.builder().id(1L).status(InternshipOffer.Status.PUBLISHED).build();
    Candidature candidature =
        Candidature.builder()
            .id(1L)
            .student(student)
            .internshipOffer(offer)
            .cv(cv)
            .status(Candidature.Status.ACCEPTED)
            .applicationDate(LocalDateTime.now())
            .build();

    EntenteStage entente =
        EntenteStage.builder()
            .id(1L)
            .candidature(candidature)
            .gestionnaire(null)
            .statut(EntenteStage.StatutEntente.EN_ATTENTE_SIGNATURE)
            .dateCreation(LocalDateTime.now())
            .build();

    when(ententeStageRepository.findAll()).thenReturn(List.of(entente));

    Optional<GestionnaireDto> result = studentService.getGestionnaireByStudentId(studentId);

    assertFalse(result.isPresent());
  }

  @Test
  void testGetGestionnaireByStudentId_InvalidStudentId_ReturnsEmpty() {
    Optional<GestionnaireDto> result = studentService.getGestionnaireByStudentId(null);
    assertFalse(result.isPresent());

    result = studentService.getGestionnaireByStudentId(0L);
    assertFalse(result.isPresent());
  }

  @Test
  void testGetEmployeursByStudentId_WithEmployeurs_ReturnsList() {
    // Arrange
    Long studentId = 1L;
    Student student = Student.builder().id(studentId).build();
    Employeur employeur1 =
        Employeur.builder()
            .id(20L)
            .firstName("Employeur1")
            .lastName("One")
            .email("emp1@company.com")
            .companyName("TechCorp")
            .field("IT")
            .build();
    Employeur employeur2 =
        Employeur.builder()
            .id(21L)
            .firstName("Employeur2")
            .lastName("Two")
            .email("emp2@company.com")
            .companyName("DevCorp")
            .field("Software")
            .build();

    Cv cv =
        Cv.builder().id(1L).student(student).pdfPath("/cv.pdf").status(Cv.Status.APPROVED).build();
    InternshipOffer offer1 =
        InternshipOffer.builder()
            .id(1L)
            .description("Stage 1")
            .employeur(employeur1)
            .status(InternshipOffer.Status.PUBLISHED)
            .build();
    InternshipOffer offer2 =
        InternshipOffer.builder()
            .id(2L)
            .description("Stage 2")
            .employeur(employeur2)
            .status(InternshipOffer.Status.PUBLISHED)
            .build();

    Candidature candidature1 =
        Candidature.builder()
            .id(1L)
            .student(student)
            .internshipOffer(offer1)
            .cv(cv)
            .status(Candidature.Status.ACCEPTED)
            .applicationDate(LocalDateTime.now())
            .build();
    Candidature candidature2 =
        Candidature.builder()
            .id(2L)
            .student(student)
            .internshipOffer(offer2)
            .cv(cv)
            .status(Candidature.Status.ACCEPTED)
            .applicationDate(LocalDateTime.now())
            .build();

    EntenteStage entente1 =
        EntenteStage.builder()
            .id(1L)
            .candidature(candidature1)
            .statut(EntenteStage.StatutEntente.VALIDEE)
            .dateCreation(LocalDateTime.now())
            .build();
    EntenteStage entente2 =
        EntenteStage.builder()
            .id(2L)
            .candidature(candidature2)
            .statut(EntenteStage.StatutEntente.VALIDEE)
            .dateCreation(LocalDateTime.now())
            .build();

    when(ententeStageRepository.findAll()).thenReturn(List.of(entente1, entente2));

    // Act
    List<EmployeurDto> result = studentService.getEmployeursByStudentId(studentId);

    // Assert
    assertEquals(2, result.size());
    assertTrue(result.stream().anyMatch(e -> e.getId().equals(20L)));
    assertTrue(result.stream().anyMatch(e -> e.getId().equals(21L)));
  }

  @Test
  void testGetEmployeursByStudentId_NoEmployeurs_ReturnsEmptyList() {
    when(ententeStageRepository.findAll()).thenReturn(List.of());

    List<EmployeurDto> result = studentService.getEmployeursByStudentId(1L);

    assertTrue(result.isEmpty());
  }

  @Test
  void testGetEmployeursByStudentId_DuplicateEmployeurs_ReturnsUniqueList() {
    // Arrange
    Long studentId = 1L;
    Student student = Student.builder().id(studentId).build();
    Employeur employeur =
        Employeur.builder()
            .id(20L)
            .firstName("Employeur")
            .lastName("Test")
            .email("emp@company.com")
            .companyName("TechCorp")
            .field("IT")
            .build();

    Cv cv =
        Cv.builder().id(1L).student(student).pdfPath("/cv.pdf").status(Cv.Status.APPROVED).build();
    InternshipOffer offer1 =
        InternshipOffer.builder()
            .id(1L)
            .description("Stage 1")
            .employeur(employeur)
            .status(InternshipOffer.Status.PUBLISHED)
            .build();
    InternshipOffer offer2 =
        InternshipOffer.builder()
            .id(2L)
            .description("Stage 2")
            .employeur(employeur)
            .status(InternshipOffer.Status.PUBLISHED)
            .build();

    Candidature candidature1 =
        Candidature.builder()
            .id(1L)
            .student(student)
            .internshipOffer(offer1)
            .cv(cv)
            .status(Candidature.Status.ACCEPTED)
            .applicationDate(LocalDateTime.now())
            .build();
    Candidature candidature2 =
        Candidature.builder()
            .id(2L)
            .student(student)
            .internshipOffer(offer2)
            .cv(cv)
            .status(Candidature.Status.ACCEPTED)
            .applicationDate(LocalDateTime.now())
            .build();

    EntenteStage entente1 =
        EntenteStage.builder()
            .id(1L)
            .candidature(candidature1)
            .statut(EntenteStage.StatutEntente.VALIDEE)
            .dateCreation(LocalDateTime.now())
            .build();
    EntenteStage entente2 =
        EntenteStage.builder()
            .id(2L)
            .candidature(candidature2)
            .statut(EntenteStage.StatutEntente.VALIDEE)
            .dateCreation(LocalDateTime.now())
            .build();

    when(ententeStageRepository.findAll()).thenReturn(List.of(entente1, entente2));

    // Act
    List<EmployeurDto> result = studentService.getEmployeursByStudentId(studentId);

    // Assert - Should return only one unique employeur
    assertEquals(1, result.size());
    assertEquals(20L, result.get(0).getId());
  }

  @Test
  void testGetEmployeursByStudentId_InvalidStudentId_ReturnsEmptyList() {
    List<EmployeurDto> result = studentService.getEmployeursByStudentId(null);
    assertTrue(result.isEmpty());

    result = studentService.getEmployeursByStudentId(0L);
    assertTrue(result.isEmpty());
  }
}
