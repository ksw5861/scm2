package com.yedam.scm.master.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.yedam.scm.vo.MaterialVO;

@Mapper
public interface MaterialMapper {
  // 자재 목록 조회
  List<MaterialVO> getMaterialList(
    @Param("matId") String matId,
    @Param("matName") String matName,
    @Param("status") String status
);

  // 자재 상세
  List<MaterialVO> getMaterialDetail(String matId);

  // 자재정보삭제
  int deleteMaterial(String matId);
  int countReferences(String matId);

  // 자재정보등록
  int insertMaterial(MaterialVO materialVO);

  // 자재정보수정
  int updateMaterial(MaterialVO materialVO);

  // 자재검색 자동완성
  List<String> autoCompleteMatName(String keyword);

}
