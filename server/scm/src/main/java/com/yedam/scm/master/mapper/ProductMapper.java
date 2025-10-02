package com.yedam.scm.master.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.yedam.scm.vo.ProductVO;

@Mapper
public interface ProductMapper {
  // 제품 목록 조회
  List<ProductVO> getProductList(
    @Param("prodId") String prodId,
    @Param("prodName") String prodName,
    @Param("status") String status,
    @Param("unit") String unit
  );

  // 제품 상세
  List<ProductVO> getProductDetail(String prodId);

  // 제품정보삭제
  int deleteProduct(String prodId);
  int countReferences(String prodId);

  // 제품정보등록
  int insertProduct(ProductVO productVO);

  // 제품정보수정
  int updateProduct(ProductVO productVO);
}
