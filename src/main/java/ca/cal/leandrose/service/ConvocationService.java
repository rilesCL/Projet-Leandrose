package ca.cal.leandrose.service;

import ca.cal.leandrose.model.Candidature;
import ca.cal.leandrose.model.Convocation;
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

    public void addConvocation(Candidature candidature, LocalDateTime convocationDate, String location, String message) {
        if (candidature == null) {
            throw new IllegalArgumentException("La candidature ne peut pas être nulle");
        }

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
        Convocation convocation = new Convocation(candidature, convocationDate, finalMessage, location);
        convocationRepository.save(convocation);
    }

    public List<ConvocationDto> getAllConvocationsByInterShipOfferId(Long internshipOfferId) {
        List<Convocation> convocations = convocationRepository.findConvocationByInternShipOfferId(internshipOfferId);
        List<ConvocationDto> convocationDtos = new ArrayList<>();
        for (Convocation convocation : convocations) {
            convocationDtos.add(ConvocationDto.create(convocation));
        }
        return convocationDtos;
    }
}
