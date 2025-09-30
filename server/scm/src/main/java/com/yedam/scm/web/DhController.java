package com.yedam.scm.web;

import org.springframework.web.bind.annotation.RestController;

import com.yedam.scm.common.service.MailService;
import com.yedam.scm.dto.AuthRes;
import com.yedam.scm.dto.EmailDTO;
import com.yedam.scm.dto.EmployeeListRes;
import com.yedam.scm.dto.EmployeeSearchDTO;
import com.yedam.scm.dto.LoginDTO;
import com.yedam.scm.dto.LoginRes;
import com.yedam.scm.dto.PageDTO;
import com.yedam.scm.login.service.LoginService;
import com.yedam.scm.master.service.EmployeeService;
import com.yedam.scm.security.JwtUtil;
import com.yedam.scm.vo.EmployeeVO;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequiredArgsConstructor
public class DhController {

  private final EmployeeService employeeSvc;
  private final LoginService loginSvc;
  private final JwtUtil jwtUtil;
  private final MailService mailSvc;

  /**
   * 확장자 없이 요청 시 자동으로 탐색하여 이미지 반환
   * 예) GET /api/img/employee/EMP001
   */

  @GetMapping("/img/employee/{employeeId}")
  public ResponseEntity<Resource> getEmployeePhoto(@PathVariable String employeeId) {
      try {
          return employeeSvc.getEmployeeImage(employeeId);
      } catch (IOException e) {
          e.printStackTrace();
          return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
      }
  }

  /**
   * 사원 목록 조회
   */
  @GetMapping("/employee")
  public EmployeeListRes getEmployeeList(
    @ModelAttribute EmployeeSearchDTO condition,
    @ModelAttribute PageDTO paging
  ) {
    return employeeSvc.getEmployeeSimpleListByCondition(condition, paging);
  }

  /**
   * 사원 상세 조회
   */
  @GetMapping("/employee/{empId}")
  public EmployeeVO getEmployee(
    @PathVariable String empId
  ) {
    return employeeSvc.getEmployeeById(empId);
  }

