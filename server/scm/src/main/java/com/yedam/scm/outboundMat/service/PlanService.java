// src/main/java/com/yedam/scm/outboundMat/service/PlanService.java
package com.yedam.scm.outboundMat.service;

import com.yedam.scm.vo.PrdPlanDetailVO;
import com.yedam.scm.vo.ReqMatVO;

import java.util.Date;
import java.util.List;

public interface PlanService {
    List<PrdPlanDetailVO> getPlanDetails(Long plId, String prodNo, Date dateFrom, Date dateTo);
    int countPlanDetails(Long plId, String prodNo, Date dateFrom, Date dateTo);

    // REQ_MAT + MATERIAL 조인 결과 반환
    List<ReqMatVO> getReqMatWithMaterialByPlDetId(Long plDetId);
}
