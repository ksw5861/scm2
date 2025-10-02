package com.yedam.scm.web;

import java.sql.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import com.yedam.scm.master.service.MaterialService;
import com.yedam.scm.master.service.ProductService;
import com.yedam.scm.master.service.WareHouseService1;
import com.yedam.scm.dto.PageDTO;
import com.yedam.scm.dto.VendorSearchDTO;
import com.yedam.scm.master.service.BomService;
import com.yedam.scm.master.service.UnitService;
import com.yedam.scm.master.service.VendorService;
import com.yedam.scm.vo.MaterialVO;
import com.yedam.scm.vo.ProductVO;
import com.yedam.scm.vo.UnitVO;
import com.yedam.scm.vo.VendorVO;
import com.yedam.scm.vo.WareHouseVO;
import com.yedam.scm.vo.BomVO;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

/*
 * 개발자:김상우
 * 자재, 제품, 창고, bom정보, 단위정보 관리
 * 
 *  *  수정일     수정자            수정내용
 *  ----------   -------    ---------------------------
 *  2025.09.28   김상우            최초 생성
 */
@RestController
public class SwController {

    @Autowired
    MaterialService materialSvc;

    @Autowired
    ProductService productSvc;

    @Autowired
    WareHouseService1 whSvc;

    @Autowired
    BomService bomSvc;

    @Autowired
    UnitService unitSvc;

    // =========================================================
    // ================ Material API ==========================
    // =========================================================

    // 자재 목록 조회
    @GetMapping("/material")
    public List<MaterialVO> getMaterialList(
            @RequestParam(required = false) String matId,
            @RequestParam(required = false) String matName,
            @RequestParam(required = false) String status) {
        return materialSvc.getMaterialList(matId, matName, status);
    }

    // 자재 상세조회 
    @GetMapping("/material/{matId}")
    public List<MaterialVO> getMaterialDetail(@PathVariable String matId) {
        return materialSvc.getMaterialDetail(matId);
    }

