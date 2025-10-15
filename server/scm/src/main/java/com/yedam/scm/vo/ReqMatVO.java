// src/main/java/com/yedam/scm/vo/ReqMatVO.java
package com.yedam.scm.vo;

import lombok.Data;

@Data
public class ReqMatVO {
    private Long   reqId;      // REQ_ID
    private String matId;      // REQ_MAT.MAT_ID
    private Long   plDetId;    // PL_DET_ID
    private Double reqWeight;  // REQ_WEIGHT

    // 조인으로 가져올 필드
    private String matName;    // MATERIAL.MAT_NAME
    private String unit;       // MATERIAL.UNIT (있으면)
}
