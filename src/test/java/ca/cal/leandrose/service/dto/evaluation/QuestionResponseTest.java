package ca.cal.leandrose.service.dto.evaluation;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class QuestionResponseTest {

  @Test
  void testQuestionResponseRecord() {
    QuestionResponse response = new QuestionResponse("Test comment", true, "5");

    assertNotNull(response);
    assertEquals("Test comment", response.comment());
    assertTrue(response.checked());
    assertEquals("5", response.rating());
  }

  @Test
  void testQuestionResponseWithNullValues() {
    QuestionResponse response = new QuestionResponse(null, null, null);

    assertNull(response.comment());
    assertNull(response.checked());
    assertNull(response.rating());
  }

  @Test
  void testQuestionResponseWithFalseChecked() {
    QuestionResponse response = new QuestionResponse("Comment", false, "3");

    assertFalse(response.checked());
    assertEquals("Comment", response.comment());
    assertEquals("3", response.rating());
  }

  @Test
  void testQuestionResponseEquals() {
    QuestionResponse response1 = new QuestionResponse("Comment", true, "5");
    QuestionResponse response2 = new QuestionResponse("Comment", true, "5");
    QuestionResponse response3 = new QuestionResponse("Different", true, "5");

    assertEquals(response1, response2);
    assertEquals(response1.hashCode(), response2.hashCode());
    assertNotEquals(response1, response3);
  }

  @Test
  void testQuestionResponseToString() {
    QuestionResponse response = new QuestionResponse("Comment", true, "5");

    assertNotNull(response.toString());
    assertTrue(response.toString().contains("QuestionResponse"));
  }

  @Test
  void testQuestionResponseWithEmptyStrings() {
    QuestionResponse response = new QuestionResponse("", false, "");

    assertEquals("", response.comment());
    assertFalse(response.checked());
    assertEquals("", response.rating());
  }
}





