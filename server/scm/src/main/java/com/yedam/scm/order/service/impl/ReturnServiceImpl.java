package com.yedam.scm.order.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yedam.scm.order.mapper.ReturnMapper;
import com.yedam.scm.order.service.ReturnService;
import com.yedam.scm.vo.ReturnVO;
import com.yedam.scm.vo.ReturnDetailVO;

@Service
public class ReturnServiceImpl implements ReturnService {

    @Autowired
    private ReturnMapper returnMapper;

    /**
     * =============================================================
     * 1. 반품 등록 (마스터 + 상세)
     * - RETURN 마스터 테이블과 RETURN_DETAIL 테이블에 동시에 등록
     * - 트랜잭션 보장 (@Transactional)
     * - RETURN_PRICE는 상세 항목의 합계로 자동 계산
     * =============================================================
     */
    @Override
    @Transactional
    public int createReturnSlip(ReturnVO returnVO) {

        // 1. 반품 합계 금액 계산 (RETURN_PRICE)
        int totalReturnPrice = 0;
        for (ReturnDetailVO detail : returnVO.getDetails()) {
            totalReturnPrice += detail.getProdUnitPrice() * detail.getReturnQty();
        }
        returnVO.setReturnPrice(totalReturnPrice);

        // 2. RETURN 마스터 등록
        int masterResult = returnMapper.insertReturnOrder(returnVO);
        if (masterResult <= 0) {
            throw new RuntimeException("반품 마스터 등록 실패 - RETURN_ID: " + returnVO.getReturnId());
        }

        // 3. RETURN_DETAIL 반복 등록
        for (ReturnDetailVO detail : returnVO.getDetails()) {
            detail.setReturnId(returnVO.getReturnId()); // FK 설정

            int detailResult = returnMapper.insertReturnDetail(detail);
            if (detailResult <= 0) {
                throw new RuntimeException("반품 상세 등록 실패 - PROD_ID: " + detail.getProdId());
            }
        }

        // 마스터 등록 성공 결과 반환
        return masterResult;
    }

    /**
     * =============================================================
     * 2. 반품 목록 조회 (마스터 기준)
     * - 반품 검색 화면에서 사용
     * =============================================================
     */
    @Override
    public List<ReturnVO> getReturnList(String startDate, String endDate,
                                        String returnStatus, String prodName, String returnNo) {
        return returnMapper.getReturnList(startDate, endDate, returnStatus, prodName, returnNo);
    }

    /**
     * =============================================================
     * 3. 반품 단건 조회 (마스터 + 상세)
     * - 반품번호 기준으로 마스터 정보와 상세 내역을 함께 조회
     * =============================================================
     */
    @Override
    public ReturnVO getReturnDetail(String returnId) {
        // 1. 반품 마스터 조회
        ReturnVO master = returnMapper.getReturnDetail(returnId);
        if (master == null) {
            return null;
        }

        // 2. 반품 상세 조회
        List<ReturnDetailVO> details = returnMapper.getReturnDetailList(returnId);

        // 3. 마스터 객체에 상세 리스트 추가
        master.setDetails(details);

        return master;
    }

    /**
     * =============================================================
     * 4. 반품 상세 목록 단독 조회
     * - 특정 반품번호에 대한 상세 내역만 단독으로 조회할 경우 사용
     * =============================================================
     */
    @Override
    public List<ReturnDetailVO> getReturnDetailList(String returnId) {
        return returnMapper.getReturnDetailList(returnId);
    }
}
