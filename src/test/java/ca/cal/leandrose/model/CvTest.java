package ca.cal.leandrose.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CvTest {

    private Cv cv;
    private Student student;

    @BeforeEach
    void setup() {
        student = mock(Student.class);

        cv = Cv.builder()
                .id(10L)
                .student(student)
                .pdfPath("/pdfs/cv123.pdf")
                .rejectionComment(null)
                .status(Cv.Status.PENDING)
                .build();
    }

    @Test
    void testBuilderCreatesValidObject() {
        assertThat(cv.getId()).isEqualTo(10L);
        assertThat(cv.getStudent()).isEqualTo(student);
        assertThat(cv.getPdfPath()).isEqualTo("/pdfs/cv123.pdf");
        assertThat(cv.getStatus()).isEqualTo(Cv.Status.PENDING);
    }

    @Test
    void testGetStudentNameReturnsCorrectName() {
        when(student.getFirstName()).thenReturn("John");
        when(student.getLastName()).thenReturn("Doe");

        String name = cv.getStudentName();

        assertThat(name).isEqualTo("John Doe");
    }

    @Test
    void testGetStudentNameReturnsNullWhenNoStudent() {
        Cv cv2 = Cv.builder()
                .id(11L)
                .student(null)
                .pdfPath("/pdfs/other.pdf")
                .status(Cv.Status.APPROVED)
                .build();

        assertThat(cv2.getStudentName()).isNull();
    }

    @Test
    void testSettersWorkCorrectly() {
        cv.setPdfPath("/new/path/cv.pdf");
        cv.setRejectionComment("Fix formatting");
        cv.setStatus(Cv.Status.REJECTED);

        assertThat(cv.getPdfPath()).isEqualTo("/new/path/cv.pdf");
        assertThat(cv.getRejectionComment()).isEqualTo("Fix formatting");
        assertThat(cv.getStatus()).isEqualTo(Cv.Status.REJECTED);
    }
}
