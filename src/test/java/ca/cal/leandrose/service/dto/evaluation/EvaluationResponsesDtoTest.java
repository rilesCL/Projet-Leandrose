package ca.cal.leandrose.service.dto.evaluation;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class EvaluationResponsesDtoTest {

  @Test
  void testEvaluationResponsesDtoRecord() {
    EvaluationResponsesDto dto = new EvaluationResponsesDto(1L, "Évaluation créée avec succès");

    assertNotNull(dto);
    assertEquals(1L, dto.evaluationId());
    assertEquals("Évaluation créée avec succès", dto.message());
  }

  @Test
  void testEvaluationResponsesDtoWithNullValues() {
    EvaluationResponsesDto dto = new EvaluationResponsesDto(null, null);

    assertNull(dto.evaluationId());
    assertNull(dto.message());
  }

  @Test
  void testEvaluationResponsesDtoEquals() {
    EvaluationResponsesDto dto1 = new EvaluationResponsesDto(1L, "Évaluation créée avec succès");
    EvaluationResponsesDto dto2 = new EvaluationResponsesDto(1L, "Évaluation créée avec succès");
    EvaluationResponsesDto dto3 = new EvaluationResponsesDto(2L, "Évaluation créée avec succès");

    assertEquals(dto1, dto2);
    assertEquals(dto1.hashCode(), dto2.hashCode());
    assertNotEquals(dto1, dto3);
  }

  @Test
  void testEvaluationResponsesDtoToString() {
    EvaluationResponsesDto dto = new EvaluationResponsesDto(1L, "Évaluation créée avec succès");

    assertNotNull(dto.toString());
    assertTrue(dto.toString().contains("EvaluationResponsesDto"));
  }

  @Test
  void testEvaluationResponsesDtoWithEmptyMessage() {
    EvaluationResponsesDto dto = new EvaluationResponsesDto(1L, "");

    assertEquals(1L, dto.evaluationId());
    assertEquals("", dto.message());
  }
}





