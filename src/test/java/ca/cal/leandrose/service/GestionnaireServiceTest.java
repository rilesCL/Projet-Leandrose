package ca.cal.leandrose.service;

import ca.cal.leandrose.model.Cv;
import ca.cal.leandrose.model.Gestionnaire;
import ca.cal.leandrose.model.InternshipOffer;
import ca.cal.leandrose.model.Student;
import ca.cal.leandrose.repository.CvRepository;
import ca.cal.leandrose.repository.GestionnaireRepository;
import ca.cal.leandrose.repository.InternshipOfferRepository;
import ca.cal.leandrose.service.dto.CvDto;
import ca.cal.leandrose.service.dto.GestionnaireDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class GestionnaireServiceTest {

    @Mock
    private CvRepository cvRepository;
    @InjectMocks
    private GestionnaireService gestionnaireService;
    @Mock
    private InternshipOfferRepository internshipOfferRepository;
    @Mock
    private GestionnaireRepository gestionnaireRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    private InternshipOffer offerPending;

    private Cv pendingCv;

    private Gestionnaire gestionnaire;

    @BeforeEach
    void setUp() {
        gestionnaire = Gestionnaire.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .email("john@company.com")
                .password("secret")
                .matricule("230232")
                .phoneNumber("514-329-3222")
                .build();
        offerPending = new InternshipOffer();
        offerPending.setId(1L);
        offerPending.setStatus(InternshipOffer.Status.PENDING_VALIDATION);

        pendingCv = Cv.builder()
                .id(10L)
                .pdfPath("path/cv.pdf")
                .status(Cv.Status.PENDING)
                .build();

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

        CvDto result = gestionnaireService.rejectCv(10L);

        assertThat(result.getStatus()).isEqualTo(Cv.Status.REJECTED);
        verify(cvRepository).findById(10L);
        verify(cvRepository).save(pendingCv);
    }

    @Test
    void testGetPendingCvs() {
        Cv pendingCv1 = Cv.builder()
                .id(1L)
                .student(new Student())
                .pdfPath("cv1.pdf")
                .status(Cv.Status.PENDING)
                .build();

        Cv pendingCv2 = Cv.builder()
                .id(2L)
                .student(new Student())
                .pdfPath("cv2.pdf")
                .status(Cv.Status.PENDING)
                .build();

        when(cvRepository.findByStatus(Cv.Status.PENDING))
                .thenReturn(List.of(pendingCv1, pendingCv2));

        List<CvDto> result = gestionnaireService.getPendingCvs();

        assertEquals(2, result.size());
        assertEquals(Cv.Status.PENDING, result.get(0).getStatus());
        assertEquals(Cv.Status.PENDING, result.get(1).getStatus());
        verify(cvRepository).findByStatus(Cv.Status.PENDING);
    }

    @Test
    void approveOffer_devrait_publier_et_definir_validationDate() {
        when(internshipOfferRepository.findById(1L)).thenReturn(Optional.of(offerPending));
        when(internshipOfferRepository.save(any(InternshipOffer.class))).thenAnswer(inv -> inv.getArgument(0));

        InternshipOffer result = gestionnaireService.approveOffer(1L);

        assertThat(result.getStatus()).isEqualTo(InternshipOffer.Status.PUBLISHED);
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
    void approveOffer_devrait_lancer_exception_si_introuvable() {
        when(internshipOfferRepository.findById(404L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> gestionnaireService.approveOffer(404L));
        verify(internshipOfferRepository).findById(404L);
        verify(internshipOfferRepository, never()).save(any());
    }

    @Test
    void rejectOffer_devrait_rejeter_avec_commentaire_et_date() {
        when(internshipOfferRepository.findById(3L)).thenReturn(Optional.of(offerPending));
        when(internshipOfferRepository.save(any(InternshipOffer.class))).thenAnswer(inv -> inv.getArgument(0));

        InternshipOffer result = gestionnaireService.rejectOffer(3L, "Incomplet");

        assertThat(result.getStatus()).isEqualTo(InternshipOffer.Status.REJECTED);
        assertThat(result.getRejectionComment()).isEqualTo("Incomplet");
        assertThat(result.getValidationDate()).isEqualTo(LocalDate.now());
        verify(internshipOfferRepository).findById(3L);
        verify(internshipOfferRepository).save(offerPending);
    }

    @Test
    void rejectOffer_devrait_echouer_si_commentaire_vide() {
        assertThrows(IllegalArgumentException.class, () -> gestionnaireService.rejectOffer(5L, "  "));
        verify(internshipOfferRepository, never()).findById(anyLong());
        verify(internshipOfferRepository, never()).save(any());
    }

    @Test
    void rejectOffer_devrait_echouer_si_etat_invalide() {
        InternshipOffer published = new InternshipOffer();
        published.setId(6L);
        published.setStatus(InternshipOffer.Status.PUBLISHED);
        when(internshipOfferRepository.findById(6L)).thenReturn(Optional.of(published));

        assertThrows(IllegalStateException.class, () -> gestionnaireService.rejectOffer(6L, "mauvais"));
        verify(internshipOfferRepository).findById(6L);
        verify(internshipOfferRepository, never()).save(any());
    }

    @Test
    void rejectOffer_devrait_lancer_exception_si_introuvable() {
        when(internshipOfferRepository.findById(7L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> gestionnaireService.rejectOffer(7L, "raison"));
        verify(internshipOfferRepository).findById(7L);
        verify(internshipOfferRepository, never()).save(any());
    }

    @Test
    void getPendingOffers_devrait_relayer_la_reponse_du_repository() {
        InternshipOffer o1 = new InternshipOffer();
        o1.setId(11L);
        o1.setStatus(InternshipOffer.Status.PENDING_VALIDATION);
        InternshipOffer o2 = new InternshipOffer();
        o2.setId(12L);
        o2.setStatus(InternshipOffer.Status.PENDING_VALIDATION);

        when(internshipOfferRepository.findByStatusOrderByStartDateDesc(InternshipOffer.Status.PENDING_VALIDATION))
                .thenReturn(List.of(o1, o2));

        List<InternshipOffer> result = gestionnaireService.getPendingOffers();

        assertThat(result).containsExactly(o1, o2);
        verify(internshipOfferRepository)
                .findByStatusOrderByStartDateDesc(InternshipOffer.Status.PENDING_VALIDATION);
    }

    @Test
    void createGestionnaire_devrait_encoder_mdp_sauver_et_retourner_dto() {
        when(passwordEncoder.encode("Password123!")).thenReturn("ENC(Password123!)");
        when(gestionnaireRepository.save(any(Gestionnaire.class))).thenAnswer(inv -> {
            Gestionnaire g = inv.getArgument(0);
            return Gestionnaire.builder()
                    .id(100L)
                    .firstName(g.getFirstName())
                    .lastName(g.getLastName())
                    .email(g.getEmail())
                    .password(g.getPassword())
                    .matricule(g.getMatricule())
                    .phoneNumber(g.getPhoneNumber())
                    .build();
        });

        GestionnaireDto dto = gestionnaireService.createGestionnaire(
                "Jean", "Dupont", "gestionnaire@test.com", "Password123!", "GS001", "514-123-4567");

        assertThat(dto.getId()).isEqualTo(100L);
        assertThat(dto.getEmail()).isEqualTo("gestionnaire@test.com");
        verify(passwordEncoder).encode("Password123!");
        verify(gestionnaireRepository).save(any(Gestionnaire.class));
    }

    @Test
    void createGestionnaire_devrait_envelopper_les_exceptions_en_RuntimeException() {
        when(passwordEncoder.encode(anyString())).thenReturn("x");
        when(gestionnaireRepository.save(any(Gestionnaire.class))).thenThrow(new IllegalStateException("DB"));

        assertThrows(RuntimeException.class, () -> gestionnaireService.createGestionnaire(
                "A", "B", "a@b.com", "pwd", "M1", "000"));

        verify(gestionnaireRepository).save(any(Gestionnaire.class));
    }
}