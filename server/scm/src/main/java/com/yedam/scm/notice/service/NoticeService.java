package com.yedam.scm.notice.service;

import java.io.File;

import org.springframework.web.multipart.MultipartFile;

import com.yedam.scm.dto.NoticeListRes;
import com.yedam.scm.dto.NoticeSearchDTO;
import com.yedam.scm.dto.PageDTO;
import com.yedam.scm.vo.NoticeVO;

public interface NoticeService {

  // 배너 이미지 불러오기
  File getNoticeBannerImgByNoticeNo(String noticeNo);
  
  // 공지사항 목록 조회
  NoticeListRes getNoticeListByConditionAndpaging(
    NoticeSearchDTO condition,
    PageDTO paging
  );

  // 공지사항 단건 조회
  NoticeVO getNoticeByNoticeNo(String noticeNo);
  
  // 공지사항 등록
  boolean addNotice(NoticeVO notice, MultipartFile bannerImg);

  // 공지사항 수정
  boolean modifyNotice(NoticeVO notice, MultipartFile bannerImg);

  // 공지사항 삭제
  boolean removeNoticeByNoticeNo(String noticeNo);

}
