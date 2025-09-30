package ca.cal.leandrose.presentation;

import ca.cal.leandrose.model.Student;
import ca.cal.leandrose.repository.StudentRepository;
import ca.cal.leandrose.security.exception.UserNotFoundException;
import ca.cal.leandrose.service.CvService;
import ca.cal.leandrose.service.UserAppService;
import ca.cal.leandrose.service.dto.CvDto;
import ca.cal.leandrose.service.dto.UserDTO;
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

@RestController
@RequestMapping("/student")
@RequiredArgsConstructor
public class StudentController {
    private final CvService cvService;
    private final StudentRepository studentRepository;
    private final UserAppService userService;

    @PostMapping(value = "/cv")
    public ResponseEntity<CvDto> uploadCv(
            HttpServletRequest request,
            @RequestPart("pdfFile") MultipartFile pdfFile
    ) throws IOException {
        UserDTO me = userService.getMe(request.getHeader("Authorization"));
        if (!me.getRole().name().equals("STUDENT")) {
            return ResponseEntity.status(403).build();
        }
        Student student = studentRepository.findById(me.getId()).orElseThrow(UserNotFoundException::new);
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
        Student student = studentRepository.findById(me.getId()).orElseThrow(UserNotFoundException::new);
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

        Student student = studentRepository.findById(me.getId()).orElseThrow(UserNotFoundException::new);

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
}