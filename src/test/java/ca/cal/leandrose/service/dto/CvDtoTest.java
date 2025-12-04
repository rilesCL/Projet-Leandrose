package ca.cal.leandrose.service.dto;

import static org.junit.jupiter.api.Assertions.*;

import ca.cal.leandrose.model.Cv;
import ca.cal.leandrose.model.Student;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CvDtoTest {

  private Student student;
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

    cv = Cv.builder().id(1L).student(student).pdfPath("/cv.pdf").status(Cv.Status.APPROVED).build();
  }

  @Test
  void testCvDtoBuilder() {
    CvDto dto =
        CvDto.builder()
            .id(1L)
            .studentId(1L)
            .studentName("John Doe")
            .pdfPath("/cv.pdf")
            .status(Cv.Status.APPROVED)
            .rejectionComment(null)
            .build();

    assertEquals(1L, dto.getId());
    assertEquals(1L, dto.getStudentId());
    assertEquals("John Doe", dto.getStudentName());
    assertEquals("/cv.pdf", dto.getPdfPath());
    assertEquals(Cv.Status.APPROVED, dto.getStatus());
    assertNull(dto.getRejectionComment());
  }

  @Test
  void testCvDtoNoArgsConstructorAndSetters() {
    CvDto dto = new CvDto();
    dto.setId(2L);
    dto.setStudentId(2L);
    dto.setStudentName("Jane Smith");
    dto.setPdfPath("/cv2.pdf");
    dto.setStatus(Cv.Status.PENDING);
    dto.setRejectionComment("Needs improvement");

    assertEquals(2L, dto.getId());
    assertEquals(2L, dto.getStudentId());
    assertEquals("Jane Smith", dto.getStudentName());
    assertEquals("/cv2.pdf", dto.getPdfPath());
    assertEquals(Cv.Status.PENDING, dto.getStatus());
    assertEquals("Needs improvement", dto.getRejectionComment());
  }

  @Test
  void testCreate_WithCompleteCv() {
    CvDto dto = CvDto.create(cv);

    assertNotNull(dto);
    assertEquals(1L, dto.getId());
    assertEquals(1L, dto.getStudentId());
    assertEquals("John Doe", dto.getStudentName());
    assertEquals("/cv.pdf", dto.getPdfPath());
    assertEquals(Cv.Status.APPROVED, dto.getStatus());
  }

  @Test
  void testCreate_WithNullCv() {
    CvDto dto = CvDto.create(null);

    assertNotNull(dto);
    assertNull(dto.getId());
    assertNull(dto.getStudentId());
    assertNull(dto.getStudentName());
    assertNull(dto.getPdfPath());
    assertNull(dto.getStatus());
  }

  @Test
  void testCreate_WithNullStudent() {
    Cv cvWithoutStudent =
        Cv.builder().id(1L).student(null).pdfPath("/cv.pdf").status(Cv.Status.APPROVED).build();

    CvDto dto = CvDto.create(cvWithoutStudent);

    assertNotNull(dto);
    assertEquals(1L, dto.getId());
    assertNull(dto.getStudentId());
    assertNull(dto.getStudentName());
    assertEquals("/cv.pdf", dto.getPdfPath());
    assertEquals(Cv.Status.APPROVED, dto.getStatus());
  }

  @Test
  void testCreate_WithRejectionComment() {
    Cv cvRejected =
        Cv.builder()
            .id(1L)
            .student(student)
            .pdfPath("/cv.pdf")
            .status(Cv.Status.REJECTED)
            .rejectionComment("CV does not meet requirements")
            .build();

    CvDto dto = CvDto.create(cvRejected);

    assertEquals(Cv.Status.REJECTED, dto.getStatus());

    assertNull(dto.getRejectionComment());
  }

  @Test
  void testEmpty() {
    CvDto dto = CvDto.empty();

    assertNotNull(dto);
    assertNull(dto.getId());
    assertNull(dto.getStudentId());
    assertNull(dto.getStudentName());
    assertNull(dto.getPdfPath());
    assertNull(dto.getStatus());
  }

  @Test
  void testCvDtoAllArgsConstructor() {
    CvDto dto = new CvDto(1L, 1L, "/cv.pdf", Cv.Status.APPROVED, "John Doe", "No issues");

    assertEquals(1L, dto.getId());
    assertEquals(1L, dto.getStudentId());
    assertEquals("/cv.pdf", dto.getPdfPath());
    assertEquals(Cv.Status.APPROVED, dto.getStatus());
    assertEquals("John Doe", dto.getStudentName());
    assertNull(dto.getRejectionComment());
  }
}
