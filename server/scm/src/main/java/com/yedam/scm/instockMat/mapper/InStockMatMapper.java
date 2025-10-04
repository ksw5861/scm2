package com.yedam.scm.instockMat.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.yedam.scm.vo.InboundVO;

@Mapper
public interface InStockMatMapper {

    List<InboundVO> getVenShipList();
    
}
