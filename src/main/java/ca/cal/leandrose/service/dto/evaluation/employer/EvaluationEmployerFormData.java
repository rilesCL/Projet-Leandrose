package ca.cal.leandrose.service.dto.evaluation.employer;

import ca.cal.leandrose.service.dto.evaluation.EvaluationForm;
import ca.cal.leandrose.service.dto.evaluation.IQuestionResponse;
import java.util.List;
import java.util.Map;

public record EvaluationEmployerFormData(
        Map<String, List<EmployerQuestionResponse>> categories,
        String generalComment,
        Integer globalAssessment,
        String globalAppreciation,
        Boolean discussedWithTrainee,
        Integer supervisionHours,
        String welcomeNextInternship,
        Boolean technicalTrainingSufficient

) implements EvaluationForm {
    @Override
    public Map<String, List<? extends IQuestionResponse>> getCategories() {
        @SuppressWarnings("unchecked")
        Map<String, List<? extends IQuestionResponse>> result = (Map<String, List<? extends IQuestionResponse>>)
                (Map<?, ?>) categories;
        return result;
    }
}
