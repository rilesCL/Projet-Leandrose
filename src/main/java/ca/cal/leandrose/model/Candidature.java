package ca.cal.leandrose.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Candidature {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(nullable = false)
    private Student student;

    @ManyToOne
    @JoinColumn(nullable = false)
    private  InternshipOffer internshipOffer;

    @ManyToOne
    @JoinColumn(nullable = false)
    private Cv cv;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @Column(nullable = false)
    private LocalDate applicationDate;

    @Column(length = 2000)
    private String feedback;

    public enum Status {
        PENDING,    // En attente de réponse employeur
        ACCEPTED,   // Acceptée par l'employeur
        REJECTED,    // Rejetée par l'employeur
        RETAINED,    // Retenue (shortlist)
        TO_REVIEW,   // Marquée « À étudier »
    }

}
