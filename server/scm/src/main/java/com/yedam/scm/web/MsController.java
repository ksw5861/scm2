package com.yedam.scm.web;

import org.springframework.web.bind.annotation.RestController;

import com.yedam.scm.dto.PageDTO;
import com.yedam.scm.purchaseMat.service.PurchaseMatService;
import com.yedam.scm.vo.ProductVO;
import com.yedam.scm.vo.ProductionPlanVO;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;



@RestController
@RequiredArgsConstructor
public class MsController {
    
    final PurchaseMatService purchaseMatService;

    //생산계획등록
     @PostMapping("/productionPlan")
     public int insertProductionPlan(@RequestBody ProductionPlanVO productionPlan) {
         return purchaseMatService.insertProductionPlan(productionPlan);
     }









    /*======================
    모달용   
    ======================*/
    @GetMapping("/productsListM")
    public Map<String, Object> getProducts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String prodName) {

        PageDTO pageDTO = new PageDTO(page, size);

        // 전체 건수 조회
        long totalCount = purchaseMatService.getProductCount(prodName);
        pageDTO.updatePageInfo(totalCount);

        // 현재 페이지 데이터
        List<ProductVO> items = purchaseMatService.getProductList(prodName, pageDTO.getOffset(), pageDTO.getSize());

        Map<String, Object> result = new HashMap<>();
        result.put("items", items);
        result.put("total", totalCount);   // DTable에서 totalRecords로 사용
        return result;
    }
}




