// src/main/java/com/yedam/scm/outboundMat/service/ReqMatService.java
package com.yedam.scm.outboundMat.service;

import com.yedam.scm.vo.ReqMatVO;
import java.util.List;

public interface ReqMatService {
    List<ReqMatVO> getReqMatByPlDetId(Long plDetId);
}
