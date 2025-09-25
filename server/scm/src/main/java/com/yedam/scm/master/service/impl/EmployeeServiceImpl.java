package com.yedam.scm.master.service.impl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.yedam.scm.dto.EmployeeListRes;
import com.yedam.scm.dto.EmployeeSearchDTO;
import com.yedam.scm.dto.PageDTO;
import com.yedam.scm.master.mapper.EmployeeMapper;
import com.yedam.scm.master.service.EmployeeService;
import com.yedam.scm.vo.EmployeeSimpleVO;
import com.yedam.scm.vo.EmployeeVO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {

  private final EmployeeMapper mapper;

  // application.yml에서 경로를 주입
  @Value("${file.upload.employee-dir}")
  private String uploadDir;

  @Override
  public EmployeeListRes getEmployeeSimpleListByCondition(EmployeeSearchDTO condition, PageDTO paging) {

    paging.updatePageInfo(mapper.selectEmployeeCountByCondition(condition));
    List<EmployeeSimpleVO> data = mapper.selectEmployeeSimpleListByCondition(condition, paging);

    return new EmployeeListRes(data, paging);
  }

  @Override
  public EmployeeVO getEmployeeById(String empId) {
    return mapper.selectEmployeeById(empId);
  }

  /**
   * 1. DB에 사원 정보 먼저 insert -> employeeId 생성
   * 2. 사진 저장 (employeeId.확장자 형식)
   * 3. 사진 저장 실패 시 예외 발생 -> DB 트랜잭션 롤백
   */
  @Override
  @Transactional
  public boolean addEmployee(EmployeeVO emp) throws IOException {
    MultipartFile photo = emp.getPhoto();

    // 1. DB insert 먼저 실행 (employeeId 생성)
    int inserted = mapper.insertEmployee(emp);

    if (inserted == 0 || emp.getEmployeeId() == null || emp.getEmployeeId().isEmpty()) {
      return false;
    }

    // 2. 사진 저장
    if (photo != null && !photo.isEmpty()) {
      saveEmployeePhoto(emp.getEmployeeId(), photo);
    }

    return true;
  }

  /**
   * 1. DB에 사원 정보 먼저 update
   * 2. 새로운 사진이 있을 경우 기존 사진 삭제 후 새 사진 저장
   * 3. 사진 저장 실패 시 예외 발생 -> DB 트랜잭션 롤백
   */
  @Override
  @Transactional
  public boolean modifyEmployeeById(EmployeeVO emp) throws IOException {
    MultipartFile photo = emp.getPhoto();
    
    // 1. DB update 먼저 실행
    int updated = mapper.updateEmployeeById(emp);
    
    if (updated == 0) {
      return false;
    }

    // 2. 새로운 사진이 있을 경우
    if (photo != null && !photo.isEmpty()) {
      // 기존 사진 삭제
      deleteEmployeePhoto(emp.getEmployeeId());
      // 새 사진 저장
      saveEmployeePhoto(emp.getEmployeeId(), photo);
    }
    
    return true;
  }

  /**
   * 1. DB에서 사원 정보 삭제
   * 2. 성공 시 사원 이미지도 삭제
   */
  @Override
  @Transactional
  public boolean removeEmployeeById(String empId) throws IOException {
    int deleted = mapper.deleteEmployeeById(empId);

    if (deleted > 0) {
      deleteEmployeePhoto(empId);
      return true;
    }
    return false;
  }
  
  /**
   * 사원 사진 파일 저장
   */
  private void saveEmployeePhoto(String employeeId, MultipartFile photo) throws IOException {
    Path uploadPath = Paths.get(uploadDir);

    if (!Files.exists(uploadPath)) {
      Files.createDirectories(uploadPath);
    }

    String originalFileName = photo.getOriginalFilename();
    String extension = "";
    if (originalFileName != null && originalFileName.contains(".")) {
      extension = originalFileName.substring(originalFileName.lastIndexOf("."));
    }

    String savedFileName = employeeId + extension;
    Path filePath = uploadPath.resolve(savedFileName);

    try {
      photo.transferTo(filePath.toFile());
    } catch (IOException e) {
      throw new IOException("사진 저장 실패", e);
    }
  }
  
  /**
   * 사원 사진 파일 삭제 (모든 확장자)
   */
  private void deleteEmployeePhoto(String employeeId) throws IOException {
    Path uploadPath = Paths.get(uploadDir);
    
    // 지원되는 확장자 목록
    String[] extensions = {".png", ".jpg", ".jpeg", ".gif", ".bmp", ".webp"};
    
    for (String ext : extensions) {
      Path filePath = uploadPath.resolve(employeeId + ext);
      if (Files.exists(filePath)) {
        Files.delete(filePath);
      }
    }
  }
}