package com.yedam.scm.order.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.yedam.scm.vo.OrderVO;

@Mapper
public interface OrderMapper {
  
  List<OrderVO> selectOrderList();
}
