package ca.cal.leandrose.service.dto.evaluation;

import java.util.List;
import java.util.Map;

public record EvaluationFormData(
        Map<String, List<QuestionResponse>> categories,
        String generalComment,
        Integer globalAssessment,
        String globalAppreciation,
        Boolean discussedWithTrainee,
        Integer supervisionHours,
        String welcomeNextInternship,
        Boolean technicalTrainingSufficient,

        Integer firstMonthsHours,
        Integer secondMonthsHours,
        Integer thirdMonthHours,
        String salaryHours

) {
}
