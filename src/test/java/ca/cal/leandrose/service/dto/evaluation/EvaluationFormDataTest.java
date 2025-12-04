package ca.cal.leandrose.service.dto.evaluation;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class EvaluationFormDataTest {

  @Test
  void testEvaluationFormDataRecord() {
    Map<String, List<QuestionResponse>> categories = new HashMap<>();
    categories.put("productivity", List.of());

    EvaluationFormData formData =
        new EvaluationFormData(
            categories, "General comment", 5, "Excellent", true, 10, "Yes", true);

    assertNotNull(formData);
    assertEquals(categories, formData.categories());
    assertEquals("General comment", formData.generalComment());
    assertEquals(5, formData.globalAssessment());
    assertEquals("Excellent", formData.globalAppreciation());
    assertTrue(formData.discussedWithTrainee());
    assertEquals(10, formData.supervisionHours());
    assertEquals("Yes", formData.welcomeNextInternship());
    assertTrue(formData.technicalTrainingSufficient());
  }

  @Test
  void testEvaluationFormDataWithNullValues() {
    EvaluationFormData formData =
        new EvaluationFormData(null, null, null, null, null, null, null, null);

    assertNull(formData.categories());
    assertNull(formData.generalComment());
    assertNull(formData.globalAssessment());
    assertNull(formData.globalAppreciation());
    assertNull(formData.discussedWithTrainee());
    assertNull(formData.supervisionHours());
    assertNull(formData.welcomeNextInternship());
    assertNull(formData.technicalTrainingSufficient());
  }

  @Test
  void testEvaluationFormDataWithEmptyCategories() {
    Map<String, List<QuestionResponse>> categories = new HashMap<>();
    EvaluationFormData formData =
        new EvaluationFormData(categories, "", 0, "", false, 0, "", false);

    assertNotNull(formData.categories());
    assertTrue(formData.categories().isEmpty());
    assertEquals("", formData.generalComment());
    assertEquals(0, formData.globalAssessment());
    assertEquals("", formData.globalAppreciation());
    assertFalse(formData.discussedWithTrainee());
    assertEquals(0, formData.supervisionHours());
    assertEquals("", formData.welcomeNextInternship());
    assertFalse(formData.technicalTrainingSufficient());
  }

  @Test
  void testEvaluationFormDataEquals() {
    Map<String, List<QuestionResponse>> categories = new HashMap<>();
    categories.put("productivity", List.of());

    EvaluationFormData formData1 =
        new EvaluationFormData(
            categories, "General comment", 5, "Excellent", true, 10, "Yes", true);

    EvaluationFormData formData2 =
        new EvaluationFormData(
            categories, "General comment", 5, "Excellent", true, 10, "Yes", true);

    assertEquals(formData1, formData2);
    assertEquals(formData1.hashCode(), formData2.hashCode());
  }

  @Test
  void testEvaluationFormDataToString() {
    Map<String, List<QuestionResponse>> categories = new HashMap<>();
    EvaluationFormData formData =
        new EvaluationFormData(
            categories, "General comment", 5, "Excellent", true, 10, "Yes", true);

    assertNotNull(formData.toString());
    assertTrue(formData.toString().contains("EvaluationFormData"));
  }
}
