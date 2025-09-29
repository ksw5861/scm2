package com.yedam.scm.master.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yedam.scm.master.mapper.MaterialVendor;
import com.yedam.scm.master.service.MaterialVendorService;
import com.yedam.scm.vo.MaterialVendorVO;

@Service
public class MaterialVendorServiceImpl implements MaterialVendorService {

    @Autowired
    private MaterialVendor materialVendorMapper;

    @Override
    public List<MaterialVendorVO> getMaterialVendorList(String matId) {
        return materialVendorMapper.getMaterialVendorList(matId);
    }

    @Override
    public MaterialVendorVO getMaterialVendorDetail(String matVendorId) {
        return materialVendorMapper.getMaterialVendorDetail(matVendorId);
    }

    @Override
    public int insertMaterialVendor(MaterialVendorVO materialVendorVO) {
        return materialVendorMapper.insertMaterialVendor(materialVendorVO);
    }

    @Override
    public int updateMaterialVendor(MaterialVendorVO materialVendorVO) {
        return materialVendorMapper.updateMaterialVendor(materialVendorVO);
    }

    @Override
    public int deleteMaterialVendor(String matVendorId) {
        return materialVendorMapper.deleteMaterialVendor(matVendorId);
    }
}
