package ca.cal.leandrose.service.dto;

import ca.cal.leandrose.model.Convocation;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Builder
@Data
public class ConvocationDto {
    private Long id;
    private Long candidatureId;
    private LocalDateTime convocationDate;
    private String location;

    public ConvocationDto(Long id, Long candidatureId, LocalDateTime convocationDate, String location) {
        this.id = id;
        this.candidatureId = candidatureId;
        this.convocationDate = convocationDate;
        this.location = location;
    }
    public static ConvocationDto create(Convocation convocation) {
        return ConvocationDto.builder()
                .id(convocation.getId())
                .candidatureId(convocation.getCandidature() != null ? convocation.getCandidature().getId() : null)
                .convocationDate(convocation.getConvocationDate())
                .location(convocation.getLocation())
                .build();
    }

}
