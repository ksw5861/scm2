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
     * - 주문 테이블은 수정하지 않음
     * - 월말 정산 시 반품 금액을 미수금에서 차감
     * =============================================================
     */
    @Override
    @Transactional
    public int createReturnSlip(ReturnVO returnVO) {
        // 1. 반품 마스터 등록
        int masterResult = returnMapper.insertReturnOrder(returnVO);
        if (masterResult <= 0) {
            throw new RuntimeException("반품 마스터 등록 실패: " + returnVO.getReturnId());
        }

        // 2. 반품 상세 등록 (여러 건 처리)
        for (ReturnDetailVO detail : returnVO.getDetails()) {
            detail.setReturnId(returnVO.getReturnId()); // FK 설정
            int detailResult = returnMapper.insertReturnDetail(detail);

            if (detailResult <= 0) {
                throw new RuntimeException("반품 상세 등록 실패: " + detail.getRdetailId());
            }
        }

        return 1;
    }

    /**
     * =============================================================
     * 2. 반품 목록 조회 (마스터 기준)
     * - 반품 검색 화면에서 사용
     * =============================================================
     */
    @Override
    public List<ReturnVO> getReturnList(String startDate, String endDate, String returnStatus, String prodName, String returnNo) {
        return returnMapper.getReturnList(startDate, endDate, returnStatus, prodName, returnNo);
    }

    /**
     * =============================================================
     * 3. 반품 단건 조회 (마스터 + 상세)
     * - 반품 번호 기준으로 마스터와 상세를 함께 조회
     * =============================================================
     */
    @Override
    public ReturnVO getReturnDetail(String returnId) {
         // 1. 반품 마스터 조회
        ReturnVO master = returnMapper.getReturnDetail(returnId);
        if (master == null) {
            return null;
        }

        // 2. 반품 상세 조회 → ✅ 올바른 타입으로 변경
        List<ReturnDetailVO> details = returnMapper.getReturnDetailList(returnId);

        // 3. 마스터에 상세 데이터 세팅
        master.setDetails(details);

        return master;
    }
    /**
     * =============================================================
     * 4. 반품 상세 리스트 단독 조회
     * - 필요 시 상세 목록만 단독으로 조회
     * =============================================================
     */
    @Override
    public List<ReturnDetailVO> getReturnDetailList(String returnId) {
        return returnMapper.getReturnDetailList(returnId);
    }
}
