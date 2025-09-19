package com.yedam.scm.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.yedam.scm.vo.WarehouseVO;

@Mapper
public interface WarehouseMapper {
  List<WarehouseVO> selectWarehouseList();
}
