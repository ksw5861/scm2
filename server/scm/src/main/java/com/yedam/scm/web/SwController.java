package com.yedam.scm.web;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import com.yedam.scm.master.service.MaterialService;
import com.yedam.scm.vo.MaterialVO;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;





@RestController
public class SwController {

  @Autowired
  MaterialService svc;

  // ==========================
  // 1. 자재 목록 조회
  // ==========================
  @GetMapping("/material")
public List<MaterialVO> getMaterialList(
        @RequestParam(required = false) String matId,
        @RequestParam(required = false) String matName,
        @RequestParam(required = false) String matType,
        @RequestParam(required = false) String matStoreCond) {
    return svc.getMaterialList(matId, matName, matType, matStoreCond);
}

  // ==========================
  // 2. 자재 상세 조회
  // ==========================
  @GetMapping("/material/{matId}")
  public List<MaterialVO> getMaterialDetail(@PathVariable String matId) {
      return svc.getMaterialDetail(matId);
  }

  // ==========================
  // 3. 자재 삭제
  // ==========================
  @DeleteMapping("/material/{matId}")
  public int deleteMaterial(@PathVariable String matId) {
      return svc.deleteMaterial(matId);
  }

  // ==========================
  // 4. 자재 등록
  // ==========================
  @PostMapping("/material")
  public int insertMaterial(@RequestBody MaterialVO materialVO) {      
      return svc.insertMaterial(materialVO);
  }
  
  @PutMapping("/material/{matId}")
public int updateMaterial(@PathVariable String matId, @RequestBody MaterialVO materialVO) {
    // PathVariable matId를 VO에 세팅
    materialVO.setMatId(matId);
    return svc.updateMaterial(materialVO);  // VO 하나만 넘김
}
}