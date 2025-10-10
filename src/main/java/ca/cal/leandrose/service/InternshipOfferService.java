package ca.cal.leandrose.service;

import ca.cal.leandrose.model.Employeur;
import ca.cal.leandrose.model.InternshipOffer;
import ca.cal.leandrose.model.Program;
import ca.cal.leandrose.repository.InternshipOfferRepository;
import ca.cal.leandrose.service.dto.InternshipOfferDto;
import ca.cal.leandrose.service.mapper.InternshipOfferMapper;
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
    public InternshipOfferDto createOfferDto(
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

        InternshipOffer saved = internshipOfferRepository.save(offer);

        return InternshipOfferMapper.toDto(saved);
    }

    public InternshipOfferDto getOffer(Long id){
        InternshipOffer offer = internshipOfferRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Offre de stage non trouvée"));
        return InternshipOfferDto.toDto(offer);
    }

//    public InternshipOffer getOffer(Long id) {
//        return internshipOfferRepository.findById(id)
//                .orElseThrow(() -> new RuntimeException("Offre de stage non trouvée"));
//    }

    public byte[] getOfferPdf(Long id) throws IOException {
        InternshipOfferDto offer = getOffer(id);
        Path path = Paths.get(offer.getPdfPath());
        return Files.readAllBytes(path);
    }

    public List<InternshipOffer> getOffersByEmployeurId(Long employeurId) {
        return internshipOfferRepository.findOffersByEmployeurId(employeurId);
    }


    public List<InternshipOffer> getPublishedOffersForStudents(String program) {
        return internshipOfferRepository.findPublishedByProgram(program);
    }
}
