package ca.cal.leandrose.service.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class ConvocationDto {
    private Long id;
    private Long candidatureId;
    private LocalDate ConvocationDate;
    private String location;

    public ConvocationDto(Long id, Long candidatureId, LocalDate convocationDate, String location) {
        this.id = id;
        this.candidatureId = candidatureId;
        ConvocationDate = convocationDate;
        this.location = location;
    }

}
