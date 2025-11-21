package ca.cal.leandrose.service.dto.evaluation.prof;

import ca.cal.leandrose.service.dto.evaluation.EvaluationForm;
import ca.cal.leandrose.service.dto.evaluation.IQuestionResponse;

import java.util.List;
import java.util.Map;

public record EvaluationProfFormDto(
        Map<String, List<QuestionResponseTeacher>> categories,
        Integer firstMonthsHours,
        Integer secondMonthsHours,
        Integer thirdMonthHours,
        String salaryHours,
        Integer preferredStage,
        Integer capacity,
        Boolean sameTraineeNextStage,
        Boolean workShiftYesNo,
        List<WorkShiftRange> workShifts
) implements EvaluationForm {
    @Override
    public Map<String, List<? extends IQuestionResponse>> getCategories() {
        // Safe cast since QuestionResponseTeacher implements IQuestionResponse
        @SuppressWarnings("unchecked")
        Map<String, List<? extends IQuestionResponse>> result = (Map<String, List<? extends IQuestionResponse>>)
                (Map<?, ?>) categories;
        return result;
    }
}
