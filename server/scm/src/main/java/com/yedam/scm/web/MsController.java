package com.yedam.scm.web;

import org.springframework.web.bind.annotation.RestController;

import com.yedam.scm.dto.PageDTO;
import com.yedam.scm.purchaseMat.service.PurchaseMatService;
import com.yedam.scm.vo.ProductVO;
import com.yedam.scm.vo.ProductionPlanVO;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;




@RestController
@RequestMapping("/mat") 
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
    @GetMapping("/productsList")
    public List<ProductVO> getProductList(){
        return purchaseMatService.getProductList();
    }
}




