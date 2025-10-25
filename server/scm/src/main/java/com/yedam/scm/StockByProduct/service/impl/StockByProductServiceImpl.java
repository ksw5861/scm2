package com.yedam.scm.StockByProduct.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.yedam.scm.StockByProduct.mapper.StockByProductMapper;
import com.yedam.scm.StockByProduct.service.StockByProductService;
import com.yedam.scm.dto.PageDTO;
import com.yedam.scm.dto.ProdSearchDTO;
import com.yedam.scm.dto.ProdStockDTO;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class StockByProductServiceImpl implements StockByProductService {

    final StockByProductMapper mapper;

    @Override
    public Map<String, Object> getProductStockList(ProdSearchDTO searchDTO, PageDTO pageDTO) {
        
        List<ProdStockDTO> list = mapper.getProductStockList(
            pageDTO.getStartRow(),
            pageDTO.getEndRow(),
            searchDTO.getProdName(),
            searchDTO.getInboundId()
        );

        Long total = mapper.getProductStockCount(
            searchDTO.getProdName(),
            searchDTO.getInboundId()
        );

        pageDTO.updatePageInfo(total);

        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("page", pageDTO);
        return result;
    }

    @Override
    public List<ProdStockDTO> getProductLotList(String prodId) {
        return mapper.getProductLotList(prodId);
    }
    
}
