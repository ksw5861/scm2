// src/main/java/com/yedam/scm/outboundMat/mapper/ReqMatMapper.java
package com.yedam.scm.outboundMat.mapper;

import com.yedam.scm.vo.ReqMatVO;
import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface ReqMatMapper {
    List<ReqMatVO> selectReqMatByPlDetId(@Param("plDetId") Long plDetId);
}
