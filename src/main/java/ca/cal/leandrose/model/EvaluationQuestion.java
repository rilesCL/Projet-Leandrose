package ca.cal.leandrose.model;

import jakarta.persistence.*;

@Entity
public class EvaluationQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String category;
    private String description;
    private String questionText;

    @Enumerated(EnumType.STRING)
    private EvaluationChoice choice;

    @ManyToOne
    @JoinColumn(name = "evaluation_id")
    private EvaluationStagiaire evaluation;

    @Column(length = 1000)
    private String comment;
}