package com.yedam.scm.master.service.impl;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yedam.scm.common.service.MailService;
import com.yedam.scm.dto.PageDTO;
import com.yedam.scm.dto.VendorSearchDTO;
import com.yedam.scm.master.mapper.VendorMapper;
import com.yedam.scm.master.service.VendorService;
import com.yedam.scm.vo.VendorVO;

@Service
public class VendorServiceImpl implements VendorService {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private MailService mailService;

    @Autowired
    private VendorMapper vendorMapper;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public String generateTempPassword(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder();
        Random rnd = new SecureRandom();
    
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(rnd.nextInt(chars.length())));
        }
    
        return sb.toString();
      }

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
    @Transactional
    public boolean insertVendor(VendorVO vendor) {

        String tempPassword = generateTempPassword(10);
        vendor.setTempPassword(passwordEncoder.encode(tempPassword));
        
        vendorMapper.insertVendor(vendor);
        if (vendor.getRowCount() < 2 || vendor.getVendorId() == null || vendor.getVendorId().isEmpty()) {
            return false;
        }

        mailService.sendMailAsync(
            vendor.getOwnerEmail(),
            "임시 비밀번호 안내",
            "안녕하세요. "
            + vendor.getCompanyName()
            + " 담당자 "
            + vendor.getOwnerName()
            + "님 임시 비밀번호 안내드립니다.<br /> "
            + tempPassword,
            fromEmail
        );
    
        return true;
    }

    @Override
    public boolean updateVendor(VendorVO vendor) {
        
        vendorMapper.updateVendor(vendor);

        if (vendor.getRowCount() == 0) {
            return false;
        }
        return true;
    }

    @Override
    public boolean deleteVendor(String vendorId) {

        Map<String, Object> param = new HashMap<>();
        param.put("vendorId", vendorId);
    
        vendorMapper.deleteVendor(param);
    
        int rowCount = (Integer) param.get("rowCount");
    
        if (rowCount == 0) {
          return false;
        }

        return true;
    }
}
