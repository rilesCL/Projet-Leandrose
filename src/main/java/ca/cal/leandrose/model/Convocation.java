package ca.cal.leandrose.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Convocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    @OneToOne
    @JoinColumn(name = "candidature_id", nullable = false, unique = true)
    Candidature candidature;
    @Column(nullable = false)
    LocalDateTime convocationDate;
    @Column(nullable = false)
    String location;
    @Column(nullable = false)
    String personnalMessage;

    public Convocation (Candidature candidature, LocalDateTime convocationDate, String location, String personnalMessage) {
        this.personnalMessage = personnalMessage;
        this.candidature = candidature;
        this.convocationDate = convocationDate;
        this.location = location;
    }
}
