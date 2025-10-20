package com.yedam.scm.dto;

import java.util.List;

import com.yedam.scm.vo.NoticeVO;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class NoticeListRes {

  private List<NoticeVO> data;
  private PageDTO page;
  
}
