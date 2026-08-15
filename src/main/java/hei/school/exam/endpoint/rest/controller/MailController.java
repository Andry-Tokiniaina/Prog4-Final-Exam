package hei.school.exam.endpoint.rest.controller;

import hei.school.exam.endpoint.event.EventProducer;
import hei.school.exam.endpoint.event.model.SendMailRequested;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class MailController {
  private final EventProducer<SendMailRequested> eventProducer;

  @GetMapping("/hello")
  @SneakyThrows
  public String helloWorld(@RequestParam String to) {
    var event = SendMailRequested.builder().to(to).build();
    eventProducer.accept(List.of(event));
    return "... world!";
  }
}
