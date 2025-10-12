package io.github.RaonJena99.financial_dashboard.controller;

import io.github.RaonJena99.financial_dashboard.domain.User;
import io.github.RaonJena99.financial_dashboard.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {
  @Autowired private UserService userService;

  @PostMapping("/register")
  public ResponseEntity<User> registerUser(@RequestBody User user) {
    // @RequestBody: HTTP 요청의 본문(body)에 담긴 JSON 데이터를 User 객체로 변환해줍니다.
    User savedUser = userService.registerUser(user);
    return ResponseEntity.ok(savedUser);
  }
}
