package com.yedam.scm.vo;

import java.time.LocalDate;
import lombok.Data;



//김동원 입고
@Data
public class ItemInboundVO {

    private String inboundId;     // 입고 ID
    private String prdLot;        // 제품 LOT 번호
    private String prodNo;        // 제품 번호
    private String prodId;        // 제품 ID
    private int inQty;            // 입고 수량
    private LocalDate inDate;     // 입고 일자
    private String employeeId;    // 담당자 ID
    private String whId;          // 창고 ID
    private String prodName;      // 제품명
    private int totalQty;

    // 화면에서 추가로 쓰는 값
    private Integer stockNow;    // 현재 잔여 재고
    private Integer stockAfter;  // 입고 후 재고
    private LocalDate endDate;   // 생산 종료일 (조인: product_plan.end_date)
    

}
