package com.yedam.scm.StockByProduct.service;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.RequestParam;

import com.yedam.scm.dto.PageDTO;
import com.yedam.scm.dto.ProdSearchDTO;
import com.yedam.scm.dto.ProdStockDTO;


public interface StockByProductService {

    Map<String, Object> getProductStockList(ProdSearchDTO searchDTO, PageDTO pageDTO);
    List<ProdStockDTO> getProductLotList(@RequestParam String prodId);

}