  /**
   * 사원 등록
   * @param emp EmployeeVO + MultipartFile(photo)
   */
  @PostMapping("/employee")
  public ResponseEntity<Map<String, String>> addEmployee(
    @ModelAttribute EmployeeVO emp
  ) {
    Map<String, String> response = new HashMap<>();

    try {
      boolean isInserted = employeeSvc.addEmployee(emp);

      if (isInserted) {
        response.put("employeeId", emp.getEmployeeId());
        response.put("status", "success");
        return ResponseEntity.ok(response);
      } else {
        response.put("status", "error");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
      }
    } catch (Exception e) {
      e.printStackTrace();
      response.put("status", "error");
      response.put("message", "서버 오류가 발생했습니다.");
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
  }

  /**
   * 사원 수정
   *
   * @param empId URL 경로 변수
   * @param emp   EmployeeVO (폼 데이터 및 이미지 파일 포함)
   */
  @PutMapping("/employee/{empId}")
  public ResponseEntity<Map<String, String>> modifyEmployee(
    @PathVariable String empId,
    @ModelAttribute EmployeeVO emp
  ) {
    Map<String, String> response = new HashMap<>();

    try {
      emp.setEmployeeId(empId);
      boolean isUpdated = employeeSvc.modifyEmployeeById(emp);

      if (isUpdated) {
        response.put("status", "success");
        return ResponseEntity.ok(response);
      } else {
        response.put("status", "not_found");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
      }
    } catch (IOException e) {
      e.printStackTrace();
      response.put("status", "error");
      response.put("message", "파일 처리 중 오류가 발생했습니다.");
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
  }

  /**
   * 사원 삭제
   */
  @DeleteMapping("/employee/{empId}")
  public ResponseEntity<Map<String, String>> removeEmployee(
    @PathVariable String empId
  ) {
    Map<String, String> response = new HashMap<>();

    try {
      boolean isDeleted = employeeSvc.removeEmployeeById(empId);

      if (isDeleted) {
        response.put("status", "success");
        return ResponseEntity.ok(response);
      } else {
        response.put("status", "not_found");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
      }
    } catch (IOException e) {
      e.printStackTrace();
      response.put("status", "error");
      response.put("message", "파일 삭제 중 오류가 발생했습니다.");
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
  }

  @PostMapping("/auth")
  public ResponseEntity<?> tempLogin(@RequestBody LoginDTO login, HttpServletResponse response) {
      try {
          AuthRes authResponse = loginSvc.processTempLogin(login);

          Cookie cookie = new Cookie("tempToken", authResponse.getTempToken());
          cookie.setHttpOnly(true);
          cookie.setSecure(false); // 배포 환경에서는 true
          cookie.setPath("/");
          cookie.setMaxAge(60);
          response.addCookie(cookie);

          return ResponseEntity.ok(authResponse);
      } catch (IllegalArgumentException e) {
          return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
              .body(Map.of("message", e.getMessage()));
      } catch (Exception e) {
          e.printStackTrace();
          return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
              .body(Map.of("message", "서버 오류가 발생했습니다."));
      }
  }

  @PostMapping("/auth/devlogin")
  public ResponseEntity<?> devLogin(@RequestBody LoginDTO login, HttpServletResponse response) {
      try {
          LoginRes account = loginSvc.loginAdminByEmailAndPassword(login);

          if (account == null) {
              return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                  .body(Map.of("message", "이메일 또는 비밀번호가 올바르지 않습니다."));
          }

          String accessToken = jwtUtil.generateToken(account);

          Cookie accessCookie = new Cookie("accessToken", accessToken);
          accessCookie.setHttpOnly(true);
          accessCookie.setSecure(false); // 배포 시 true
          accessCookie.setPath("/");
          accessCookie.setMaxAge(60 * 60); // 1시간
          response.addCookie(accessCookie);

          return ResponseEntity.ok(Map.of(
              "message", "로그인 성공",
              "accessToken", accessToken
          ));
      } catch (IllegalArgumentException e) {
          return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
              .body(Map.of("message", e.getMessage()));
      } catch (Exception e) {
          e.printStackTrace();
          return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
              .body(Map.of("message", "서버 오류가 발생했습니다."));
      }
  }

  @PostMapping("/auth/login")
  public ResponseEntity<?> completeLogin(
      @CookieValue(value = "tempToken", required = false) String tempToken,
      HttpServletResponse response
  ) {
      if (tempToken == null || tempToken.isEmpty()) {
          return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
              .body(Map.of("message", "인증 정보가 없습니다."));
      }

      String accountId;
      try {
          accountId = jwtUtil.getSubject(tempToken);
      } catch (Exception e) {
          return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
              .body(Map.of("message", "잘못된 인증 토큰입니다."));
      }

      LoginRes account = loginSvc.getAccountByAccountId(accountId);
      if (account == null) {
          return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
              .body(Map.of("message", "계정을 찾을 수 없습니다."));
      }

      List<EmailDTO> messages;
      try {
          messages = mailSvc.fetchRecentEmails();
      } catch (Exception e) {
          return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
              .body(Map.of("message", "이메일 인증 처리 중 오류가 발생했습니다."));
      }

      boolean isVerified = messages.stream().anyMatch(message -> {
          String phone = account.getPhone().replaceAll("-", "");       // 하이픈 제거
          String sender = message.getFrom().replaceAll("-", "");       // 메일 발신자 하이픈 제거
          return sender.contains(phone) && message.getBody().contains(tempToken);
      });

      if (!isVerified) {
          return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
              .body(Map.of("message", "인증 메시지를 찾을 수 없습니다."));
      }

      // 인증 성공 → Access Token 발급
      String accessToken = jwtUtil.generateToken(account);

      Cookie accessCookie = new Cookie("accessToken", accessToken);
      accessCookie.setHttpOnly(true);
      accessCookie.setSecure(false); // 배포 시 true
      accessCookie.setPath("/");
      accessCookie.setMaxAge(60 * 60);
      response.addCookie(accessCookie);

      return ResponseEntity.ok(Map.of(
          "message", "로그인 성공",
          "accessToken", accessToken
      ));
  }

  @GetMapping("/auth/me")
  public ResponseEntity<LoginRes> getCurrentUser(
      @CookieValue(value = "accessToken", required = false) String token
  ) {
      if (token == null || token.isBlank()) {
          return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
      }

      boolean valid;
      try {
          valid = jwtUtil.validateToken(token);
      } catch (Exception ex) {
          ex.printStackTrace();
          return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
      }

      if (!valid) {
          return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
      }

      Claims claims;
      try {
          claims = jwtUtil.getClaims(token);
      } catch (Exception ex) {
          ex.printStackTrace();
          return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
      }

      try {
          LoginRes user = new LoginRes();
          user.setAccountId(claims.getSubject());
          user.setName((String) claims.get("name"));
          user.setCode((String) claims.get("code"));
          user.setRole((String) claims.get("role"));
          user.setTempPassword((String) claims.get("tempPassword"));
          return ResponseEntity.ok(user);
      } catch (Exception ex) {
          ex.printStackTrace();
          return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
      }
  }

  @PostMapping("/auth/change-password")
  public ResponseEntity<?> modifyAccountPassword(
      @CookieValue(value = "accessToken", required = false) String token,
      @RequestBody Map<String, String> body,
      HttpServletResponse response
  ) {
      if (token == null || !jwtUtil.validateToken(token)) {
          return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "인증되지 않은 사용자입니다."));
      }

      String newPassword = body.get("newPassword");
      if (newPassword == null || newPassword.trim().isEmpty()) {
          return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "새 비밀번호가 필요합니다."));
      }

      Claims claims = jwtUtil.getClaims(token);
      String accountId = claims.getSubject();

      try {
          boolean changed = loginSvc.modifyAccountPassword(accountId, newPassword);
      if (changed) {
          String name = (String) claims.get("name");
          String code = (String) claims.get("code");
          String role = (String) claims.get("role");

          LoginRes info = new LoginRes();

          info.setAccountId(accountId);
          info.setName(name);
          info.setCode(code);
          info.setRole(role);
          info.setTempPassword("N");

          String newToken = jwtUtil.generateToken(info);

          Cookie cookie = new Cookie("accessToken", newToken);
          cookie.setHttpOnly(true);
          cookie.setSecure(false);
          cookie.setPath("/");
          cookie.setMaxAge(60 * 30);
          response.addCookie(cookie);

          return ResponseEntity.ok(Map.of("status", "success", "message", "비밀번호가 변경되었습니다."));
      } else {
              return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("status", "fail", "message", "비밀번호 변경에 실패했습니다."));
          }
      } catch (Exception e) {
          e.printStackTrace();
          return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("status", "error", "message", "서버 오류가 발생했습니다."));
      }
  }

  @PostMapping("/auth/logout")
  public ResponseEntity<?> logout(HttpServletResponse response) {
      Cookie cookie = new Cookie("accessToken", "");
      cookie.setPath("/");
      cookie.setHttpOnly(true);
      cookie.setMaxAge(0);
      response.addCookie(cookie);
      return ResponseEntity.ok().body(Map.of("message", "로그아웃 되었습니다."));
  }

}