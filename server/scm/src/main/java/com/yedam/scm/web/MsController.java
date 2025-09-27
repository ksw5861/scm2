package com.yedam.scm.web;

import org.springframework.web.bind.annotation.RestController;

import com.yedam.scm.purchaseMat.service.PurchaseMatService;
import com.yedam.scm.vo.MatVendorVO;
import com.yedam.scm.vo.MrpDetailVO;
import com.yedam.scm.vo.PrdPlanDetailVO;
import com.yedam.scm.vo.ProductVO;
import com.yedam.scm.vo.ProductionPlanVO;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;



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

    //생산계획마스터목록
    @GetMapping("/planMasterList")
    public List<ProductionPlanVO > getPlanMasterList() {
        return purchaseMatService.getPlanMasterList();
    }

    //생산계획디테일목록
    @GetMapping("/planList")
    public List<PrdPlanDetailVO> getPlanList() {
        return purchaseMatService.getPlanList();
    }
    
    //map 자재소요량
    @GetMapping("/mrpList")
    public List<MrpDetailVO> getMrpDetailList(){
        return purchaseMatService.getMrpDetailList();
    }

    //자재별 공급처
    @GetMapping("/matVendorList")
    public List<MatVendorVO> getMatVendor(@RequestParam String matId) {
        return purchaseMatService.getMatVendorList(matId);
    }
    




    /*======================
    모달용   
    ======================*/
    @GetMapping("/productsList")
    public List<ProductVO> getProductList(){
        return purchaseMatService.getProductList();
    }
}




