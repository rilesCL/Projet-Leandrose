package ca.cal.leandrose.service;

import ca.cal.leandrose.model.Employeur;
import ca.cal.leandrose.model.InternshipOffer;
import ca.cal.leandrose.repository.InternshipOfferRepository;
import com.itextpdf.text.pdf.PdfReader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InternshipOfferService {

    private final InternshipOfferRepository internshipOfferRepository;

    private static final String BASE_UPLOAD_DIR = "uploads/offers/";

    @Transactional
    public InternshipOffer createOffer(
            String description,
            LocalDate startDate,
            int durationInWeeks,
            String address,
            Float remuneration,
            Employeur employeur,
            MultipartFile pdfFile
    ) throws IOException {

        try {
            PdfReader reader = new PdfReader(pdfFile.getBytes());
            if (reader.getNumberOfPages() == 0) {
                throw new IllegalArgumentException("PDF invalide: aucune page trouvée");
            }
            reader.close();
        } catch (Exception e) {
            throw new IllegalArgumentException("PDF invalide", e);
        }

        Path employerDir = Paths.get(BASE_UPLOAD_DIR, String.valueOf(employeur.getId()));
        if (!Files.exists(employerDir)) {
            Files.createDirectories(employerDir);
        }

        String originalName = pdfFile.getOriginalFilename();
        String validFileName = (originalName != null ? originalName.replaceAll("\\s+", "_") : "offer");
        long timestamp = System.currentTimeMillis();
        String fileName = validFileName + "_" + timestamp + ".pdf";

        Path filePath = employerDir.resolve(fileName);
        Files.copy(pdfFile.getInputStream(), filePath);

        InternshipOffer offer = InternshipOffer.builder()
                .description(description)
                .startDate(startDate)
                .durationInWeeks(durationInWeeks)
                .address(address)
                .remuneration(remuneration != null ? remuneration : 0f)
                .employeur(employeur)
                .pdfPath(filePath.toString())
                .status(InternshipOffer.Status.PENDING_VALIDATION)
                .build();

        return internshipOfferRepository.save(offer);
    }


    public InternshipOffer getOffer(Long id) {
        return internshipOfferRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Offre de stage non trouvée"));
    }

    public byte[] getOfferPdf(Long id) throws IOException {
        InternshipOffer offer = getOffer(id);
        Path path = Paths.get(offer.getPdfPath());
        return Files.readAllBytes(path);
    }

    public List<InternshipOffer> getOffersByEmployeurId(Long employeurId) {
        return internshipOfferRepository.findOffersByEmployeurId(employeurId);
    }

    // Use Case 1: Liste des offres en attente avec infos entreprise
    public List<InternshipOffer> getPendingOffersWithDetails() {
        return internshipOfferRepository.findByStatusWithEmployeur(InternshipOffer.Status.PENDING_VALIDATION);
    }
    // Use Case 2: Détail complet d'une offre (existant mais améliorer)
    public InternshipOffer getOfferDetails(Long id) {
        return internshipOfferRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Offre de stage non trouvée"));
    }

    @Transactional
    public InternshipOffer approveOffer(Long offerId, Long gestionnaireId) {
        InternshipOffer offer = getOffer(offerId);

        if (offer.getStatus() != InternshipOffer.Status.PENDING_VALIDATION) {
            throw new IllegalStateException("Cette offre ne peut pas être approuvée");
        }

        offer.setStatus(InternshipOffer.Status.PUBLISHED);
        offer.setValidationDate(LocalDate.now());

        // Use Case 5: Notification employeur (à implémenter)
        // notificationService.notifyEmployeurApproval(offer);

        return internshipOfferRepository.save(offer);
    }

    @Transactional
    public InternshipOffer rejectOffer(Long offerId, String rejectionComment, Long gestionnaireId) {
        if (rejectionComment == null || rejectionComment.trim().isEmpty()) {
            throw new IllegalArgumentException("Un commentaire est obligatoire pour rejeter une offre");
        }

        InternshipOffer offer = getOffer(offerId);

        if (offer.getStatus() != InternshipOffer.Status.PENDING_VALIDATION) {
            throw new IllegalStateException("Cette offre ne peut pas être rejetée");
        }

        offer.setStatus(InternshipOffer.Status.REJECTED);
        offer.setRejectionComment(rejectionComment);
        offer.setValidationDate(LocalDate.now());

        // Use Case 5: Notification employeur (à implémenter)
        // notificationService.notifyEmployeurRejection(offer, rejectionComment);

        return internshipOfferRepository.save(offer);
    }

    public List<InternshipOffer> getPublishedOffersForStudents() {
        return internshipOfferRepository.findByStatusOrderByStartDateDesc(InternshipOffer.Status.PUBLISHED);
    }

}
