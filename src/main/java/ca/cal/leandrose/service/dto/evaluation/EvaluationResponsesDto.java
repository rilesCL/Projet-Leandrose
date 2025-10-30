package ca.cal.leandrose.service.dto.evaluation;

import java.util.List;

public record EvaluationResponsesDto(
        List<EvaluationCategoryResponse> categories,
        String generalComment
) {}
