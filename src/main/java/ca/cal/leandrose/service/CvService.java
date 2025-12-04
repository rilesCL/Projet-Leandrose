package ca.cal.leandrose.service;

import ca.cal.leandrose.model.Cv;
import ca.cal.leandrose.model.Student;
import ca.cal.leandrose.repository.CvRepository;
import ca.cal.leandrose.service.dto.CvDto;
import com.itextpdf.text.pdf.PdfReader;
import java.io.IOException;
import java.nio.file.*;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class CvService {

  private static final long KILOBYTE = 1024L;
  private static final long MEGABYTE = KILOBYTE * KILOBYTE;
  private static final int DEFAULT_MAX_SIZE_MB = 5;
  private static final String DEFAULT_BASE_DIR = "uploads/cvs";
  private static final String PDF_EXTENSION = ".pdf";
  private static final String PDF_CONTENT_TYPE = "application/pdf";
  private final CvRepository cvRepository;

  @Value("${app.cv.max-size-mb:" + DEFAULT_MAX_SIZE_MB + "}")
  private int maxSizeMb;

  @Value("${app.cv.base-dir:" + DEFAULT_BASE_DIR + "}")
  private String baseUploadDir;

  @Transactional
  public CvDto uploadCv(Long studentId, MultipartFile file) throws IOException {
    final long maxSizeBytes = ((long) maxSizeMb) * MEGABYTE;

    if (file == null || file.isEmpty()) {
      throw new IllegalArgumentException("Fichier vide ou manquant.");
    }
    if (file.getSize() > maxSizeBytes) {
      throw new IllegalArgumentException(
          "Fichier trop volumineux. Taille maximale : " + maxSizeMb + " Mo");
    }
    String originalName = file.getOriginalFilename();
    if (originalName == null || !originalName.toLowerCase().endsWith(PDF_EXTENSION)) {
      throw new IllegalArgumentException(
          "Format de fichier invalide : l'extension doit être " + PDF_EXTENSION);
    }
    String contentType = file.getContentType();
    if (contentType == null || !contentType.equalsIgnoreCase(PDF_CONTENT_TYPE)) {
      throw new IllegalArgumentException(
          "Content type invalide. Seul " + PDF_CONTENT_TYPE + " est autorisé.");
    }

    PdfReader reader = null;
    try {
      reader = new PdfReader(file.getBytes());
      if (reader.getNumberOfPages() <= 0) {
        throw new IllegalArgumentException("PDF invalide : aucune page trouvée.");
      }
    } catch (IOException ioe) {
      throw ioe;
    } catch (Exception e) {
      throw new IllegalArgumentException("PDF invalide ou corrompu.", e);
    } finally {
      if (reader != null) {
        try {
          reader.close();
        } catch (Exception ignored) {
        }
      }
    }

    Path studentDir =
        Paths.get(baseUploadDir, String.valueOf(studentId)).toAbsolutePath().normalize();
    if (!Files.exists(studentDir)) {
      Files.createDirectories(studentDir);
    }

    String storageFilename = UUID.randomUUID() + "_" + System.currentTimeMillis() + PDF_EXTENSION;
    Path targetPath = studentDir.resolve(storageFilename);

    Optional<Cv> existingOpt = cvRepository.findByStudentId(studentId);
    existingOpt.ifPresent(
        existing -> {
          try {
            Path oldPath = Paths.get(existing.getPdfPath());
            if (!oldPath.isAbsolute()) {
              oldPath = studentDir.resolve(oldPath).normalize();
            }
            Files.deleteIfExists(oldPath);
          } catch (Exception ex) {
            System.err.println("Erreur lors de la suppression de l'ancien CV: " + ex.getMessage());
          }
          cvRepository.delete(existing);
        });

    Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

    Student studentRef = new Student();
    studentRef.setId(studentId);

    Cv cv =
        Cv.builder()
            .student(studentRef)
            .pdfPath(targetPath.toString())
            .status(Cv.Status.PENDING)
            .build();

    Cv saved = cvRepository.save(cv);

    return CvDto.create(saved);
  }

  public CvDto getCvByStudentId(Long studentId) {
    Cv cv =
        cvRepository
            .findByStudentId(studentId)
            .orElseThrow(() -> new RuntimeException("CV non trouvé pour l'étudiant " + studentId));
    CvDto cvDto = CvDto.create(cv);
    cvDto.setRejectionComment(cv.getRejectionComment());
    return cvDto;
  }

  public Resource downloadCv(Long id) throws IOException {
    Cv cv = cvRepository.findById(id).orElseThrow(() -> new RuntimeException("Cv introuvable"));
    Path filepath = Paths.get(cv.getPdfPath());
    Resource resource = new UrlResource(filepath.toUri());

    if (!resource.exists()) throw new RuntimeException("Fichier non trouvé " + cv.getPdfPath());
    return resource;
  }
}
