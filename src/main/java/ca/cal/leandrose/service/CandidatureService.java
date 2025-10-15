package ca.cal.leandrose.service;

import ca.cal.leandrose.model.*;
import ca.cal.leandrose.repository.*;
import ca.cal.leandrose.service.dto.CandidatureDto;
import ca.cal.leandrose.service.dto.CandidatureEmployeurDto;
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

    public List<CandidatureEmployeurDto> getCandidaturesByOffer(Long offerId) {
        return candidatureRepository.findByInternshipOfferIdOrderByApplicationDateDesc(offerId)
                .stream()
                .map(CandidatureEmployeurDto::fromEntity)
                .toList();
    }

    public List<CandidatureEmployeurDto> getCandidaturesByEmployeur(Long employeurId) {
        return candidatureRepository.findByEmployeurIdOrderByApplicationDateDesc(employeurId)
                .stream()
                .map(CandidatureEmployeurDto::fromEntity)
                .toList();
    }
    /**
    @Transactional
    public void rejectCandidature(Long candidatureId) {
        Candidature candidature = candidatureRepository.findById(candidatureId)
                .orElseThrow(() -> new RuntimeException("Candidature non trouvée avec l'ID: " + candidatureId));

        if (candidature.getStatus() == Candidature.Status.REJECTED) {
            throw new IllegalStateException("Cette candidature est déjà rejetée");
        }

        if (candidature.getStatus() == Candidature.Status.ACCEPTED) {
            throw new IllegalStateException("Impossible de rejeter une candidature déjà acceptée");
        }

        candidature.setStatus(Candidature.Status.REJECTED);
        candidatureRepository.save(candidature);
    }
    **/
    /**
    @Transactional
    public CandidatureDto accept(Long candidatureId) {
        Candidature cand = candidatureRepository
                .findById(candidatureId)
                .orElseThrow(() -> new RuntimeException("Candidature introuvable ou non autorisée"));

        cand.setStatus(Candidature.Status.ACCEPTED);
        Candidature saved = candidatureRepository.save(cand);
        return CandidatureDto.fromEntity(saved);
    }
/**/

    @Transactional
    public CandidatureDto acceptByEmployeur(Long candidatureId) {
        Candidature cand = candidatureRepository
                .findById(candidatureId)
                .orElseThrow(() -> new RuntimeException("Candidature introuvable"));

        if (cand.getStatus() == Candidature.Status.REJECTED) {
            throw new IllegalStateException("Impossible d'accepter une candidature déjà rejetée");
        }

        if (cand.getStatus() == Candidature.Status.ACCEPTED) {
            throw new IllegalStateException("Cette candidature est déjà entièrement acceptée");
        }

        if (cand.getStatus() == Candidature.Status.ACCEPTEDBYEMPLOYEUR) {
            throw new IllegalStateException("Vous avez déjà accepté cette candidature, en attente de la réponse de l'étudiant");
        }

        cand.setStatus(Candidature.Status.ACCEPTEDBYEMPLOYEUR);
        Candidature saved = candidatureRepository.save(cand);
        return CandidatureDto.fromEntity(saved);
    }

    @Transactional
    public CandidatureDto acceptByStudent(Long candidatureId, Long studentId) {
        Candidature cand = candidatureRepository
                .findById(candidatureId)
                .orElseThrow(() -> new RuntimeException("Candidature introuvable"));

        if (!cand.getStudent().getId().equals(studentId)) {
            throw new IllegalStateException("Cette candidature ne vous appartient pas");
        }

        if (cand.getStatus() != Candidature.Status.ACCEPTEDBYEMPLOYEUR) {
            throw new IllegalStateException("L'employeur doit d'abord accepter cette candidature");
        }

        cand.setStatus(Candidature.Status.ACCEPTED);
        Candidature saved = candidatureRepository.save(cand);
        return CandidatureDto.fromEntity(saved);
    }
    @Transactional
    public CandidatureDto rejectByStudent(Long candidatureId, Long studentId) {
        Candidature cand = candidatureRepository
                .findById(candidatureId)
                .orElseThrow(() -> new RuntimeException("Candidature introuvable"));

        if (!cand.getStudent().getId().equals(studentId)) {
            throw new IllegalStateException("Cette candidature ne vous appartient pas");
        }

        if (cand.getStatus() != Candidature.Status.ACCEPTEDBYEMPLOYEUR) {
            throw new IllegalStateException("Vous ne pouvez refuser que les candidatures acceptées par l'employeur");
        }

        cand.setStatus(Candidature.Status.REJECTED);
        Candidature saved = candidatureRepository.save(cand);
        return CandidatureDto.fromEntity(saved);
    }

    @Transactional
    public CandidatureDto rejectByEmployeur(Long candidatureId) {
        Candidature cand = candidatureRepository
                .findById(candidatureId)
                .orElseThrow(() -> new RuntimeException("Candidature introuvable"));

        if (cand.getStatus() == Candidature.Status.ACCEPTED) {
            throw new IllegalStateException("Impossible de rejeter une candidature déjà acceptée par les deux parties");
        }

        if (cand.getStatus() == Candidature.Status.REJECTED) {
            throw new IllegalStateException("Cette candidature est déjà rejetée");
        }

        cand.setStatus(Candidature.Status.REJECTED);
        Candidature saved = candidatureRepository.save(cand);
        return CandidatureDto.fromEntity(saved);
    }
    /**
    @Transactional
    public CandidatureDto reject(Long candidatureId) {
        Candidature cand = candidatureRepository
                .findById(candidatureId)
                .orElseThrow(() -> new RuntimeException("Candidature introuvable ou non autorisée"));

        cand.setStatus(Candidature.Status.REJECTED);
        Candidature saved = candidatureRepository.save(cand);
        return CandidatureDto.fromEntity(saved);
    }
    /**/
}
