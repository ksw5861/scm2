package com.yedam.scm.master.service.impl;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yedam.scm.dto.PageDTO;
import com.yedam.scm.dto.VendorSearchDTO;
import com.yedam.scm.master.mapper.VendorMapper;
import com.yedam.scm.master.service.VendorService;
import com.yedam.scm.vo.VendorVO;

@Service
public class VendorServiceImpl implements VendorService {

    @Autowired
    private VendorMapper vendorMapper;

    @Override
    public Map<String, Object> getVendorList(VendorSearchDTO condition, PageDTO paging) {
        Map<String, Object> res = new HashMap<>();
        paging.updatePageInfo(vendorMapper.getVendorListTotalCount(condition));
        res.put("data", vendorMapper.getVendorList(condition, paging));
        res.put("page", paging);
        return res;
    }

    @Override
    public VendorVO getVendorDetail(String vendorId) {
        return vendorMapper.getVendorDetail(vendorId);
    }

    @Override
    public int insertVendor(VendorVO vendorVO) {
        return vendorMapper.insertVendor(vendorVO);
    }

    @Override
    public int updateVendor(VendorVO vendorVO) {
        return vendorMapper.updateVendor(vendorVO);
    }

    @Override
    public int deleteVendor(String vendorId) {
        return vendorMapper.deleteVendor(vendorId);
    }
}
