package com.yedam.scm.web;

import org.springframework.web.bind.annotation.RestController;

import com.yedam.scm.dto.EmployeeListRes;
import com.yedam.scm.dto.EmployeeSearchDTO;
import com.yedam.scm.dto.PageDTO;
import com.yedam.scm.master.service.EmployeeService;
import com.yedam.scm.vo.EmployeeVO;

import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

  @GetMapping("/employee")
  public EmployeeListRes getEmployeeList(
    @ModelAttribute EmployeeSearchDTO condition, 
    @ModelAttribute PageDTO paging
  ) {

    return employeeSvc.getEmployeeSimpleListByCondition(condition, paging);

  }

  @GetMapping("/employee/{empId}")
  public EmployeeVO getEmployee(@PathVariable String empId) {

    return employeeSvc.getEmployeeById(empId);

  }

  @PostMapping("/employee")
  public ResponseEntity<Map<String, String>> addEmployee(
    @RequestBody EmployeeVO emp
  ) {

    boolean isInserted = employeeSvc.addEmployee(emp);

    Map<String, String> response = new HashMap<>();
    if (isInserted) {
      response.put("employeeId", emp.getEmployeeId());
      response.put("status", "success");
      return ResponseEntity.ok(response);
    } else {
      response.put("status", "error");
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

  }
  
  @PutMapping("/employee/{empId}")
  public ResponseEntity<Map<String, String>> modifyEmployee(
      @PathVariable String empId,
      @RequestBody EmployeeVO emp
  ) {
      emp.setEmployeeId(empId);

      boolean isUpdated = employeeSvc.modifyEmployeeById(emp);

      Map<String, String> response = new HashMap<>();
      if (isUpdated) {
          response.put("status", "success");
          return ResponseEntity.ok(response);
      } else {
          response.put("status", "not_found");
          return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
      }
  }


  @DeleteMapping("/employee/{empId}")
  public ResponseEntity<Map<String, String>> removeEmployee(
    @PathVariable String empId
  ) {

    boolean isDeleted = employeeSvc.removeEmployeeById(empId);

    Map<String, String> response = new HashMap<>();
    if (isDeleted) {
        response.put("status", "success");
        return ResponseEntity.ok(response);
    } else {
        response.put("status", "not_found");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

  }

}