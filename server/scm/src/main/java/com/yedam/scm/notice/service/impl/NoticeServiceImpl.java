package com.yedam.scm.notice.service.impl;

import java.io.File;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.yedam.scm.dto.NoticeListRes;
import com.yedam.scm.dto.NoticeSearchDTO;
import com.yedam.scm.dto.PageDTO;
import com.yedam.scm.notice.mapper.NoticeMapper;
import com.yedam.scm.notice.service.NoticeService;
import com.yedam.scm.vo.NoticeVO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NoticeServiceImpl implements NoticeService {

  @Value("${file.upload.notice-dir}")
  private String uploadDir; // 배너 이미지 업로드 경로 주입

  private final NoticeMapper mapper;

  @Override
  public File getNoticeBannerImgByNoticeNo(String noticeNo) {
    File dir = new File(uploadDir);
    if (dir.exists() && dir.isDirectory()) {
        File[] files = dir.listFiles((d, name) -> name.startsWith(noticeNo));
        if (files != null && files.length > 0) {
            return files[0];
        }
    }
    return null;
  }

  @Override
  public NoticeListRes getNoticeListByConditionAndpaging(
    NoticeSearchDTO condition,
    PageDTO paging
  ) {

    paging.updatePageInfo(mapper.selectNoticeTotalCountByCondition(condition));
    List<NoticeVO> data = mapper.selectNoticeListByConditionAndPaging(condition, paging);

    return new NoticeListRes(data, paging);
  }

  @Override
  public NoticeVO getNoticeByNoticeNo(String noticeNo) {
    return mapper.selectNoticeByNoticeNo(noticeNo);
  }

  @Override
  public NoticeListRes getNeighborNotices(String noticeNo) {

    // Mapper에서 위/아래 post 받아오기
    List<NoticeVO> data = mapper.selectNeighborNotices(noticeNo);

    return new NoticeListRes(data, null);
  }

  @Override
  @Transactional
  public boolean addNotice(NoticeVO notice, MultipartFile bannerImg) {

    if (mapper.insertNotice(notice) <= 0) {
      return false;
    }

    if (bannerImg != null && !bannerImg.isEmpty()) {
      savePhoto(notice.getNoticeNo(), bannerImg);
    }

    return true;
  }

  @Override
  @Transactional
  public boolean modifyNotice(NoticeVO notice, MultipartFile bannerImg) {
    if (mapper.updateNotice(notice) <= 0) {
      return false;
    }

    if (bannerImg != null && !bannerImg.isEmpty()) {
      savePhoto(notice.getNoticeNo(), bannerImg);
    }

    return true;
  }

  @Override
  @Transactional
  public boolean removeNoticeByNoticeNo(String noticeNo) {

      if (mapper.deleteNoticeByNoticeNo(noticeNo) <= 0) {
          return false;
      }

      File dir = new File(uploadDir);

      if (dir.exists() && dir.isDirectory()) {
          File[] files = dir.listFiles((d, name) -> name.startsWith(noticeNo));
          if (files != null) {
              for (File file : files) {
                  file.delete();
              }
          }
      }

      return true;
  }

  // 배너 이미지 저장
  private void savePhoto(String noticeNo, MultipartFile file) {
    try {
        File dir = new File(uploadDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        String originalName = file.getOriginalFilename();
        if (originalName == null) {
            throw new RuntimeException("업로드된 파일 이름이 없습니다.");
        }

        String ext = "";
        int dotIndex = originalName.lastIndexOf(".");
        if (dotIndex != -1) { 
            ext = originalName.substring(dotIndex);
        }

        String fileName = noticeNo + ext;
        File saveFile = new File(uploadDir + fileName);

        file.transferTo(saveFile);

    } catch (Exception e) {
        throw new RuntimeException("배너 이미지 저장 중 오류 발생", e);
    }
  }

}