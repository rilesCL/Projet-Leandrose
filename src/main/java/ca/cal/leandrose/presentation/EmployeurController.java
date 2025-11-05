package ca.cal.leandrose.presentation;

import ca.cal.leandrose.model.SchoolTerm;
import ca.cal.leandrose.presentation.request.InternshipOfferRequest;
import ca.cal.leandrose.service.*;
import ca.cal.leandrose.service.dto.*;
import ca.cal.leandrose.service.dto.evaluation.*;
import ca.cal.leandrose.service.mapper.InternshipOfferMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@RequiredArgsConstructor
@RestController
@RequestMapping("/employeur")
public class EmployeurController {

    private final UserAppService userService;
    private final InternshipOfferService internshipOfferService;
    private final EmployeurService employeurService;
    private final CandidatureService candidatureService;
    private final ConvocationService convocationService;
    private final EntenteStageService ententeStageService;
    private final EvaluationStagiaireService evaluationStagiaireService;

  @GetMapping("/offers")
  public ResponseEntity<List<InternshipOfferDto>> getMyOffers(HttpServletRequest request) {
    UserDTO me = userService.getMe(request.getHeader("Authorization"));

    if (!me.getRole().name().equals("EMPLOYEUR")) {
      return ResponseEntity.status(403).build();
    }

    List<InternshipOfferDto> offers =
        internshipOfferService.getOffersByEmployeurId(me.getId()).stream()
            .map(InternshipOfferMapper::toDto)
            .toList();

    return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(offers);
  }

  @PostMapping(value = "/offers", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<InternshipOfferDto> uploadOffer(
      HttpServletRequest request,
      @RequestPart("offer") InternshipOfferRequest offerRequest,
      @RequestPart("pdfFile") MultipartFile pdfFile)
      throws IOException {
    UserDTO me = userService.getMe(request.getHeader("Authorization"));

    if (!me.getRole().name().equals("EMPLOYEUR")) {
      return ResponseEntity.status(403).build();
    }

    if (offerRequest.getDescription() == null || offerRequest.getDescription().trim().isEmpty()) {
      return ResponseEntity.badRequest().body(new InternshipOfferDto("La description est requise"));
    }

    if (offerRequest.getDescription().length() > 50) {
      return ResponseEntity.badRequest()
          .body(new InternshipOfferDto("La description ne doit pas dépasser 50 caractères"));
    }

    EmployeurDto employeurDto = employeurService.getEmployeurById(me.getId());

    InternshipOfferDto offerDto =
        internshipOfferService.createOfferDto(
            offerRequest.getDescription(),
            LocalDate.parse(offerRequest.getStartDate()),
            offerRequest.getDurationInWeeks(),
            offerRequest.getAddress(),
            offerRequest.getRemuneration(),
            employeurDto,
            new SchoolTerm(SchoolTerm.Season.WINTER, 2026),
            pdfFile);

    return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(offerDto);
  }

  @GetMapping("/offers/{offerId}/download")
  public ResponseEntity<Resource> downloadOffer(
      HttpServletRequest request, @PathVariable Long offerId) {
    UserDTO me = userService.getMe(request.getHeader("Authorization"));

    if (!me.getRole().name().equals("EMPLOYEUR")) {
      return ResponseEntity.status(403).build();
    }

    try {
      InternshipOfferDto offer = internshipOfferService.getOffer(offerId);

      if (!offer.getEmployeurDto().getId().equals(me.getId())) {
        return ResponseEntity.status(403).build();
      }

      Path filePath = Paths.get(offer.getPdfPath());
      Resource resource = new UrlResource(filePath.toUri());

      if (resource.exists() && resource.isReadable()) {
        String filename =
            "Offre_"
                + offer
                    .getDescription()
                    .substring(0, Math.min(30, offer.getDescription().length()))
                    .replaceAll("[^a-zA-Z0-9]", "_")
                + ".pdf";

        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_PDF)
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
            .body(resource);
      } else {
        return ResponseEntity.notFound().build();
      }
    } catch (RuntimeException | MalformedURLException e) {
      return ResponseEntity.notFound().build();
    }
  }

