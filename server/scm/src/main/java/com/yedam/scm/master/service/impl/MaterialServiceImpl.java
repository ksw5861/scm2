package com.yedam.scm.master.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yedam.scm.master.mapper.MaterialMapper;
import com.yedam.scm.master.service.MaterialService;
import com.yedam.scm.vo.MaterialVO;
@Service
public class MaterialServiceImpl implements MaterialService {
  
  @Autowired
  MaterialMapper mapper;

  @Override
  public List<MaterialVO> getMaterialList(String matId, String matName, String status){
    return mapper.getMaterialList(matId, matName, status);
}

  @Override
  public List<MaterialVO> getMaterialDetail(String matId){
    return mapper.getMaterialDetail(matId);
  }

  @Override
  public int deleteMaterial(String matId){
    // 자재사용여부 확인
    // 자재거래업체삭제후
    // 자재삭제
    
    int refCount = mapper.countReferences(matId);
    if (refCount > 0) {
        throw new IllegalStateException("해당 자재는 다른 테이블에서 참조 중이라 삭제할 수 없습니다.");
    }
    return mapper.deleteMaterial(matId);
  }

  @Override
  public boolean insertMaterial(MaterialVO materialVO){
    return mapper.insertMaterial(materialVO) > 0;
  }

  @Override
  public int updateMaterial(MaterialVO materialVO){
    return mapper.updateMaterial(materialVO);
  }

}
