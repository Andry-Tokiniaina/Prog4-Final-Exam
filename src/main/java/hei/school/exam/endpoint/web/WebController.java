package hei.school.exam.endpoint.web;

import hei.school.exam.service.event.CohortService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class WebController {

  private final CohortService cohortService;

  @GetMapping("/")
  public String index(Model model) {
    model.addAttribute("cohorts", cohortService.findAll());
    return "index";
  }
}
