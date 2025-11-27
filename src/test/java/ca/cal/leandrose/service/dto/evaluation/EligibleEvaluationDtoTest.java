package ca.cal.leandrose.service.dto.evaluation;

import static org.junit.jupiter.api.Assertions.*;

import ca.cal.leandrose.model.SchoolTerm;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class EligibleEvaluationDtoTest {

  private SchoolTerm schoolTerm;

  @BeforeEach
  void setUp() {
    schoolTerm = new SchoolTerm(SchoolTerm.Season.FALL, 2025);
  }

  @Test
  void testEligibleEvaluationDtoRecord() {
    EligibleEvaluationDto dto =
        new EligibleEvaluationDto(
            1L,
            10L,
            20L,
            "John",
            "Doe",
            "Computer Science",
            "Stage développement",
            "TechCorp",
            schoolTerm,
            LocalDate.of(2025, 9, 1),
            LocalDate.of(2025, 12, 1),
            false,
            null,
            false);

    assertNotNull(dto);
    assertEquals(1L, dto.agreementId());
    assertEquals(10L, dto.studentId());
    assertEquals(20L, dto.offerId());
    assertEquals("John", dto.studentFirstName());
    assertEquals("Doe", dto.studentLastName());
    assertEquals("Computer Science", dto.studentProgram());
    assertEquals("Stage développement", dto.internshipDescription());
    assertEquals("TechCorp", dto.companyName());
    assertEquals(schoolTerm, dto.internshipTerm());
    assertEquals(LocalDate.of(2025, 9, 1), dto.startDate());
    assertEquals(LocalDate.of(2025, 12, 1), dto.endDate());
    assertFalse(dto.hasEvaluation());
    assertNull(dto.evaluationId());
    assertFalse(dto.evaluationSubmitted());
  }

  @Test
  void testEligibleEvaluationDtoWithEvaluation() {
    EligibleEvaluationDto dto =
        new EligibleEvaluationDto(
            1L,
            10L,
            20L,
            "John",
            "Doe",
            "Computer Science",
            "Stage développement",
            "TechCorp",
            schoolTerm,
            LocalDate.of(2025, 9, 1),
            LocalDate.of(2025, 12, 1),
            true,
            100L,
            true);

    assertTrue(dto.hasEvaluation());
    assertEquals(100L, dto.evaluationId());
    assertTrue(dto.evaluationSubmitted());
  }

  @Test
  void testEligibleEvaluationDtoWithNullValues() {
    EligibleEvaluationDto dto =
        new EligibleEvaluationDto(
            null, null, null, null, null, null, null, null, null, null, null, null, null, null);

    assertNull(dto.agreementId());
    assertNull(dto.studentId());
    assertNull(dto.offerId());
    assertNull(dto.studentFirstName());
    assertNull(dto.studentLastName());
    assertNull(dto.studentProgram());
    assertNull(dto.internshipDescription());
    assertNull(dto.companyName());
    assertNull(dto.internshipTerm());
    assertNull(dto.startDate());
    assertNull(dto.endDate());
    assertNull(dto.hasEvaluation());
    assertNull(dto.evaluationId());
    assertNull(dto.evaluationSubmitted());
  }
}



