package ca.cal.leandrose.service;

import ca.cal.leandrose.model.*;
import ca.cal.leandrose.repository.*;
import ca.cal.leandrose.service.dto.CandidatureDto;
import ca.cal.leandrose.service.dto.EntenteStageDto;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EntenteStageService {

    private final EntenteStageRepository ententeRepository;
    private final CandidatureRepository candidatureRepository;
    private final PDFGeneratorService pdfGeneratorService;

    public List<CandidatureDto> getCandidaturesAcceptees() {
        List<Candidature> candidatures = candidatureRepository
                .findByStatus(Candidature.Status.ACCEPTED);

        return candidatures.stream()
                .filter(c -> !ententeRepository.existsByCandidatureId(c.getId()))
                .map(CandidatureDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public EntenteStageDto creerEntente(EntenteStageDto dto) {
        if (dto.getCandidatureId() == null) {
            throw new IllegalArgumentException("La candidature est obligatoire");
        }

        Candidature candidature = candidatureRepository.findById(dto.getCandidatureId())
                .orElseThrow(() -> new EntityNotFoundException("Candidature non trouvée"));

        if (candidature.getStatus() != Candidature.Status.ACCEPTED) {
            throw new IllegalStateException("La candidature doit avoir le statut ACCEPTED");
        }

        if (ententeRepository.existsByCandidatureId(candidature.getId())) {
            throw new IllegalStateException("Une entente existe déjà pour cette candidature");
        }

        validateEntente(dto);

        EntenteStage entente = EntenteStage.builder()
                .candidature(candidature)
                .missionsObjectifs(dto.getMissionsObjectifs())
                .statut(EntenteStage.StatutEntente.BROUILLON)
                .dateCreation(LocalDateTime.now())
                .build();

        entente = ententeRepository.save(entente);

        return EntenteStageDto.fromEntity(entente);
    }

    @Transactional
    public EntenteStageDto validerEtGenererEntente(Long ententeId) {
        EntenteStage entente = ententeRepository.findById(ententeId)
                .orElseThrow(() -> new EntityNotFoundException("Entente non trouvée"));

        validateChampsObligatoires(entente);

        String pdfPath = pdfGeneratorService.genererEntentePDF(entente);

        entente.setCheminDocumentPDF(pdfPath);
        entente.setStatut(EntenteStage.StatutEntente.EN_ATTENTE_SIGNATURE);
        entente.setDateModification(LocalDateTime.now());

        entente = ententeRepository.save(entente);

        return EntenteStageDto.fromEntity(entente);
    }

    @Transactional
    public EntenteStageDto modifierEntente(Long ententeId, EntenteStageDto dto) {
        EntenteStage entente = ententeRepository.findById(ententeId)
                .orElseThrow(() -> new EntityNotFoundException("Entente non trouvée"));

        if (entente.getStatut() != EntenteStage.StatutEntente.BROUILLON) {
            throw new IllegalStateException("Impossible de modifier une entente qui n'est pas en brouillon");
        }

        if (dto.getMissionsObjectifs() != null) entente.setMissionsObjectifs(dto.getMissionsObjectifs());

        entente.setDateModification(LocalDateTime.now());

        entente = ententeRepository.save(entente);

        return EntenteStageDto.fromEntity(entente);
    }

    public List<EntenteStageDto> getAllEntentes() {
        return ententeRepository.findAll().stream()
                .map(EntenteStageDto::fromEntity)
                .collect(Collectors.toList());
    }

    public EntenteStageDto getEntenteById(Long id) {
        EntenteStage entente = ententeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Entente non trouvée"));
        return EntenteStageDto.fromEntity(entente);
    }

    public byte[] telechargerPDF(Long ententeId) {
        EntenteStage entente = ententeRepository.findById(ententeId)
                .orElseThrow(() -> new EntityNotFoundException("Entente non trouvée"));

        if (entente.getCheminDocumentPDF() == null) {
            throw new IllegalStateException("Aucun PDF généré pour cette entente");
        }

        return pdfGeneratorService.lireFichierPDF(entente.getCheminDocumentPDF());
    }

    @Transactional
    public void supprimerEntente(Long ententeId) {
        EntenteStage entente = ententeRepository.findById(ententeId)
                .orElseThrow(() -> new EntityNotFoundException("Entente non trouvée"));

        if (entente.getStatut() != EntenteStage.StatutEntente.BROUILLON) {
            throw new IllegalStateException("Impossible de supprimer une entente qui n'est pas en brouillon");
        }

        ententeRepository.delete(entente);
    }

    private void validateEntente(EntenteStageDto dto) {
        if (dto.getDateDebut() == null) {
            throw new IllegalArgumentException("La date de début est obligatoire");
        }
        if (dto.getDuree() < 0) {
            throw new IllegalArgumentException("La durée est obligatoire");
        }

        if (dto.getMissionsObjectifs() == null || dto.getMissionsObjectifs().isBlank()) {
            throw new IllegalArgumentException("Les missions et objectifs sont obligatoires");
        }
        if (dto.getRemuneration() != null && dto.getRemuneration() < 0) {
            throw new IllegalArgumentException("La rémunération doit être positive ou zéro");
        }
    }

    private void validateChampsObligatoires(EntenteStage entente) {
        if (entente.getStartDate() == null) {
            throw new IllegalArgumentException("La date de début est obligatoire");
        }
        if (entente.getDurationInWeeks() < 1) {
            throw new IllegalArgumentException("La durée est obligatoire");
        }
        if (entente.getMissionsObjectifs() == null || entente.getMissionsObjectifs().isBlank()) {
            throw new IllegalArgumentException("Les missions et objectifs sont obligatoires");
        }
    }
}