package ca.cal.leandrose.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InternshipOffer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String description;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private Integer durationInWeeks;

    @Column(nullable = false)
    private String address;

    @Column
    private float remuneration;

    @ManyToOne
    private Employeur employeur;

    private String pdfPath;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    public enum Status {
        PENDING_VALIDATION, PUBLISHED, ASSIGNED
    }
}
