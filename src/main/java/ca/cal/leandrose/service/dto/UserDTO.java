package ca.cal.leandrose.service.dto;

import ca.cal.leandrose.model.UserApp;
import ca.cal.leandrose.model.auth.Role;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Data
public class UserDTO {
  private Long id;
  private String firstName;
  private String lastName;
  private String email;
  private Role role;

  public UserDTO(String firstName, String lastName, Role role) {
    this.firstName = firstName;
    this.lastName = lastName;
    this.role = role;
  }

  public UserDTO(UserApp user) {
    this.id = user.getId();
    this.firstName = user.getFirstName();
    this.lastName = user.getLastName();
    this.email = user.getEmail();
    this.role = user.getRole();
  }
}
