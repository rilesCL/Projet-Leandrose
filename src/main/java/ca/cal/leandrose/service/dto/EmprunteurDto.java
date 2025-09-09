package ca.cal.leandrose.service.dto;

import ca.cal.leandrose.model.Emprunteur;
import ca.cal.leandrose.model.auth.Role;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
public class EmprunteurDto extends UserDTO {
    private LocalDate since;

    @Builder
    public EmprunteurDto(Long id, String firstName, String lastname, String email, Role role, LocalDate since) {
        super(id, firstName, lastname, email, role);
        this.since = since;
    }

    public EmprunteurDto() {}

    public static EmprunteurDto create(Emprunteur emprunteur) {
        return EmprunteurDto.builder()
                .id(emprunteur.getId())
                .firstName(emprunteur.getFirstName())
                .lastname(emprunteur.getLastName())
                .email(emprunteur.getEmail())
                .role(emprunteur.getRole())
                .since(emprunteur.getSince())
                .build();



    }

    public static EmprunteurDto empty() {
        return new EmprunteurDto();
    }
}
