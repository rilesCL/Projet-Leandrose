package ca.cal.leandrose.service.dto;

import ca.cal.leandrose.model.Program;
import lombok.*;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ProgramDto {
    private Long id;
    private String name;
    private Map<String,String> error;

    public static ProgramDto create(Program program){
        return new ProgramDto(program.getId(), program.getName());
    }
    public ProgramDto(Long id, String name) {
        this.id = id;
        this.name = name;
    }
    public ProgramDto(String error) {
        this.error = Map.of("error",error);
    }
}
