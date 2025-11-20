package ca.cal.leandrose.service.dto.evaluation;

public record EmployerQuestionResponse(
        String comment,
        Boolean checked,
        String rating
) implements IQuestionResponse {

    @Override
    public String getComment(){
        return comment;
    }
    @Override
    public String getRating(){
        return rating;
    }
    public Boolean getChecked(){
        return checked;
    }

}
