
package ca.cal.leandrose.presentation;


import ca.cal.leandrose.service.ManagerService;
import ca.cal.leandrose.service.dto.CvDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/gestionnaire")
@RequiredArgsConstructor
public class ManagerController {
    private final ManagerService service;

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
}


