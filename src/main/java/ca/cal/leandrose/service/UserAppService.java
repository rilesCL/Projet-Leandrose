package ca.cal.leandrose.service;

import ca.cal.leandrose.model.*;
import ca.cal.leandrose.presentation.request.UpdateUserRequest;
import ca.cal.leandrose.repository.*;
import ca.cal.leandrose.security.JwtTokenProvider;
import ca.cal.leandrose.security.exception.UserNotFoundException;
import ca.cal.leandrose.service.dto.*;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserAppService {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserAppRepository userAppRepository;
    private final StudentRepository studentRepository;
    private final EmployeurRepository employeurRepository;
    private final GestionnaireRepository gestionnaireRepository;
    private final ProfRepository profRepository;
    private final PasswordEncoder passwordEncoder;



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



    private UserApp getUserFromAuthHeader(String authHeader) {
        String token =
                authHeader.startsWith("Bearer") ? authHeader.substring(7) : authHeader;
        String email = jwtTokenProvider.getEmailFromJWT(token);

        return userAppRepository
                .findUserAppByEmail(email)
                .orElseThrow(UserNotFoundException::new);
    }



    public boolean verifyPassword(String authHeader, String rawPassword) {
        UserApp user = getUserFromAuthHeader(authHeader);
        return passwordEncoder.matches(rawPassword, user.getPassword());
    }


    public UserDTO updateProfile(String authHeader, UpdateUserRequest req) {
        UserApp user = getUserFromAuthHeader(authHeader);

        if (req.getNewPassword() != null
                && !req.getNewPassword().isBlank()
                && user.getCredentials() != null) {
            if (req.getCurrentPassword() == null || req.getCurrentPassword().isBlank()) {
                throw new IllegalArgumentException("Le mot de passe actuel est requis pour changer le mot de passe");
            }
            if (!passwordEncoder.matches(req.getCurrentPassword(), user.getPassword())) {
                throw new IllegalArgumentException("Le mot de passe actuel est incorrect");
            }
        }

        if (req.getFirstName() != null) {
            user.setFirstName(req.getFirstName());
        }
        if (req.getLastName() != null) {
            user.setLastName(req.getLastName());
        }

        if (req.getEmail() != null && user.getCredentials() != null) {
            user.getCredentials().setEmail(req.getEmail().trim());
        }

        if (req.getNewPassword() != null
                && !req.getNewPassword().isBlank()
                && user.getCredentials() != null) {

            String encoded = passwordEncoder.encode(req.getNewPassword());
            user.getCredentials().setPassword(encoded);
        }

        if (req.getPhoneNumber() != null) {
            switch (user.getRole()) {
                case GESTIONNAIRE -> {
                    Gestionnaire g = gestionnaireRepository.findById(user.getId()).orElseThrow();
                    g.setPhoneNumber(req.getPhoneNumber());
                    gestionnaireRepository.save(g);
                }
                case STUDENT -> {
                    Student s = studentRepository.findById(user.getId()).orElseThrow();
                    s.setPhoneNumber(req.getPhoneNumber());
                    studentRepository.save(s);
                }
                case EMPLOYEUR -> {
                    Employeur e = employeurRepository.findById(user.getId()).orElseThrow();
                    e.setPhoneNumber(req.getPhoneNumber());
                    employeurRepository.save(e);
                }
                case PROF -> {
                    Prof p = profRepository.findById(user.getId()).orElseThrow();
                    p.setPhoneNumber(req.getPhoneNumber());
                    profRepository.save(p);
                }
            }
        }

        userAppRepository.save(user);

        return getMe(authHeader);
    }
}
