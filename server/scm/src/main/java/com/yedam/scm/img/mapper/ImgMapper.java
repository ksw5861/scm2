package com.yedam.scm.img.mapper;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ImgMapper {

  // 이미지 경로 가져오기
  String getLogimg(String inLogId);
}
