package ca.cal.leandrose.presentation;

import ca.cal.leandrose.service.AuthService;
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


@RequiredArgsConstructor
@RestController
@RequestMapping("/user")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174"})
public class UserController {

    private final AuthService authService;
    private final UserAppService userService;

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


}
