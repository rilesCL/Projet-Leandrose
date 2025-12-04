package ca.cal.leandrose.service.dto.evaluation;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class EvaluationInfoDtoTest {

  @Test
  void testEvaluationInfoDtoRecord() {
    StudentInfoDto studentInfo = new StudentInfoDto(1L, "John", "Doe", "Computer Science");
    InternshipInfoDto internshipInfo = new InternshipInfoDto(1L, "Stage développement", "TechCorp");

    EvaluationInfoDto dto = new EvaluationInfoDto(studentInfo, internshipInfo);

    assertNotNull(dto);
    assertEquals(studentInfo, dto.studentInfo());
    assertEquals(internshipInfo, dto.internshipInfo());
  }

  @Test
  void testEvaluationInfoDtoWithNullValues() {
    EvaluationInfoDto dto = new EvaluationInfoDto(null, null);

    assertNull(dto.studentInfo());
    assertNull(dto.internshipInfo());
  }

  @Test
  void testEvaluationInfoDtoEquals() {
    StudentInfoDto studentInfo = new StudentInfoDto(1L, "John", "Doe", "Computer Science");
    InternshipInfoDto internshipInfo = new InternshipInfoDto(1L, "Stage développement", "TechCorp");

    EvaluationInfoDto dto1 = new EvaluationInfoDto(studentInfo, internshipInfo);
    EvaluationInfoDto dto2 = new EvaluationInfoDto(studentInfo, internshipInfo);

    assertEquals(dto1, dto2);
    assertEquals(dto1.hashCode(), dto2.hashCode());
  }

  @Test
  void testEvaluationInfoDtoToString() {
    StudentInfoDto studentInfo = new StudentInfoDto(1L, "John", "Doe", "Computer Science");
    InternshipInfoDto internshipInfo = new InternshipInfoDto(1L, "Stage développement", "TechCorp");

    EvaluationInfoDto dto = new EvaluationInfoDto(studentInfo, internshipInfo);

    assertNotNull(dto.toString());
    assertTrue(dto.toString().contains("EvaluationInfoDto"));
  }
}
