package com.yedam.scm.master.service.impl;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import com.yedam.scm.master.mapper.BomMapper;
import com.yedam.scm.master.service.BomService;
import com.yedam.scm.vo.BomVO;

@Service
public class BomServiceImpl implements BomService {

    @Autowired
    private BomMapper bomMapper;

    @Override
    public List<BomVO> getBomDetailByProdId(String prodId) {
        return bomMapper.getBomByProdId(prodId);
    }

    @Override
    public BomVO getBomDetail(String bomId) {
        return bomMapper.getBomDetail(bomId);
    }

    @Override
    public int insertBom(BomVO bomVO) {
        return bomMapper.insertBom(bomVO);
    }

    @Override
    public int updateBom(BomVO bomVO) {
        return bomMapper.updateBom(bomVO);
    }

    @Override
    public int deleteBom(String bomId) {
        return bomMapper.deleteBom(bomId);
    }
}
