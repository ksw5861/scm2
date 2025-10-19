package com.yedam.scm.web;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.yedam.scm.dto.AdjStockDTO;
import com.yedam.scm.dto.MatStockSearchDTO;
import com.yedam.scm.dto.PageDTO;
import com.yedam.scm.dto.PurchaseListSearchDTO;
import com.yedam.scm.instockMat.service.InStockMatService;
import com.yedam.scm.purchaseMat.service.PurchaseMatService;
import com.yedam.scm.vo.InboundDetailVO;
import com.yedam.scm.vo.InboundLogVO;
import com.yedam.scm.vo.MatLotStockAdjVO;
import com.yedam.scm.vo.MatLotVO;
import com.yedam.scm.vo.MatStatusVO;
import com.yedam.scm.vo.MatVendorVO;
import com.yedam.scm.vo.MrpDetailVO;
import com.yedam.scm.vo.PrdPlanDetailVO;
import com.yedam.scm.vo.ProductVO;
import com.yedam.scm.vo.ProductionPlanVO;
import com.yedam.scm.vo.PurStatusLogVO;
import com.yedam.scm.vo.PurchaseMatVO;
import com.yedam.scm.vo.WareHouseVO;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;



@RestController
@RequestMapping("/mat")
@RequiredArgsConstructor
public class MsController {
    
    final PurchaseMatService purchaseMatService;  //자재주문
    final InStockMatService inStockMatService;    //자재입고
    
    //======================================================================주문part
    //생산계획등록
     @PostMapping("/productionPlan")
     public int insertProductionPlan(@RequestBody ProductionPlanVO productionPlan) {
         return purchaseMatService.insertProductionPlan(productionPlan);
     }

    //생산계획마스터목록(모달)
    @GetMapping("/planMasterList")
    public List<ProductionPlanVO > getPlanMasterList() {
        return purchaseMatService.getPlanMasterList();
    }

     // 생산계획 상세조회 (MRP 모달에서 선택 시)
    @GetMapping("/planDetailList/{plId}")
    public List<PrdPlanDetailVO> getPlanDetailList(@PathVariable Long plId) {
        return purchaseMatService.getPlanDetailList(plId);
    }
    //MRP산출
    @PostMapping("/calcMrp/{plId}")
    public ResponseEntity<String> calcMrp(@PathVariable Long plId, @RequestParam String empName) {
        purchaseMatService.callCalcMrpProc(plId, empName);
        return ResponseEntity.ok("MRP 산출 완료");
    }

    //map 자재소요량
    @GetMapping("/mrpList")
    public List<MrpDetailVO> getMrpList() {
    return purchaseMatService.getMrpList();
    }

    //자재발주등록
    @PostMapping("/reqMaterial")
    public void callReqestMaterial(@RequestBody List<PurchaseMatVO> requestList) {      
        purchaseMatService.callReqestMatProc(requestList);
    }
    
    //발주목록
    @GetMapping("/purchaseList")
    public ResponseEntity<Map<String, Object>> getPurchaseList(PurchaseListSearchDTO searchDTO, PageDTO pageDTO) {

        Map<String, Object> result = purchaseMatService.getPurchaseList(searchDTO, pageDTO);
        return ResponseEntity.ok(result);
    }
    
