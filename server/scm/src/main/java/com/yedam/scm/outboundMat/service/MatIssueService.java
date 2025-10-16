package com.yedam.scm.outboundMat.service;

import com.yedam.scm.dto.MatIssueLineResult;

public interface MatIssueService {
  void executeMatLotIssue(MatIssueLineResult request);
}
