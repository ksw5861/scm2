package com.yedam.scm.instockMat.service.impl;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
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
import com.yedam.scm.dto.MatStockSearchDTO;
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
    public Map<String, Object> getVenShipList(PageDTO pageDTO) {

       List<InboundVO> list = mapper.getVenShipList(pageDTO.getStartRow(), pageDTO.getEndRow());
       Long total = mapper.getVenShipListCount();

       pageDTO.updatePageInfo(total);

       Map<String, Object> result = new HashMap<>();
       result.put("list", list);
       result.put("page", pageDTO);
       return result;

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
    public Map<String, Object> getApproveUnload(PageDTO pageDTO) {
       List<InboundVO> list = mapper.getApproveUnload(pageDTO.getStartRow(), pageDTO.getEndRow());
       Long total = mapper.getApproveUnloadListCount();

       pageDTO.updatePageInfo(total);

       Map<String, Object> result = new HashMap<>();
       result.put("list", list);
       result.put("page", pageDTO);

       return result;
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
public Map<String, Object> getMatStockList(PageDTO pageDTO, MatStockSearchDTO searchDTO) {

    List<MatLotVO> list = mapper.getMatStockList(
        pageDTO.getStartRow(),
        pageDTO.getEndRow(),
        searchDTO.getMaterialId(),
        searchDTO.getMaterialName(),
        searchDTO.getLotNo(),
        searchDTO.getLotStatus()
    );

    Long total = mapper.getMatStockCount(
        searchDTO.getMaterialId(),
        searchDTO.getMaterialName(),
        searchDTO.getLotNo(),
        searchDTO.getLotStatus()
    );

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
            Map<String, Object> paramMap = new HashMap<>();
             
            paramMap.put("inboundDetId", defectData.getInboundDetId());
            paramMap.put("logRejQty", defectData.getLogRejQty());
            paramMap.put("logMemo", defectData.getLogMemo());
            paramMap.put("logName", defectData.getLogName());
            paramMap.put("logId", null); // OUT 파라미터 받을 자리 
            //=> 값 리턴받을때는 자동매핑처리가 안되니 별도 mapping작업해서 넘겨줘야함. in은 vo로 자동매칭해서 넘어가나 out이 안됨.

            //프로시저 호출
            mapper.callRegMatDefect(paramMap);

            Long logId = paramMap.get("logId") != null ? ((BigDecimal) paramMap.get("logId")).longValue() : null;

            // 2) 이미지첨부여부 check 후 이미지등록 호출
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
