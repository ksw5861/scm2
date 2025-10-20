package com.yedam.scm.vo;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;

@Data
public class NoticeVO {

  // 공지사항 게시글 번호
  private String noticeNo;

  // 공지사항 게시글 제목
  private String title;

  // 공지사항 작성자의 이름 ( DB에선 계정 코드가 저장됨, 불러올 때에는 계정 테이블에서 이름)
  private String author;

  // 공지사항 게시글 내용
  private String content;

  // 공지사항 작성 일자
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy년 MM월 dd일 HH:mm", timezone = "Asia/Seoul")
  private Date createdAt;
}
