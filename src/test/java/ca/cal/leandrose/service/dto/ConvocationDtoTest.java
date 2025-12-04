package ca.cal.leandrose.service.dto;

import static org.junit.jupiter.api.Assertions.*;

import ca.cal.leandrose.model.*;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ConvocationDtoTest {

  private Convocation convocation;
  private Candidature candidature;

  @BeforeEach
  void setUp() {
    Student student =
        Student.builder()
            .id(1L)
            .firstName("John")
            .lastName("Doe")
            .email("john.doe@student.com")
            .password("password")
            .studentNumber("STU001")
            .program("Computer Science")
            .build();

    Employeur employeur =
        Employeur.builder()
            .id(1L)
            .firstName("Jane")
            .lastName("Smith")
            .email("jane@company.com")
            .password("password")
            .companyName("TechCorp")
            .field("Software")
            .build();

    InternshipOffer offer =
        InternshipOffer.builder()
            .id(1L)
            .description("Stage développement")
            .employeur(employeur)
            .status(InternshipOffer.Status.PUBLISHED)
            .build();

    Cv cv =
        Cv.builder().id(1L).student(student).pdfPath("/cv.pdf").status(Cv.Status.APPROVED).build();

    candidature =
        Candidature.builder()
            .id(1L)
            .student(student)
            .internshipOffer(offer)
            .cv(cv)
            .status(Candidature.Status.PENDING)
            .applicationDate(LocalDateTime.now())
            .build();

    convocation =
        Convocation.builder()
            .id(1L)
            .candidature(candidature)
            .convocationDate(LocalDateTime.of(2025, 9, 15, 14, 30))
            .location("Bureau 201, 123 Rue Test")
            .personnalMessage("Veuillez vous présenter à l'heure indiquée")
            .build();
  }

  @Test
  void testConvocationDtoBuilder() {
    ConvocationDto dto =
        ConvocationDto.builder()
            .id(1L)
            .candidatureId(1L)
            .convocationDate(LocalDateTime.of(2025, 9, 15, 14, 30))
            .location("Bureau 201")
            .message("Test message")
            .build();

    assertEquals(1L, dto.getId());
    assertEquals(1L, dto.getCandidatureId());
    assertEquals(LocalDateTime.of(2025, 9, 15, 14, 30), dto.getConvocationDate());
    assertEquals("Bureau 201", dto.getLocation());
    assertEquals("Test message", dto.getMessage());
  }

  @Test
  void testConvocationDtoNoArgsConstructorAndSetters() {
    ConvocationDto dto = new ConvocationDto();
    dto.setId(2L);
    dto.setCandidatureId(2L);
    dto.setConvocationDate(LocalDateTime.of(2025, 10, 1, 10, 0));
    dto.setLocation("Bureau 202");
    dto.setMessage("New message");

    assertEquals(2L, dto.getId());
    assertEquals(2L, dto.getCandidatureId());
    assertEquals(LocalDateTime.of(2025, 10, 1, 10, 0), dto.getConvocationDate());
    assertEquals("Bureau 202", dto.getLocation());
    assertEquals("New message", dto.getMessage());
  }

  @Test
  void testCreate_WithCompleteConvocation() {
    ConvocationDto dto = ConvocationDto.create(convocation);

    assertNotNull(dto);
    assertEquals(1L, dto.getId());
    assertEquals(1L, dto.getCandidatureId());
    assertEquals(LocalDateTime.of(2025, 9, 15, 14, 30), dto.getConvocationDate());
    assertEquals("Bureau 201, 123 Rue Test", dto.getLocation());
    assertEquals("Veuillez vous présenter à l'heure indiquée", dto.getMessage());
  }

  @Test
  void testCreate_WithNullCandidature() {
    Convocation convocationWithoutCandidature =
        Convocation.builder()
            .id(1L)
            .candidature(null)
            .convocationDate(LocalDateTime.of(2025, 9, 15, 14, 30))
            .location("Bureau 201")
            .personnalMessage("Test message")
            .build();

    ConvocationDto dto = ConvocationDto.create(convocationWithoutCandidature);

    assertNotNull(dto);
    assertEquals(1L, dto.getId());
    assertNull(dto.getCandidatureId());
    assertEquals(LocalDateTime.of(2025, 9, 15, 14, 30), dto.getConvocationDate());
    assertEquals("Bureau 201", dto.getLocation());
    assertEquals("Test message", dto.getMessage());
  }

  @Test
  void testConvocationDtoAllArgsConstructor() {
    ConvocationDto dto =
        new ConvocationDto(
            1L, 1L, LocalDateTime.of(2025, 9, 15, 14, 30), "Bureau 201", "Test message", null);

    assertEquals(1L, dto.getId());
    assertEquals(1L, dto.getCandidatureId());
    assertEquals(LocalDateTime.of(2025, 9, 15, 14, 30), dto.getConvocationDate());
    assertEquals("Bureau 201", dto.getLocation());
    assertEquals("Test message", dto.getMessage());
    assertNull(dto.getError());
  }
}
