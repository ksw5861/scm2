// src/main/java/com/yedam/scm/outboundMat/service/impl/ReqMatServiceImpl.java
package com.yedam.scm.outboundMat.service.impl;

import com.yedam.scm.outboundMat.mapper.ReqMatMapper;
import com.yedam.scm.outboundMat.service.ReqMatService;
import com.yedam.scm.vo.ReqMatVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReqMatServiceImpl implements ReqMatService {
    private final ReqMatMapper reqMatMapper;

    @Override
    public List<ReqMatVO> getReqMatByPlDetId(Long plDetId) {
        return reqMatMapper.selectReqMatByPlDetId(plDetId);
    }
}
