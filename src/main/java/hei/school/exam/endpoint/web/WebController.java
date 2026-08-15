package hei.school.exam.endpoint.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebController {
  private record MyObject(String name, int age) {}

  @GetMapping("/")
  public String index(Model model) {
    MyObject myObject = new MyObject("John", 30);
    model.addAttribute("ob", myObject);
    return "index";
  }
}
