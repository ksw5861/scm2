package com.yedam.scm.master.service;

import java.util.List;

import com.yedam.scm.vo.MaterialVO;

public interface MaterialService {
  List<MaterialVO> getMaterialList(String matId, String matName, String matType, String matStoreCond);
  List<MaterialVO> getMaterialDetail(String matId);
  int deleteMaterial(String matId);
  int insertMaterial(MaterialVO materialVO);
  int updateMaterial(MaterialVO materialVO);
}
