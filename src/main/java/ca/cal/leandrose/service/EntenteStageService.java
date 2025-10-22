package ca.cal.leandrose.service;

import ca.cal.leandrose.model.*;
import ca.cal.leandrose.repository.*;
import ca.cal.leandrose.service.dto.CandidatureDto;
import ca.cal.leandrose.service.dto.EntenteStageDto;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
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

    /**
     * Crée une entente ET génère immédiatement le PDF
     * Le PDF doit être disponible pour que l'étudiant et l'employeur puissent le consulter avant signature
     */
    @Transactional
    public EntenteStageDto creerEntente(EntenteStageDto dto) {
        log.info("🔵 Début création entente pour candidature {}", dto.getCandidatureId());

        // 🔍 DEBUG: Afficher le contenu du DTO reçu
        log.info("📋 DTO reçu - candidatureId: {}", dto.getCandidatureId());
        log.info("📋 DTO reçu - dateDebut: {}", dto.getDateDebut());
        log.info("📋 DTO reçu - duree: {}", dto.getDuree());
        log.info("📋 DTO reçu - lieu: {}", dto.getLieu());
        log.info("📋 DTO reçu - remuneration: {}", dto.getRemuneration());
        log.info("📋 DTO reçu - missionsObjectifs: {}", dto.getMissionsObjectifs());

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

        // ÉTAPE 1 : Créer l'entente en BROUILLON
        EntenteStage entente = EntenteStage.builder()
                .candidature(candidature)
                .missionsObjectifs(dto.getMissionsObjectifs())
                .statut(EntenteStage.StatutEntente.BROUILLON)
                .dateCreation(LocalDateTime.now())
                .build();

        entente = ententeRepository.save(entente);
        log.info("✅ Entente créée avec ID: {}", entente.getId());

        // ÉTAPE 2 : Générer le PDF immédiatement
        try {
            log.info("📄 Génération du PDF pour entente {}", entente.getId());
            String pdfPath = pdfGeneratorService.genererEntentePDF(entente);
            log.info("✅ PDF généré avec succès: {}", pdfPath);

            // ÉTAPE 3 : Sauvegarder le chemin et changer le statut
            entente.setCheminDocumentPDF(pdfPath);
            entente.setStatut(EntenteStage.StatutEntente.EN_ATTENTE_SIGNATURE);
            entente.setDateModification(LocalDateTime.now());

            entente = ententeRepository.save(entente);
            log.info("✅ Chemin PDF sauvegardé en BD: {}", entente.getCheminDocumentPDF());
            log.info("✅ Statut changé en: {}", entente.getStatut());

        } catch (Exception e) {
            log.error("❌ ERREUR lors de la génération du PDF pour entente {}", entente.getId(), e);
            throw new RuntimeException("Impossible de générer le PDF de l'entente: " + e.getMessage(), e);
        }

        return EntenteStageDto.fromEntity(entente);
    }

    /**
     * Valide une entente et génère le PDF
     * Cette méthode peut être utilisée pour régénérer un PDF si nécessaire
     */
    @Transactional
    public EntenteStageDto validerEtGenererEntente(Long ententeId) {
        log.info("🔵 Validation et génération PDF pour entente {}", ententeId);

        EntenteStage entente = ententeRepository.findById(ententeId)
                .orElseThrow(() -> new EntityNotFoundException("Entente non trouvée"));

        if (entente.getStatut() != EntenteStage.StatutEntente.BROUILLON) {
            throw new IllegalStateException("Seules les ententes en brouillon peuvent être validées");
        }

        validateChampsObligatoires(entente);

        String pdfPath = pdfGeneratorService.genererEntentePDF(entente);
        log.info("✅ PDF généré: {}", pdfPath);

        entente.setCheminDocumentPDF(pdfPath);
        entente.setStatut(EntenteStage.StatutEntente.EN_ATTENTE_SIGNATURE);
        entente.setDateModification(LocalDateTime.now());

        entente = ententeRepository.save(entente);
        log.info("✅ Entente validée - Chemin: {}", entente.getCheminDocumentPDF());

        return EntenteStageDto.fromEntity(entente);
    }

    @Transactional
    public EntenteStageDto modifierEntente(Long ententeId, EntenteStageDto dto) {
        log.info("🔵 Modification entente {}", ententeId);

        EntenteStage entente = ententeRepository.findById(ententeId)
                .orElseThrow(() -> new EntityNotFoundException("Entente non trouvée"));

        if (entente.getStatut() != EntenteStage.StatutEntente.BROUILLON) {
            throw new IllegalStateException("Impossible de modifier une entente qui n'est pas en brouillon");
        }

        if (dto.getMissionsObjectifs() != null && !dto.getMissionsObjectifs().isBlank()) {
            entente.setMissionsObjectifs(dto.getMissionsObjectifs());
        }

        entente.setDateModification(LocalDateTime.now());
        entente = ententeRepository.save(entente);

        log.info("✅ Entente modifiée: {}", entente.getId());

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
        log.info("📥 Téléchargement PDF entente {}", ententeId);

        EntenteStage entente = ententeRepository.findById(ententeId)
                .orElseThrow(() -> new EntityNotFoundException("Entente non trouvée"));

        if (entente.getCheminDocumentPDF() == null || entente.getCheminDocumentPDF().isBlank()) {
            log.error("❌ Aucun PDF pour entente {}", ententeId);
            throw new IllegalStateException("Aucun PDF généré pour cette entente. Veuillez d'abord valider l'entente.");
        }

        log.info("✅ Lecture du PDF: {}", entente.getCheminDocumentPDF());
        return pdfGeneratorService.lireFichierPDF(entente.getCheminDocumentPDF());
    }

    @Transactional
    public void supprimerEntente(Long ententeId) {
        log.info("🗑️ Suppression entente {}", ententeId);

        EntenteStage entente = ententeRepository.findById(ententeId)
                .orElseThrow(() -> new EntityNotFoundException("Entente non trouvée"));

        if (entente.getStatut() != EntenteStage.StatutEntente.BROUILLON) {
            throw new IllegalStateException("Impossible de supprimer une entente qui n'est pas en brouillon");
        }

        // Supprimer le fichier PDF s'il existe
        if (entente.getCheminDocumentPDF() != null) {
            pdfGeneratorService.supprimerFichierPDF(entente.getCheminDocumentPDF());
        }

        ententeRepository.delete(entente);
        log.info("✅ Entente supprimée: {}", ententeId);
    }

    private void validateEntente(EntenteStageDto dto) {
        if (dto.getDateDebut() == null) {
            throw new IllegalArgumentException("La date de début est obligatoire");
        }
        if (dto.getDuree() < 1) {
            throw new IllegalArgumentException("La durée doit être d'au moins 1 semaine");
        }
        if (dto.getMissionsObjectifs() == null || dto.getMissionsObjectifs().isBlank()) {
            throw new IllegalArgumentException("Les missions et objectifs sont obligatoires");
        }
        if (dto.getRemuneration() != null && dto.getRemuneration() < 0) {
            throw new IllegalArgumentException("La rémunération ne peut pas être négative");
        }
    }

    private void validateChampsObligatoires(EntenteStage entente) {
        if (entente.getStartDate() == null) {
            throw new IllegalArgumentException("La date de début est obligatoire");
        }
        if (entente.getDurationInWeeks() < 1) {
            throw new IllegalArgumentException("La durée doit être d'au moins 1 semaine");
        }
        if (entente.getMissionsObjectifs() == null || entente.getMissionsObjectifs().isBlank()) {
            throw new IllegalArgumentException("Les missions et objectifs sont obligatoires");
        }
        if (entente.getAddress() == null || entente.getAddress().isBlank()) {
            throw new IllegalArgumentException("L'adresse du stage est obligatoire");
        }
    }
    @Transactional
    public EntenteStageDto signerParEmployeur(Long ententeId, Long employeurId) {
        EntenteStage entente = ententeRepository.findById(ententeId)
                .orElseThrow(() -> new EntityNotFoundException("Entente non trouvée"));

        if (entente.getStatut() != EntenteStage.StatutEntente.EN_ATTENTE_SIGNATURE) {
            throw new IllegalStateException("L'entente doit être en attente de signature.");
        }

        Long employeurEntenteId = entente.getCandidature().getInternshipOffer().getEmployeurId();
        if (!employeurEntenteId.equals(employeurId)) {
            throw new IllegalArgumentException("Cet employeur n'est pas autorisé à signer cette entente.");
        }

        if (entente.getDateSignatureEmployeur() != null) {
            throw new IllegalStateException("L'employeur a déjà signé cette entente.");
        }

        entente.setDateSignatureEmployeur(LocalDateTime.now());
        entente.setDateModification(LocalDateTime.now());

        if (entente.getDateSignatureEtudiant() != null &&
                entente.getDateSignatureGestionnaire() != null) {
            entente.setStatut(EntenteStage.StatutEntente.VALIDEE);
        }

        EntenteStage saved = ententeRepository.save(entente);
        return EntenteStageDto.fromEntity(saved);
    }
    @Transactional
    public EntenteStageDto signerParEtudiant(Long ententeId, Long studentId) {
        EntenteStage entente = ententeRepository.findById(ententeId)
                .orElseThrow(() -> new EntityNotFoundException("Entente non trouvée"));

        if (entente.getStatut() != EntenteStage.StatutEntente.EN_ATTENTE_SIGNATURE) {
            throw new IllegalStateException("L'entente doit être en attente de signature.");
        }

        Long etudiantEntenteId = entente.getCandidature().getStudent().getId();
        if (!etudiantEntenteId.equals(studentId)) {
            throw new IllegalArgumentException("Cet étudiant n'est pas autorisé à signer cette entente.");
        }

        if (entente.getDateSignatureEtudiant() != null) {
            throw new IllegalStateException("L'étudiant a déjà signé cette entente.");
        }

        entente.setDateSignatureEtudiant(LocalDateTime.now());
        entente.setDateModification(LocalDateTime.now());

        if (entente.getDateSignatureEmployeur() != null &&
                entente.getDateSignatureGestionnaire() != null) {
            entente.setStatut(EntenteStage.StatutEntente.VALIDEE);
        }

        EntenteStage saved = ententeRepository.save(entente);
        return EntenteStageDto.fromEntity(saved);
    }

}