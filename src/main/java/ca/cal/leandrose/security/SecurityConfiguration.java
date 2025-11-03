package ca.cal.leandrose.security;

import ca.cal.leandrose.repository.UserAppRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import static org.springframework.http.HttpMethod.*;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
@Profile("!test")
public class SecurityConfiguration {

  private final JwtTokenProvider jwtTokenProvider;
  private final UserAppRepository userRepository;
  private final JwtAuthenticationEntryPoint authenticationEntryPoint;

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http.csrf(AbstractHttpConfigurer::disable)
        .cors(Customizer.withDefaults())
        .authorizeHttpRequests(
            auth ->
                auth.requestMatchers(POST, "/user/login", "/api/register/**")
                    .permitAll()
                    .requestMatchers(GET, "/api/register/programs")
                    .permitAll()
                    .requestMatchers(GET, "/gestionnaire/**")
                    .hasAuthority("GESTIONNAIRE")
                    .requestMatchers(POST, "/student/**")
                    .hasAuthority("STUDENT")
                    .requestMatchers(GET, "/student/**")
                    .hasAuthority("STUDENT")
                    .requestMatchers(
                        GET, "/student/ententes", "/student/ententes/*", "/student/ententes/*/pdf")
                    .hasAuthority("STUDENT")
                    .requestMatchers(POST, "/student/ententes/*/signer")
                    .hasAuthority("STUDENT")
                    .requestMatchers(POST, "/employeur/**")
                    .hasAuthority("EMPLOYEUR")
                    .requestMatchers(POST, "/employer/ententes/*/signer")
                    .hasAuthority("EMPLOYEUR")
                    .requestMatchers(PUT, "/employeur/**")
                    .hasAuthority("EMPLOYEUR").requestMatchers(PUT, "/student/**")
                    .hasAuthority("STUDENT")
                    .requestMatchers(GET, "/employeur/**")
                    .hasAuthority("EMPLOYEUR")
                    .requestMatchers(GET, "/ententes")
                    .hasAuthority("EMPLOYEUR")
                    .requestMatchers(
                        GET,
                        "/student/offers",
                        "/student/offers/*",
                        "/student/offers/*/pdf",
                        "/student/applications")
                    .hasAuthority("STUDENT")
                    .requestMatchers(POST, "/employeur/offers")
                    .hasAuthority("EMPLOYEUR")
                    .requestMatchers(GET, "/student/cv", "/student/cv/download")
                    .hasAuthority("STUDENT")
                    .requestMatchers(GET, "/user/*")
                    .hasAnyAuthority("EMPLOYEUR", "GESTIONNAIRE", "STUDENT")
                    .requestMatchers(GET, "/employeur/offers", "/employeur/offers/*/download")
                    .hasAuthority("EMPLOYEUR")
                    .requestMatchers(
                        GET,
                        "/employeur/offers/*/candidatures",
                        "/employeur/candidatures",
                        "/employeur/candidatures/*/cv")
                    .hasAuthority("EMPLOYEUR")
                    .requestMatchers("/gestionnaire/**")
                    .hasAuthority("GESTIONNAIRE")
                    .requestMatchers("/user/me")
                    .permitAll()
                    .anyRequest()
                    .denyAll())
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
        .exceptionHandling(handler -> handler.authenticationEntryPoint(authenticationEntryPoint));

    return http.build();
  }

  @Bean
  public CorsFilter corsFilter() {
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowCredentials(true);
    config.addAllowedOrigin("http://localhost:5173");
    config.addAllowedMethod("*");
    config.addAllowedHeader("*");
    source.registerCorsConfiguration("/**", config);
    return new CorsFilter(source);
  }

  @Bean
  public JwtAuthenticationFilter jwtAuthenticationFilter() {
    return new JwtAuthenticationFilter(jwtTokenProvider, userRepository);
  }

  @Bean
  public AuthenticationManager authenticationManager(
      AuthenticationConfiguration authenticationConfiguration) throws Exception {
    return authenticationConfiguration.getAuthenticationManager();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
}
