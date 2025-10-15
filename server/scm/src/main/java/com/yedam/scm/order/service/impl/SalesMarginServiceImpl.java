// src/main/java/com/yedam/scm/order/service/impl/SalesMarginServiceImpl.java
package com.yedam.scm.order.service.impl;

import com.yedam.scm.order.mapper.SalesMarginMapper;
import com.yedam.scm.order.service.SalesMarginService;
import com.yedam.scm.vo.SalesMarginVO;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SalesMarginServiceImpl implements SalesMarginService {

    private final SalesMarginMapper mapper;

    @Override
    public List<SalesMarginVO> getList() {
        return mapper.selectSalesMarginList();
    }

    @Override
    @Transactional
    public void save(SalesMarginVO vo) {
        normalize(vo);
        mapper.updateSalesMargin(vo);
    }

    @Override
    @Transactional
    public void saveAll(List<SalesMarginVO> list) {
        if (list == null) return;
        int i = 1;
        for (SalesMarginVO vo : list) {
            // sortNo가 비어있다면 화면 순서대로 자동 부여
            if (vo.getSortNo() == null) vo.setSortNo(i++);
            normalize(vo);
            mapper.updateSalesMargin(vo);
        }
    }

    @Override
    @Transactional
    public void delete(String saleProdId) {
        mapper.deleteSalesMargin(saleProdId);
    }

    /** 서버에서 가격/YN을 보정해서 신뢰성 유지 */
    private void normalize(SalesMarginVO vo) {
        if (vo.getPosShowYn() == null) vo.setPosShowYn("N");
        // 판매가는 서버에서 재계산 (prod + prod*margin/100)
        if (vo.getProdUnitPrice() != null && vo.getSaleMargin() != null) {
            int price = (int) Math.round(
                vo.getProdUnitPrice() + (vo.getProdUnitPrice() * (vo.getSaleMargin() / 100.0))
            );
            vo.setSaleProdPrice(price);
        }
    }
}
