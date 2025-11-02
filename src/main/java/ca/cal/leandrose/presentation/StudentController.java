package ca.cal.leandrose.presentation;

import ca.cal.leandrose.presentation.request.UpdateStudentInfoRequest;
import ca.cal.leandrose.service.*;
import ca.cal.leandrose.service.dto.*;
import jakarta.persistence.EntityNotFoundException;
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
import java.util.List;

@RestController
@RequestMapping("/student")
@RequiredArgsConstructor
public class StudentController {
  private final CvService cvService;
  private final UserAppService userService;
  private final CandidatureService candidatureService;
  private final StudentService studentService;
  private final InternshipOfferService internshipOfferService;
  private final ConvocationService convocationService;
  private final EntenteStageService ententeStageService;



    @GetMapping("/me")
    public ResponseEntity<StudentDto> getCurrentStudent(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || authHeader.isBlank()) {
            return ResponseEntity.status(401).build();
        }
        UserDTO me;
        try {
            me = userService.getMe(authHeader);
        } catch (Exception e) {
            return ResponseEntity.status(401).build();
        }
        if (me == null) {
            return ResponseEntity.status(401).build();
        }
        if (!"STUDENT".equals(me.getRole().name())) {
            return ResponseEntity.status(403).build();
        }
        try {
            StudentDto student = studentService.getStudentById(me.getId());
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(student);
        } catch (Exception e) {
            return ResponseEntity.status(404).build();
        }
    }


  @PostMapping(value = "/cv")
  public ResponseEntity<CvDto> uploadCv(
      HttpServletRequest request, @RequestPart("pdfFile") MultipartFile pdfFile)
      throws IOException {
    UserDTO me = userService.getMe(request.getHeader("Authorization"));
    if (!me.getRole().name().equals("STUDENT")) {
      return ResponseEntity.status(403).build();
    }

    StudentDto student = studentService.getStudentById(me.getId());
    CvDto cvDto = cvService.uploadCv(student.getId(), pdfFile);
    return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(cvDto);
  }

  @GetMapping(value = "/cv", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<CvDto> getCv(
      @RequestHeader(name = "Authorization", required = false) String authorization) {
    if (authorization == null || authorization.isBlank()) {
      return ResponseEntity.status(401).build();
    }
    String token = authorization.startsWith("Bearer ") ? authorization.substring(7) : authorization;
    UserDTO me;
    try {
      me = userService.getMe(token);
    } catch (Exception e) {
      return ResponseEntity.status(401).build();
    }
    if (me == null) {
      return ResponseEntity.status(401).build();
    }
    if (!"STUDENT".equals(me.getRole().name())) {
      return ResponseEntity.status(403).build();
    }
    StudentDto student = studentService.getStudentById(me.getId());

    CvDto cvDto;
    try {
      cvDto = cvService.getCvByStudentId(student.getId());
    } catch (RuntimeException e) {
      return ResponseEntity.status(404).contentType(MediaType.APPLICATION_JSON).body(null);
    }
    return ResponseEntity.ok().body(cvDto);
  }

  @GetMapping("/cv/download")
  public ResponseEntity<Resource> downloadCv(
      @RequestHeader(name = "Authorization", required = false) String authorization) {
    if (authorization == null || authorization.isBlank()) {
      return ResponseEntity.status(401).build();
    }

    String token = authorization.startsWith("Bearer ") ? authorization.substring(7) : authorization;
    UserDTO me;
    try {
      me = userService.getMe(token);
    } catch (Exception e) {
      return ResponseEntity.status(401).build();
    }

    if (me == null || !"STUDENT".equals(me.getRole().name())) {
      return ResponseEntity.status(403).build();
    }
    StudentDto student = studentService.getStudentById(me.getId());

    try {
      CvDto cvDto = cvService.getCvByStudentId(student.getId());
      Path filePath = Paths.get(cvDto.getPdfPath());
      Resource resource = new UrlResource(filePath.toUri());

      if (resource.exists() && resource.isReadable()) {
        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_PDF)
            .header(
                HttpHeaders.CONTENT_DISPOSITION,
                "inline; filename=\"" + filePath.getFileName().toString() + "\"")
            .body(resource);
      } else {
        return ResponseEntity.notFound().build();
      }
    } catch (RuntimeException | MalformedURLException e) {
      return ResponseEntity.notFound().build();
    }
  }

  @GetMapping("/offers")
  public ResponseEntity<List<InternshipOfferDto>> getPublishedOffers(HttpServletRequest request) {
    UserDTO me = userService.getMe(request.getHeader("Authorization"));
    if (!me.getRole().name().equals("STUDENT")) {
      return ResponseEntity.status(403).build();
    }

    StudentDto student = studentService.getStudentById(me.getId());
    System.out.println(student.isExpired());

    List<InternshipOfferDto> offers =
        internshipOfferService.getPublishedOffersForStudents(
            student.getProgram(), student.getInternshipTerm());

    return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(offers);
  }

  @PutMapping("/update-info")
  public ResponseEntity<StudentDto> updateStudentInfo(
      HttpServletRequest request, @RequestBody UpdateStudentInfoRequest updateRequest) {

    UserDTO me = userService.getMe(request.getHeader("Authorization"));
    if (!me.getRole().name().equals("STUDENT")) {
      return ResponseEntity.status(403).build();
    }

    try {
      StudentDto updated = studentService.updateStudentInfo(me.getId(), updateRequest.getProgram());
      return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(updated);
    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest().body(new StudentDto(e.getMessage()));
    } catch (Exception e) {
      return ResponseEntity.status(500).body(new StudentDto("Une erreur est survenue"));
    }
  }

  @GetMapping("/offers/{id}")
  public ResponseEntity<InternshipOfferDto> getOfferDetails(@PathVariable Long id) {
    InternshipOfferDto offer = internshipOfferService.getOffer(id);

    if (!"PUBLISHED".equals(offer.getStatus())) {
      return ResponseEntity.status(403).build();
    }

    return ResponseEntity.ok(offer);
  }

  @PostMapping("/offers/{offerId}/apply")
  public ResponseEntity<CandidatureDto> applyToOffer(
      HttpServletRequest request, @PathVariable Long offerId, @RequestParam Long cvId) {

    UserDTO me = userService.getMe(request.getHeader("Authorization"));

    if (!me.getRole().name().equals("STUDENT")) {
      return ResponseEntity.status(403).build();
    }

    try {
      CandidatureDto candidature = candidatureService.postuler(me.getId(), offerId, cvId);
      return ResponseEntity.ok(candidature);
    } catch (IllegalStateException e) {
      return ResponseEntity.badRequest().build();
    }
  }

  @GetMapping("/applications")
  public ResponseEntity<List<CandidatureDto>> getMyCandidatures(HttpServletRequest request) {

    UserDTO me = userService.getMe(request.getHeader("Authorization"));
    if (!me.getRole().name().equals("STUDENT")) {
      return ResponseEntity.status(403).build();
    }

    List<CandidatureDto> candidatures = candidatureService.getCandidaturesByStudent(me.getId());
    return ResponseEntity.ok(candidatures);
  }

  @GetMapping("/offers/{id}/pdf")
  public ResponseEntity<byte[]> downloadOfferPdf(@PathVariable Long id) {
    try {
      InternshipOfferDto offer = internshipOfferService.getOffer(id);

      if (!"PUBLISHED".equals(offer.getStatus())) {
        return ResponseEntity.status(403).build();
      }

      byte[] pdfData = internshipOfferService.getOfferPdf(id);
      return ResponseEntity.ok()
          .header(HttpHeaders.CONTENT_TYPE, "application/pdf")
          .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=offer_" + id + ".pdf")
          .body(pdfData);
    } catch (Exception e) {
      return ResponseEntity.notFound().build();
    }
  }

  @GetMapping("/convocations")
  public ResponseEntity<List<ConvocationDto>> getMyConvocations(HttpServletRequest request) {
    UserDTO me = userService.getMe(request.getHeader("Authorization"));
    if (!me.getRole().name().equals("STUDENT")) {
      return ResponseEntity.status(403).build();
    }

    List<ConvocationDto> convocations = convocationService.getConvocationsByStudentId(me.getId());

    return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(convocations);
  }

  @PostMapping("/applications/{candidatureId}/accept")
  public ResponseEntity<?> acceptCandidature(
      HttpServletRequest request, @PathVariable Long candidatureId) {

    UserDTO me = userService.getMe(request.getHeader("Authorization"));
    if (!me.getRole().name().equals("STUDENT")) {
      return ResponseEntity.status(403).build();
    }

    try {
      CandidatureDto updated = candidatureService.acceptByStudent(candidatureId, me.getId());
      return ResponseEntity.ok(updated);
    } catch (IllegalStateException e) {
      return ResponseEntity.badRequest().body(e.getMessage());
    } catch (RuntimeException e) {
      return ResponseEntity.status(404).body("Candidature non trouvée");
    }
  }

  @PostMapping("/applications/{candidatureId}/reject")
  public ResponseEntity<?> rejectCandidature(
      HttpServletRequest request, @PathVariable Long candidatureId) {

    UserDTO me = userService.getMe(request.getHeader("Authorization"));
    if (!me.getRole().name().equals("STUDENT")) {
      return ResponseEntity.status(403).build();
    }

    try {
      CandidatureDto updated = candidatureService.rejectByStudent(candidatureId, me.getId());
      return ResponseEntity.ok(updated);
    } catch (IllegalStateException e) {
      return ResponseEntity.badRequest().body(e.getMessage());
    } catch (RuntimeException e) {
      return ResponseEntity.status(404).body("Candidature non trouvée");
    }
  }

  @PostMapping("/ententes/{ententeId}/signer")
  public ResponseEntity<EntenteStageDto> signerEntente(
      HttpServletRequest request, @PathVariable Long ententeId) {
    UserDTO me = userService.getMe(request.getHeader("Authorization"));

    if (!me.getRole().name().equals("STUDENT")) {
      return ResponseEntity.status(403).build();
    }

    try {
      EntenteStageDto result = ententeStageService.signerParEtudiant(ententeId, me.getId());
      return ResponseEntity.ok(result);
    } catch (EntityNotFoundException e) {
      return ResponseEntity.status(404)
          .body(EntenteStageDto.withErrorMessage("Entente non trouvée"));
    } catch (IllegalArgumentException | IllegalStateException e) {
      return ResponseEntity.badRequest().body(EntenteStageDto.withErrorMessage(e.getMessage()));
    }
  }

  @GetMapping("/ententes")
  public ResponseEntity<List<EntenteStageDto>> getEntentesPourEtudiant(HttpServletRequest request) {
    UserDTO me = userService.getMe(request.getHeader("Authorization"));

    if (!me.getRole().name().equals("STUDENT")) {
      return ResponseEntity.status(403).build();
    }

    try {

      List<EntenteStageDto> studentEntentes =
          ententeStageService.getEntentesByStudentId(me.getId());
      return ResponseEntity.ok(studentEntentes);
    } catch (Exception e) {
      return ResponseEntity.status(500).build();
    }
  }

  @GetMapping("/ententes/{ententeId}/pdf")
  public ResponseEntity<Resource> getEntentePdf(
      HttpServletRequest request, @PathVariable Long ententeId) {
    UserDTO me = userService.getMe(request.getHeader("Authorization"));

    if (!me.getRole().name().equals("STUDENT")) {
      return ResponseEntity.status(403).build();
    }

    try {
      EntenteStageDto entente = ententeStageService.getEntenteById(ententeId);

      if (entente.getStudent() == null || !me.getEmail().equals(entente.getStudent().getEmail())) {
        return ResponseEntity.status(403).build();
      }

      if (entente.getCheminDocumentPDF() == null || entente.getCheminDocumentPDF().isBlank()) {
        return ResponseEntity.status(404).build();
      }

      Path filePath = Paths.get(entente.getCheminDocumentPDF());
      Resource resource = new UrlResource(filePath.toUri());

      if (resource.exists() && resource.isReadable()) {
        String filename =
            "Entente_Stage_"
                + entente.getStudent().getFirstName()
                + "_"
                + entente.getStudent().getLastName()
                + ".pdf";

        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_PDF)
            .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
            .body(resource);
      } else {
        return ResponseEntity.notFound().build();
      }

    } catch (EntityNotFoundException e) {
      return ResponseEntity.status(404).build();
    } catch (Exception e) {
      return ResponseEntity.status(500).build();
    }
  }
}
