package ca.cal.leandrose.presentation;

import ca.cal.leandrose.presentation.request.RejectOfferRequest;
import ca.cal.leandrose.service.CvService;
import ca.cal.leandrose.service.GestionnaireService;
import ca.cal.leandrose.service.InternshipOfferService;
import ca.cal.leandrose.service.dto.CvDto;
import ca.cal.leandrose.service.dto.InternshipOfferDto;
import ca.cal.leandrose.service.dto.ProgramDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequestMapping("/gestionnaire")
@CrossOrigin(origins = {"http://localhost:5173"})
@RequiredArgsConstructor
public class GestionnaireController {

    private final InternshipOfferService internshipOfferService;
    private final GestionnaireService gestionnaireService;
    private final CvService cvService;

    @PostMapping("/cv/{cvId}/approve")
    public ResponseEntity<CvDto> approveCv(@PathVariable Long cvId) {
        return ResponseEntity.ok(gestionnaireService.approveCv(cvId));
    }

    @PostMapping("/cv/{cvId}/reject")
    public ResponseEntity<CvDto> rejectCv(
            @PathVariable Long cvId,
            @RequestBody String comment
    ) {
        return ResponseEntity.ok(gestionnaireService.rejectCv(cvId, comment));
    }

    @GetMapping("/offers/pending")
    public ResponseEntity<List<InternshipOfferDto>> getPendingOffers() {
        return ResponseEntity.ok(gestionnaireService.getPendingOffers());
    }
    @GetMapping("/offers/approved")
    public ResponseEntity<List<InternshipOfferDto>> getApprovedOffers(){
        return ResponseEntity.ok(gestionnaireService.getApprovedOffers());
    }
    @GetMapping("/offers/reject")
    public ResponseEntity<List<InternshipOfferDto>> getRejectedOffers(){
        return ResponseEntity.ok(gestionnaireService.getRejectedoffers());
    }
    @GetMapping("/cvs/pending")
    public ResponseEntity<List<CvDto>> getPendingCvs() {
        return ResponseEntity.ok(gestionnaireService.getPendingCvs());
    }

    @GetMapping("/offers/{id}")
    public ResponseEntity<InternshipOfferDto> getOfferDetails(@PathVariable Long id) {
        return ResponseEntity.ok(internshipOfferService.getOffer(id));
    }

    @GetMapping("/cv/{cvId}/download")
    public ResponseEntity<?> downloadCv(@PathVariable Long cvId){
        try{

            Resource resource = cvService.downloadCv(cvId);
            Path filepath = Paths.get(resource.getFile().getAbsolutePath());

            return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filepath.getFileName() + "\"")
                .body(resource);
        }
        catch(RuntimeException | IOException e){
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());
        }
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
    public ResponseEntity<InternshipOfferDto> approveOffer(@PathVariable Long id) {
        return ResponseEntity.ok(gestionnaireService.approveOffer(id));
    }

    @PostMapping("/offers/{id}/reject")
    public ResponseEntity<InternshipOfferDto> rejectOffer(
            @PathVariable Long id,
            @Valid @RequestBody RejectOfferRequest request) {
        return ResponseEntity.ok(gestionnaireService.rejectOffer(id, request.getComment()));
    }

    @GetMapping("/programs")
    public ResponseEntity<List<ProgramDto>> getPrograms() {
        return ResponseEntity.ok(gestionnaireService.getAllPrograms());
    }

}
