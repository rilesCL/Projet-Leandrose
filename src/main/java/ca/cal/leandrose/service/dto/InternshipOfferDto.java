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

    public InternshipOfferDto(Long id, String description, LocalDate startDate, int durationInWeeks,
                              String address, Float remuneration, String status, Long employeurId,
                              String companyName, String pdfPath) {
        this.id = id;
        this.description = description;
        this.startDate = startDate;
        this.durationInWeeks = durationInWeeks;
        this.address = address;
        this.remuneration = remuneration;
        this.status = status;
        this.employeurId = employeurId;
        this.companyName = companyName;
        this.pdfPath = pdfPath;
    }

    public InternshipOfferDto(String errorMessage){
        this.errorMessage = errorMessage;
    }
}
