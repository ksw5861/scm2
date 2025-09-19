package com.yedam.scm.vo;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
public class WarehouseVO {

  String whId;
  String whName;
  String whAddress;
  String whOwner;
  String ownerTel;
  String status;

}
