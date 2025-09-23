package com.yedam.scm.vo;

import lombok.Data;


//김동원 출하지시상세
@Data
public class ShipOrderDetailVO {

    private Long shipdtlId;     // 출하지시 상세 ID
    private String shipId;      // 출하지시 ID
    private Long productId;     // 제품 ID
    private Long orderDtlId;    // 주문 상세 ID
    private int qtyOrdered;     // 주문 수량
    private int qtyShip;        // 출하지시 수량
    private int qtyShipped;     // 실출하 수량
    private String status;      // 출하 상태

}
