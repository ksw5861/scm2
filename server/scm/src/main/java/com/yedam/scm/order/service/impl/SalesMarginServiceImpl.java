package com.yedam.scm.order.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yedam.scm.order.mapper.SalesMarginMapper;
import com.yedam.scm.order.service.SalesMarginService;
import com.yedam.scm.vo.SalesDetailVO;
import com.yedam.scm.vo.SalesMarginVO;
import com.yedam.scm.vo.SalesMasterVO;

@Service
public class SalesMarginServiceImpl implements SalesMarginService {

    @Autowired
    private SalesMarginMapper mapper;

    @Override
    public List<SalesMarginVO> getList() {
        return mapper.selectSalesMarginList();
    }

    @Override
    public SalesMarginVO getById(String saleProdId) {
        return mapper.selectSalesMarginById(saleProdId);
    }

    @Transactional
    @Override
    public int saveAll(List<SalesMarginVO> list) {
        // 1️⃣ 기존 데이터 전체 삭제
        mapper.deleteAll();

        // 2️⃣ 새 데이터 삽입
        int insertedCount = 0;
        for (SalesMarginVO vo : list) {
            insertedCount += mapper.insert(vo);
        }

        // 3️⃣ 결과 반환
        return insertedCount;
    }

    @Override
    public int delete(String saleProdId) {
        return mapper.deleteSalesMargin(saleProdId);
    }

    // ✅ POS 결제 등록
    @Override
    @Transactional
    public void registerSale(SalesMasterVO vo) {
        mapper.insertSaleMaster(vo);

        if (vo.getSalesDetails() != null && !vo.getSalesDetails().isEmpty()) {
            for (SalesDetailVO detail : vo.getSalesDetails()) {
                detail.setSaleId(vo.getSaleId());
                mapper.insertSaleDetail(detail);
            }
        }
    }

    // ✅ 매출이력조회
    @Override
    public List<SalesMasterVO> getSalesHistory(String vendorId) {
        return mapper.getSalesHistory(vendorId);
    }

}
