package ca.cal.leandrose.service.dto.evaluation.prof;

import ca.cal.leandrose.service.dto.evaluation.QuestionResponse;
import ca.cal.leandrose.service.dto.evaluation.WorkShiftRange;

import java.util.List;
import java.util.Map;

public record EvaluationProfFormDto(
        Map<String, List<QuestionResponse>> categories,
        Integer firstMonthsHours,
        Integer secondMonthsHours,
        Integer thirdMonthHours,
        String salaryHours,
        Integer preferredStage,
        Integer capacity,
        Boolean sameTraineeNextStage,
        List<WorkShiftRange> workShifts
) {
}
