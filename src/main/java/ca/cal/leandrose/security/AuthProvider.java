package ca.cal.leandrose.security;

import ca.cal.leandrose.model.UserApp;
import ca.cal.leandrose.repository.UserAppRepository;
import ca.cal.leandrose.security.exception.AuthenticationException;
import ca.cal.leandrose.security.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthProvider implements AuthenticationProvider {
  private final PasswordEncoder passwordEncoder;
  private final UserAppRepository userAppRepository;

  @Override
  public Authentication authenticate(Authentication authentication) {
    UserApp user = loadUserByEmail(authentication.getPrincipal().toString());
    validateAuthentication(authentication, user);
    return new UsernamePasswordAuthenticationToken(
        user.getEmail(), user.getPassword(), user.getAuthorities());
  }

  @Override
  public boolean supports(Class<?> authentication) {
    return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
  }

  private UserApp loadUserByEmail(String email) throws UsernameNotFoundException {
    return userAppRepository.findUserAppByEmail(email).orElseThrow(UserNotFoundException::new);
  }

  private void validateAuthentication(Authentication authentication, UserApp user) {
    if (!passwordEncoder.matches(authentication.getCredentials().toString(), user.getPassword()))
      throw new AuthenticationException(HttpStatus.FORBIDDEN, "Incorrect username or password");
  }
}
