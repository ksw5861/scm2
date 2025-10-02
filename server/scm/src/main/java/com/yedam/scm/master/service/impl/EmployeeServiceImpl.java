package com.yedam.scm.master.service.impl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.HttpHeaders;

import com.yedam.scm.common.service.MailService;
import com.yedam.scm.dto.EmployeeListRes;
import com.yedam.scm.dto.EmployeeSearchDTO;
import com.yedam.scm.dto.PageDTO;
import com.yedam.scm.master.mapper.EmployeeMapper;
import com.yedam.scm.master.service.EmployeeService;
import com.yedam.scm.vo.EmployeeVO;

import org.springframework.core.io.Resource;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {

  private final EmployeeMapper mapper;
  private final MailService mailService;
  private final PasswordEncoder passwordEncoder;

  @Value("${spring.mail.username}")
  private String fromEmail;

  @Value("${file.upload.employee-dir}")
  private String uploadDir;

  @Override
  public EmployeeListRes getEmployeeSimpleListByCondition(EmployeeSearchDTO condition, PageDTO paging) {

    paging.updatePageInfo(mapper.selectEmployeeCountByCondition(condition));
    List<EmployeeVO> data = mapper.selectEmployeeSimpleListByCondition(condition, paging);

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

    String tempPassword = generateTempPassword(10);

    emp.setTempPassword(passwordEncoder.encode(tempPassword));
    MultipartFile photo = emp.getPhoto();

    mapper.insertEmployee(emp);
    if (emp.getRowCount() < 2 || emp.getEmployeeId() == null || emp.getEmployeeId().isEmpty()) {
        return false;
    }

    if (photo != null && !photo.isEmpty()) {
        saveEmployeePhoto(emp.getEmployeeId(), photo);
    }

    mailService.sendMailAsync(
        emp.getEmail(),
        "임시 비밀번호 안내",
        "안녕하세요. 임시 비밀번호 안내드립니다.<br /> " + tempPassword,
        fromEmail
    );

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
    mapper.updateEmployeeById(emp);

    if (emp.getRowCount() == 0) {
      return false;
    }

    MultipartFile photo = emp.getPhoto();
    if (photo != null && !photo.isEmpty()) {
      deleteEmployeePhoto(emp.getEmployeeId());
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
    Map<String, Object> param = new HashMap<>();
    param.put("empId", empId);

    mapper.deleteEmployeeById(param);

    int rowCount = (Integer) param.get("rowCount");

    if (rowCount < 2) {
      return false;
    }

    deleteEmployeePhoto(empId);
    return true;
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
    
    String[] extensions = {".png", ".jpg", ".jpeg", ".gif", ".bmp", ".webp"};
    
    for (String ext : extensions) {
      Path filePath = uploadPath.resolve(employeeId + ext);
      if (Files.exists(filePath)) {
        Files.delete(filePath);
      }
    }
  }

  /**
   * 임시 비밀번호 생성
   */
  public String generateTempPassword(int length) {
    String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    StringBuilder sb = new StringBuilder();
    Random rnd = new SecureRandom();

    for (int i = 0; i < length; i++) {
        sb.append(chars.charAt(rnd.nextInt(chars.length())));
    }

    return sb.toString();
  }

    @Override
    public ResponseEntity<Resource> getEmployeeImage(String employeeId) throws IOException {
        String[] extensions = {".png", ".jpg", ".jpeg", ".gif", ".bmp", ".webp"};

        for (String ext : extensions) {
            Path filePath = Paths.get(uploadDir, employeeId + ext);
            File file = filePath.toFile();

            if (file.exists() && file.isFile()) {
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
        // 파일이 없으면 404 반환
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

}