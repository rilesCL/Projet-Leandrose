package ca.cal.leandrose.service.dto.evaluation;

import java.util.List;

public record EvaluationCategory(
        String key,
        String titleKey,
        String descriptionKey,
        List<String> questionsKeys
) {}
