package ca.cal.leandrose.service.dto.evaluation;

import java.util.List;

public record EvaluationCategoryResponse(
        String key,
        List<QuestionResponse> questions,
        String categoryComment
) {
    public EvaluationCategoryResponse{
        categoryComment = categoryComment != null ? categoryComment : "";
    }
}
