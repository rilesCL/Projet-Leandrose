package ca.cal.leandrose.presentation;

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
public class UserController {

	private final UserAppService userService;

	@PostMapping("/login")
	public ResponseEntity<JWTAuthResponse> authenticateUser(@RequestBody LoginDTO loginDto){
		try {
			String accessToken = userService.authenticateUser(loginDto);
			final JWTAuthResponse authResponse = new JWTAuthResponse(accessToken);
			return ResponseEntity.accepted()
					.contentType(MediaType.APPLICATION_JSON)
					.body(authResponse);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new JWTAuthResponse());
		}
	}

	@GetMapping("/me")
	public ResponseEntity<UserDTO> getMe(HttpServletRequest request){
		return ResponseEntity.accepted().contentType(MediaType.APPLICATION_JSON).body(
			userService.getMe(request.getHeader("Authorization")));
	}


}
