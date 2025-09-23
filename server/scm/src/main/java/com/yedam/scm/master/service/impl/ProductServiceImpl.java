package com.yedam.scm.master.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yedam.scm.master.mapper.ProductMapper;
import com.yedam.scm.master.service.ProductService;
import com.yedam.scm.vo.ProductVO;

@Service
public class ProductServiceImpl implements ProductService {
  
  @Autowired
  ProductMapper mapper;

  @Override
  public List<ProductVO> getProductList(String prodId, String prodName, String status, String unit) {
    return mapper.getProductList(prodId, prodName, status, unit);
  }

  @Override
  public List<ProductVO> getProductDetail(String prodId) {
    return mapper.getProductDetail(prodId);
  }

  @Override
  public int deleteProduct(String prodId) {
    return mapper.deleteProduct(prodId);
  }

  @Override
  public int insertProduct(ProductVO productVO) {
    return mapper.insertProduct(productVO);
  }

  @Override
  public int updateProduct(ProductVO productVO) {
    return mapper.updateProduct(productVO);
  }
}
