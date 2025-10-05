package ca.cal.leandrose.presentation;

import ca.cal.leandrose.model.InternshipOffer;
import ca.cal.leandrose.presentation.request.RejectOfferRequest;
import ca.cal.leandrose.model.Cv;
import ca.cal.leandrose.repository.CvRepository;
import ca.cal.leandrose.service.GestionnaireService;
import ca.cal.leandrose.service.InternshipOfferService;
import ca.cal.leandrose.service.dto.CvDto;
import ca.cal.leandrose.service.dto.ProgramDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequestMapping("/gestionnaire")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174"})
@RequiredArgsConstructor
public class GestionnaireController {

    private final InternshipOfferService internshipOfferService;
    private final GestionnaireService gestionnaireService;
    private final CvRepository cvRepository;

    @PostMapping("/cv/{cvId}/approve")
    public ResponseEntity<CvDto> approveCv(@PathVariable Long cvId) {
        return ResponseEntity.ok(gestionnaireService.approveCv(cvId));
    }

    @PostMapping("/cv/{cvId}/reject")
    public ResponseEntity<CvDto> rejectCv(
            @PathVariable Long cvId,
            @RequestBody(required = false) String comment
    ) {
        return ResponseEntity.ok(gestionnaireService.rejectCv(cvId, comment));
    }

  @GetMapping("/offers/pending")
  public ResponseEntity<List<InternshipOffer>> getPendingOffers() {
    return ResponseEntity.ok(gestionnaireService.getPendingOffers());
  }

    @GetMapping("/cvs/pending")
    public ResponseEntity<List<CvDto>> getPendingCvs() {
        return ResponseEntity.ok(gestionnaireService.getPendingCvs());
    }

    @GetMapping("/offers/{id}")
    public ResponseEntity<InternshipOffer> getOfferDetails(@PathVariable Long id) {
        return ResponseEntity.ok(internshipOfferService.getOffer(id));
    }

    @GetMapping("/cv/{cvId}/download")
    public ResponseEntity<Resource> downloadCv(@PathVariable Long cvId) throws IOException {
        Cv cv = cvRepository.findById(cvId)
                .orElseThrow(() -> new RuntimeException("CV introuvable"));

        Path filePath = Paths.get(cv.getPdfPath());
        Resource resource = new UrlResource(filePath.toUri());

        if (!resource.exists()) {
            throw new RuntimeException("Fichier non trouvé: " + cv.getPdfPath());
        }

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filePath.getFileName() + "\"")
                .body(resource);
    }

    @GetMapping("/offers/{id}/pdf")
    public ResponseEntity<byte[]> getOfferPdf(@PathVariable Long id) {
        try {
            byte[] pdfData = internshipOfferService.getOfferPdf(id);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, "application/pdf")
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=offer_" + id + ".pdf")
                    .body(pdfData);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/offers/{id}/approve")
    public ResponseEntity<InternshipOffer> approveOffer(@PathVariable Long id) {
        return ResponseEntity.ok(gestionnaireService.approveOffer(id));
    }

    @PostMapping("/offers/{id}/reject")
    public ResponseEntity<InternshipOffer> rejectOffer(
            @PathVariable Long id,
            @Valid @RequestBody RejectOfferRequest request) {
        return ResponseEntity.ok(gestionnaireService.rejectOffer(id, request.getComment()));
    }

    @GetMapping("/programs")
    public ResponseEntity<List<ProgramDto>> getPrograms() {
        return ResponseEntity.ok(gestionnaireService.getAllPrograms());
    }
    @PostMapping("/addProgram")
    public ResponseEntity<ProgramDto> addProgram(@RequestBody String programName) {
        try {
            ProgramDto programDto = gestionnaireService.addProgram(programName);
            return ResponseEntity.ok(programDto);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ProgramDto(e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(new ProgramDto("Erreur serveur"));
        }
    }
}
