package com.yedam.scm.StockByProduct.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.yedam.scm.dto.ProdStockDTO;

@Mapper
public interface StockByProductMapper {
    List<ProdStockDTO> getProductStockList(@Param("startRow") int startRow,
                                   @Param("endRow") int endRow,
                                   @Param("prodName") String prodName,
                                   @Param("productLot") String productLot);

   Long getProductStockCount(@Param("prodName") String prodName,
                         @Param("productLot") String productLot);
    //자재별LOT목록
    List<ProdStockDTO> getProductLotList(@Param("prodId") String prodId);
}
