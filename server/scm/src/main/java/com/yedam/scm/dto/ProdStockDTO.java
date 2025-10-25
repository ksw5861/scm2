package com.yedam.scm.dto;



import java.util.Date;

import lombok.Data;

@Data
public class ProdStockDTO {
    private String inboundId; //제품로트번호
    private String prodId;    //제품ID
    private String prodName;  //제품명
    private Long totalRemainQty; //보유총재고수량
    private Long remainQty;  //가용재고수량
    private String unit;      //단위
    private Date inDate;     //입고일자
    private Date expDate;    //유통기한
    private String whName;    //창고명
}
