package com.yedam.scm.order.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yedam.scm.order.mapper.GoDelMapper;   
import com.yedam.scm.order.service.GoDelService;
import com.yedam.scm.vo.SalesOrderVO;
import com.yedam.scm.vo.SalesOrderDetailVO;

@Service
public class GoDelServiceImpl implements GoDelService {

    @Autowired
    private GoDelMapper goDelMapper;

    @Override
    public boolean createDeliveryInstruction(List<String> orderIds) {
        try {
            for (String orderId : orderIds) {
                SalesOrderVO order = new SalesOrderVO();
                order.setOrderId(orderId);
                goDelMapper.createDeliveryInstruction(order);
              
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean registerShipment(List<String> orderIds) {
        try {
            for (String orderId : orderIds) {
                goDelMapper.updateOrderStatus(orderId, "출고완료");
                goDelMapper.updateSendDate(orderId);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public List<SalesOrderVO> selectPendingDeliveryOrders() {
        return goDelMapper.selectPendingDeliveryOrders();
    }

    @Override
    public List<SalesOrderVO> getDeliveryInProgressOrders() {
        return goDelMapper.selectDeliveryInProgressOrders();
    }

    @Override
    public List<SalesOrderDetailVO> getDeliveryDetailList(String orderId) {
        return goDelMapper.selectDeliveryDetailList(orderId);
    }

    @Override
    public List<Map<String, Object>> selectReadyToDeliverSimple() {
        return goDelMapper.selectReadyToDeliverSimple(); 
    }

}
