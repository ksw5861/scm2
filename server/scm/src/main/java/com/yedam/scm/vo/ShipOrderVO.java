package com.yedam.scm.vo;

import java.time.LocalDate;
import lombok.Data;


//김동원 출하지시
@Data
public class ShipOrderVO {

    private String shipId;        // 출하지시 ID
    private String shipNo;        // 출하지시 번호
    private String  orderId;         // 주문 ID
    private LocalDate shipDate;   // 출하 일자
    private String shipStatus;    // 출하 상태
    private Long warehouseId;     // 창고 ID
    private Long createdBy;       // 등록자
    private LocalDate createdAt;  // 등록일자
    private LocalDate updatedAt;  // 수정일자
    private String status;


    // 추가 1002
    private String vendorName; 

}
