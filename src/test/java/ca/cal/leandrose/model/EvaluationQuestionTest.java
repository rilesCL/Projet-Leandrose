package ca.cal.leandrose.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class EvaluationQuestionTest {

  @Test
  void testEvaluationQuestionExists() {
    EvaluationQuestion question = new EvaluationQuestion();
    assertNotNull(question);
  }

  @Test
  void testEvaluationQuestionCanBeInstantiated() {
    EvaluationQuestion question = new EvaluationQuestion();
    assertNotNull(question);
    // Note: EvaluationQuestion does not have public getters/setters,
    // so we can only test that it can be instantiated
  }
}