  @GetMapping("/offers/{offerId}/convocations")
  public ResponseEntity<List<ConvocationDto>> getConvocationsByOffer(
      HttpServletRequest request, @PathVariable Long offerId) {
    UserDTO me = userService.getMe(request.getHeader("Authorization"));

    if (!me.getRole().name().equals("EMPLOYEUR")) {
      return ResponseEntity.status(403).build();
    }

    try {
      InternshipOfferDto offer = internshipOfferService.getOffer(offerId);

      if (!offer.getEmployeurDto().getId().equals(me.getId())) {
        return ResponseEntity.status(403).build();
      }

      List<ConvocationDto> convocations =
          convocationService.getAllConvocationsByInterShipOfferId(offerId);

      return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(convocations);
    } catch (RuntimeException e) {
      return ResponseEntity.notFound().build();
    }
  }

  @PostMapping("/candidatures/{candidatureId}/convocations")
  public ResponseEntity<String> createConvocation(
      HttpServletRequest request,
      @PathVariable Long candidatureId,
      @RequestBody ConvocationDto convocationRequest) {
    UserDTO me = userService.getMe(request.getHeader("Authorization"));

    if (!me.getRole().name().equals("EMPLOYEUR")) {
      return ResponseEntity.status(403).build();
    }

    try {
      CandidatureDto candidatureDto = candidatureService.getCandidatureById(candidatureId);

      if (!candidatureDto.getEmployeurId().equals(me.getId())) {
        return ResponseEntity.status(403).build();
      }

      convocationService.addConvocation(
          candidatureId,
          convocationRequest.getConvocationDate(),
          convocationRequest.getLocation(),
          convocationRequest.getMessage());

      return ResponseEntity.ok().body("Convocation créée avec succès");

    } catch (IllegalArgumentException | IllegalStateException e) {
      return ResponseEntity.badRequest().body(e.getMessage());
    } catch (Exception e) {
      return ResponseEntity.status(500).body("Erreur lors de la création de la convocation");
    }
  }

  @GetMapping("/offers/{offerId}/candidatures")
  public ResponseEntity<List<CandidatureEmployeurDto>> getCandidaturesForOffer(
      HttpServletRequest request, @PathVariable Long offerId) {

    UserDTO me = userService.getMe(request.getHeader("Authorization"));

    if (!me.getRole().name().equals("EMPLOYEUR")) {
      return ResponseEntity.status(403).build();
    }

    InternshipOfferDto offer = internshipOfferService.getOffer(offerId);
    if (!offer.getEmployeurDto().getId().equals(me.getId())) {
      return ResponseEntity.status(403).build();
    }

    List<CandidatureEmployeurDto> candidatures = candidatureService.getCandidaturesByOffer(offerId);

    return ResponseEntity.ok(candidatures);
  }

  @GetMapping("/candidatures")
  public ResponseEntity<List<CandidatureEmployeurDto>> getAllMyCandidatures(
      HttpServletRequest request) {

    UserDTO me = userService.getMe(request.getHeader("Authorization"));

    if (!me.getRole().name().equals("EMPLOYEUR")) {
      return ResponseEntity.status(403).build();
    }

    List<CandidatureEmployeurDto> candidatures =
        candidatureService.getCandidaturesByEmployeur(me.getId());

    return ResponseEntity.ok(candidatures);
  }

