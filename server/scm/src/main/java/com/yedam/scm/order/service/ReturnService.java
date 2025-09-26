package com.yedam.scm.order.service;

import java.util.List;
import com.yedam.scm.vo.ReturnVO;
import com.yedam.scm.vo.ReturnDetailVO;

public interface ReturnService {

    // =============================
    // 1. 반품 등록
    // =============================
    /**
     * 반품 마스터 + 상세 등록 (월정산 방식)
     * @param returnVO 마스터 및 상세 데이터 포함 VO
     * @return 등록 성공 시 1 이상
     */
    int createReturnSlip(ReturnVO returnVO);

    // =============================
    // 2. 반품 목록 조회
    // =============================
    /**
     * 조건에 따른 반품 목록 조회
     * @param startDate 시작일
     * @param endDate 종료일
     * @param returnStatus 반품 상태
     * @param prodName 제품명
     * @param returnNo 반품번호
     * @return 반품 마스터 목록
     */
    List<ReturnVO> getReturnList(String startDate,
                                  String endDate,
                                  String returnStatus,
                                  String prodName,
                                  String returnNo);

    // =============================
    // 3. 반품 단건 조회
    // =============================
    /**
     * 반품 단건 조회 (마스터 + 상세 포함)
     * @param returnId 반품 ID
     * @return 마스터 및 상세 정보 포함된 VO
     */
    ReturnVO getReturnDetail(String returnId);

    // =============================
    // 4. 반품 상세 목록 조회
    // =============================
    /**
     * 특정 반품건의 상세 목록 조회
     * @param returnId 반품 ID
     * @return 반품 상세 리스트
     */
    List<ReturnDetailVO> getReturnDetailList(String returnId);
}
