package ca.cal.leandrose.presentation;

import ca.cal.leandrose.model.Employeur;
import ca.cal.leandrose.model.InternshipOffer;
import ca.cal.leandrose.presentation.request.InternshipOfferRequest;
import ca.cal.leandrose.service.AuthService;
import ca.cal.leandrose.service.InternshipOfferService;
import ca.cal.leandrose.service.UserAppService;
import ca.cal.leandrose.service.dto.JWTAuthResponse;
import ca.cal.leandrose.service.dto.LoginDTO;
import ca.cal.leandrose.service.dto.UserDTO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/user")
public class UserController {

    private final AuthService authService;
    private final UserAppService userService;
    private final InternshipOfferService internshipOfferService;

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginDTO loginDto) {
        try {
            String accessToken = authService.login(loginDto);
            final JWTAuthResponse authResponse = new JWTAuthResponse(accessToken);
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(authResponse);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body("{\"error\":\"Invalid credentials\"}");
        }
    }

    @GetMapping("/me")
    public ResponseEntity<UserDTO> getMe(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(userService.getMe(authHeader));
    }
    @GetMapping("/employeur/offers")
    public ResponseEntity<List<InternshipOffer>> getMyOffers(HttpServletRequest request) {
        UserDTO me = userService.getMe(request.getHeader("Authorization"));

        if (!me.getRole().name().equals("ROLE_EMPLOYEUR")) {
            return ResponseEntity.status(403).build();
        }

        List<InternshipOffer> offers = internshipOfferService.getOffersByEmployeurId(me.getId());
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(offers);
    }

    @PostMapping(value = "/employeur/offers", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<InternshipOffer> uploadOffer(
            HttpServletRequest request,
            @RequestPart("offer") InternshipOfferRequest offerRequest,
            @RequestPart("pdfFile") MultipartFile pdfFile
    ) throws IOException {
        UserDTO me = userService.getMe(request.getHeader("Authorization"));

        if (!me.getRole().name().equals("EMPLOYEUR")) {
            return ResponseEntity.status(403).build();
        }

        Employeur employeur = new Employeur();
        employeur.setId(me.getId());

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

}
