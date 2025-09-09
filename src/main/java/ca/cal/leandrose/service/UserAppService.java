package ca.cal.leandrose.service;

import ca.cal.leandrose.model.*;
import ca.cal.leandrose.repository.EmprunteurRepository;
import ca.cal.leandrose.repository.GestionnaireRepository;
import ca.cal.leandrose.repository.PreposeRepository;
import ca.cal.leandrose.repository.UserAppRepository;
import ca.cal.leandrose.security.JwtTokenProvider;
import ca.cal.leandrose.security.exception.UserNotFoundException;
import ca.cal.leandrose.service.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserAppService {
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserAppRepository userAppRepository;
    private final EmprunteurRepository emprunteurRepository;
    private final PreposeRepository preposeRepository;
    private final GestionnaireRepository gestionnaireRepository;

    public String authenticateUser(LoginDTO loginDto) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginDto.getEmail(), loginDto.getPassword()));
        return jwtTokenProvider.generateToken(authentication);
    }

    public UserDTO getMe(String token) {
        token = token.startsWith("Bearer") ? token.substring(7) : token;
        String email = jwtTokenProvider.getEmailFromJWT(token);
        UserApp user = userAppRepository.findUserAppByEmail(email).orElseThrow(UserNotFoundException::new);
        return switch(user.getRole()){
            case EMPRUNTEUR -> getEmprunteurDto(user.getId());
            case PREPOSE -> getPreposeDto(user.getId());
            case GESTIONNAIRE -> getGestionnaireDto(user.getId());
        };
    }

    private GestionnaireDto getGestionnaireDto(Long id) {
        final Optional<Gestionnaire> gestionnaireOptional = gestionnaireRepository.findById(id);
        return gestionnaireOptional.isPresent() ?
                GestionnaireDto.create(gestionnaireOptional.get()) :
                GestionnaireDto.empty();
    }

    private PreposeDto getPreposeDto(Long id) {
        final Optional<Prepose> preposeOptional = preposeRepository.findById(id);
        return preposeOptional.isPresent() ?
                PreposeDto.create(preposeOptional.get()) :
                PreposeDto.empty();
    }

    private EmprunteurDto getEmprunteurDto(Long id) {
        final Optional<Emprunteur> emprunteurOptional = emprunteurRepository.findById(id);
        return emprunteurOptional.isPresent() ?
                EmprunteurDto.create(emprunteurOptional.get()) :
                EmprunteurDto.empty();
    }
}
