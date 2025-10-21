package ca.cal.leandrose.service;


import ca.cal.leandrose.repository.CandidatureRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EntenteStageService {

    private final EntanteStageRepository entanteRepository;
    private final CandidatureRepository candidatureRepository;
    private final PDFGeneratorService pdfGeneratorService;
    private final NotificationService notificationService;

    /**
     * Récupérer les candidatures acceptées (ACCEPTED)
     * Ce sont les seules éligibles pour créer une entente
     */
    public List<CandidatureDTO> getCandidaturesAcceptees() {
        List<Candidature> candidatures = candidatureRepository
                .findByStatus(Candidature.Status.ACCEPTED);

        return candidatures.stream()
                .filter(c -> !entanteRepository.existsByCandidatureId(c.getId())) // Exclure celles qui ont déjà une entente
                .map(this::mapCandidatureToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Créer une nouvelle entente (brouillon)
     */
    @Transactional
    public EntanteStageDTO creerEntante(EntanteStageCreateDTO dto) {
        // Validation : la candidature doit être ACCEPTED
        Candidature candidature = candidatureRepository.findById(dto.getCandidatureId())
                .orElseThrow(() -> new EntityNotFoundException("Candidature non trouvée"));

        if (candidature.getStatus() != Candidature.Status.ACCEPTED) {
            throw new IllegalStateException("La candidature doit avoir le statut ACCEPTED");
        }

        // Vérifier qu'une entente n'existe pas déjà
        if (entanteRepository.existsByCandidatureId(candidature.getId())) {
            throw new IllegalStateException("Une entente existe déjà pour cette candidature");
        }

        // Créer l'entente
        EntanteStage entante = EntanteStage.builder()
                .candidature(candidature)
                .nomEntreprise(dto.getNomEntreprise())
                .contactEntreprise(dto.getContactEntreprise())
                .titreStage(dto.getTitreStage())
                .dateDebut(dto.getDateDebut())
                .dateFin(dto.getDateFin())
                .duree(dto.getDuree())
                .horaires(dto.getHoraires())
                .lieu(dto.getLieu())
                .modalitesTeletravail(dto.getModalitesTeletravail())
                .remuneration(dto.getRemuneration())
                .missionsObjectifs(dto.getMissionsObjectifs())
                .statut(EntanteStage.StatutEntante.BROUILLON)
                .dateCreation(LocalDateTime.now())
                .build();

        entante = entanteRepository.save(entante);

        return mapEntityToDTO(entante);
    }

    /**
     * Valider et générer le PDF de l'entente
     */
    @Transactional
    public EntanteStageDTO validerEtGenererEntante(Long entanteId) {
        EntanteStage entante = entanteRepository.findById(entanteId)
                .orElseThrow(() -> new EntityNotFoundException("Entente non trouvée"));

        // Validation des champs obligatoires
        validateChampsObligatoires(entante);

        // Générer le PDF
        String pdfPath = pdfGeneratorService.genererEntantePDF(entante);

        entante.setCheminDocumentPDF(pdfPath);
        entante.setStatut(EntanteStage.StatutEntante.EN_ATTENTE_SIGNATURE);
        entante.setDateModification(LocalDateTime.now());

        entante = entanteRepository.save(entante);

        // Notifier les parties
        notificationService.notifierEntanteCreee(entante);

        return mapEntityToDTO(entante);
    }

    /**
     * Modifier une entente (uniquement si statut = BROUILLON)
     */
    @Transactional
    public EntanteStageDTO modifierEntante(Long entanteId, EntanteStageUpdateDTO dto) {
        EntanteStage entante = entanteRepository.findById(entanteId)
                .orElseThrow(() -> new EntityNotFoundException("Entente non trouvée"));

        if (entante.getStatut() != EntanteStage.StatutEntante.BROUILLON) {
            throw new IllegalStateException("Impossible de modifier une entente qui n'est pas en brouillon");
        }

        // Mettre à jour les champs
        if (dto.getNomEntreprise() != null) entante.setNomEntreprise(dto.getNomEntreprise());
        if (dto.getContactEntreprise() != null) entante.setContactEntreprise(dto.getContactEntreprise());
        if (dto.getTitreStage() != null) entante.setTitreStage(dto.getTitreStage());
        if (dto.getDateDebut() != null) entante.setDateDebut(dto.getDateDebut());
        if (dto.getDateFin() != null) entante.setDateFin(dto.getDateFin());
        if (dto.getDuree() != null) entante.setDuree(dto.getDuree());
        if (dto.getHoraires() != null) entante.setHoraires(dto.getHoraires());
        if (dto.getLieu() != null) entante.setLieu(dto.getLieu());
        if (dto.getModalitesTeletravail() != null) entante.setModalitesTeletravail(dto.getModalitesTeletravail());
        if (dto.getRemuneration() != null) entante.setRemuneration(dto.getRemuneration());
        if (dto.getMissionsObjectifs() != null) entante.setMissionsObjectifs(dto.getMissionsObjectifs());

        entante.setDateModification(LocalDateTime.now());

        entante = entanteRepository.save(entante);

        return mapEntityToDTO(entante);
    }

    /**
     * Récupérer toutes les ententes
     */
    public List<EntanteStageDTO> getAllEntantes() {
        return entanteRepository.findAll().stream()
                .map(this::mapEntityToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Récupérer une entente par ID
     */
    public EntanteStageDTO getEntanteById(Long id) {
        EntanteStage entante = entanteRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Entente non trouvée"));
        return mapEntityToDTO(entante);
    }

    /**
     * Télécharger le PDF d'une entente
     */
    public byte[] telechargerPDF(Long entanteId) {
        EntanteStage entante = entanteRepository.findById(entanteId)
                .orElseThrow(() -> new EntityNotFoundException("Entente non trouvée"));

        if (entante.getCheminDocumentPDF() == null) {
            throw new IllegalStateException("Aucun PDF généré pour cette entente");
        }

        return pdfGeneratorService.lireFichierPDF(entante.getCheminDocumentPDF());
    }

    /**
     * Supprimer une entente (seulement si BROUILLON)
     */
    @Transactional
    public void supprimerEntante(Long entanteId) {
        EntanteStage entante = entanteRepository.findById(entanteId)
                .orElseThrow(() -> new EntityNotFoundException("Entente non trouvée"));

        if (entante.getStatut() != EntanteStage.StatutEntante.BROUILLON) {
            throw new IllegalStateException("Impossible de supprimer une entente qui n'est pas en brouillon");
        }

        entanteRepository.delete(entante);
    }

    // ========== MÉTHODES PRIVÉES ==========

    private void validateChampsObligatoires(EntanteStage entante) {
        if (entante.getNomEntreprise() == null || entante.getNomEntreprise().isBlank()) {
            throw new IllegalArgumentException("Le nom de l'entreprise est obligatoire");
        }
        if (entante.getContactEntreprise() == null || entante.getContactEntreprise().isBlank()) {
            throw new IllegalArgumentException("Le contact de l'entreprise est obligatoire");
        }
        if (entante.getTitreStage() == null || entante.getTitreStage().isBlank()) {
            throw new IllegalArgumentException("Le titre du stage est obligatoire");
        }
        if (entante.getDateDebut() == null) {
            throw new IllegalArgumentException("La date de début est obligatoire");
        }
        if (entante.getDateFin() == null) {
            throw new IllegalArgumentException("La date de fin est obligatoire");
        }
        if (entante.getDuree() == null || entante.getDuree().isBlank()) {
            throw new IllegalArgumentException("La durée est obligatoire");
        }
        if (entante.getHoraires() == null || entante.getHoraires().isBlank()) {
            throw new IllegalArgumentException("Les horaires sont obligatoires");
        }
        if (entante.getMissionsObjectifs() == null || entante.getMissionsObjectifs().isBlank()) {
            throw new IllegalArgumentException("Les missions et objectifs sont obligatoires");
        }
    }

    private EntanteStageDTO mapEntityToDTO(EntanteStage entante) {
        return EntanteStageDTO.builder()
                .id(entante.getId())
                .candidatureId(entante.getCandidature().getId())
                .studentId(entante.getStudent().getId())
                .studentNom(entante.getStudent().getLastName())
                .studentPrenom(entante.getStudent().getFirstName())
                .internshipOfferId(entante.getInternshipOffer().getId())
                .nomEntreprise(entante.getNomEntreprise())
                .contactEntreprise(entante.getContactEntreprise())
                .titreStage(entante.getTitreStage())
                .dateDebut(entante.getDateDebut())
                .dateFin(entante.getDateFin())
                .duree(entante.getDuree())
                .horaires(entante.getHoraires())
                .lieu(entante.getLieu())
                .modalitesTeletravail(entante.getModalitesTeletravail())
                .remuneration(entante.getRemuneration())
                .missionsObjectifs(entante.getMissionsObjectifs())
                .statut(entante.getStatut())
                .dateCreation(entante.getDateCreation())
                .dateModification(entante.getDateModification())
                .dateSignatureEtudiant(entante.getDateSignatureEtudiant())
                .dateSignatureEmployeur(entante.getDateSignatureEmployeur())
                .dateSignatureGestionnaire(entante.getDateSignatureGestionnaire())
                .build();
    }

    private CandidatureDTO mapCandidatureToDTO(Candidature candidature) {
        return CandidatureDTO.builder()
                .id(candidature.getId())
                .studentId(candidature.getStudent().getId())
                .studentNom(candidature.getStudent().getLastName())
                .studentPrenom(candidature.getStudent().getFirstName())
                .internshipOfferId(candidature.getInternshipOffer().getId())
                .internshipOfferTitre(candidature.getInternshipOffer().getTitle())
                .status(candidature.getStatus())
                .applicationDate(candidature.getApplicationDate())
                .build();
    }
}