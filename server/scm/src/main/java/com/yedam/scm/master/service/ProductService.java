package com.yedam.scm.master.service;

import java.util.Date;
import java.util.List;

import com.yedam.scm.vo.ProductVO;

public interface ProductService {
  List<ProductVO> getProductList(String prodId, String prodName, String status, String unit, Date createdAt);
  List<ProductVO> getProductDetail(String prodId);
  int deleteProduct(String prodId);
  int insertProduct(ProductVO productVO);
  int updateProduct(ProductVO productVO);
  List<String> autoCompletePrdName(String keyword);

}
