package ca.cal.leandrose.presentation;

import ca.cal.leandrose.model.InternshipOffer;
import ca.cal.leandrose.service.InternshipOfferService;
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

    // Use Case 1: Liste des offres en attente
    @GetMapping("/offers/pending")
    public ResponseEntity<List<InternshipOffer>> getPendingOffers() {
        List<InternshipOffer> offers = internshipOfferService.getPendingOffersWithDetails();
        return ResponseEntity.ok(offers);
    }

    // Use Case 2: Consulter le détail d'une offre
    @GetMapping("/offers/{id}")
    public ResponseEntity<InternshipOffer> getOfferDetails(@PathVariable Long id) {
        InternshipOffer offer = internshipOfferService.getOfferDetails(id);
        return ResponseEntity.ok(offer);
    }
}
