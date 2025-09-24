package com.yedam.scm.order.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yedam.scm.order.mapper.ReturnMapper;
import com.yedam.scm.order.service.ReturnService;
import com.yedam.scm.vo.ReturnVO;

@Service
public class ReturnServiceImpl implements ReturnService {

    @Autowired
    private ReturnMapper returnMapper;

    @Override
    public int insertReturn(ReturnVO returnVO) {
        return returnMapper.insertReturn(returnVO);
    }

    @Override
    public int insertReturnList(List<ReturnVO> returnList) {
        int count = 0;
        for (ReturnVO vo : returnList) {
            count += returnMapper.insertReturn(vo);
        }
        return count;
    }

    @Override
    public List<ReturnVO> getReturnList(String startDate, String endDate, String returnStatus, String prodName, String returnNo) {
        return returnMapper.getReturnList(startDate, endDate, returnStatus, prodName, returnNo);
    }





    @Override
    public ReturnVO getReturnDetail(String returnId) {
        return returnMapper.getReturnDetail(returnId);
    }
}
