package ca.cal.leandrose.service.dto;

import ca.cal.leandrose.model.Program;

public record ProgramDto(String code, String translationKey) {
    public static ProgramDto fromEnum(Program program){
        return new ProgramDto(program.name(), program.getTranslationKey());
    }
}
