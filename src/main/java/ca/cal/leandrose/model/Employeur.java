package ca.cal.leandrose.model;

import ca.cal.leandrose.model.auth.Credentials;
import ca.cal.leandrose.model.auth.Role;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@DiscriminatorValue("E")
@Getter
@NoArgsConstructor
public class Employeur extends UserApp {
    private String companyName;
    private String field;
    @Builder
    public Employeur(
            Long id, String firstName, String lastName, String email, String password,
            String companyName, String field){
        super(id, firstName, lastName, Credentials.builder().email(email).password(password).role(Role.EMPLOYEUR).build());
        this.companyName = companyName;
        this.field = field;
    }
}
