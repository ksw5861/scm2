package com.yedam.scm.product.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.yedam.scm.dto.InboundListRes;
import com.yedam.scm.dto.PageDTO;
import com.yedam.scm.dto.WarehouseListRes;
import com.yedam.scm.product.mapper.InboundMapper;
import com.yedam.scm.product.service.InboundService;
import com.yedam.scm.vo.ItemInboundVO;
import com.yedam.scm.vo.ProductVO;
import com.yedam.scm.vo.WareHouseVO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InboundServiceImpl implements InboundService {

    private final InboundMapper inboundMapper;

 
    @Override
    public List<ItemInboundVO> selectInboundLots(ItemInboundVO vo){
        return inboundMapper.selectInboundLots(vo);
        
    }


      @Override
    public boolean registerInbound(Map<String,Object> inbound) {

    // {
    //     "prodNo": "PNO20250924-004",
    //     "employeeId": "asd",
    //     "totalQty": 10,
    //     "whId": "asd"
    // }

        inboundMapper.callInsertInbound(inbound);

    // {
    //     "prodNo": "PNO20250924-004",
    //     "employeeId": "asd",
    //     "totalQty": 10,
    //     "whId": "asd"
    //     "rowCount": "1"
    // }

        return ((int) inbound.get("rowCount")) > 0; // 실행 성공하면 true 리턴
    }
    

    //제품입고 페이지 삭제버튼
    @Override
    public int deleteInbound(String inboundId) {
        return inboundMapper.deleteInbound(inboundId);
    }



    //모달 부분
    @Override
    public InboundListRes getInboundProductList(String condition, PageDTO paging) {
    // 목록 조회
    List<ProductVO> data = inboundMapper.selectInboundProductListByCondition(condition, paging);

    // 전체 건수 조회 후 PageDTO 업데이트
    long total = inboundMapper.selectInboundProductCountByCondition(condition);
    paging.updatePageInfo(total);

       // 응답 DTO 구성
    return new InboundListRes(data, paging);
            //모달 부분//
    
  }



      @Override
    public WarehouseListRes getWarehouseList(String keyword, PageDTO paging) {
        // 조건 검색 (mapper에서 keyword로 id/name 둘 다 LIKE 처리하도록 수정 필요)
        List<WareHouseVO> data = inboundMapper.getWareHouseList(keyword, keyword, null);

        long total = data.size(); // 단순히 size() 써도 되고, count 쿼리 따로 만들면 더 좋음
        paging.updatePageInfo(total);

        return new WarehouseListRes(data, paging);
    }





  
} // end 
