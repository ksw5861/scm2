package com.yedam.scm.vo;

import java.util.Date;
import java.time.LocalDate;

import lombok.Data;


//김동원 제품LOT
@Data
public class ProductLotVO {

    
    private String prodNo; //Lot번호
    private String prodId;        // 제품 ID
    private int proQty;         // 총 수량
    private String matStatus; //자재입고상태
    private LocalDate endDate;  //유통기한
    private Date proDate; //입고일자
    private String prodName; //제품명
    private String spec; //규격
    private String unit; //단위


    private String whId;          // 창고 ID
    private int useQty;           // 사용 수량
    private int remainQty;        // 잔여 수량
    private LocalDate transferDate; // 이동 일자

    
    private String status;        // 상태


    //추가




}
