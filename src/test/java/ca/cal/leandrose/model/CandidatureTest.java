package ca.cal.leandrose.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class CandidatureTest {

    private Candidature candidature;
    private Student student;
    private InternshipOffer internshipOffer;
    private Cv cv;

    @BeforeEach
    void setup() {
        student = mock(Student.class);
        internshipOffer = mock(InternshipOffer.class);
        cv = mock(Cv.class);

        candidature = Candidature.builder()
                .id(1L)
                .student(student)
                .internshipOffer(internshipOffer)
                .cv(cv)
                .convocation(null)
                .status(Candidature.Status.PENDING)
                .applicationDate(LocalDateTime.now())
                .build();
    }

    @Test
    void testBuilderCreatesValidObject() {
        assertThat(candidature.getId()).isEqualTo(1L);
        assertThat(candidature.getStudent()).isEqualTo(student);
        assertThat(candidature.getInternshipOffer()).isEqualTo(internshipOffer);
        assertThat(candidature.getCv()).isEqualTo(cv);
        assertThat(candidature.getStatus()).isEqualTo(Candidature.Status.PENDING);
        assertThat(candidature.getApplicationDate()).isNotNull();
    }

    @Test
    void testGetEmployeurIdReturnsInternshipOfferEmployeurId() {
        when(internshipOffer.getEmployeurId()).thenReturn(42L);

        Long result = candidature.getEmployeurId();

        assertThat(result).isEqualTo(42L);
        verify(internshipOffer, times(1)).getEmployeurId();
    }

    @Test
    void testSettersWorkCorrectly() {
        Cv newCv = mock(Cv.class);
        candidature.setCv(newCv);

        assertThat(candidature.getCv()).isEqualTo(newCv);
    }

    @Test
    void testStatusUpdatesCorrectly() {
        candidature.setStatus(Candidature.Status.ACCEPTED);

        assertThat(candidature.getStatus())
                .isEqualTo(Candidature.Status.ACCEPTED);
    }

    @Test
    void testConvocationIsOptional() {
        assertThat(candidature.getConvocation()).isNull();
    }
}
