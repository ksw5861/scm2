package com.yedam.scm.web;

import org.springframework.web.bind.annotation.RestController;

import com.yedam.scm.master.service.ProductService;
import com.yedam.scm.purchaseMat.service.PurchaseMatService;
import com.yedam.scm.vo.ProductVO;
import com.yedam.scm.vo.ProductionPlanVO;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;



@RestController
@RequiredArgsConstructor
public class MsController {
    
    final PurchaseMatService purchaseMatService;
    final ProductService productSvc;

    //제품모달
    // @GetMapping("/productModal")
    // public List<ProductVO> getProductModalList() {
    //     return productSvc.getProductList();
    // }
  

    //생산계획등록
    // @PostMapping("/productionPlan")
    // public int insertProductionPlan(@RequestBody ProductionPlanVO productionPlanVO) {
    //     return purchaseMatService.insertProductionPlan(productionPlanVO);
    // }

}