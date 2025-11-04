package ca.cal.leandrose.service.dto.evaluation;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class CategoryData {
    private String title;
    private String description;
    private List<String> questions;
}
