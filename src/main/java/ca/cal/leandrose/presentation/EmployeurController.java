package ca.cal.leandrose.presentation;

import ca.cal.leandrose.model.Employeur;
import ca.cal.leandrose.model.InternshipOffer;
import ca.cal.leandrose.presentation.request.InternshipOfferRequest;
import ca.cal.leandrose.security.exception.UserNotFoundException;
import ca.cal.leandrose.service.InternshipOfferService;
import ca.cal.leandrose.service.UserAppService;
import ca.cal.leandrose.service.dto.UserDTO;
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

@RequiredArgsConstructor
@RestController
@RequestMapping("/employeur")
public class EmployeurController {

    private final UserAppService userService;
    private final InternshipOfferService internshipOfferService;
    private final EmployeurRepository employeurRepository;

    @GetMapping("/offers")
    public ResponseEntity<List<InternshipOffer>> getMyOffers(HttpServletRequest request) {
        UserDTO me = userService.getMe(request.getHeader("Authorization"));

        if (!me.getRole().name().equals("EMPLOYEUR")) {
            return ResponseEntity.status(403).build();
        }

        List<InternshipOffer> offers = internshipOfferService.getOffersByEmployeurId(me.getId());
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(offers);
    }

    @PostMapping(value = "/offers", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<InternshipOffer> uploadOffer(
            HttpServletRequest request,
            @RequestPart("offer") InternshipOfferRequest offerRequest,
            @RequestPart("pdfFile") MultipartFile pdfFile
    ) throws IOException {
        UserDTO me = userService.getMe(request.getHeader("Authorization"));
        System.out.println(me.getRole().name());
        if (!me.getRole().name().equals("EMPLOYEUR")) {
            return ResponseEntity.status(403).build();
        }

        Employeur employeur = employeurRepository.findById(me.getId()).orElseThrow(UserNotFoundException::new);

        InternshipOffer offer = internshipOfferService.createOffer(
                offerRequest.getDescription(),
                LocalDate.parse(offerRequest.getStartDate()),
                offerRequest.getDurationInWeeks(),
                offerRequest.getAddress(),
                offerRequest.getRemuneration(),
                employeur,
                pdfFile
        );

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(offer);
    }

    // Add new download endpoint
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
            // Get the offer and verify it belongs to the current employer
            InternshipOffer offer = internshipOfferService.getOffer(offerId);

            // Verify the offer belongs to the current employer
            if (!offer.getEmployeur().getId().equals(me.getId())) {
                return ResponseEntity.status(403).build(); // Forbidden - not their offer
            }

            Path filePath = Paths.get(offer.getPdfPath());
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                String filename = "Offre_" + offer.getDescription().substring(0, Math.min(30, offer.getDescription().length())).replaceAll("[^a-zA-Z0-9]", "_") + ".pdf";

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
}