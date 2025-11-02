package ca.cal.leandrose.model;

import ca.cal.leandrose.model.auth.Credentials;
import ca.cal.leandrose.model.auth.Role;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public abstract class UserApp {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String firstName;

  private String lastName;

  @Embedded private Credentials credentials;

  public String getEmail() {
    return credentials.getEmail();
  }

  public String getPassword() {
    return credentials.getPassword();
  }

  public Role getRole() {
    return credentials.getRole();
  }

  public Collection<? extends GrantedAuthority> getAuthorities() {
    return credentials.getAuthorities();
  }
}
