package ca.cal.leandrose.presentation;

//import ca.cal.leandrose.model.InternshipOffer;
//import ca.cal.leandrose.model.Student;
import ca.cal.leandrose.repository.StudentRepository;
import ca.cal.leandrose.security.exception.UserNotFoundException;
import ca.cal.leandrose.service.*;
import ca.cal.leandrose.service.dto.*;
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
    private final StudentRepository studentRepository;
    private final UserAppService userService;
    private final CandidatureService candidatureService;
    private final StudentService studentService;
    private final InternshipOfferService internshipOfferService;
    private final ConvocationService convocationService;

    @PostMapping(value = "/cv")
    public ResponseEntity<CvDto> uploadCv(
            HttpServletRequest request,
            @RequestPart("pdfFile") MultipartFile pdfFile
    ) throws IOException {
        UserDTO me = userService.getMe(request.getHeader("Authorization"));
        if (!me.getRole().name().equals("STUDENT")) {
            return ResponseEntity.status(403).build();
        }

        StudentDto student = studentService.getStudentById(me.getId());
//        Student student = studentRepository.findById(me.getId()).orElseThrow(UserNotFoundException::new);
        CvDto cvDto = cvService.uploadCv(student.getId(), pdfFile);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(cvDto);
    }

    @GetMapping(value = "/cv", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CvDto> getCv(
            @RequestHeader(name = "Authorization", required = false) String authorization
    ) {
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

//        Student student = studentRepository.findById(me.getId()).orElseThrow(UserNotFoundException::new);
        CvDto cvDto;
        try {
            cvDto = cvService.getCvByStudentId(student.getId());
        } catch (RuntimeException e) {
            return ResponseEntity.status(404)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(null);
        }
        return ResponseEntity.ok().body(cvDto);
    }

    @GetMapping("/cv/download")
    public ResponseEntity<Resource> downloadCv(
            @RequestHeader(name = "Authorization", required = false) String authorization
    ) {
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
//        Student student = studentRepository.findById(me.getId()).orElseThrow(UserNotFoundException::new);

        try {
            CvDto cvDto = cvService.getCvByStudentId(student.getId());
            Path filePath = Paths.get(cvDto.getPdfPath());
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                return ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_PDF)
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filePath.getFileName().toString() + "\"")
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

//        Student student = studentRepository.findById(me.getId()).orElseThrow(UserNotFoundException::new);
        List<InternshipOfferDto> offers = internshipOfferService.getPublishedOffersForStudents(student.getProgram());

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(offers);
    }

    @GetMapping("/offers/{id}")
    public ResponseEntity<InternshipOfferDto> getOfferDetails(@PathVariable Long id) {
        InternshipOfferDto offer = internshipOfferService.getOffer(id);

        if (!"PUBLISHED".equals(offer.getStatus())){
            return ResponseEntity.status(403).build();
        }

//        if (offer.getStatus().equals(InternshipOffer.Status.PUBLISHED)) {
//            return ResponseEntity.status(403).build();
//        }

        return ResponseEntity.ok(offer);
    }

    @PostMapping("/offers/{offerId}/apply")
    public ResponseEntity<CandidatureDto> applyToOffer(
            HttpServletRequest request,
            @PathVariable Long offerId,
            @RequestParam Long cvId) {

        UserDTO me = userService.getMe(request.getHeader("Authorization"));

        if (!me.getRole().name().equals("STUDENT")) {
            return ResponseEntity.status(403).build();
        }

        try {
            CandidatureDto candidature = candidatureService.postuler(
                    me.getId(), offerId, cvId);
            return ResponseEntity.ok(candidature);
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/applications")
    public ResponseEntity<List<CandidatureDto>> getMyCandidatures(
            HttpServletRequest request) {

        UserDTO me = userService.getMe(request.getHeader("Authorization"));
        if (!me.getRole().name().equals("STUDENT")) {
            return ResponseEntity.status(403).build();
        }

        List<CandidatureDto> candidatures =
                candidatureService.getCandidaturesByStudent(me.getId());
        return ResponseEntity.ok(candidatures);
    }

    @GetMapping("/offers/{id}/pdf")
    public ResponseEntity<byte[]> downloadOfferPdf(@PathVariable Long id) {
        try {
            InternshipOfferDto offer = internshipOfferService.getOffer(id);

            if (!"PUBLISHED".equals(offer.getStatus())){
                return ResponseEntity.status(403).build();
            }
//            if (offer.getStatus().equals(InternshipOffer.Status.PUBLISHED.name())) {
//                return ResponseEntity.status(403).build();
//            }

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

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(convocations);
    }
}