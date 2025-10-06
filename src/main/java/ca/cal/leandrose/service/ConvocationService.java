package ca.cal.leandrose.service;

import ca.cal.leandrose.model.Candidature;
import ca.cal.leandrose.model.Convocation;
import ca.cal.leandrose.repository.CandidatureRepository;
import ca.cal.leandrose.repository.ConvocationRepository;
import ca.cal.leandrose.service.dto.ConvocationDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Service
@RequiredArgsConstructor
public class ConvocationService {
    private final ConvocationRepository convocationRepository;
    private final CandidatureRepository candidatureRepository;

    public void addConvocation(Long candidatureId, LocalDateTime convocationDate, String location, String message) {
        Candidature candidature = candidatureRepository.findById(candidatureId)
                .orElseThrow(() -> new RuntimeException("Candidature non trouvée"));

        if (convocationDate == null) {
            throw new IllegalArgumentException("La date de convocation ne peut pas être nulle");
        }

        if (convocationDate.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("La date de convocation ne peut pas être dans le passé");
        }

        if (location == null || location.trim().isEmpty()) {
            throw new IllegalArgumentException("Le lieu ne peut pas être vide");
        }

        if (candidature.getStatus() == Candidature.Status.CONVENED) {
            throw new IllegalStateException("Cette candidature a déjà une convocation");
        }

        String finalMessage = (message == null || message.trim().isEmpty())
                ? "Vous êtes convoqué(e) pour un entretien."
                : message;

        candidature.setStatus(Candidature.Status.CONVENED);
        Convocation convocation = new Convocation(candidature, convocationDate, location, finalMessage);
        convocationRepository.save(convocation);
    }

    public List<ConvocationDto> getAllConvocationsByInterShipOfferId(Long internshipOfferId) {
        return convocationRepository.findByCandidatureInternshipOfferId(internshipOfferId)
                .stream()
                .map(ConvocationDto::create)
                .toList();
    }
}