package com.yedam.scm.notice.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.yedam.scm.dto.NoticeSearchDTO;
import com.yedam.scm.dto.PageDTO;
import com.yedam.scm.vo.NoticeVO;

@Mapper
public interface NoticeMapper {

  // 공지사항 총 데이터 수
  Long selectNoticeTotalCountByCondition(NoticeSearchDTO condition);

  // 공지사항 목록 조회
  List<NoticeVO> selectNoticeListByConditionAndPaging(
    @Param("condition") NoticeSearchDTO condition,
    @Param("paging") PageDTO paging
  );

  // 공지사항 단건 조회
  NoticeVO selectNoticeByNoticeNo(String noticeNo);

  // 선택된 공지사항 기준 위/아래 공지사항 목록 조회
  List<NoticeVO> selectNeighborNotices(String noticeNo);
  
  // 공지사항 등록
  int insertNotice(NoticeVO notice);

  // 공지사항 수정
  int updateNotice(NoticeVO notice);

  // 공지사항 삭제
  int deleteNoticeByNoticeNo(String noticeNo);

}
