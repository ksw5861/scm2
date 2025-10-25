package com.yedam.scm.web;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.yedam.scm.StockByProduct.service.StockByProductService;
import com.yedam.scm.dto.AdjStockDTO;
import com.yedam.scm.dto.MatStockSearchDTO;
import com.yedam.scm.dto.MatUnloadSearchDTO;
import com.yedam.scm.dto.PageDTO;
import com.yedam.scm.dto.ProdSearchDTO;
import com.yedam.scm.dto.ProdStockDTO;
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
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.util.JRLoader;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.io.InputStream;
import java.sql.Connection;


@RestController
@RequestMapping("${api.base-path:/}")
@RequiredArgsConstructor
public class MsController {
    
    final PurchaseMatService purchaseMatService;  //자재주문
    final InStockMatService inStockMatService;    //자재입고
    final StockByProductService stockByProductService;    //제품재고
    private final DataSource dataSource;   // jasper
    private final ResourceLoader resourceLoader;

    
    //======================================================================주문part
    //생산계획등록
     @PostMapping("/mproductionPlan")
     public int insertProductionPlan(@RequestBody ProductionPlanVO productionPlan) {
         return purchaseMatService.insertProductionPlan(productionPlan);
     }

    //생산계획마스터목록(모달)
    @GetMapping("/mplanMasterList")
    public List<ProductionPlanVO > getPlanMasterList() {
        return purchaseMatService.getPlanMasterList();
    }

     // 생산계획 상세조회 (MRP 모달에서 선택 시)
    @GetMapping("/mplanDetailList/{plId}")
    public List<PrdPlanDetailVO> getPlanDetailList(@PathVariable Long plId) {
        return purchaseMatService.getPlanDetailList(plId);
    }
    //MRP산출
    @PostMapping("/mcalcMrp/{plId}")
    public ResponseEntity<String> calcMrp(@PathVariable Long plId, @RequestParam String empName) {
        purchaseMatService.callCalcMrpProc(plId, empName);
        return ResponseEntity.ok("MRP 산출 완료");
    }

    //map 자재소요량
    @GetMapping("/mmrpList")
    public List<MrpDetailVO> getMrpList() {
    return purchaseMatService.getMrpList();
    }

    //자재발주등록
    @PostMapping("/mreqMaterial")
    public void callReqestMaterial(@RequestBody List<PurchaseMatVO> requestList) {      
        purchaseMatService.callReqestMatProc(requestList);
    }
    
    //발주목록
    @GetMapping("/mpurchaseList")
    public ResponseEntity<Map<String, Object>> getPurchaseList(PurchaseListSearchDTO searchDTO, PageDTO pageDTO) {

        Map<String, Object> result = purchaseMatService.getPurchaseList(searchDTO, pageDTO);
        return ResponseEntity.ok(result);
    }
    
