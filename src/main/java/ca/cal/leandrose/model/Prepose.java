package ca.cal.leandrose.model;

import ca.cal.leandrose.model.auth.Credentials;
import ca.cal.leandrose.model.auth.Role;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@DiscriminatorValue("P")
@Getter
@Setter
@NoArgsConstructor
public class Prepose extends UserApp {
    private String passeKey;
    @Builder
    public Prepose(
            Long id, String firstName, String lastName, String email, String password,
            String passeKey){
        super(id, firstName, lastName, Credentials.builder().email(email).password(password).role(Role.PREPOSE).build());
        this.passeKey = passeKey;
    }
}
