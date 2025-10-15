package com.yedam.scm.instockMat.service.impl;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.yedam.scm.config.FileUploadProperties;
import com.yedam.scm.dto.PageDTO;
import com.yedam.scm.instockMat.mapper.InStockMatMapper;
import com.yedam.scm.instockMat.service.InStockMatService;
import com.yedam.scm.vo.InboundDetailVO;
import com.yedam.scm.vo.InboundLogVO;
import com.yedam.scm.vo.InboundVO;
import com.yedam.scm.vo.MatLotVO;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class InStockMatServiceImpl implements InStockMatService {
    
    final InStockMatMapper mapper;
    final FileUploadProperties fileUploadProperties;   //환경설정으로 지정한 첨부파일저장경로 주입 [application.yml에 경로지정됨.]

    @Override
    public List<InboundVO> getVenShipList() {
        return mapper.getVenShipList();
    }

    @Override
    public List<InboundDetailVO> getVenShipDetailList(Long inboundId) {
       return mapper.getVenShipDetailList(inboundId);
    }

    @Override
    public void callApproveUnload(Long inboundId, String unloadEmp) {
        mapper.callApproveUnload(inboundId, unloadEmp);
    }

    @Override
    public void callUnloadReturn(Long inboundId, String unloadEmp, String rejMemo) {
        mapper.callUnloadReturn(inboundId, unloadEmp, rejMemo);
    }

    @Override
    public List<InboundVO> getApproveUnload() {
        return mapper.getApproveUnload();
    }

     @Override
    public List<InboundDetailVO> getApproveUnloadDetailList(Long inboundId) {
       return mapper.getApproveUnloadDetailList(inboundId);
    } 

    @Override
    public void callMatInboundStock(InboundDetailVO inStockInfo) {
        mapper.callMatInboundStock(inStockInfo);
    }

    @Override
    public Map<String, Object> getMatStockList(PageDTO pageDTO) {
       
       List<MatLotVO> list = mapper.getMatStockList(pageDTO.getStartRow(), pageDTO.getEndRow());
       long total = mapper.getMatStockCount();

       pageDTO.updatePageInfo(total);

       Map<String, Object> result = new HashMap<>();
       result.put("list", list);
       result.put("page", pageDTO);
       return result;
    }

    @Override
    public List<MatLotVO> getMatLotList(String matId) {
       return mapper.getMatLotList(matId);
    }

    @Transactional
    @Override
    public ResponseEntity<?> callRegMatDefect(InboundLogVO defectData, MultipartFile file) {
        //프로세스 분리 1) 정보등록  2) 불량정보등록 시퀀스아이디get 3) 이미지첨부여부에 따른 이미지 등록
        try {
            //1)불량정보등록
            mapper.callRegMatDefect(defectData);

            //2)불량정보 등록한 최신시퀀스 받아오기 currval
            Long logId = mapper.selectRecentSeq();

            //3) 이미지첨부여부 check
            if (file != null && !file.isEmpty()) {
                String filePath = saveDefectImage(file);  // 실제 파일 저장
                defectData.setLogImg(filePath);           // 경로 세팅
                defectData.setInLogId(logId);
                mapper.updateDefectImagePath(defectData); // DB 경로 업데이트
            }

            return ResponseEntity.ok("불량 등록 완료");

        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                        .body("파일 처리 중 오류가 발생했습니다.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body("불량 등록 중 서버 오류가 발생했습니다.");
        }
    }


    //로컬에 이미지저장 메소드
    private String saveDefectImage(MultipartFile file) throws IOException {
        //1) application.yml에 지정된 저장경로 받아옴. 
        String uploadDir = fileUploadProperties.getDefectDir();

        //2) 폴더 없으면 자동 생성
        File directory = new File(uploadDir);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        //3) 파일명 생성
        String originalName = file.getOriginalFilename();
        String extension = originalName.substring(originalName.lastIndexOf("."));
        String newFileName = System.currentTimeMillis() + "_" + UUID.randomUUID() + extension;

        //4) 파일 저장
        File saveFile = new File(uploadDir + newFileName);
        file.transferTo(saveFile);

        //5)DB에 넣을 상대경로 리턴
        return uploadDir + newFileName;
    }
}