    //발주상세조회
    @GetMapping("/mpurchaseListStatus")
    public List<PurStatusLogVO> getPurchaseStatus(@RequestParam Long purId) {
        return purchaseMatService.getPurchaseStatus(purId);
    }
    //============================================================================ 입고Part
    //하차대기목록(마스터)
    @GetMapping("/mshipedList")
    public ResponseEntity<Map<String, Object>> getVenShipList(PageDTO pageDTO, PurchaseListSearchDTO searchDTO) {
       
       Map<String, Object> result = inStockMatService.getVenShipList(pageDTO, searchDTO);
       return ResponseEntity.ok(result);
    }
    //하차대기목록(상세)
    @GetMapping("/mshipedDetailList")
    public List<InboundDetailVO> getVenShipDetailList(@RequestParam Long inboundId) {
        return inStockMatService.getVenShipDetailList(inboundId);
    }
    //하차승인
    @PostMapping("/mapproveUnload")
    public void callApproveUnload(@RequestParam Long inboundId, @RequestParam String unloadEmp ) {      
        inStockMatService.callApproveUnload(inboundId, unloadEmp);
    }
    //하차반품
    @PostMapping("/munloadReturn")
    public void callUnloadReturn(@RequestParam Long inboundId, @RequestParam String unloadEmp, @RequestParam String rejMemo) {
        inStockMatService.callUnloadReturn(inboundId, unloadEmp, rejMemo);
    }
    //입고대기목록
    @GetMapping("/munloadList")
    public ResponseEntity<Map<String, Object>> getApproveUnload(MatUnloadSearchDTO searchDTO, PageDTO pageDTO) {
        Map<String, Object> result = inStockMatService.getApproveUnload(searchDTO, pageDTO);
        return ResponseEntity.ok(result);
    }
    //입고대기목록(상세)
    @GetMapping("/munloadDetailList")
    public List<InboundDetailVO> getApproveUnloadDetailList(@RequestParam Long inboundId) {
        return inStockMatService.getApproveUnloadDetailList(inboundId);
    }
    //입고등록
    @PostMapping("/mmatInStock")
    public void callMatInboundStock(@RequestBody InboundDetailVO inStockInfo) {      
        inStockMatService.callMatInboundStock(inStockInfo);
    }
    //불량등록
    @PostMapping("/mdefect")
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
    @GetMapping("/mmatStockList")
    public ResponseEntity<Map<String, Object>> getMatStockList(MatStockSearchDTO searchDTO, PageDTO pageDTO) {
        Map<String, Object> result = inStockMatService.getMatStockList(pageDTO, searchDTO);
        return ResponseEntity.ok(result);
    }
    //자재별LOT현황
    @GetMapping("/mmatLotList")
    public List<MatLotVO> getMatLotList(@RequestParam String matId) {
        return inStockMatService.getMatLotList(matId);
    }
    //재고조정
    @PostMapping("/madjustStock")
    public ResponseEntity<?> adjustStock(@RequestBody AdjStockDTO dto) {
        Long id = inStockMatService.adjustMatStock(dto);
        return ResponseEntity.ok().body(id);
    }
    //조정이력
    @GetMapping("/madjustHistory")
    public ResponseEntity<List<MatLotStockAdjVO>> getAdjustHistory(@RequestParam Long lotId) {
        List<MatLotStockAdjVO> history = inStockMatService.getAdjustHistoryByLotId(lotId);
        return ResponseEntity.ok(history);
    }
    
    /*======================
    드롭다운/모달용   
    ======================*/
    //생산계획시 제품 드롭다운
    @GetMapping("/mproductsList")
    public List<ProductVO> getProductList(){
        return purchaseMatService.getProductList();
    }

     //자재주문시 계약된 공급처 드롭다운
    @GetMapping("/mmatVendorList")
    public List<MatVendorVO> getMatVendor(@RequestParam String matId) {
        return purchaseMatService.getMatVendorList(matId);
    }

    //창고리스트 드롭다운 
    @GetMapping("/mwarehouseList")
    public List<WareHouseVO> getWarehouseList(){
        return purchaseMatService.getWarehouseList();
    }

    /*======================================
     * 공통코드 상태값
     * ======================================*/
    @GetMapping("/mstatus/{groupId}")
    public List<MatStatusVO> selectCodeList(@PathVariable String groupId) {
        return purchaseMatService.selectCodeList(groupId);
    }

    /*==============================================
     * 재스퍼 출고명세서
     ===============================================*/
    @GetMapping("/mshipment/{inboundId}")
    public ResponseEntity<byte[]> exportShipment(@PathVariable Long inboundId) throws Exception {
    Map<String, Object> params = new HashMap<>();
    params.put("InboundId", inboundId);

    Resource resource = resourceLoader.getResource("classpath:/reports/ship.jasper");
    System.out.println("📦 exists: " + resource.exists());
    System.out.println("📁 URL: " + resource.getURL());

    try (InputStream jasperStream = resource.getInputStream();
         Connection conn = dataSource.getConnection()) {

        // ✅ 수정 부분: compileReport → JRLoader.loadObject 로 변경
        JasperReport jasperReport = (JasperReport) JRLoader.loadObject(jasperStream);

        JasperPrint print = JasperFillManager.fillReport(jasperReport, params, conn);
        byte[] pdf = JasperExportManager.exportReportToPdf(print);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("filename", "shipment_" + inboundId + ".pdf");

        return new ResponseEntity<>(pdf, headers, HttpStatus.OK);
    }
}

 /*======================================
     *  제품 재고조회
     * ======================================*/
    @GetMapping("/stockByProd")
    public ResponseEntity<Map<String, Object>> getProductStockList(ProdSearchDTO searchDTO, PageDTO pageDTO) {
        Map<String, Object> result = stockByProductService.getProductStockList(searchDTO, pageDTO);
        return ResponseEntity.ok(result);
    }
    //자재별LOT현황
    @GetMapping("/stockByProdLotList")
    public List<ProdStockDTO> getProductLotList(@RequestParam String prodId) {
        return stockByProductService.getProductLotList(prodId);
    }

}




