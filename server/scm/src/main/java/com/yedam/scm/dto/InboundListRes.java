package com.yedam.scm.dto;

import java.util.List;
import com.yedam.scm.vo.ProductVO;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class InboundListRes {
  private List<ProductVO> data; // 제품 리스트 (VO)
  private PageDTO page;         // 페이징 정보
}

// {
//   data : 
//      {
//        prodId : "",
//        prodName : "",
//      },
//      {
//        prodId : "",
//        prodName : "",
//      }
//
//   page {
//     데이터가 총 몇개인지,
//     총 몇 페이지인지,
//     현재 페이지는 어디인지
//   }
// }