
package ca.cal.leandrose.presentation;


import ca.cal.leandrose.model.Cv;
import ca.cal.leandrose.repository.CvRepository;
import ca.cal.leandrose.service.ManagerService;
import ca.cal.leandrose.service.dto.CvDto;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequestMapping("/gestionnaire")
@RequiredArgsConstructor
public class ManagerController {
    private final ManagerService service;
    private final CvRepository cvRepository;

    @PostMapping("/cv/{cvId}/approve")
    public ResponseEntity<CvDto> approveCv(@PathVariable Long cvId){
        return ResponseEntity.ok(service.approveCv(cvId));
    }

    @PostMapping("/cv/{cvId}/reject")
    public ResponseEntity<CvDto> rejectCv(@PathVariable Long cvId){
        return ResponseEntity.ok(service.rejectCv(cvId));
    }

    @GetMapping
    public ResponseEntity<List<CvDto>> getPendingCvs(){
        return ResponseEntity.ok(service.getPendingCvs());
    }

    @GetMapping("/cv/{cvId}/download")
    public ResponseEntity<Resource> downloadCv(@PathVariable Long cvId) throws IOException {
        Cv cv = cvRepository.findById(cvId)
                .orElseThrow(() -> new RuntimeException("CV introuvable"));

        Path filePath = Paths.get(cv.getPdfPath());
        Resource resource = new UrlResource(filePath.toUri());

        if (!resource.exists()) {
            throw new RuntimeException("Fichier non trouvé: " + cv.getPdfPath());
        }

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filePath.getFileName() + "\"")
                .body(resource);
    }
}


