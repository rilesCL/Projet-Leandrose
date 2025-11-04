package ca.cal.leandrose.service;

import ca.cal.leandrose.model.Cv;
import ca.cal.leandrose.model.Gestionnaire;
import ca.cal.leandrose.model.InternshipOffer;
import ca.cal.leandrose.model.Student;
import ca.cal.leandrose.repository.CvRepository;
import ca.cal.leandrose.repository.InternshipOfferRepository;
import ca.cal.leandrose.service.dto.CvDto;
import ca.cal.leandrose.service.dto.InternshipOfferDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class GestionnaireServiceTest {

  @Mock private CvRepository cvRepository;
  @InjectMocks private GestionnaireService gestionnaireService;
  @Mock private InternshipOfferRepository internshipOfferRepository;
  private InternshipOffer offerPending;

  private Cv pendingCv;

  private Gestionnaire gestionnaire;

  @BeforeEach
  void setUp() {
    gestionnaire =
        Gestionnaire.builder()
            .id(1L)
            .firstName("John")
            .lastName("Doe")
            .email("john@company.com")
            .password("secret")
            .phoneNumber("514-329-3222")
            .build();
    offerPending = new InternshipOffer();
    offerPending.setId(1L);
    offerPending.setStatus(InternshipOffer.Status.PENDING_VALIDATION);

    pendingCv = Cv.builder().id(10L).pdfPath("path/cv.pdf").status(Cv.Status.PENDING).build();
  }

  @Test
  void testCvAccept() {
    when(cvRepository.findById(10L)).thenReturn(Optional.of(pendingCv));
    when(cvRepository.save(any(Cv.class))).thenAnswer(invocation -> invocation.getArgument(0));

    CvDto result = gestionnaireService.approveCv(10L);

    assertThat(result.getStatus()).isEqualTo(Cv.Status.APPROVED);
    verify(cvRepository).findById(10L);
    verify(cvRepository).save(pendingCv);
  }

  @Test
  void testCvReject() {
    when(cvRepository.findById(10L)).thenReturn(Optional.of(pendingCv));
    when(cvRepository.save(any(Cv.class))).thenAnswer(invocation -> invocation.getArgument(0));

    CvDto result = gestionnaireService.rejectCv(10L, "cv trop court");

    assertThat(result.getStatus()).isEqualTo(Cv.Status.REJECTED);
    verify(cvRepository).findById(10L);
    verify(cvRepository).save(pendingCv);
  }

  @Test
  void testGetPendingCvs() {
    Cv pendingCv1 =
        Cv.builder()
            .id(1L)
            .student(new Student())
            .pdfPath("cv1.pdf")
            .status(Cv.Status.PENDING)
            .build();

    Cv pendingCv2 =
        Cv.builder()
            .id(1L)
            .student(new Student())
            .pdfPath("cv2.pdf")
            .status(Cv.Status.PENDING)
            .build();

    when(cvRepository.findByStatus(Cv.Status.PENDING)).thenReturn(List.of(pendingCv1, pendingCv2));

    List<CvDto> result = gestionnaireService.getPendingCvs();

    assertEquals(2, result.size());
    assertEquals(Cv.Status.PENDING, result.get(0).getStatus());
    assertEquals(Cv.Status.PENDING, result.get(1).getStatus());
    verify(cvRepository).findByStatus(Cv.Status.PENDING);
  }

  @Test
  void approveOffer_devrait_publier_et_definir_validationDate() {
    when(internshipOfferRepository.findById(1L)).thenReturn(Optional.of(offerPending));
    when(internshipOfferRepository.save(any(InternshipOffer.class)))
        .thenAnswer(inv -> inv.getArgument(0));

    InternshipOfferDto result = gestionnaireService.approveOffer(1L);

    assertThat(result.getStatus()).isEqualTo(InternshipOffer.Status.PUBLISHED.name());
    assertThat(result.getValidationDate()).isEqualTo(LocalDate.now());
    verify(internshipOfferRepository).findById(1L);
    verify(internshipOfferRepository).save(offerPending);
  }

  @Test
  void approveOffer_devrait_echouer_si_etat_invalide() {
    InternshipOffer published = new InternshipOffer();
    published.setId(2L);
    published.setStatus(InternshipOffer.Status.PUBLISHED);
    when(internshipOfferRepository.findById(2L)).thenReturn(Optional.of(published));

    assertThrows(IllegalStateException.class, () -> gestionnaireService.approveOffer(2L));
    verify(internshipOfferRepository).findById(2L);
    verify(internshipOfferRepository, never()).save(any());
  }

  @Test
  void getOneRejectedOffer() {
    InternshipOffer rejected = new InternshipOffer();
    rejected.setId(2L);
    rejected.setStatus(InternshipOffer.Status.REJECTED);

    when(internshipOfferRepository.findByStatusOrderByStartDateDesc(
            InternshipOffer.Status.REJECTED))
        .thenReturn(List.of(rejected));

    List<InternshipOfferDto> list_rejected_offers = gestionnaireService.getRejectedoffers();

    assertEquals(1, list_rejected_offers.size());
  }

  @Test
  void getMultiplesRejectedOffers() {
    List<InternshipOffer> rejectedOffers = new ArrayList<>();

    for (long i = 0; i <= 5; i++) {
      InternshipOffer offer = new InternshipOffer();
      offer.setId(i);
      offer.setStatus(InternshipOffer.Status.REJECTED);
      rejectedOffers.add(offer);
    }

    when(internshipOfferRepository.findByStatusOrderByStartDateDesc(
            InternshipOffer.Status.REJECTED))
        .thenReturn(rejectedOffers);
    List<InternshipOfferDto> list_rejected_offers = gestionnaireService.getRejectedoffers();

    assertEquals(rejectedOffers.size(), list_rejected_offers.size());
    assertTrue(list_rejected_offers.stream().allMatch(o -> "REJECTED".equals(o.getStatus())));
  }

  @Test
  void getOneApprovedOffer() {
    InternshipOffer accepted = new InternshipOffer();
    accepted.setId(2L);
    accepted.setStatus(InternshipOffer.Status.PUBLISHED);

    when(internshipOfferRepository.findByStatusOrderByStartDateDesc(
            InternshipOffer.Status.PUBLISHED))
        .thenReturn(List.of(accepted));

    List<InternshipOfferDto> list_approved_offers = gestionnaireService.getApprovedOffers();

    assertEquals(1, list_approved_offers.size());
  }

  @Test
  void getMutlipleApprovedOffers() {
    List<InternshipOffer> acceptedOffers = new ArrayList<>();

    for (long i = 0; i <= 5; i++) {
      InternshipOffer offer = new InternshipOffer();
      offer.setId(i);
      offer.setStatus(InternshipOffer.Status.PUBLISHED);
      acceptedOffers.add(offer);
    }

    when(internshipOfferRepository.findByStatusOrderByStartDateDesc(
            InternshipOffer.Status.PUBLISHED))
        .thenReturn(acceptedOffers);
    List<InternshipOfferDto> list_approved_offers = gestionnaireService.getApprovedOffers();

    assertEquals(acceptedOffers.size(), list_approved_offers.size());
    assertTrue(list_approved_offers.stream().allMatch(o -> "PUBLISHED".equals(o.getStatus())));
  }
}
