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

    @OneToOne(mappedBy = "candidature", cascade = CascadeType.ALL, optional = true)
    private Convocation convocation;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @Column(nullable = false)
    private LocalDate applicationDate;

    public Long getEmployeurId() {
        return internshipOffer.getEmployeurId();
    }


    public enum Status {
        PENDING,
        ACCEPTED,
        CONVENED,
        REJECTED
    }

}
