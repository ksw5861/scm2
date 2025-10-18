package com.yedam.scm.master.service;

import java.util.List;

import com.yedam.scm.vo.MaterialVO;

public interface MaterialService {
  List<MaterialVO> getMaterialList(String matId, String matName, String status);
  List<MaterialVO> getMaterialDetail(String matId);
  int deleteMaterial(String matId);
  boolean insertMaterial(MaterialVO materialVO);
  int updateMaterial(MaterialVO materialVO);
  List<String> autoCompleteMatName(String keyword);

}
