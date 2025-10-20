package com.yedam.scm.web;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.yedam.scm.dto.NoticeListRes;
import com.yedam.scm.dto.NoticeSearchDTO;
import com.yedam.scm.dto.PageDTO;
import com.yedam.scm.notice.service.NoticeService;
import com.yedam.scm.vo.NoticeVO;

import lombok.RequiredArgsConstructor;

/**
 * NoticeController
 * ============================================================
 * 공지사항 API Controller
 * ============================================================
 */

@RestController
@RequiredArgsConstructor
public class NoticeController {

  private final NoticeService noticeSvc;

  // =================================================================
  // 1. 배너 이미지 불러오기 ( 공지사항 번호 )
  // =================================================================

  @GetMapping(value = "/img/notice-banner/{noticeNo}", produces = MediaType.IMAGE_JPEG_VALUE)
  public ResponseEntity<byte[]> getNoticeBannerImg(@PathVariable String noticeNo) {
      try {
          File file = noticeSvc.getNoticeBannerImgByNoticeNo(noticeNo);

          if (file == null || !file.exists()) {
              return ResponseEntity.notFound().build();
          }

          byte[] imageBytes = Files.readAllBytes(file.toPath());
          return ResponseEntity.ok().body(imageBytes);
      } catch (Exception e) {
          return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
      }
  }

  // =================================================================
  // 2. 공지사항 목록 조회 ( 검색 조건 + 페이징 )
  // =================================================================

  @GetMapping("/notice")
  public ResponseEntity<NoticeListRes> getNoticeList(
    @ModelAttribute NoticeSearchDTO condition,
    @ModelAttribute PageDTO paging
  ) {

    NoticeListRes response = noticeSvc.getNoticeListByConditionAndpaging(condition, paging);

    if (response == null || response.getData() == null || response.getData().isEmpty()) {
        return ResponseEntity.noContent().build();
    }

    return ResponseEntity.ok(response);

  }

  // =================================================================
  // 3. 공지사항 단건 조회 ( 공지사항 번호 )
  // =================================================================

  @GetMapping("/notice/{noticeId}")
  public ResponseEntity<Map<String, Object>> getNotice(
    @PathVariable String noticeId
  ) {

    NoticeVO notice = noticeSvc.getNoticeByNoticeNo(noticeId);

    if (notice == null) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                             .body(Map.of("message", "해당 공지사항을 찾을 수 없습니다."));
    }

    return ResponseEntity.ok(Map.of("data", notice));
  }

  // =================================================================
  // 4. 공지사항 등록
  // =================================================================

  @PostMapping(value = "/notice", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<Map<String, Object>> addNotice(
    @ModelAttribute NoticeVO notice,
    @RequestPart(required = false) MultipartFile selectedBannerImg
  ) {

    return noticeSvc.addNotice(notice, selectedBannerImg)
      ? ResponseEntity.ok().build()
      : ResponseEntity.badRequest().build();

  }

  // =================================================================
  // 5. 공지사항 수정
  // =================================================================

  @PutMapping(value = "/notice", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<Map<String, Object>> modifyNotice(
    @ModelAttribute NoticeVO notice,
    @RequestPart(required = false) MultipartFile selectedBannerImg
  ) {

    return noticeSvc.modifyNotice(notice, selectedBannerImg)
      ? ResponseEntity.ok().build()
      : ResponseEntity.badRequest().build();
  }

  // =================================================================
  // 6. 공지사항 삭제 ( 공지사항 번호 )
  // =================================================================

  @DeleteMapping("/notice/{noticeNo}")
  public ResponseEntity<Map<String, Object>> removeNotice(
    @PathVariable String noticeNo
  ) {

    return noticeSvc.removeNoticeByNoticeNo(noticeNo)
      ? ResponseEntity.ok().build()
      : ResponseEntity.status(HttpStatus.NOT_FOUND).build();
  }
}