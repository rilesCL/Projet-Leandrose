package ca.cal.leandrose.service.dto.evaluation;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CategoryData {
  private String title;
  private String description;
  private List<String> questions;
}
