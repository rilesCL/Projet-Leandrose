package ca.cal.leandrose.service.dto;

import static org.junit.jupiter.api.Assertions.*;

import ca.cal.leandrose.model.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CandidatureDtoTest {

  private Candidature candidature;
  private Student student;
  private InternshipOffer offer;
  private Cv cv;
  private SchoolTerm schoolTerm;

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

    schoolTerm = new SchoolTerm(SchoolTerm.Season.FALL, 2025);

    offer =
        InternshipOffer.builder()
            .id(1L)
            .description("Stage développement")
            .startDate(LocalDate.of(2025, 9, 1))
            .durationInWeeks(12)
            .address("123 Rue Test")
            .remuneration(500.0f)
            .employeur(employeur)
            .status(InternshipOffer.Status.PUBLISHED)
            .schoolTerm(schoolTerm)
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
  void testCandidatureDtoBuilder() {
    CandidatureDto dto =
        CandidatureDto.builder()
            .id(1L)
            .status(Candidature.Status.PENDING)
            .applicationDate(LocalDateTime.now())
            .build();

    assertEquals(1L, dto.getId());
    assertEquals(Candidature.Status.PENDING, dto.getStatus());
    assertNotNull(dto.getApplicationDate());
  }

  @Test
  void testCandidatureDtoNoArgsConstructorAndSetters() {
    CandidatureDto dto = new CandidatureDto();
    dto.setId(2L);
    dto.setStatus(Candidature.Status.REJECTED);
    dto.setApplicationDate(LocalDateTime.now());

    assertEquals(2L, dto.getId());
    assertEquals(Candidature.Status.REJECTED, dto.getStatus());
  }

  @Test
  void testFromEntity_WithCompleteCandidature() {
    CandidatureDto dto = CandidatureDto.fromEntity(candidature);

    assertNotNull(dto);
    assertEquals(1L, dto.getId());
    assertEquals(Candidature.Status.ACCEPTED, dto.getStatus());
    assertNotNull(dto.getStudent());
    assertNotNull(dto.getInternshipOffer());
    assertNotNull(dto.getCv());
    assertEquals(1L, dto.getInternshipOffer().getId());
    assertEquals("Stage développement", dto.getInternshipOffer().getDescription());
    assertEquals("FALL 2025", dto.getInternshipOffer().getSchoolTerm());
  }

  @Test
  void testFromEntity_WithNullCandidature() {
    CandidatureDto dto = CandidatureDto.fromEntity(null);

    assertNull(dto);
  }

  @Test
  void testGetEmployeurId() {
    CandidatureDto dto = CandidatureDto.fromEntity(candidature);

    assertEquals(1L, dto.getEmployeurId());
  }

  @Test
  void testGetEmployeurId_WithNullInternshipOffer() {
    CandidatureDto dto = new CandidatureDto();
    dto.setInternshipOffer(null);

    assertThrows(NullPointerException.class, () -> dto.getEmployeurId());
  }

  @Test
  void testFromEntity_WithNullSchoolTerm() {
    offer.setSchoolTerm(null);
    candidature.setInternshipOffer(offer);

    assertThrows(NullPointerException.class, () -> CandidatureDto.fromEntity(candidature));
  }

  @Test
  void testCandidatureDtoAllArgsConstructor() {
    StudentDto studentDto = StudentDto.empty();
    InternshipOfferDto offerDto = new InternshipOfferDto();
    CvDto cvDto = CvDto.empty();

    CandidatureDto dto =
        new CandidatureDto(1L, studentDto, offerDto, cvDto, Candidature.Status.PENDING, null, null);

    assertEquals(1L, dto.getId());
    assertEquals(studentDto, dto.getStudent());
    assertEquals(offerDto, dto.getInternshipOffer());
    assertEquals(cvDto, dto.getCv());
    assertEquals(Candidature.Status.PENDING, dto.getStatus());
    assertNull(dto.getApplicationDate());
    assertNull(dto.getError());
  }
}



