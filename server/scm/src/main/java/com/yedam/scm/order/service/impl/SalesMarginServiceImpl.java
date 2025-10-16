package com.yedam.scm.order.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yedam.scm.order.mapper.SalesMarginMapper;
import com.yedam.scm.order.service.SalesMarginService;
import com.yedam.scm.vo.SalesMarginVO;

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

    @Override
    public int save(SalesMarginVO vo) {
        return mapper.mergeSalesMargin(vo);
    }

    @Transactional
    @Override
    public int saveAll(List<SalesMarginVO> list) {
        int count = 0;
        for (SalesMarginVO vo : list) {
            count += mapper.mergeSalesMargin(vo);
        }
        return count;
    }


    @Override
    public int delete(String saleProdId) {
        return mapper.deleteSalesMargin(saleProdId);
    }
}
