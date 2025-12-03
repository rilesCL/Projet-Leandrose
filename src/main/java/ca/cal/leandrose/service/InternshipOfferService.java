package ca.cal.leandrose.service;

import static ca.cal.leandrose.service.mapper.InternshipOfferMapper.toDto;

import ca.cal.leandrose.model.Employeur;
import ca.cal.leandrose.model.InternshipOffer;
import ca.cal.leandrose.model.Program;
import ca.cal.leandrose.model.SchoolTerm;
import ca.cal.leandrose.repository.EmployeurRepository;
import ca.cal.leandrose.repository.InternshipOfferRepository;
import ca.cal.leandrose.service.dto.EmployeurDto;
import ca.cal.leandrose.service.dto.InternshipOfferDto;
import ca.cal.leandrose.service.mapper.InternshipOfferMapper;
import com.itextpdf.text.pdf.PdfReader;
import jakarta.persistence.EntityNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class InternshipOfferService {

  private static final String BASE_UPLOAD_DIR = "uploads/offers/";
  private final InternshipOfferRepository internshipOfferRepository;
  private final EmployeurRepository employeurRepository;

  @Transactional
  public InternshipOfferDto createOfferDto(
      String description,
      LocalDate startDate,
      int durationInWeeks,
      String address,
      Float remuneration,
      EmployeurDto employeur,
      MultipartFile pdfFile)
      throws IOException {

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

    Employeur employeurEntity =
        employeurRepository
            .findById(employeur.getId())
            .orElseThrow(
                () -> new RuntimeException("Employer not found with id : " + employeur.getId()));

    InternshipOffer offer =
        InternshipOffer.builder()
            .description(description)
            .startDate(startDate)
            .durationInWeeks(durationInWeeks)
            .address(address)
            .remuneration(remuneration != null ? remuneration : 0f)
            .employeur(employeurEntity)
            .schoolTerm(SchoolTerm.getNextTerm())
            .pdfPath(filePath.toString())
            .status(InternshipOffer.Status.PENDING_VALIDATION)
            .build();

    InternshipOffer saved = internshipOfferRepository.save(offer);
    System.out.println(saved.getSchoolTerm());

    return toDto(saved);
  }

  public InternshipOfferDto getOffer(Long id) {
    InternshipOffer offer =
        internshipOfferRepository
            .findById(id)
            .orElseThrow(() -> new RuntimeException("Offre de stage non trouvée"));
    return InternshipOfferMapper.toDto(offer);
  }

  public InternshipOfferDto getOfferDetails(Long id) {
    InternshipOffer offer =
        internshipOfferRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Offer not found with id" + id));
    return toDto(offer);
  }

  public byte[] getOfferPdf(Long id) throws IOException {
    InternshipOfferDto offer = getOffer(id);
    Path path = Paths.get(offer.getPdfPath());
    return Files.readAllBytes(path);
  }

  public List<InternshipOffer> getOffersByEmployeurId(Long employeurId) {
    return internshipOfferRepository.findOffersByEmployeurId(employeurId);
  }

    public List<InternshipOfferDto> getPublishedOffersForStudents(String program, String schoolTerm) {
        if (Arrays.stream(Program.values()).noneMatch(p -> p.getTranslationKey().equals(program))) {
            throw new IllegalArgumentException("Invalid program: " + program);
        }

        SchoolTerm term = parseSchoolTerm(schoolTerm);
        System.out.println("Fetching offers for program: " + program + ", term: " + term.getTermAsString());

        return internshipOfferRepository.findPublishedByProgram(program).stream()
                .filter(offer -> {
                    boolean matchesTerm = offer.getSchoolTerm() != null
                            && offer.getSchoolTerm().getSeason() == term.getSeason()
                            && offer.getSchoolTerm().getYear() == term.getYear();
                    System.out.println("Offer ID: " + offer.getId() + ", Matches term: " + matchesTerm);
                    return matchesTerm;
                })
                .map(InternshipOfferMapper::toDto)
                .toList();
    }



  private SchoolTerm parseSchoolTerm(String termString) {
    if (termString == null || termString.isBlank()) {
      throw new IllegalArgumentException("School term cannot be null or empty");
    }

    String[] parts = termString.trim().split("\\s+");
    if (parts.length != 2) {
      throw new IllegalArgumentException("Invalid school term format: " + termString);
    }

    String seasonStr = parts[0];
    int year = Integer.parseInt(parts[1]);

    SchoolTerm.Season season = SchoolTerm.Season.valueOf(seasonStr.toUpperCase());

    return new SchoolTerm(season, year);
  }

  @Transactional
    public InternshipOfferDto disableOffer(Long employerId, Long offerId){
      return setOfferStatus(employerId, offerId, false);
  }

  @Transactional
    public InternshipOfferDto enableOffer(Long employerId, Long offerId){
      return setOfferStatus(employerId, offerId, true);
  }

  @Transactional
    public InternshipOfferDto setOfferStatus(Long employerId, Long offerId, boolean enable){
      InternshipOffer offer = internshipOfferRepository.findById(offerId)
              .orElseThrow(() -> new RuntimeException("Offre de stage non trouvée"));
      if(!offer.getEmployeur().getId().equals(employerId)){
          throw new RuntimeException("Vous n'êtes pas autorisé à modifier cette offre");
      }

      if(enable){
          if(offer.getStartDate().isBefore(LocalDate.now())){
              throw new RuntimeException("Impossible de réactiver une offre dont la date de début est expiré");
          }
          offer.setStatus(InternshipOffer.Status.PUBLISHED);
      }
      else{
          offer.setStatus(InternshipOffer.Status.DISABLED);
      }
      InternshipOffer saved = internshipOfferRepository.save(offer);
      return InternshipOfferMapper.toDto(saved);
  }
}
