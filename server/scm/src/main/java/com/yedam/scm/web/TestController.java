package com.yedam.scm.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.yedam.scm.service.TestService;

@RestController
public class TestController {

  // @Autowired
  // TestService svc;

  @GetMapping("/test")
  public String test() {

    return "<h1>test</h1>";
  }

}
