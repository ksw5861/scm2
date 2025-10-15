// src/main/java/com/yedam/scm/outboundMat/mapper/PlanMapper.java
package com.yedam.scm.outboundMat.mapper;

import com.yedam.scm.vo.PrdPlanDetailVO;
import com.yedam.scm.vo.ReqMatVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

@Mapper
public interface PlanMapper {

    // 좌측(PL_DETAIL) 목록
    List<PrdPlanDetailVO> selectPlanDetails(
            @Param("plId") Long plId,
            @Param("prodNo") String prodNo,
            @Param("dateFrom") Date dateFrom,
            @Param("dateTo") Date dateTo
    );

    int countPlanDetails(
            @Param("plId") Long plId,
            @Param("prodNo") String prodNo,
            @Param("dateFrom") Date dateFrom,
            @Param("dateTo") Date dateTo
    );

    // 우측: REQ_MAT + MATERIAL 조인 (자재명/단위 포함)
    List<ReqMatVO> selectReqMatWithMaterialByPlDetId(@Param("plDetId") Long plDetId);
}
