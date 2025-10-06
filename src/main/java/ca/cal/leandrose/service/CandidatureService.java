package ca.cal.leandrose.service;

import ca.cal.leandrose.model.*;
import ca.cal.leandrose.repository.*;
import ca.cal.leandrose.service.dto.CandidatureDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CandidatureService {

    private final CandidatureRepository candidatureRepository;
    private final InternshipOfferRepository internshipOfferRepository;
    private final StudentRepository studentRepository;
    private final CvRepository cvRepository;

    @Transactional
    public CandidatureDto postuler(Long studentId, Long offerId, Long cvId) {

        candidatureRepository.findByStudentIdAndInternshipOfferId(studentId, offerId)
                .ifPresent(c -> {
                    throw new IllegalStateException("Vous avez déjà postulé à cette offre");
                });

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Étudiant non trouvé"));

        InternshipOffer offer = internshipOfferRepository.findById(offerId)
                .orElseThrow(() -> new RuntimeException("Offre non trouvée"));

        if (offer.getStatus() != InternshipOffer.Status.PUBLISHED) {
            throw new IllegalStateException("Cette offre n'est pas disponible");
        }

        Cv cv = cvRepository.findById(cvId)
                .orElseThrow(() -> new RuntimeException("CV non trouvé"));

        if (cv.getStatus() != Cv.Status.APPROVED) {
            throw new IllegalStateException("Votre CV doit être approuvé pour postuler");
        }

        Candidature candidature = Candidature.builder()
                .student(student)
                .internshipOffer(offer)
                .cv(cv)
                .status(Candidature.Status.PENDING)
                .applicationDate(LocalDate.now())
                .build();

        Candidature saved = candidatureRepository.save(candidature);

        return CandidatureDto.fromEntity(saved);
    }

    public List<CandidatureDto> getCandidaturesByStudent(Long studentId) {
        return candidatureRepository.findByStudentIdOrderByApplicationDateDesc(studentId)
                .stream()
                .map(CandidatureDto::fromEntity)
                .toList();
    }

    public CandidatureDto getCandidatureById(Long candidatureId) {
        Candidature candidature = candidatureRepository.findById(candidatureId)
                .orElseThrow(() -> new RuntimeException("Candidature non trouvée avec l'ID: " + candidatureId));
        return CandidatureDto.fromEntity(candidature);
    }
}