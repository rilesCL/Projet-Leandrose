package ca.cal.leandrose.model;

import jakarta.persistence.Column;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.*;

import java.time.LocalDate;
import java.util.Optional;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Convocation {
    @Column
    @OneToOne
    @JoinColumn(name = "candidature_id", nullable = false, unique = true)
    Candidature candidature;
    @Column(nullable = false)
    LocalDate convocationDate;
    @Column(nullable = false)
    String location;
    @Column(nullable = false)
    String personnalMessage;

    public Convocation (Candidature candidature,Convocation convocation, String location) {
        this.candidature = candidature;
        this.convocationDate = convocation.getConvocationDate();
        this.location = location;
    }
}
