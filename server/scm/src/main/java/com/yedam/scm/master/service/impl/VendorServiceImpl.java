package com.yedam.scm.master.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yedam.scm.master.mapper.VendorMapper;
import com.yedam.scm.master.service.VendorService;
import com.yedam.scm.vo.VendorVO;

@Service
public class VendorServiceImpl implements VendorService {

    @Autowired
    private VendorMapper vendorMapper;

    @Override
    public List<VendorVO> getVendorList(String vendorId, String companyName, String isActive) {
        return vendorMapper.getVendorList(vendorId, companyName, isActive);
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
