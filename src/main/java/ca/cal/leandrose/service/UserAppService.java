package ca.cal.leandrose.service;

import ca.cal.leandrose.model.*;
import ca.cal.leandrose.repository.*;
import ca.cal.leandrose.security.JwtTokenProvider;
import ca.cal.leandrose.security.exception.UserNotFoundException;
import ca.cal.leandrose.service.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserAppService {
  private final JwtTokenProvider jwtTokenProvider;
  private final UserAppRepository userAppRepository;
  private final StudentRepository studentRepository;
  private final EmployeurRepository employeurRepository;
  private final GestionnaireRepository gestionnaireRepository;
  private final ProfRepository profRepository;

  public UserDTO getMe(String token) {
    token = token.startsWith("Bearer") ? token.substring(7) : token;
    String email = jwtTokenProvider.getEmailFromJWT(token);
    UserApp user =
        userAppRepository.findUserAppByEmail(email).orElseThrow(UserNotFoundException::new);
    return switch (user.getRole()) {
      case EMPLOYEUR -> getEmployeurDto(user.getId());
      case GESTIONNAIRE -> getGestionnaireDto(user.getId());
      case STUDENT -> getStudentDto(user.getId());
      case PROF -> getProfDto(user.getId());
    };
  }

  private GestionnaireDto getGestionnaireDto(Long id) {
    final Optional<Gestionnaire> gestionnaireOptional = gestionnaireRepository.findById(id);
    return gestionnaireOptional.isPresent()
        ? GestionnaireDto.create(gestionnaireOptional.get())
        : GestionnaireDto.empty();
  }

  private StudentDto getStudentDto(Long id) {
    final Optional<Student> preposeOptional = studentRepository.findById(id);
    return preposeOptional.isPresent()
        ? StudentDto.create(preposeOptional.get())
        : StudentDto.empty();
  }

  private EmployeurDto getEmployeurDto(Long id) {
    final Optional<Employeur> employeurOptional = employeurRepository.findById(id);
    return employeurOptional.isPresent()
        ? EmployeurDto.create(employeurOptional.get())
        : EmployeurDto.empty();
  }

  private ProfDto getProfDto(Long id) {
    final Optional<Prof> profOptional = profRepository.findById(id);
    return profOptional.isPresent()
        ? ProfDto.create(profOptional.get())
        : ProfDto.empty();
  }
}
