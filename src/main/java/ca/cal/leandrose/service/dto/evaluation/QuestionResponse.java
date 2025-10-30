package ca.cal.leandrose.service.dto.evaluation;

import ca.cal.leandrose.model.EvaluationChoice;

public record QuestionResponse(
        String questionKey,
        String comment,
        Boolean checked,
        EvaluationChoice rating
) {
    public QuestionResponse{
        checked = checked != null ? checked: false;
        comment = comment != null ? comment: "";
    }
}
