package ca.cal.leandrose.model;

import ca.cal.leandrose.model.auth.Credentials;
import ca.cal.leandrose.model.auth.Role;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@DiscriminatorValue("G")
@Getter
@NoArgsConstructor
public class Gestionnaire extends UserApp {
	@Column(unique = true, nullable = false)
	private String phoneNumber;

	@Builder
	public Gestionnaire(
	Long id, String firstName, String lastName, String email, String password,
        String phoneNumber){
		super(id, firstName, lastName, Credentials.builder().email(email).password(password).role(Role.GESTIONNAIRE).build());
		this.phoneNumber = phoneNumber;
	}
}
