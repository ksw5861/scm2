package com.yedam.scm.web;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
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
    @GetMapping("/material")
    public List<MaterialVO> getMaterialList(
            @RequestParam(required = false) String matId,
            @RequestParam(required = false) String matName,
            @RequestParam(required = false) String status) {
        return materialSvc.getMaterialList(matId, matName, status);
    }

    @GetMapping("/material/{matId}")
    public List<MaterialVO> getMaterialDetail(@PathVariable String matId) {
        return materialSvc.getMaterialDetail(matId);
    }

    @DeleteMapping("/material/{matId}")
    public int deleteMaterial(@PathVariable String matId) {
        return materialSvc.deleteMaterial(matId);
    }

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
            @RequestParam(required = false) String unit) {
        return productSvc.getProductList(prodId, prodName, status, unit);
    }

    @GetMapping("/product/{prodId}")
    public List<ProductVO> getProductDetail(@PathVariable String prodId) {
        return productSvc.getProductDetail(prodId);
    }

    @DeleteMapping("/product/{prodId}")
    public int deleteProduct(@PathVariable String prodId) {
        return productSvc.deleteProduct(prodId);
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
    public com.yedam.scm.vo.VendorVO getVendorDetail(@PathVariable String vendorId) {
        return vendorSvc.getVendorDetail(vendorId);
    }

    @PostMapping("/vendor")
    public int insertVendor(@RequestBody com.yedam.scm.vo.VendorVO vendorVO) {
        return vendorSvc.insertVendor(vendorVO);
    }

    @PutMapping("/vendor/{vendorId}")
    public int updateVendor(@PathVariable String vendorId, @RequestBody com.yedam.scm.vo.VendorVO vendorVO) {
        vendorVO.setVendorId(vendorId);
        return vendorSvc.updateVendor(vendorVO);
    }

    @DeleteMapping("/vendor/{vendorId}")
    public int deleteVendor(@PathVariable String vendorId) {
        return vendorSvc.deleteVendor(vendorId);
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
