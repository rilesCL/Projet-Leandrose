package ca.cal.leandrose.service.dto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;


import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
public class InternshipOfferDto {
    private Long id;
    private String description;
    private LocalDate startDate;
    private int durationInWeeks;
    private String address;
    private Float remuneration;
    private String status;
    private Long employeurId;
    private String companyName;
    private String pdfPath;
    private String errorMessage;
    private LocalDate validationDate;
    private EmployeurDto employeurDto;
    String rejectionComment;

    public InternshipOfferDto(Long id, String description, LocalDate startDate, int durationInWeeks,
                              String address, Float remuneration, String status, EmployeurDto employeurDto, String pdfPath) {
        this.id = id;
        this.description = description;
        this.startDate = startDate;
        this.durationInWeeks = durationInWeeks;
        this.address = address;
        this.remuneration = remuneration;
        this.status = status;
        this.employeurDto = employeurDto;
        this.pdfPath = pdfPath;
        this.rejectionComment = null;
    }

    public InternshipOfferDto(String errorMessage){
        this.errorMessage = errorMessage;
    }
}
