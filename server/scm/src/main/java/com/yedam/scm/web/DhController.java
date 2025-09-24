package com.yedam.scm.web;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import com.yedam.scm.dto.EmployeeListRes;
import com.yedam.scm.dto.EmployeeSearchDTO;
import com.yedam.scm.dto.PageDTO;
import com.yedam.scm.master.service.EmployeeService;
import com.yedam.scm.vo.EmployeeVO;

import lombok.RequiredArgsConstructor;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;

@RestController
@RequiredArgsConstructor
public class DhController {

  private final EmployeeService employeeSvc;

  @Value("${file.upload.employee-dir}")
  private String employeeUploadDir;

  /**
   * 확장자 없이 요청 시 자동으로 탐색하여 이미지 반환
   * 예) GET /api/img/employee/EMP001
   */
  @GetMapping("/img/employee/{employeeId}")
  public ResponseEntity<Resource> getEmployeePhoto(
    @PathVariable String employeeId
  ) {
    try {
      // 지원되는 확장자 목록
      String[] extensions = {".png", ".jpg", ".jpeg", ".gif", ".bmp", ".webp"};

      for (String ext : extensions) {
        Path filePath = Paths.get(employeeUploadDir, employeeId + ext);
        File file = filePath.toFile();

        if (file.exists()) {
          FileSystemResource resource = new FileSystemResource(file);

          String contentType = Files.probeContentType(filePath);
          if (contentType == null) {
            contentType = "application/octet-stream";
          }

          return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_TYPE, contentType)
            .body(resource);
        }
      }

      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();

    } catch (Exception e) {
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
}