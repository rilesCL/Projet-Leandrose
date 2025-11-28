package ca.cal.leandrose.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
  @JoinColumn(name = "professeur_id")
  private Prof professeur;

  @ManyToOne
  @JoinColumn(name = "internship_id")
  private InternshipOffer internshipOffer;

  @ManyToOne
  @JoinColumn(name = "entente_stage_id")
  private EntenteStage ententeStage;

  private String employerPdfFilePath;
  private String professorPdfFilePath;

  private boolean submittedByEmployer;
  private boolean submittedByProfessor;

  @Enumerated(EnumType.STRING)
  private EvaluationStatus status;
}
