package ca.cal.leandrose.presentation.request;

import lombok.Data;

@Data
public class InternshipOfferRequest {
    private String description;
    private String startDate;       // format: YYYY-MM-DD
    private int durationInWeeks;
    private String address;
    private Float remuneration;
}
