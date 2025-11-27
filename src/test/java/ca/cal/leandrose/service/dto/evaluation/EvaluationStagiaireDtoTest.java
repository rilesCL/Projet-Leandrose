package ca.cal.leandrose.service.dto.evaluation;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class EvaluationStagiaireDtoTest {

  @Test
  void testEvaluationStagiaireDtoRecord() {
    EvaluationStagiaireDto dto =
        new EvaluationStagiaireDto(
            1L,
            LocalDate.of(2025, 6, 15),
            10L,
            20L,
            30L,
            40L,
            "/uploads/employer_eval.pdf",
            "/uploads/professor_eval.pdf",
            true,
            false);

    assertNotNull(dto);
    assertEquals(1L, dto.id());
    assertEquals(LocalDate.of(2025, 6, 15), dto.dateEvaluation());
    assertEquals(10L, dto.studentId());
    assertEquals(20L, dto.employeurId());
    assertEquals(30L, dto.professeurId());
    assertEquals(40L, dto.internshipOfferId());
    assertEquals("/uploads/employer_eval.pdf", dto.employerPdfPath());
    assertEquals("/uploads/professor_eval.pdf", dto.professorPdfPath());
    assertTrue(dto.submittedByEmployer());
    assertFalse(dto.submittedByProfessor());
  }

  @Test
  void testEvaluationStagiaireDtoWithNullValues() {
    EvaluationStagiaireDto dto =
        new EvaluationStagiaireDto(
            null, null, null, null, null, null, null, null, false, false);

    assertNull(dto.id());
    assertNull(dto.dateEvaluation());
    assertNull(dto.studentId());
    assertNull(dto.employeurId());
    assertNull(dto.professeurId());
    assertNull(dto.internshipOfferId());
    assertNull(dto.employerPdfPath());
    assertNull(dto.professorPdfPath());
    assertFalse(dto.submittedByEmployer());
    assertFalse(dto.submittedByProfessor());
  }

  @Test
  void testEvaluationStagiaireDtoEquals() {
    EvaluationStagiaireDto dto1 =
        new EvaluationStagiaireDto(
            1L,
            LocalDate.of(2025, 6, 15),
            10L,
            20L,
            30L,
            40L,
            "/uploads/employer_eval.pdf",
            "/uploads/professor_eval.pdf",
            true,
            false);

    EvaluationStagiaireDto dto2 =
        new EvaluationStagiaireDto(
            1L,
            LocalDate.of(2025, 6, 15),
            10L,
            20L,
            30L,
            40L,
            "/uploads/employer_eval.pdf",
            "/uploads/professor_eval.pdf",
            true,
            false);

    assertEquals(dto1, dto2);
    assertEquals(dto1.hashCode(), dto2.hashCode());
  }

  @Test
  void testEvaluationStagiaireDtoToString() {
    EvaluationStagiaireDto dto =
        new EvaluationStagiaireDto(
            1L,
            LocalDate.of(2025, 6, 15),
            10L,
            20L,
            30L,
            40L,
            "/uploads/employer_eval.pdf",
            "/uploads/professor_eval.pdf",
            true,
            false);

    assertNotNull(dto.toString());
    assertTrue(dto.toString().contains("EvaluationStagiaireDto"));
  }

  @Test
  void testEvaluationStagiaireDtoWithBothSubmitted() {
    EvaluationStagiaireDto dto =
        new EvaluationStagiaireDto(
            1L,
            LocalDate.of(2025, 6, 15),
            10L,
            20L,
            30L,
            40L,
            "/uploads/employer_eval.pdf",
            "/uploads/professor_eval.pdf",
            true,
            true);

    assertTrue(dto.submittedByEmployer());
    assertTrue(dto.submittedByProfessor());
  }
}




