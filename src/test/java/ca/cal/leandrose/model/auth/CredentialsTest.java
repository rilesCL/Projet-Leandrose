package ca.cal.leandrose.model.auth;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Collection;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

class CredentialsTest {

    @Test
    @DisplayName("Builder creates valid Credentials with expected values")
    void testBuilderCreatesValidCredentials() {
        Credentials creds =
                Credentials.builder()
                        .email("test@example.com")
                        .password("secure123")
                        .role(Role.STUDENT)
                        .build();

        assertEquals("test@example.com", creds.getEmail());
        assertEquals("secure123", creds.getPassword());
        assertEquals(Role.STUDENT, creds.getRole());
    }

    @Test
    @DisplayName("getAuthorities returns a SimpleGrantedAuthority with role name")
    void testGetAuthorities() {
        Credentials creds =
                Credentials.builder()
                        .email("manager@example.com")
                        .password("pass")
                        .role(Role.GESTIONNAIRE)
                        .build();

        Collection<? extends GrantedAuthority> authorities = creds.getAuthorities();

        assertEquals(1, authorities.size());
        assertTrue(authorities.contains(new SimpleGrantedAuthority("GESTIONNAIRE")));
    }

    @Test
    @DisplayName("getUsername returns email")
    void testGetUsername() {
        Credentials creds =
                Credentials.builder()
                        .email("user@example.com")
                        .password("1234")
                        .role(Role.EMPLOYEUR)
                        .build();

        assertEquals("user@example.com", creds.getUsername());
    }

    @Test
    @DisplayName("All UserDetails boolean methods return true")
    void testUserDetailsFlags() {
        Credentials creds =
                Credentials.builder()
                        .email("x@y.com")
                        .password("pwd")
                        .role(Role.STUDENT)
                        .build();

        assertTrue(creds.isAccountNonExpired());
        assertTrue(creds.isAccountNonLocked());
        assertTrue(creds.isCredentialsNonExpired());
        assertTrue(creds.isEnabled());
    }

    @Test
    @DisplayName("toString does not throw and contains key fields")
    void testToString() {
        Credentials creds =
                Credentials.builder()
                        .email("toString@test.com")
                        .password("pwd")
                        .role(Role.STUDENT)
                        .build();

        String out = creds.toString();

        assertNotNull(out);
        assertTrue(out.contains("toString@test.com"));
        assertTrue(out.contains("STUDENT"));
    }

    @Test
    @DisplayName("Equals and hashCode work because of @Data")
    void testEqualsAndHashCode() {
        Credentials c1 =
                Credentials.builder()
                        .email("same@example.com")
                        .password("123")
                        .role(Role.STUDENT)
                        .build();

        Credentials c2 =
                Credentials.builder()
                        .email("same@example.com")
                        .password("123")
                        .role(Role.STUDENT)
                        .build();

        assertEquals(c1, c2);
        assertEquals(c1.hashCode(), c2.hashCode());
    }
}
