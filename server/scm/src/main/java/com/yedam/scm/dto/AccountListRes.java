package com.yedam.scm.dto;

import java.util.List;

import com.yedam.scm.vo.AccountVO;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AccountListRes {

  private List<AccountVO> data;
  private PageDTO page;

}
