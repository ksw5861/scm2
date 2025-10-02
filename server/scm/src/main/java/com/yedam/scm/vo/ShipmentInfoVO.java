package com.yedam.scm.vo;

import lombok.Data;

@Data
public class ShipmentInfoVO {
    private Long shipId;        // ship_id (PK)
    private Long inboundId;     // inbound_id (FK)

    private String carrierName; // 운송사명
    private String driverName;  // 기사명
    private String driverPhone; // 기사 연락처
    private String vehicleNo;   // 차량 번호
    private String shipFrom;    // 출발지
    private String shipTo;      // 도착지
    private String trackingNo;  // 운송장번호 (택배사/운송사 발행 번호)
}
