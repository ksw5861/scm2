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
    public int insertBom(BomVO bom) {
        return bomMapper.insertBom(bom);
    }

    @Override
    public int updateBom(BomVO bom) {
        return bomMapper.updateBom(bom);
    }

    @Override
    public int deleteBom(String bomId) {
        return bomMapper.deleteBom(bomId);
    }

    @Override
    public List<BomVO> getBomList(BomVO bomVO) {
      return bomMapper.getBomList(bomVO);
      
    }

    @Override
    public BomVO getBomDetail(String bomId) {
      return bomMapper.getBomDetail(bomId);
    }
}
