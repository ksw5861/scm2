package com.yedam.scm.outboundMat.service.impl;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yedam.scm.dto.MatIssueLineResult;
import com.yedam.scm.outboundMat.mapper.MatIssueMapper;
import com.yedam.scm.outboundMat.service.MatIssueService;

@Service
@Transactional
public class MatIssueServiceImpl implements MatIssueService {
  @Autowired
  private MatIssueMapper mapper;
  
  @Override
  public void executeMatLotIssue(MatIssueLineResult request) {
        System.out.printf("ㅇㅇㅇ" + request.getManagerId());
        mapper.callProcMatLotIssueSimple(request);
    }
}