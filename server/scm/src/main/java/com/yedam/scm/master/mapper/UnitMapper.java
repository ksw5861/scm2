package com.yedam.scm.master.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.yedam.scm.vo.UnitVO;


@Mapper
public interface UnitMapper {
    List<UnitVO> getUnitList();
}