    // 자재 정보 삭제
    @DeleteMapping("/material/{matId}")
    public ResponseEntity<?> deleteMaterial(@PathVariable String matId) {
    try {
        materialSvc.deleteMaterial(matId); // 참조 있으면 IllegalStateException 발생
        return ResponseEntity.ok(Map.of("status", "success"));
    } catch (IllegalStateException e) {
        // 자재가 참조 중일 때 → 409 Conflict
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("status", "error", "message", e.getMessage()));
    } catch (Exception e) {
        // 그 외 DB 오류 등 → 500 Internal Server Error
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("status", "error", "message", "서버 오류가 발생했습니다."));
        }
    }

    // 정보 등록
    @PostMapping("/material")
    public Map<String, Object> insertMaterial(@RequestBody MaterialVO materialVO) {    
        
        Map<String, Object> result = new HashMap<>();
        if (materialSvc.insertMaterial(materialVO)) {
            result.put("status", "success");
            // result.put("new_id", materialVO.getMatId());
        } else {
            result.put("status", "fail");
            // result.put("data", null);
        }
        
        return result;
    }

    // result.data => 1
    // result.data => { "status" : "success" }

    @PutMapping("/material/{matId}")
    public int updateMaterial(@PathVariable String matId, @RequestBody MaterialVO materialVO) {
        materialVO.setMatId(matId);
        return materialSvc.updateMaterial(materialVO);
    }

    // =========================================================
    // ================ Product API ===========================
    // =========================================================
    


    @GetMapping("/product")
    public List<ProductVO> getProductList(
            @RequestParam(required = false) String prodId,
            @RequestParam(required = false) String prodName,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String unit,
            @RequestParam(required = false) Date createdAt) {
        return productSvc.getProductList(prodId, prodName, status, unit, createdAt);
    }

    @GetMapping("/product/{prodId}")
    public List<ProductVO> getProductDetail(@PathVariable String prodId) {
        return productSvc.getProductDetail(prodId);
    }

    @DeleteMapping("/product/{prodId}")
    public ResponseEntity<?> deleteProduct(@PathVariable String prodId) {
        try{
            productSvc.deleteProduct(prodId); // 참조있으면 IllegalStateException 발생
            return ResponseEntity.ok(Map.of("status", "success"));
        } catch(IllegalStateException e){
            // 자재가 참조 중일 때 -> 409 conflict
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("status", "error", "message", e.getMessage()));
        } catch (Exception e){
            // 그 외 DB오류 등 -> 500 Internal Server Error
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("status", "error", "message", "서버 오류가 발생했습니다."));
        }
    }

    @PostMapping("/product")
    public int insertProduct(@RequestBody ProductVO productVO) {
        return productSvc.insertProduct(productVO);
    }

    @PutMapping("/product/{prodId}")
    public int updateProduct(@PathVariable String prodId, @RequestBody ProductVO productVO) {
        productVO.setProdId(prodId);
        return productSvc.updateProduct(productVO);
    }

    // =========================================================
    // ================ Warehouse API =========================
    // =========================================================
    @GetMapping("/warehouse1")
    public List<WareHouseVO> getWareHouseList(
            @RequestParam(required = false) String whId,
            @RequestParam(required = false) String whName,
            @RequestParam(required = false) String status) {
        return whSvc.getWareHouseList(whId, whName, status);
    }

    @GetMapping("/warehouse1/{whId}")
    public WareHouseVO getWareHouseDetail(@PathVariable String whId) {
        return whSvc.getWareHouseDetail(whId);
    }

    @DeleteMapping("/warehouse1/{whId}")
    public int deleteWareHouse(@PathVariable String whId) {
        return whSvc.deleteWareHouse(whId);
    }

    @PostMapping("/warehouse1")
    public int insertWareHouse(@RequestBody WareHouseVO wareHouseVO) {
        return whSvc.insertWareHouse(wareHouseVO);
    }

    @PutMapping("/warehouse1/{whId}")
    public int updateWareHouse(@PathVariable String whId, @RequestBody WareHouseVO wareHouseVO) {
        wareHouseVO.setWhId(whId);
        return whSvc.updateWareHouse(wareHouseVO);
    }

    // =========================================================
    // ================ BOM API ===============================
    // =========================================================
    // BOM 버전관리 버전변경유무 또는 정보UPDATE시 버전변경
    @GetMapping("/bom/{prodId}")
    public List<BomVO> getBomDetail(@PathVariable String prodId) {
        return bomSvc.getBomDetailByProdId(prodId);
    }

    @GetMapping("/bom/detail/{bomId}")
    public BomVO getBomDetailById(@PathVariable String bomId) {
        return bomSvc.getBomDetail(bomId);
    }

    @DeleteMapping("/bom/{bomId}")
    public int deleteBom(@PathVariable String bomId) {
        return bomSvc.deleteBom(bomId);
    }

    @PostMapping("/bom")
    public int insertBom(@RequestBody BomVO bomVO) {
        return bomSvc.insertBom(bomVO);
    }

    @PutMapping("/bom/{bomId}")
    public int updateBom(@PathVariable String bomId, @RequestBody BomVO bomVO) {
        bomVO.setBomId(bomId);
        return bomSvc.updateBom(bomVO);
    }

    // =========================================================
    // ================ Unit API ==============================
    // =========================================================
    @GetMapping("/unit")
    public List<UnitVO> getUnitList(
            @RequestParam(required = false) String unitId,
            @RequestParam(required = false) String unitName) {
        return unitSvc.getUnitList();
    }

    // =========================================================
    // ================ Vendor API ============================
    // =========================================================
    @Autowired
    VendorService vendorSvc;

    @GetMapping("/vendor")
    public Map<String, Object> getVendorList(
        @ModelAttribute VendorSearchDTO condition,
        @ModelAttribute PageDTO paging
    ) {

        return vendorSvc.getVendorList(condition, paging);
    }

    @GetMapping("/vendor/{vendorId}")
    public VendorVO getVendorDetail(@PathVariable String vendorId) {
        return vendorSvc.getVendorDetail(vendorId);
    }

    @PostMapping("/vendor")
    public ResponseEntity<Map<String, Object>> insertVendor(@RequestBody VendorVO vendor) {

        Map<String, Object> response = new HashMap<>();

        try {
            boolean isInserted = vendorSvc.insertVendor(vendor);

            if (isInserted) {
                response.put("vendorId", vendor.getVendorId());
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
        } catch (Exception e) {
            response.put("message", "서버 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }

    }

    @PutMapping("/vendor")
    public ResponseEntity<Map<String, Object>> updateVendor(@RequestBody VendorVO vendor) {
        
        Map<String, Object> response = new HashMap<>();

        try {
            boolean isUpdated = vendorSvc.updateVendor(vendor);

            if (isUpdated) {
                response.put("vendorId", vendor.getVendorId());
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
        } catch (Exception e) {
            response.put("message", "서버 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @DeleteMapping("/vendor/{vendorId}")
    public ResponseEntity<Map<String, Object>> deleteVendor(@PathVariable String vendorId) {

        Map<String, Object> response = new HashMap<>();

        try {
            boolean isDeleted = vendorSvc.deleteVendor(vendorId);

            if (isDeleted) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
        } catch (Exception e) {
            response.put("message", "서버 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // =========================================================
    // ================ MaterialVendor API =====================
    // =========================================================
    @Autowired
    com.yedam.scm.master.service.MaterialVendorService materialVendorSvc;

    // 자재별 거래처 목록 조회
    @GetMapping("/material/{matId}/vendor")
    public List<com.yedam.scm.vo.MaterialVendorVO> getMaterialVendorList(@PathVariable String matId) {
        return materialVendorSvc.getMaterialVendorList(matId);
    }

    // 거래처 상세 조회
    @GetMapping("/material/vendor/{matVendorId}")
    public com.yedam.scm.vo.MaterialVendorVO getMaterialVendorDetail(@PathVariable String matVendorId) {
        return materialVendorSvc.getMaterialVendorDetail(matVendorId);
    }

    // 거래처 등록
    @PostMapping("/material/vendor")
    public int insertMaterialVendor(@RequestBody com.yedam.scm.vo.MaterialVendorVO materialVendorVO) {
        return materialVendorSvc.insertMaterialVendor(materialVendorVO);
    }

    // 거래처 수정
    @PutMapping("/material/vendor/{matVendorId}")
    public int updateMaterialVendor(@PathVariable String matVendorId, @RequestBody com.yedam.scm.vo.MaterialVendorVO materialVendorVO) {
        materialVendorVO.setMatVendorId(matVendorId);
        return materialVendorSvc.updateMaterialVendor(materialVendorVO);
    }

    // 거래처 삭제
    @DeleteMapping("/material/vendor/{matVendorId}")
    public int deleteMaterialVendor(@PathVariable String matVendorId) {
        return materialVendorSvc.deleteMaterialVendor(matVendorId);
    }
}
