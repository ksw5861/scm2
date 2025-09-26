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
    return mapper.deleteMaterial(matId);
  }

  @Override
  public int insertMaterial(MaterialVO materialVO){
    return mapper.insertMaterial(materialVO);
  }

  @Override
  public int updateMaterial(MaterialVO materialVO){
    return mapper.updateMaterial(materialVO);
  }

}
