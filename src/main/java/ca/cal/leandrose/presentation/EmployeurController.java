package ca.cal.leandrose.presentation;

//import ca.cal.leandrose.model.Candidature;
//import ca.cal.leandrose.model.Cv;
//import ca.cal.leandrose.model.Employeur;
import ca.cal.leandrose.model.InternshipOffer;
import ca.cal.leandrose.presentation.request.InternshipOfferRequest;
import ca.cal.leandrose.repository.CandidatureRepository;
import ca.cal.leandrose.security.exception.UserNotFoundException;
import ca.cal.leandrose.service.*;
import ca.cal.leandrose.service.dto.*;
import ca.cal.leandrose.service.mapper.InternshipOfferMapper;
import ca.cal.leandrose.repository.EmployeurRepository;
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

//todo: Cliqyé sur l'offre de stage m'amène une erreur 401, et le nombre de candidatures n'est pas mis en jour.

@RequiredArgsConstructor
@RestController
@RequestMapping("/employeur")
public class EmployeurController {

    private final UserAppService userService;
    private final InternshipOfferService internshipOfferService;
    private final EmployeurRepository employeurRepository;
    private final EmployeurService employeurService;
    private final CandidatureService candidatureService;
    private final ConvocationService convocationService;
    private final CandidatureRepository candidatureRepository;

    @GetMapping("/offers")
    public ResponseEntity<List<InternshipOfferDto>> getMyOffers(HttpServletRequest request) {
        UserDTO me = userService.getMe(request.getHeader("Authorization"));

        if (!me.getRole().name().equals("EMPLOYEUR")) {
            return ResponseEntity.status(403).build();
        }

        List<InternshipOfferDto> offers = internshipOfferService.getOffersByEmployeurId(me.getId())
                .stream()
                .map(InternshipOfferMapper::toDto)
                .toList();

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(offers);
    }

    @PostMapping(value = "/offers", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<InternshipOfferDto> uploadOffer(
            HttpServletRequest request,
            @RequestPart("offer") InternshipOfferRequest offerRequest,
            @RequestPart("pdfFile") MultipartFile pdfFile
    ) throws IOException {
        UserDTO me = userService.getMe(request.getHeader("Authorization"));

        if (!me.getRole().name().equals("EMPLOYEUR")) {
            return ResponseEntity.status(403).build();
        }

        if (offerRequest.getDescription() == null || offerRequest.getDescription().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(new InternshipOfferDto("La description est requise"));
        }

        if (offerRequest.getDescription().length() > 50) {
            return ResponseEntity.badRequest().body(new InternshipOfferDto("La description ne doit pas dépasser 50 caractères"));
        }

        EmployeurDto employeurDto = employeurService.getEmployeurById(me.getId());

//        Employeur employeur = employeurRepository.findById(me.getId())
//                .orElseThrow(UserNotFoundException::new);

        InternshipOfferDto offerDto = internshipOfferService.createOfferDto(
                offerRequest.getDescription(),
                LocalDate.parse(offerRequest.getStartDate()),
                offerRequest.getDurationInWeeks(),
                offerRequest.getAddress(),
                offerRequest.getRemuneration(),
                employeurDto,
                pdfFile
        );

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(offerDto);
    }

    @GetMapping("/offers/{offerId}/download")
    public ResponseEntity<Resource> downloadOffer(
            HttpServletRequest request,
            @PathVariable Long offerId
    ) {
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
                String filename = "Offre_" + offer.getDescription()
                        .substring(0, Math.min(30, offer.getDescription().length()))
                        .replaceAll("[^a-zA-Z0-9]", "_") + ".pdf";

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
            HttpServletRequest request,
            @PathVariable Long offerId
    ) {
        UserDTO me = userService.getMe(request.getHeader("Authorization"));

        if (!me.getRole().name().equals("EMPLOYEUR")) {
            return ResponseEntity.status(403).build();
        }

        try {
            InternshipOfferDto offer = internshipOfferService.getOffer(offerId);

            if (!offer.getEmployeurDto().getId().equals(me.getId())) {
                return ResponseEntity.status(403).build();
            }

            List<ConvocationDto> convocations = convocationService.getAllConvocationsByInterShipOfferId(offerId);

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(convocations);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }


    @PostMapping("/candidatures/{candidatureId}/convocations")
    public ResponseEntity<String> createConvocation(
            HttpServletRequest request,
            @PathVariable Long candidatureId,
            @RequestBody ConvocationDto convocationRequest
    ) {
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
                    convocationRequest.getMessage()
            );

            return ResponseEntity.ok().body("Convocation créée avec succès");

        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Erreur lors de la création de la convocation");
        }
    }

    @GetMapping("/offers/{offerId}/candidatures")
    public ResponseEntity<List<CandidatureEmployeurDto>> getCandidaturesForOffer(
            HttpServletRequest request,
            @PathVariable Long offerId) {

        UserDTO me = userService.getMe(request.getHeader("Authorization"));

        if (!me.getRole().name().equals("EMPLOYEUR")) {
            return ResponseEntity.status(403).build();
        }

        InternshipOfferDto offer = internshipOfferService.getOffer(offerId);
        if (!offer.getEmployeurDto().getId().equals(me.getId())) {
            return ResponseEntity.status(403).build();
        }

        List<CandidatureEmployeurDto> candidatures =
                candidatureService.getCandidaturesByOffer(offerId);

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
            HttpServletRequest request,
            @PathVariable Long candidatureId) {

        UserDTO me = userService.getMe(request.getHeader("Authorization"));

        if (!me.getRole().name().equals("EMPLOYEUR")) {
            return ResponseEntity.status(403).build();
        }

        try {
            CandidatureDto candidatureDto = candidatureService.getCandidatureById(candidatureId);

            if (!candidatureDto.getId().equals(me.getId())) {
                return ResponseEntity.status(403).build();
            }

//            Candidature candidature = candidatureRepository.findById(candidatureId)
//                    .orElseThrow(() -> new RuntimeException("Candidature non trouvée"));
//
//            if (!candidature.getInternshipOffer().getEmployeur().getId().equals(me.getId())) {
//                return ResponseEntity.status(403).build();
//            }

            CvDto cv = candidatureDto.getCv();
            Path filePath = Paths.get(cv.getPdfPath());
//            CvD cv = candidature.getCv();
//            Path filePath = Paths.get(cv.getPdfPath());

//
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                return ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_PDF)
                        .header(HttpHeaders.CONTENT_DISPOSITION,
                                "attachment; filename=\"CV_" + candidatureDto.getStudentName() + ".pdf\"")
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/candidatures/{candidatureId}/reject")
    public ResponseEntity<String> rejectCandidature(
            HttpServletRequest request,
            @PathVariable Long candidatureId
    ) {
        UserDTO me = userService.getMe(request.getHeader("Authorization"));

        if (!me.getRole().name().equals("EMPLOYEUR")) {
            return ResponseEntity.status(403).build();
        }

        try {
            CandidatureDto candidatureDto = candidatureService.getCandidatureById(candidatureId);

            if (!candidatureDto.getEmployeurId().equals(me.getId())) {
                return ResponseEntity.status(403).build();
            }

            candidatureService.rejectCandidature(candidatureId);

            return ResponseEntity.ok().body("Candidature rejetée avec succès");

        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Erreur lors du rejet de la candidature");
        }
    }
}
