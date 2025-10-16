package com.yedam.scm.outboundMat.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.yedam.scm.dto.MatIssueLineResult;


@Mapper
public interface MatIssueMapper {

    void callProcMatLotIssueSimple(MatIssueLineResult request);
}
