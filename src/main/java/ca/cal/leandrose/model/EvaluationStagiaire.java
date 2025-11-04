package ca.cal.leandrose.model;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;


@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EvaluationStagiaire {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate dateEvaluation;

    @ManyToOne
    @JoinColumn(name = "employeur_id")
    private Employeur employeur;

    @ManyToOne
    @JoinColumn(name = "student_id")
    private Student student;

    @ManyToOne
    @JoinColumn(name = "internship_id")
    private InternshipOffer internshipOffer;
//
//    @Column(columnDefinition = "TEXT")
//    private String evaluationTemplate;
//
//    @Column(columnDefinition = "TEXT")
//    private String evaluationResponses;

    private String pdfFilePath;


//    @Column(length = 2000)
//    private String generalComment;

    private boolean submitted;

}