  @GetMapping("/candidatures/{candidatureId}/cv")
  public ResponseEntity<Resource> downloadCandidateCv(
      HttpServletRequest request, @PathVariable Long candidatureId) {

    UserDTO me = userService.getMe(request.getHeader("Authorization"));

    if (!me.getRole().name().equals("EMPLOYEUR")) {
      return ResponseEntity.status(403).build();
    }

    try {
      CandidatureDto candidatureDto = candidatureService.getCandidatureById(candidatureId);

      if (!candidatureDto.getEmployeurId().equals(me.getId())) {
        return ResponseEntity.status(403).build();
      }

      CvDto cv = candidatureDto.getCv();
      Path filePath = Paths.get(cv.getPdfPath());

      Resource resource = new UrlResource(filePath.toUri());

      if (resource.exists() && resource.isReadable()) {
        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_PDF)
            .header(
                HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"CV_" + candidatureDto.getStudent().getName() + ".pdf\"")
            .body(resource);
      } else {
        return ResponseEntity.notFound().build();
      }
    } catch (Exception e) {
      return ResponseEntity.notFound().build();
    }
  }

  @PostMapping("/candidatures/{id}/accept")
  public ResponseEntity<?> acceptCandidature(
      HttpServletRequest request, @PathVariable("id") Long candidatureId) {
    UserDTO me = userService.getMe(request.getHeader("Authorization"));
    if (!me.getRole().name().equals("EMPLOYEUR")) {
      return ResponseEntity.status(403).build();
    }

    try {
      CandidatureDto candidatureDto = candidatureService.getCandidatureById(candidatureId);

      if (!candidatureDto.getEmployeurId().equals(me.getId())) {
        return ResponseEntity.status(403).build();
      }

      CandidatureDto updated = candidatureService.acceptByEmployeur(candidatureId);
      return ResponseEntity.ok(updated);
    } catch (IllegalStateException e) {
      return ResponseEntity.badRequest().body(e.getMessage());
    } catch (RuntimeException e) {
      return ResponseEntity.status(404).body("Candidature non trouvée");
    }
  }

  @PostMapping("/candidatures/{id}/reject")
  public ResponseEntity<?> rejectCandidature(
      HttpServletRequest request, @PathVariable("id") Long candidatureId) {
    UserDTO me = userService.getMe(request.getHeader("Authorization"));
    if (!me.getRole().name().equals("EMPLOYEUR")) {
      return ResponseEntity.status(403).build();
    }

    try {
      CandidatureDto candidatureDto = candidatureService.getCandidatureById(candidatureId);

      if (!candidatureDto.getEmployeurId().equals(me.getId())) {
        return ResponseEntity.status(403).build();
      }

      CandidatureDto updated = candidatureService.rejectByEmployeur(candidatureId);
      return ResponseEntity.ok(updated);
    } catch (IllegalStateException e) {
      return ResponseEntity.badRequest().body(e.getMessage());
    } catch (RuntimeException e) {
      return ResponseEntity.status(404).body("Candidature non trouvée");
    }
  }

  @PostMapping("/ententes/{ententeId}/signer")
  public ResponseEntity<EntenteStageDto> signerEntente(
      HttpServletRequest request, @PathVariable Long ententeId) {
    UserDTO me = userService.getMe(request.getHeader("Authorization"));

    if (!me.getRole().name().equals("EMPLOYEUR")) {
      return ResponseEntity.status(403).build();
    }

    try {
      EntenteStageDto result = ententeStageService.signerParEmployeur(ententeId, me.getId());
      return ResponseEntity.ok(result);
    } catch (jakarta.persistence.EntityNotFoundException e) {
      return ResponseEntity.status(404)
          .body(EntenteStageDto.withErrorMessage("Entente non trouvée"));
    } catch (IllegalArgumentException | IllegalStateException e) {
      return ResponseEntity.badRequest().body(EntenteStageDto.withErrorMessage(e.getMessage()));
    }
  }

  @GetMapping("/ententes")
  public ResponseEntity<List<EntenteStageDto>> getEntentesPourEmployeur(
      HttpServletRequest request) {
    UserDTO me = userService.getMe(request.getHeader("Authorization"));

    if (!me.getRole().name().equals("EMPLOYEUR")) {
      return ResponseEntity.status(403).build();
    }

    try {
      List<EntenteStageDto> allEntentes = ententeStageService.getAllEntentes();
      List<EntenteStageDto> employerEntentes =
          allEntentes.stream()
              .filter(
                  entente -> {
                    if (entente.getInternshipOffer() == null
                        || entente.getInternshipOffer().getEmployeurDto() == null) {
                      return false;
                    }
                    return me.getEmail()
                        .equals(entente.getInternshipOffer().getEmployeurDto().getEmail());
                  })
              .collect(Collectors.toList());
      return ResponseEntity.ok(employerEntentes);
    } catch (Exception e) {
      return ResponseEntity.status(500).build();
    }
  }

  @GetMapping("/ententes/{ententeId}/pdf")
  public ResponseEntity<Resource> getEntentePdf(
      HttpServletRequest request, @PathVariable Long ententeId) {
    UserDTO me = userService.getMe(request.getHeader("Authorization"));

    if (!me.getRole().name().equals("EMPLOYEUR")) {
      return ResponseEntity.status(403).build();
    }

    try {
      EntenteStageDto entente = ententeStageService.getEntenteById(ententeId);

      if (entente.getInternshipOffer() == null
          || entente.getInternshipOffer().getEmployeurDto() == null
          || !me.getEmail().equals(entente.getInternshipOffer().getEmployeurDto().getEmail())) {
        return ResponseEntity.status(403).build();
      }

      if (entente.getCheminDocumentPDF() == null || entente.getCheminDocumentPDF().isBlank()) {
        return ResponseEntity.status(404).build();
      }

      Path filePath = Paths.get(entente.getCheminDocumentPDF());
      Resource resource = new UrlResource(filePath.toUri());

      if (resource.exists() && resource.isReadable()) {
        String filename =
            "Entente_Stage_"
                + entente.getStudent().getFirstName()
                + "_"
                + entente.getStudent().getLastName()
                + ".pdf";

        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_PDF)
            .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
            .body(resource);
      } else {
        return ResponseEntity.notFound().build();
      }

    } catch (jakarta.persistence.EntityNotFoundException e) {
      return ResponseEntity.status(404).build();
    } catch (MalformedURLException e) {
      return ResponseEntity.status(500).build();
    } catch (Exception e) {
      return ResponseEntity.status(500).build();
    }
  }


    @PostMapping("/evaluations")
    public ResponseEntity<?> createEvaluation(
            HttpServletRequest request,
            @RequestBody CreateEvaluationRequest createRequest){
        UserDTO me = userService.getMe(request.getHeader("Authorization"));

        if(!me.getRole().name().equals("EMPLOYEUR")){
            return ResponseEntity.status(403).build();
        }

        try{
            boolean isEligible = evaluationStagiaireService.isEvaluationEligible(me.getId(),
                    createRequest.studentId(), createRequest.internshipOfferId());

            if (!isEligible){
                return ResponseEntity.badRequest().body(
                        new EvaluationResponsesDto(null, "Evaluation not allowed - agreement not validated or not found")
                );
            }
            EvaluationStagiaireDto response = evaluationStagiaireService.createEvaluation(
                    me.getId(), createRequest.studentId(), createRequest.internshipOfferId()
            );
            return ResponseEntity.ok(response);

        } catch(Exception e){
            return ResponseEntity.badRequest().body(new EvaluationResponsesDto(null, e.getMessage()));
        }

    }

    @PostMapping("/evaluations/{evaluationId}/generate-pdf")
    public ResponseEntity<?> generateEvaluationPdf(
            HttpServletRequest request,
            @PathVariable Long evaluationId,
            @RequestBody EvaluationFormData formData,
            @RequestHeader(value= "Accept-Language", defaultValue = "fr") String language) {

        UserDTO me = userService.getMe(request.getHeader("Authorization"));

        if (!me.getRole().name().equals("EMPLOYEUR")) {
            return ResponseEntity.status(403).build();
        }

        try {
            EvaluationStagiaireDto evaluation = evaluationStagiaireService.getEvaluationById(evaluationId);
            if (!evaluation.employeurId().equals(me.getId())) {
                return ResponseEntity.status(403).body(new PdfGenerationResponse(null, "Accès non autorisé"));
            }

            String lang = language.startsWith("en") ? "en" : "fr";

            EvaluationStagiaireDto updatedEvaluation = evaluationStagiaireService.generateEvaluationPdf(evaluationId, formData, lang);
            return ResponseEntity.ok(new PdfGenerationResponse(updatedEvaluation.pdfFilePath(), "PDF généré avec succès"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new PdfGenerationResponse(null, e.getMessage()));
        }
    }

    @GetMapping("/evaluations/{evaluationId}/pdf")
    public ResponseEntity<?> getEvaluationPdf(
            HttpServletRequest request,
            @PathVariable Long evaluationId) {

        UserDTO me = userService.getMe(request.getHeader("Authorization"));

        if (!me.getRole().name().equals("EMPLOYEUR")) {
            return ResponseEntity.status(403).build();
        }

        try {
            EvaluationStagiaireDto evaluation = evaluationStagiaireService.getEvaluationById(evaluationId);
            if (!evaluation.employeurId().equals(me.getId())) {
                return ResponseEntity.status(403).build();
            }

            byte[] pdfBytes = evaluationStagiaireService.getEvaluationPdf(evaluationId);
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"evaluation_" + evaluationId + ".pdf\"")
                    .body(pdfBytes);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/evaluations")
    public ResponseEntity<?> getMyEvaluations(HttpServletRequest request) {
        UserDTO me = userService.getMe(request.getHeader("Authorization"));

        if (!me.getRole().name().equals("EMPLOYEUR")) {
            return ResponseEntity.status(403).build();
        }

        try {
            List<EvaluationStagiaireDto> evaluations = evaluationStagiaireService.getEvaluationsByEmployeur(me.getId());
            return ResponseEntity.ok(evaluations);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Erreur lors de la récupération des évaluations");
        }
    }
    @GetMapping("/evaluation/{evaluationId}")
    public ResponseEntity<?> getEvaluation(
            HttpServletRequest request,
            @PathVariable Long evaluationId) {

        UserDTO me = userService.getMe(request.getHeader("Authorization"));

        if (!me.getRole().name().equals("EMPLOYEUR")) {
            return ResponseEntity.status(403).build();
        }

        try {
            EvaluationStagiaireDto evaluation = evaluationStagiaireService.getEvaluationById(evaluationId);

            if (!evaluation.employeurId().equals(me.getId())) {
                return ResponseEntity.status(403).body("Accès non autorisé");
            }

            return ResponseEntity.ok(evaluation);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
    @GetMapping("/evaluations/eligible")
    public ResponseEntity<List<EligibleEvaluationDto>> getEligibleEvaluations(HttpServletRequest request) {
        UserDTO me = userService.getMe(request.getHeader("Authorization"));

        if (!me.getRole().name().equals("EMPLOYEUR")) {
            return ResponseEntity.status(403).build();
        }

        List<EligibleEvaluationDto> eligibleEvaluations = evaluationStagiaireService.getEligibleEvaluations(me.getId());
        return ResponseEntity.ok(eligibleEvaluations);
    }

    @GetMapping("/evaluations/info")
    public ResponseEntity<?> getEvaluationInfo(
            HttpServletRequest request,
            @RequestParam Long studentId,
            @RequestParam Long offerId) {

        UserDTO me = userService.getMe(request.getHeader("Authorization"));

        if (!me.getRole().name().equals("EMPLOYEUR")) {
            return ResponseEntity.status(403).build();
        }

        try {
            EvaluationInfoDto info = evaluationStagiaireService.getEvaluationInfo(
                    me.getId(), studentId, offerId);
            return ResponseEntity.ok(info);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

}
