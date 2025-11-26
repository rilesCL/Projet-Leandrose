package ca.cal.leandrose.service.dto.evaluation.prof;

import ca.cal.leandrose.service.dto.evaluation.IQuestionResponse;

public record QuestionResponseTeacher(String rating) implements IQuestionResponse {
    @Override
    public String getComment(){
        return null;
    }
    @Override
    public String getRating(){
        return rating;
    }
}
