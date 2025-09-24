package ca.cal.leandrose.model;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Cv {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "student_id")

    private Student student;
    private String pdfPath;

    public enum Status {
        APPROVED, PENDING, REJECTED
    }
}