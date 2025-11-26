package ca.cal.leandrose.service.dto.evaluation;

import java.util.List;
import java.util.Map;

public interface EvaluationForm {
    Map<String, List<? extends IQuestionResponse>> getCategories();;
}
