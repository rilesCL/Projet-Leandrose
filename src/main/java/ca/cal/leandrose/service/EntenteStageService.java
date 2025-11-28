package ca.cal.leandrose.service;

import ca.cal.leandrose.model.*;
import ca.cal.leandrose.repository.*;
import ca.cal.leandrose.service.dto.CandidatureDto;
import ca.cal.leandrose.service.dto.EntenteStageDto;
import ca.cal.leandrose.service.dto.ProfStudentItemDto;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class EntenteStageService {

  private final EntenteStageRepository ententeRepository;
  private final CandidatureRepository candidatureRepository;
  private final PDFGeneratorService pdfGeneratorService;
  private final ProfRepository profRepository;
  private final EvaluationStagiaireRepository evaluationStagiaireRepository;
  private final GestionnaireRepository gestionnaireRepository;

  public List<CandidatureDto> getCandidaturesAcceptees() {
    List<Candidature> candidatures =
        candidatureRepository.findByStatus(Candidature.Status.ACCEPTED);

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

    Candidature candidature =
        candidatureRepository
            .findById(dto.getCandidatureId())
            .orElseThrow(() -> new EntityNotFoundException("Candidature non trouvée"));

    if (candidature.getStatus() != Candidature.Status.ACCEPTED) {
      throw new IllegalStateException("La candidature doit avoir le statut ACCEPTED");
    }

    if (ententeRepository.existsByCandidatureId(candidature.getId())) {
      throw new IllegalStateException("Une entente existe déjà pour cette candidature");
    }

    validateEntente(dto);

    EntenteStage entente =
        EntenteStage.builder()
            .candidature(candidature)
            .missionsObjectifs(dto.getMissionsObjectifs())
            .statut(EntenteStage.StatutEntente.BROUILLON)
            .dateCreation(LocalDateTime.now())
            .build();

    entente = ententeRepository.save(entente);

    try {
      String pdfPath = pdfGeneratorService.genererEntentePDF(entente);

      entente.setCheminDocumentPDF(pdfPath);
      entente.setStatut(EntenteStage.StatutEntente.EN_ATTENTE_SIGNATURE);
      entente.setDateModification(LocalDateTime.now());

      entente = ententeRepository.save(entente);

    } catch (Exception e) {
      throw new RuntimeException("Impossible de générer le PDF de l'entente: " + e.getMessage(), e);
    }

    return EntenteStageDto.fromEntity(entente);
  }

  @Transactional
  public EntenteStageDto validerEtGenererEntente(Long ententeId) {

    EntenteStage entente =
        ententeRepository
            .findById(ententeId)
            .orElseThrow(() -> new EntityNotFoundException("Entente non trouvée"));

    if (entente.getStatut() != EntenteStage.StatutEntente.BROUILLON) {
      throw new IllegalStateException("Seules les ententes en brouillon peuvent être validées");
    }

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

    EntenteStage entente =
        ententeRepository
            .findById(ententeId)
            .orElseThrow(() -> new EntityNotFoundException("Entente non trouvée"));

    if (entente.getStatut() != EntenteStage.StatutEntente.BROUILLON) {
      throw new IllegalStateException(
          "Impossible de modifier une entente qui n'est pas en brouillon");
    }

    if (dto.getMissionsObjectifs() != null && !dto.getMissionsObjectifs().isBlank()) {
      entente.setMissionsObjectifs(dto.getMissionsObjectifs());
    }

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
    EntenteStage entente =
        ententeRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Entente non trouvée"));
    return EntenteStageDto.fromEntity(entente);
  }

  public byte[] telechargerPDF(Long ententeId) {

    EntenteStage entente =
        ententeRepository
            .findById(ententeId)
            .orElseThrow(() -> new EntityNotFoundException("Entente non trouvée"));

    if (entente.getCheminDocumentPDF() == null || entente.getCheminDocumentPDF().isBlank()) {
      throw new IllegalStateException(
          "Aucun PDF généré pour cette entente. Veuillez d'abord valider l'entente.");
    }

    return pdfGeneratorService.lireFichierPDF(entente.getCheminDocumentPDF());
  }

  @Transactional
  public void supprimerEntente(Long ententeId) {

    EntenteStage entente =
        ententeRepository
            .findById(ententeId)
            .orElseThrow(() -> new EntityNotFoundException("Entente non trouvée"));

    if (entente.getStatut() != EntenteStage.StatutEntente.BROUILLON) {
      throw new IllegalStateException(
          "Impossible de supprimer une entente qui n'est pas en brouillon");
    }

    if (entente.getCheminDocumentPDF() != null) {
      pdfGeneratorService.supprimerFichierPDF(entente.getCheminDocumentPDF());
    }

    ententeRepository.delete(entente);
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
    EntenteStage entente =
        ententeRepository
            .findById(ententeId)
            .orElseThrow(() -> new EntityNotFoundException("Entente non trouvée"));

    if (entente.getStatut() != EntenteStage.StatutEntente.EN_ATTENTE_SIGNATURE) {
      throw new IllegalStateException("L'entente doit être en attente de signature.");
    }

    Long employeurEntenteId = entente.getCandidature().getInternshipOffer().getEmployeurId();
    if (!employeurEntenteId.equals(employeurId)) {
      throw new IllegalArgumentException(
          "Cet employeur n'est pas autorisé à signer cette entente.");
    }

    if (entente.getDateSignatureEmployeur() != null) {
      throw new IllegalStateException("L'employeur a déjà signé cette entente.");
    }

    entente.setDateSignatureEmployeur(LocalDateTime.now());
    entente.setDateModification(LocalDateTime.now());

    if (entente.getDateSignatureEtudiant() != null
        && entente.getDateSignatureGestionnaire() != null) {
      entente.setStatut(EntenteStage.StatutEntente.VALIDEE);
    }

    EntenteStage saved = ententeRepository.save(entente);
    return EntenteStageDto.fromEntity(saved);
  }

  @Transactional
  public EntenteStageDto signerParEtudiant(Long ententeId, Long studentId) {
    EntenteStage entente =
        ententeRepository
            .findById(ententeId)
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

    if (entente.getDateSignatureEmployeur() != null
        && entente.getDateSignatureGestionnaire() != null) {
      entente.setStatut(EntenteStage.StatutEntente.VALIDEE);
    }

    EntenteStage saved = ententeRepository.save(entente);
    return EntenteStageDto.fromEntity(saved);
  }

  @Transactional
  public EntenteStageDto signerParGestionnaire(Long ententeId, Long gestionnaireId) {
    if (ententeId == null || ententeId <= 0) {
      throw new IllegalArgumentException("L'id de l'entente doit être valide");
    }
    if (gestionnaireId == null || gestionnaireId <= 0) {
      throw new IllegalArgumentException("L'id du gestionnaire doit être valide");
    }

    EntenteStage entente =
        ententeRepository
            .findById(ententeId)
            .orElseThrow(() -> new EntityNotFoundException("Entente non trouvée"));

    Gestionnaire gestionnaire =
        gestionnaireRepository
            .findById(gestionnaireId)
            .orElseThrow(() -> new EntityNotFoundException("Gestionnaire non trouvé"));

    if (entente.getStatut() != EntenteStage.StatutEntente.EN_ATTENTE_SIGNATURE) {
      throw new IllegalStateException("L'entente doit être en attente de signature.");
    }

    if (entente.getDateSignatureGestionnaire() != null) {
      throw new IllegalStateException("Le gestionnaire a déjà signé cette entente.");
    }

    entente.setGestionnaire(gestionnaire);
    entente.setDateSignatureGestionnaire(LocalDateTime.now());
    entente.setDateModification(LocalDateTime.now());

    if (entente.getDateSignatureEtudiant() != null && entente.getDateSignatureEmployeur() != null) {
      entente.setStatut(EntenteStage.StatutEntente.VALIDEE);
    }

    EntenteStage saved = ententeRepository.save(entente);
    return EntenteStageDto.fromEntity(saved);
  }

  public List<EntenteStageDto> getEntentesByEmployeurId(Long employeurId) {
    if (employeurId == null) {
      return List.of();
    }

    return ententeRepository.findAll().stream()
        .filter(
            entente -> {
              Long employeurEntenteId =
                  entente.getCandidature().getInternshipOffer().getEmployeurId();
              return employeurEntenteId != null && employeurEntenteId.equals(employeurId);
            })
        .map(EntenteStageDto::fromEntity)
        .collect(Collectors.toList());
  }

  public List<EntenteStageDto> getEntentesByStudentId(Long studentId) {
    if (studentId == null) {
      return List.of();
    }

    return ententeRepository.findAll().stream()
        .filter(
            entente -> {
              Long studentEntenteId = entente.getCandidature().getStudent().getId();
              return studentEntenteId != null && studentEntenteId.equals(studentId);
            })
        .map(EntenteStageDto::fromEntity)
        .collect(Collectors.toList());
  }

  @Transactional
  public EntenteStageDto attribuerProf(Long ententeId, Long profId) {
    if (ententeId == null || ententeId <= 0) {
      throw new IllegalArgumentException("L'id de l'entente doit être valide");
    }
    if (profId == null || profId <= 0) {
      throw new IllegalArgumentException("L'id du professeur doit être valide");
    }

    EntenteStage entente =
        ententeRepository
            .findById(ententeId)
            .orElseThrow(() -> new EntityNotFoundException("Entente non trouvée"));

    if (entente.getStatut() != EntenteStage.StatutEntente.VALIDEE) {
      throw new IllegalStateException(
          "Impossible d'attribuer un professeur. L'entente doit être validée (toutes les signatures présentes)");
    }

    if (entente.getDateSignatureEtudiant() == null
        || entente.getDateSignatureEmployeur() == null
        || entente.getDateSignatureGestionnaire() == null) {
      throw new IllegalStateException(
          "Impossible d'attribuer un professeur. Toutes les signatures doivent être présentes");
    }

    Prof prof =
        profRepository
            .findById(profId)
            .orElseThrow(() -> new EntityNotFoundException("Professeur non trouvé"));

    entente.setProf(prof);
    entente.setDateModification(LocalDateTime.now());

    EntenteStage saved = ententeRepository.save(entente);
    return EntenteStageDto.fromEntity(saved);
  }

  @Transactional(readOnly = true)
  public List<ProfStudentItemDto> getEtudiantsPourProf(
      Long profId,
      String nameFilter,
      String companyFilter,
      LocalDate dateFrom,
      LocalDate dateTo,
      String evaluationFilter,
      String sortBy,
      Boolean asc) {
    if (profId == null || profId <= 0) return List.of();

    List<EntenteStage> ententes = ententeRepository.findAllByProf_Id(profId);
    LocalDate today = LocalDate.now();

      return ententes.stream()
          .map(
              e -> {
                var cand = e.getCandidature();
                var student = (cand != null) ? cand.getStudent() : null;
                var offer = (cand != null) ? cand.getInternshipOffer() : null;

                LocalDate start =
                    (e.getStartDate() != null)
                        ? e.getStartDate()
                        : (offer != null ? offer.getStartDate() : null);

                int weeks =
                    (e.getDurationInWeeks() > 0)
                        ? e.getDurationInWeeks()
                        : (offer != null ? offer.getDurationInWeeks() : 0);

                LocalDate end =
                        start != null && weeks > 0 ? start.plusWeeks(weeks) : null;

                String stageStatus;
                if (start == null || end == null) {
                  stageStatus = "EN_COURS";
                } else {
                  stageStatus = (today.isAfter(end)) ? "TERMINE" : "EN_COURS";
                }

                String evaluationStatus = getEvaluationStatus(e, student, offer);

                return ProfStudentItemDto.builder()
                    .ententeId(e.getId())
                    .studentId(student != null ? student.getId() : null)
                    .studentFirstName(student != null ? student.getFirstName() : null)
                    .studentLastName(student != null ? student.getLastName() : null)
                    .companyName(offer != null ? offer.getCompanyName() : null)
                    .offerTitle(offer != null ? offer.getDescription() : null)
                    .startDate(start)
                    .endDate(end)
                    .stageStatus(stageStatus)
                    .evaluationStatus(evaluationStatus)
                    .build();
              })
          .filter(
              i -> {
                if (nameFilter != null && !nameFilter.isBlank()) {
                  String q = nameFilter.toLowerCase();
                  String full =
                      (Objects.toString(i.getStudentFirstName(), "")
                              + " "
                              + Objects.toString(i.getStudentLastName(), ""))
                          .toLowerCase();
                  return full.contains(q);
                }
                return true;
              })
          .filter(
              i -> {
                if (companyFilter != null && !companyFilter.isBlank()) {
                  String q = companyFilter.toLowerCase();
                  return i.getCompanyName() != null
                      && i.getCompanyName().toLowerCase().contains(q);
                }
                return true;
              })
          .filter(
              i -> {
                if (dateFrom != null) {
                  return i.getEndDate() == null || !i.getEndDate().isBefore(dateFrom);
                }
                return true;
              })
          .filter(
              i -> {
                if (dateTo != null) {
                  return i.getStartDate() == null || !i.getStartDate().isAfter(dateTo);
                }
                return true;
              })
          .filter(
              i -> {
                if (evaluationFilter != null && !evaluationFilter.isBlank()) {
                  return evaluationFilter.equalsIgnoreCase(i.getEvaluationStatus());
                }
                return true;
              })
          .sorted(getComparator(sortBy, Boolean.TRUE.equals(asc)))
          .collect(Collectors.toList());
  }

  private Comparator<ProfStudentItemDto> getComparator(String sortBy, boolean asc) {
    Comparator<ProfStudentItemDto> comp;
    if ("date".equalsIgnoreCase(sortBy)) {
      comp =
          Comparator.comparing(
              ProfStudentItemDto::getStartDate, Comparator.nullsLast(Comparator.naturalOrder()));
    } else if ("company".equalsIgnoreCase(sortBy)) {
      comp =
          Comparator.comparing(
              ProfStudentItemDto::getCompanyName,
              Comparator.nullsLast(String::compareToIgnoreCase));
    } else {
      comp =
          Comparator.comparing(
                  (ProfStudentItemDto i) -> Objects.toString(i.getStudentLastName(), ""),
                  String::compareToIgnoreCase)
              .thenComparing(
                  i -> Objects.toString(i.getStudentFirstName(), ""), String::compareToIgnoreCase);
    }
    return asc ? comp : comp.reversed();
  }

  public boolean isTeacherAssigned(Long studentId, Long offerId) {
    Optional<EntenteStage> ententeStage =
        ententeRepository.findByCandidature_Student_IdAndCandidature_InternshipOffer_Id(
            studentId, offerId);
    return ententeStage.isPresent() && ententeStage.get().getProf() != null;
  }

  private String getEvaluationStatus(EntenteStage ententeState, Student student, InternshipOffer offer){
      if(student == null || offer == null)
          return EvaluationStatus.A_FAIRE.name();

      Optional<EvaluationStagiaire> evaluationOpt = evaluationStagiaireRepository
              .findByStudentIdAndInternshipOfferId(student.getId(), offer.getId());

      if(evaluationOpt.isPresent()) {
          EvaluationStagiaire evaluation = evaluationOpt.get();

          if(evaluation.isSubmittedByEmployer() && evaluation.isSubmittedByProfessor()){
              return EvaluationStatus.TERMINEE.name();
          }
          else if(evaluation.isSubmittedByEmployer() || evaluation.isSubmittedByProfessor()){
              return EvaluationStatus.EN_COURS.name();
          }

      }
      return EvaluationStatus.A_FAIRE.name();
  }
}
