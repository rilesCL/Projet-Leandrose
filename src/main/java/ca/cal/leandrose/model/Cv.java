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
    private String rejectionComment;

    public enum Status {
        APPROVED, PENDING, REJECTED
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private Status status;

    public String getStudentName() {
        if (student != null) {
            return student.getFirstName() + " " + student.getLastName();
        }
        return null;
    }
}