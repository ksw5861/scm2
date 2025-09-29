package com.yedam.scm.vo;

import lombok.Data;

@Data
public class MrpDetailVO {

    private Long mrpDetId;
    private String matId;
    private Long needsQty;
    private String status;
    private Long mrpId;

    MaterialVO materialVO;
}
