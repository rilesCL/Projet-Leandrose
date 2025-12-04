package ca.cal.leandrose.service.dto.evaluation;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class CreateEvaluationRequestTest {

  @Test
  void testCreateEvaluationRequestRecord() {
    CreateEvaluationRequest request = new CreateEvaluationRequest(1L, 10L);

    assertNotNull(request);
    assertEquals(1L, request.studentId());
    assertEquals(10L, request.internshipOfferId());
  }

  @Test
  void testCreateEvaluationRequestWithNullValues() {
    CreateEvaluationRequest request = new CreateEvaluationRequest(null, null);

    assertNull(request.studentId());
    assertNull(request.internshipOfferId());
  }

  @Test
  void testCreateEvaluationRequestEquals() {
    CreateEvaluationRequest request1 = new CreateEvaluationRequest(1L, 10L);
    CreateEvaluationRequest request2 = new CreateEvaluationRequest(1L, 10L);
    CreateEvaluationRequest request3 = new CreateEvaluationRequest(2L, 10L);

    assertEquals(request1, request2);
    assertEquals(request1.hashCode(), request2.hashCode());
    assertNotEquals(request1, request3);
  }

  @Test
  void testCreateEvaluationRequestToString() {
    CreateEvaluationRequest request = new CreateEvaluationRequest(1L, 10L);

    assertNotNull(request.toString());
    assertTrue(request.toString().contains("CreateEvaluationRequest"));
  }
}
