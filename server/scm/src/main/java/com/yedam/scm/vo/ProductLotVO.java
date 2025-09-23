package com.yedam.scm.vo;

import java.time.LocalDate;
import lombok.Data;


//김동원 제품LOT
@Data
public class ProductLotVO {

    private String prdLot;        // 제품 LOT 번호
    private String prodId;        // 제품 ID
    private int totalQty;         // 총 수량
    private String whId;          // 창고 ID
    private int useQty;           // 사용 수량
    private int remainQty;        // 잔여 수량
    private LocalDate transferDate; // 이동 일자
    private String status;        // 상태 (Y: 사용, N: 미사용 등)

}
