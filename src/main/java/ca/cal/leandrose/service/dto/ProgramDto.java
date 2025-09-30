package ca.cal.leandrose.service.dto;

import ca.cal.leandrose.model.Program;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ProgramDto {
    private Long id;
    private String name;

    public static ProgramDto create(Program program){
        return new ProgramDto(program.getId(), program.getName());
    }
}
