package ca.cal.leandrose.service.dto;

import static org.junit.jupiter.api.Assertions.*;

import ca.cal.leandrose.model.*;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CandidatureEmployeurDtoTest {

  private Candidature candidature;
  private Student student;
  private InternshipOffer offer;
  private Cv cv;

  @BeforeEach
  void setUp() {
    student =
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

    offer =
        InternshipOffer.builder()
            .id(1L)
            .description("Stage développement")
            .employeur(employeur)
            .status(InternshipOffer.Status.PUBLISHED)
            .build();

    cv = Cv.builder().id(1L).student(student).pdfPath("/cv.pdf").status(Cv.Status.APPROVED).build();

    candidature =
        Candidature.builder()
            .id(1L)
            .student(student)
            .internshipOffer(offer)
            .cv(cv)
            .status(Candidature.Status.ACCEPTED)
            .applicationDate(LocalDateTime.now())
            .build();
  }

  @Test
  void testCandidatureEmployeurDtoBuilder() {
    CandidatureEmployeurDto dto =
        CandidatureEmployeurDto.builder()
            .id(1L)
            .studentId(1L)
            .studentFirstName("John")
            .studentLastName("Doe")
            .studentProgram("Computer Science")
            .applicationDate(LocalDateTime.now())
            .status(Candidature.Status.ACCEPTED)
            .cvId(1L)
            .cvStatus("APPROVED")
            .offerId(1L)
            .offerDescription("Stage développement")
            .build();

    assertEquals(1L, dto.getId());
    assertEquals(1L, dto.getStudentId());
    assertEquals("John", dto.getStudentFirstName());
    assertEquals("Doe", dto.getStudentLastName());
    assertEquals("Computer Science", dto.getStudentProgram());
    assertEquals(Candidature.Status.ACCEPTED, dto.getStatus());
    assertEquals(1L, dto.getCvId());
    assertEquals("APPROVED", dto.getCvStatus());
    assertEquals(1L, dto.getOfferId());
    assertEquals("Stage développement", dto.getOfferDescription());
  }

  @Test
  void testCandidatureEmployeurDtoNoArgsConstructorAndSetters() {
    CandidatureEmployeurDto dto = new CandidatureEmployeurDto();
    dto.setId(2L);
    dto.setStudentId(2L);
    dto.setStudentFirstName("Jane");
    dto.setStudentLastName("Smith");
    dto.setStudentProgram("Mathematics");
    dto.setStatus(Candidature.Status.PENDING);
    dto.setCvId(2L);
    dto.setCvStatus("PENDING");
    dto.setOfferId(2L);
    dto.setOfferDescription("Stage test");

    assertEquals(2L, dto.getId());
    assertEquals(2L, dto.getStudentId());
    assertEquals("Jane", dto.getStudentFirstName());
    assertEquals("Smith", dto.getStudentLastName());
    assertEquals("Mathematics", dto.getStudentProgram());
    assertEquals(Candidature.Status.PENDING, dto.getStatus());
    assertEquals(2L, dto.getCvId());
    assertEquals("PENDING", dto.getCvStatus());
    assertEquals(2L, dto.getOfferId());
    assertEquals("Stage test", dto.getOfferDescription());
  }

  @Test
  void testFromEntity_WithCompleteCandidature() {
    CandidatureEmployeurDto dto = CandidatureEmployeurDto.fromEntity(candidature);

    assertNotNull(dto);
    assertEquals(1L, dto.getId());
    assertEquals(1L, dto.getStudentId());
    assertEquals("John", dto.getStudentFirstName());
    assertEquals("Doe", dto.getStudentLastName());
    assertEquals("Computer Science", dto.getStudentProgram());
    assertEquals(Candidature.Status.ACCEPTED, dto.getStatus());
    assertEquals(1L, dto.getCvId());
    assertEquals("APPROVED", dto.getCvStatus());
    assertEquals(1L, dto.getOfferId());
    assertEquals("Stage développement", dto.getOfferDescription());
  }

  @Test
  void testCandidatureEmployeurDtoAllArgsConstructor() {
    CandidatureEmployeurDto dto =
        new CandidatureEmployeurDto(
            1L,
            1L,
            "John",
            "Doe",
            "Computer Science",
            LocalDateTime.now(),
            Candidature.Status.PENDING,
            1L,
            "APPROVED",
            1L,
            "Stage test");

    assertEquals(1L, dto.getId());
    assertEquals(1L, dto.getStudentId());
    assertEquals("John", dto.getStudentFirstName());
    assertEquals("Doe", dto.getStudentLastName());
    assertEquals("Computer Science", dto.getStudentProgram());
    assertEquals(Candidature.Status.PENDING, dto.getStatus());
    assertEquals(1L, dto.getCvId());
    assertEquals("APPROVED", dto.getCvStatus());
    assertEquals(1L, dto.getOfferId());
    assertEquals("Stage test", dto.getOfferDescription());
  }
}