    //발주상세조회
    @GetMapping("/purchaseListStatus")
    public List<PurStatusLogVO> getPurchaseStatus(@RequestParam Long purId) {
        return purchaseMatService.getPurchaseStatus(purId);
    }
    //============================================================================ 입고Part
    //하차대기목록(마스터)
    @GetMapping("/shipedList")
    public ResponseEntity<Map<String, Object>> getVenShipList(PageDTO pageDTO, PurchaseListSearchDTO searchDTO) {
       
       Map<String, Object> result = inStockMatService.getVenShipList(pageDTO, searchDTO);
       return ResponseEntity.ok(result);
    }
    //하차대기목록(상세)
    @GetMapping("/shipedDetailList")
    public List<InboundDetailVO> getVenShipDetailList(@RequestParam Long inboundId) {
        return inStockMatService.getVenShipDetailList(inboundId);
    }
    //하차승인
    @PostMapping("/approveUnload")
    public void callApproveUnload(@RequestParam Long inboundId, @RequestParam String unloadEmp ) {      
        inStockMatService.callApproveUnload(inboundId, unloadEmp);
    }
    //하차반품
    @PostMapping("/unloadReturn")
    public void callUnloadReturn(@RequestParam Long inboundId, @RequestParam String unloadEmp, @RequestParam String rejMemo) {
        inStockMatService.callUnloadReturn(inboundId, unloadEmp, rejMemo);
    }
    //입고대기목록
    @GetMapping("/unloadList")
    public ResponseEntity<Map<String, Object>> getApproveUnload(MatUnloadSearchDTO searchDTO, PageDTO pageDTO) {
        Map<String, Object> result = inStockMatService.getApproveUnload(searchDTO, pageDTO);
        return ResponseEntity.ok(result);
    }
    //입고대기목록(상세)
    @GetMapping("/unloadDetailList")
    public List<InboundDetailVO> getApproveUnloadDetailList(@RequestParam Long inboundId) {
        return inStockMatService.getApproveUnloadDetailList(inboundId);
    }
    //입고등록
    @PostMapping("/matInStock")
    public void callMatInboundStock(@RequestBody InboundDetailVO inStockInfo) {      
        inStockMatService.callMatInboundStock(inStockInfo);
    }
    //불량등록
    @PostMapping("/defect")
    public ResponseEntity<?> callRegMatDefect( @RequestPart("data") InboundLogVO defectData, @RequestPart(value = "file", required = false) MultipartFile file) {       
        try {
            // 서비스 호출
            inStockMatService.callRegMatDefect(defectData, file);
            return ResponseEntity.ok("불량 등록 완료");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("불량 등록 실패: " + e.getMessage());
        }
    }
    /*
     * ResponseEntity<?> : Controller가 응답을 “정교하게 제어”할 수 있게 해주는 클래스
     * HTTP 응답 코드 + 데이터 + 헤더를 전부 다 설정가능.
     * 
     */

    //=============================================================================================재고part
    //자재재고현황
    @GetMapping("/matStockList")
    public ResponseEntity<Map<String, Object>> getMatStockList(MatStockSearchDTO searchDTO, PageDTO pageDTO) {
        Map<String, Object> result = inStockMatService.getMatStockList(pageDTO, searchDTO);
        return ResponseEntity.ok(result);
    }
    //자재별LOT현황
    @GetMapping("/matLotList")
    public List<MatLotVO> getMatLotList(@RequestParam String matId) {
        return inStockMatService.getMatLotList(matId);
    }
    //재고조정
    @PostMapping("/adjustStock")
    public ResponseEntity<?> adjustStock(@RequestBody AdjStockDTO dto) {
        Long id = inStockMatService.adjustMatStock(dto);
        return ResponseEntity.ok().body(id);
    }
    //조정이력
    @GetMapping("/adjustHistory")
    public ResponseEntity<List<MatLotStockAdjVO>> getAdjustHistory(@RequestParam Long lotId) {
        List<MatLotStockAdjVO> history = inStockMatService.getAdjustHistoryByLotId(lotId);
        return ResponseEntity.ok(history);
    }

    /*======================
    드롭다운/모달용   
    ======================*/
    //생산계획시 제품 드롭다운
    @GetMapping("/productsList")
    public List<ProductVO> getProductList(){
        return purchaseMatService.getProductList();
    }

     //자재주문시 계약된 공급처 드롭다운
    @GetMapping("/matVendorList")
    public List<MatVendorVO> getMatVendor(@RequestParam String matId) {
        return purchaseMatService.getMatVendorList(matId);
    }

    //창고리스트 드롭다운 
    @GetMapping("/warehouseList")
    public List<WareHouseVO> getWarehouseList(){
        return purchaseMatService.getWarehouseList();
    }

    /*======================================
     * 공통코드 상태값
     * ======================================*/
    @GetMapping("/status/{groupId}")
    public List<MatStatusVO> selectCodeList(@PathVariable String groupId) {
        return purchaseMatService.selectCodeList(groupId);
    }

}




