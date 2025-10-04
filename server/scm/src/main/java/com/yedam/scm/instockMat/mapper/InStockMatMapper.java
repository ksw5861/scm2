package com.yedam.scm.instockMat.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.yedam.scm.vo.InboundDetailVO;
import com.yedam.scm.vo.InboundVO;

@Mapper
public interface InStockMatMapper {
    //하차대기(마스터)
    List<InboundVO> getVenShipList();
    //하차대기(상세)
    List<InboundDetailVO> getVenShipDetailList(@Param("inboundId")Long inboundId);
    //하차승인
    void callApproveUnload(@Param("inboundId")Long inboundId, @Param("unloadEmp")String unloadEmp);
}