package ca.cal.leandrose.presentation;

import ca.cal.leandrose.service.EntenteStageService;
import ca.cal.leandrose.service.EvaluationStagiaireService;
import ca.cal.leandrose.service.UserAppService;
import ca.cal.leandrose.service.dto.ProfStudentItemDto;
import ca.cal.leandrose.service.dto.UserDTO;
import ca.cal.leandrose.service.dto.evaluation.*;
import ca.cal.leandrose.service.dto.evaluation.prof.EvaluationProfFormDto;
import ca.cal.leandrose.service.dto.evaluation.prof.EvaluationTeacherInfoDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/prof")
@CrossOrigin(origins = {"http://localhost:5173"})
@RequiredArgsConstructor
public class ProfController {

    private final EntenteStageService ententeStageService;
    private final EvaluationStagiaireService evaluationStagiaireService;
    private final UserAppService userService;

    @GetMapping("/{profId}/etudiants")
    public List<ProfStudentItemDto> getEtudiantsAttribues(
            @PathVariable @Min(1) Long profId,
            @RequestParam(required = false) String nom,
            @RequestParam(required = false) String entreprise,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestParam(required = false) String evaluationStatus,
            @RequestParam(required = false, defaultValue = "name") String sortBy,
            @RequestParam(required = false, defaultValue = "true") Boolean asc
    ) {
        return ententeStageService.getEtudiantsPourProf(
                profId,
                nom,
                entreprise,
                dateFrom,
                dateTo,
                evaluationStatus,
                sortBy,
                asc
        );
    }
    @PostMapping("/evaluations")
    public ResponseEntity<?> createEvaluation(
            HttpServletRequest request,
            @RequestBody CreateEvaluationRequest createRequest){
        UserDTO me = userService.getMe(request.getHeader("Authorization"));

        if(!me.getRole().name().equals("PROF")){
            return ResponseEntity.status(403).build();
        }

        try{
            boolean isEligible = evaluationStagiaireService.isEvaluationEligible(CreatorTypeEvaluation.PROF, me.getId(),
                    createRequest.studentId(), createRequest.internshipOfferId());

            if (!isEligible){
                return ResponseEntity.badRequest().body(
                        new EvaluationResponsesDto(null, "Evaluation not allowed - agreement not validated or not found")
                );
            }
            EvaluationStagiaireDto response = evaluationStagiaireService.createEvaluationByProf(
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
            @RequestBody EvaluationProfFormDto formData,
            @RequestHeader(value = "Accept-Language", defaultValue = "fr") String language) {

        System.out.println("=== PROF CONTROLLER DEBUG ===");
        System.out.println("Request URI: " + request.getRequestURI());
        System.out.println("Request URL: " + request.getRequestURL());
        System.out.println("FormData: " + formData);
        System.out.println("Authorization header present: " + (request.getHeader("Authorization") != null));

        try {
            UserDTO me = userService.getMe(request.getHeader("Authorization"));
            System.out.println("User authenticated: " + me.getRole().name());
            if (!me.getRole().name().equals("PROF")) {
                return ResponseEntity.status(403).build();
            }

            try {
                EvaluationStagiaireDto evaluation = evaluationStagiaireService.getEvaluationById(evaluationId);
                if (!evaluation.professeurId().equals(me.getId())) {
                    return ResponseEntity.status(403).body("Accès non autorisé");
                }

                String lang = language.startsWith("en") ? "en" : "fr";
                EvaluationStagiaireDto updatedEvaluation =
                        evaluationStagiaireService.generateEvaluationByTeacher(evaluationId, formData, lang);

                return ResponseEntity.ok(new PdfGenerationResponse(updatedEvaluation.professorPdfPath(), "PDF généré avec succès"));

            } catch (Exception e) {
                e.printStackTrace();
                return ResponseEntity.badRequest().body(e.getMessage());
            }
        } catch (Exception e) {
            System.out.println("Authentication failed: " + e.getMessage());
            return ResponseEntity.status(401).build();
        }
    }

    @GetMapping("/evaluations/{evaluationId}/pdf")
    public ResponseEntity<?> getEvaluationPdf(
            HttpServletRequest request,
            @PathVariable Long evaluationId) {

        String authHeader = request.getHeader("Authorization");

        UserDTO me = userService.getMe(authHeader);


        if (!me.getRole().name().equals("PROF")) {
            return ResponseEntity.status(403).build();
        }

        try {
            EvaluationStagiaireDto evaluation = evaluationStagiaireService.getEvaluationById(evaluationId);
            if (!evaluation.professeurId().equals(me.getId())) {
                return ResponseEntity.status(403).build();
            }

            byte[] pdfBytes = evaluationStagiaireService.getEvaluationPdf(evaluationId, CreatorTypeEvaluation.PROF);
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

        if (!me.getRole().name().equals("PROF")) {
            return ResponseEntity.status(403).build();
        }

        try {
            List<EvaluationStagiaireDto> evaluations = evaluationStagiaireService.getEvaluationsByProfesseur(me.getId());
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

        if (!me.getRole().name().equals("PROF")) {
            return ResponseEntity.status(403).build();
        }

        try {
            EvaluationStagiaireDto evaluation = evaluationStagiaireService.getEvaluationById(evaluationId);

            if (!evaluation.professeurId().equals(me.getId())) {
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

        if (!me.getRole().name().equals("PROF")) {
            return ResponseEntity.status(403).build();
        }

        List<EligibleEvaluationDto> eligibleEvaluations = evaluationStagiaireService.getEligibleEvaluations(CreatorTypeEvaluation.PROF, me.getId());
        return ResponseEntity.ok(eligibleEvaluations);
    }

    @GetMapping("/evaluations/info")
    public ResponseEntity<?> getEvaluationInfo(
            HttpServletRequest request,
            @RequestParam Long studentId,
            @RequestParam Long offerId) {

        UserDTO me = userService.getMe(request.getHeader("Authorization"));

        if (!me.getRole().name().equals("PROF")) {
            return ResponseEntity.status(403).build();
        }

        try {
            EvaluationTeacherInfoDto info = evaluationStagiaireService.getEvaluationInfoForTeacher(
                    me.getId(), studentId, offerId);
            return ResponseEntity.ok(info);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    @GetMapping("/evaluations/check-existing")
    public ResponseEntity<?> checkExistingEvaluation(
            HttpServletRequest request,
            @RequestParam Long studentId,
            @RequestParam Long offerId) {

        UserDTO me = userService.getMe(request.getHeader("Authorization"));

        if (!me.getRole().name().equals("PROF")) {
            return ResponseEntity.status(403).build();
        }

        try {
            Optional<EvaluationStagiaireDto> existingEvaluation = evaluationStagiaireService
                    .getExistingEvaluation(studentId, offerId);

            if (existingEvaluation.isPresent()) {
                return ResponseEntity.ok().body(Map.of(
                        "exists", true,
                        "evaluation", existingEvaluation.get(),
                        "message", "Une évaluation existe déjà"
                ));
            } else {
                return ResponseEntity.ok().body(Map.of(
                        "exists", false,
                        "message", "Aucune évaluation existante"
                ));
            }

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    @GetMapping("/evaluations/check-teacher-assigned")
    public ResponseEntity<?> checkTeacherAssigned(HttpServletRequest request,
                                                  @RequestParam Long studentId,
                                                  @RequestParam Long offerId) throws AccessDeniedException {
        UserDTO me = userService.getMe(request.getHeader("Authorization"));

        if(!me.getRole().name().equals("PROF")){
            throw new AccessDeniedException("Prof access is required");
        }

        try{
            boolean isTeacherAssigned = ententeStageService.isTeacherAssigned(studentId, offerId);
            Map<String, Object> response = new HashMap<>();

            response.put("teacherAssigned", isTeacherAssigned);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", e.getMessage()
            ));
        }
    }
}
