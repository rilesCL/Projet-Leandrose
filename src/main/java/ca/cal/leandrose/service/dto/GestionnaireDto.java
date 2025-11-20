package ca.cal.leandrose.service.dto;

import ca.cal.leandrose.model.Gestionnaire;
import ca.cal.leandrose.model.auth.Role;
import lombok.Builder;
import lombok.Getter;

@Getter
public class GestionnaireDto extends UserDTO {
  private String phoneNumber;

  @Builder
  public GestionnaireDto(
      Long id, String firstName, String lastname, String email, Role role, String phoneNumber) {
    super(id, firstName, lastname, email, role);
    this.phoneNumber = phoneNumber;
  }

  public GestionnaireDto() {}

  public static GestionnaireDto create(Gestionnaire gestionnaire) {
    return GestionnaireDto.builder()
        .id(gestionnaire.getId())
        .firstName(gestionnaire.getFirstName())
        .lastname(gestionnaire.getLastName())
        .email(gestionnaire.getEmail())
        .role(gestionnaire.getRole())
        .phoneNumber(gestionnaire.getPhoneNumber())
        .build();
  }

  public static GestionnaireDto empty() {
    return new GestionnaireDto();
  }
}
