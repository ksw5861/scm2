package com.yedam.scm.vo;

import java.util.Date;

import lombok.Data;

@Data
public class PurchaseMatVO {
    
    private Long purId;
    private String matId;
    private Long reqQty;  //주문수량
    private Long outTotalQty;   //누적출고수량
    private String purMatStatus;
    private String resonComm; //(공급처)발주반려
    private Date rejDate; //반려일자
    private Long mrpDetId;
    private String vendorId;
    private String empName;
    private Date regDate;
    private String purNo;
    private Long total;
    private Date dueDate;
    private Date expectDate;
    private String shipOrderNo;
    private Long outQty; //출고승인시 입력되는 출고수량 (부분출고시 매번 바뀜)
    private String toWarehouse;

    MaterialVO materialVO;
    VendorVO vendorVO;
    WareHouseVO wareHouseVO;

    PurStatusLogVO purStatusLogVO;
}

