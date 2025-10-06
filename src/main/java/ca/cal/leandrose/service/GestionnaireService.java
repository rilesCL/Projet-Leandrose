package ca.cal.leandrose.service;

import ca.cal.leandrose.model.Cv;
import ca.cal.leandrose.model.Gestionnaire;
import ca.cal.leandrose.model.InternshipOffer;
import ca.cal.leandrose.model.Program;
import ca.cal.leandrose.repository.CvRepository;
import ca.cal.leandrose.repository.GestionnaireRepository;
import ca.cal.leandrose.repository.InternshipOfferRepository;
import ca.cal.leandrose.service.dto.CvDto;
import ca.cal.leandrose.service.dto.GestionnaireDto;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GestionnaireService {
    private final CvRepository cvRepository;
    private final InternshipOfferRepository internshipOfferRepository;
    private final GestionnaireRepository gestionnaireRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public CvDto approveCv(Long cvId) {
        Cv cv = cvRepository.findById(cvId)
                .orElseThrow(() -> new RuntimeException("Cv non trouvé"));
        cv.setStatus(Cv.Status.APPROVED);
        Cv saved = cvRepository.save(cv);
        return CvDto.create(saved);
    }

    @Transactional
    public CvDto rejectCv(Long cvId, String comment) {
        Cv cv = cvRepository.findById(cvId)
                .orElseThrow(() -> new RuntimeException("Cv non trouvé"));
        cv.setStatus(Cv.Status.REJECTED);
        cv.setRejectionComment(comment);
        Cv saved = cvRepository.save(cv);
        return CvDto.create(saved);
    }

    public List<CvDto> getPendingCvs() {
        return cvRepository.findByStatus(Cv.Status.PENDING)
                .stream()
                .map(CvDto::create)
                .toList();
    }

    @Transactional
    public InternshipOffer approveOffer(Long offerId) {
        InternshipOffer offer = internshipOfferRepository.findById(offerId)
                .orElseThrow(() -> new RuntimeException("Offre non trouvée"));

        if (offer.getStatus() != InternshipOffer.Status.PENDING_VALIDATION) {
            throw new IllegalStateException("Cette offre ne peut pas être approuvée");
        }

        offer.setStatus(InternshipOffer.Status.PUBLISHED);
        offer.setValidationDate(LocalDate.now());

        return internshipOfferRepository.save(offer);
    }

    @Transactional
    public InternshipOffer rejectOffer(Long offerId, String rejectionComment) {
        if (rejectionComment == null || rejectionComment.trim().isEmpty()) {
            throw new IllegalArgumentException("Un commentaire est obligatoire pour rejeter une offre");
        }

        InternshipOffer offer = internshipOfferRepository.findById(offerId)
                .orElseThrow(() -> new RuntimeException("Offre non trouvée"));

        if (offer.getStatus() != InternshipOffer.Status.PENDING_VALIDATION) {
            throw new IllegalStateException("Cette offre ne peut pas être rejetée");
        }

        offer.setStatus(InternshipOffer.Status.REJECTED);
        offer.setRejectionComment(rejectionComment);
        offer.setValidationDate(LocalDate.now());

        return internshipOfferRepository.save(offer);
    }

    public List<InternshipOffer> getPendingOffers() {
        return internshipOfferRepository.findByStatusOrderByStartDateDesc(InternshipOffer.Status.PENDING_VALIDATION);
    }

    @Transactional
    public GestionnaireDto createGestionnaire(
            String firstName, String lastName, String email, String rawPassword, String phoneNumber) {

        Gestionnaire gestionnaire = Gestionnaire.builder()
                .firstName(firstName)
                .lastName(lastName)
                .email(email)
                .password(passwordEncoder.encode(rawPassword))
                .phoneNumber(phoneNumber)
                .build();

        Gestionnaire savedGestionnaire = gestionnaireRepository.save(gestionnaire);
        return GestionnaireDto.create(savedGestionnaire);
    }
    public List<Program> getAllPrograms(){
        return List.of(Program.values());
    }
}
