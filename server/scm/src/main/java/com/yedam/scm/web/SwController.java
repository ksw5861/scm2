package com.yedam.scm.web;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import com.yedam.scm.master.service.MaterialService;
import com.yedam.scm.master.service.ProductService;
import com.yedam.scm.master.service.WareHouseService1;
import com.yedam.scm.master.service.BomService;
import com.yedam.scm.master.service.UnitService;
import com.yedam.scm.vo.MaterialVO;
import com.yedam.scm.vo.ProductVO;
import com.yedam.scm.vo.UnitVO;
import com.yedam.scm.vo.WareHouseVO;
import com.yedam.scm.vo.BomVO;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
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
            @RequestParam(required = false) String matType,
            @RequestParam(required = false) String matStoreCond) {
        return materialSvc.getMaterialList(matId, matName, matType, matStoreCond);
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
    public int insertMaterial(@RequestBody MaterialVO materialVO) {      
        return materialSvc.insertMaterial(materialVO);
    }

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
}
