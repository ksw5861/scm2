package com.yedam.scm.dto;

import java.util.List;

import com.yedam.scm.vo.WareHouseVO;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class WarehouseListRes {
    private List<WareHouseVO> data; // 창고 리스트
    private PageDTO page;           // 페이징 정보
}
