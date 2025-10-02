package com.yedam.scm.dto;

import java.util.List;

import com.yedam.scm.vo.InboundDetailVO;
import com.yedam.scm.vo.InboundVO;
import com.yedam.scm.vo.PurchaseMatVO;
import com.yedam.scm.vo.ShipmentInfoVO;

import lombok.Data;

@Data
public class MatInboundregisterDTO {
    
    private InboundVO inbound;                 // 입고 마스터
    private List<InboundDetailVO> details;     // 입고 상세들
    private ShipmentInfoVO shipment;           //배송정보
}
