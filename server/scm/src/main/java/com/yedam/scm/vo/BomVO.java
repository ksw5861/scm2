package com.yedam.scm.vo;

import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;

@Data
public class BomVO {
    private String bomId;
    private String bomVersion;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "Asia/Seoul")
    private Date effectiveDate;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "Asia/Seoul")
    private Date expireDate;
    private String prodId;
    private String matId;
    private Date createdAt;
    private Integer qty; // 수량
    private Date lastUpdateDate;

    private Double lossRate;

    private MaterialVO material; // 자재 정보 포함
    private ProductVO product;
    private BomDetailVO bomDetail;
    

private List<BomDetailVO> details; // BOM_DETAIL 리스트

}
