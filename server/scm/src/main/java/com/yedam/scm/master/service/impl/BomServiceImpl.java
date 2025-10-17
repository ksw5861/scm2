package com.yedam.scm.master.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;

import com.yedam.scm.master.mapper.BomMapper;
import com.yedam.scm.master.service.BomService;
import com.yedam.scm.vo.BomVO;
import com.yedam.scm.vo.BomDetailVO;

@Service
public class BomServiceImpl implements BomService {

    @Autowired
    private BomMapper bomMapper;

    @Transactional(readOnly = true)
    @Override
    public List<BomVO> getBomDetailByProdId(String prodId) {
        return bomMapper.getBomByProdId(prodId);
    }

    @Transactional(readOnly = true)
    @Override
    public BomVO getBomDetail(String bomId) {
        return bomMapper.getBomDetail(bomId);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public int insertBom(BomVO bomVO) {
        return bomMapper.insertBom(bomVO);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public int updateBom(BomVO bomVO) {
        return bomMapper.updateBom(bomVO);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public int deleteBom(String bomId) {
        bomMapper.deleteBomDetailsByBomId(bomId);
        return bomMapper.deleteBom(bomId);
    }

    // 부모-자식 트랜잭션
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void insertBomWithDetails(BomVO bom, List<BomDetailVO> details) {
        bomMapper.insertBom(bom);
        for (BomDetailVO d : details) {
            d.setBomId(bom.getBomId());
            bomMapper.insertBomDetail(d);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateBomWithDetails(BomVO bom, List<BomDetailVO> details) {
        bomMapper.updateBom(bom);
        for (BomDetailVO d : details) {
            d.setBomId(bom.getBomId());
            if (d.getBomDeId() == null) {
                bomMapper.insertBomDetail(d);
            } else {
                bomMapper.updateBomDetail(d);
            }
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public int deleteBomWithDetails(String bomId) {
        bomMapper.deleteBomDetailsByBomId(bomId);
        return bomMapper.deleteBom(bomId);
    }
}
