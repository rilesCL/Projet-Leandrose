package ca.cal.leandrose.service;


import ca.cal.leandrose.model.Cv;
import ca.cal.leandrose.model.Gestionnaire;
import ca.cal.leandrose.model.Student;
import ca.cal.leandrose.repository.CvRepository;
import ca.cal.leandrose.service.dto.CvDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class ManagerServiceTest {

    @Mock
    private CvRepository cvRepository;

    @InjectMocks
    private ManagerService managerService;

    private Gestionnaire gestionnaire;

    @BeforeEach
    void setUp(){
        MockitoAnnotations.openMocks(this);
        gestionnaire = Gestionnaire.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .email("john@company.com")
                .password("secret")
                .matricule("230232")
                .phoneNumber("514-329-3222")
                .build();
    }

    @Test
    void testGetPendingCvs(){
        Cv pendingCv1 = Cv.builder()
                .id(1L)
                .student(new Student())
                .pdfPath("cv1.pdf")
                .status(Cv.Status.PENDING)
                .build();

        Cv pendingCv2 = Cv.builder()
                .id(1L)
                .student(new Student())
                .pdfPath("cv2.pdf")
                .status(Cv.Status.PENDING)
                .build();

        when(cvRepository.findByStatus(Cv.Status.PENDING))
                .thenReturn(List.of(pendingCv1, pendingCv2));


        List<CvDto> result = managerService.getPendingCvs();


        assertEquals(2, result.size());
        assertEquals(Cv.Status.PENDING, result.getFirst().getStatus());
        assertEquals(Cv.Status.PENDING, result.get(1).getStatus());
        verify(cvRepository, times(1)).findByStatus(Cv.Status.PENDING);

    }
}
