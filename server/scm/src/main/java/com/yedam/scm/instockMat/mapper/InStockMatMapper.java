package com.yedam.scm.instockMat.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.yedam.scm.dto.PageDTO;
import com.yedam.scm.vo.InboundDetailVO;
import com.yedam.scm.vo.InboundLogVO;
import com.yedam.scm.vo.InboundVO;
import com.yedam.scm.vo.MatLotVO;
import com.yedam.scm.web.MatUnloadSearchDTO;

@Mapper
public interface InStockMatMapper {
    //하차대기(마스터)
    List<InboundVO> getVenShipList(@Param("startRow")int start, @Param("endRow")int end);
    Long getVenShipListCount();
    //하차대기(상세)
    List<InboundDetailVO> getVenShipDetailList(@Param("inboundId")Long inboundId);
    //하차승인
    void callApproveUnload(@Param("inboundId")Long inboundId, @Param("unloadEmp")String unloadEmp);
    void callUnloadReturn(@Param("inboundId")Long inboundId,  @Param("unloadEmp")String unloadEmp, @Param("rejMemo")String rejMemo);
    //입고대기목록
    List<InboundVO> getApproveUnload(@Param("page")PageDTO pageDTO, @Param("search")MatUnloadSearchDTO searchDTO);
    Long getApproveUnloadListCount(@Param("search")MatUnloadSearchDTO searchDTO);
    //입고대기목록(상세)
    List<InboundDetailVO> getApproveUnloadDetailList(@Param("inboundId")Long inboundId);
    //입고등록
    void callMatInboundStock(InboundDetailVO inStockInfo);
    //불량등록 [1.불량정보등록 프로시저 2.불량정보등록 시퀀스아이디get 3.이미지등록]
    Long callRegMatDefect(Map<String, Object> paramMap);
    //Long selectRecentSeq();
    void updateDefectImagePath(InboundLogVO defectData);
    List<MatLotVO> getMatStockList(@Param("startRow") int startRow,
                                   @Param("endRow") int endRow,
                                   @Param("materialId") String materialId,
                                   @Param("materialName") String materialName,
                                   @Param("lotNo") String lotNo,
                                   @Param("lotStatus") String lotStatus);

Long getMatStockCount(@Param("materialId") String materialId,
                      @Param("materialName") String materialName,
                      @Param("lotNo") String lotNo,
                      @Param("lotStatus") String lotStatus);
    //자재별LOT목록
    List<MatLotVO> getMatLotList(@Param("matId") String matId);
}