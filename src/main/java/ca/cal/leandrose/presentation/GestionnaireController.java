package ca.cal.leandrose.presentation;

import ca.cal.leandrose.model.InternshipOffer;
import ca.cal.leandrose.presentation.request.RejectOfferRequest;
import ca.cal.leandrose.service.GestionnaireService;
import ca.cal.leandrose.service.InternshipOfferService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/gestionnaire")
@CrossOrigin(origins = "http://localhost:5173")
@RequiredArgsConstructor
public class GestionnaireController {
    private final InternshipOfferService internshipOfferService;
    private final GestionnaireService gestionnaireService;

    // Use Case 1: Liste des offres en attente
    @GetMapping("/offers/pending")
    public ResponseEntity<List<InternshipOffer>> getPendingOffers() {
        List<InternshipOffer> offers = gestionnaireService.getPendingOffers();
        return ResponseEntity.ok(offers);
    }

    // Use Case 2: Consulter le détail d'une offre
    @GetMapping("/offers/{id}")
    public ResponseEntity<InternshipOffer> getOfferDetails(@PathVariable Long id) {
        InternshipOffer offer = internshipOfferService.getOffer(id);
        return ResponseEntity.ok(offer);
    }

    // Use Case 2: Télécharger le PDF de l'offre
    @GetMapping("/offers/{id}/pdf")
    public ResponseEntity<byte[]> getOfferPdf(@PathVariable Long id) {
        try {
            byte[] pdfData = internshipOfferService.getOfferPdf(id);
            return ResponseEntity.ok()
                    .header("Content-Type", "application/pdf")
                    .header("Content-Disposition", "inline; filename=offer_" + id + ".pdf")
                    .body(pdfData);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // AJOUTER: Use Case 3 - Approuver une offre
    @PostMapping("/offers/{id}/approve")
    public ResponseEntity<InternshipOffer> approveOffer(@PathVariable Long id) {
        InternshipOffer approvedOffer = gestionnaireService.approveOffer(id);
        return ResponseEntity.ok(approvedOffer);
    }

    // AJOUTER: Use Case 3 + 4 - Rejeter une offre avec commentaire
    @PostMapping("/offers/{id}/reject")
    public ResponseEntity<InternshipOffer> rejectOffer(
            @PathVariable Long id,
            @Valid @RequestBody RejectOfferRequest request) {

        InternshipOffer rejectedOffer = gestionnaireService.rejectOffer(id, request.getComment());
        return ResponseEntity.ok(rejectedOffer);
    }
}
