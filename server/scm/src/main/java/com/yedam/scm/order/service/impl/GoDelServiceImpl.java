package com.yedam.scm.order.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yedam.scm.order.mapper.GoDelMapper;   
import com.yedam.scm.order.service.GoDelService;
import com.yedam.scm.vo.SalesOrderVO;
import com.yedam.scm.vo.SalesOrderDetailVO;

@Service
public class GoDelServiceImpl implements GoDelService {

    @Autowired
    private GoDelMapper goDelMapper;

@Override
@Transactional
public boolean createDeliveryInstruction(List<String> orderIds) {
    try {
        for (String orderId : orderIds) {

            // 출고 지시 생성 (헤더)
            goDelMapper.createDeliveryInstruction(new SalesOrderVO(orderId));

            // 주문 상세 여러 개 조회
            List<SalesOrderDetailVO> details = goDelMapper.getProdandQTY(orderId);

            for (SalesOrderDetailVO info : details) {
                String prodId = info.getProdId();
                Long orderQty = info.getOrderQty();

                goDelMapper.callProcStockReduction(prodId, orderQty);

                System.out.println("제품코드: " + prodId + ", 수량: " + orderQty);
            }
        }
        return true;
    } catch (Exception e) {
        System.out.println("출고지시 생성 중 오류 발생: " + e.getMessage());
        e.printStackTrace();
        return false;
    }
}



    @Override
    public boolean registerShipment(String shipId) {

        if(goDelMapper.updateOrderStatus(shipId, "출고완료") > 0) {
            return true;
        }

        return false;

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

    @Override
    public List<SalesOrderVO> selectShippedOrders() {
        return goDelMapper.selectShippedOrders();
    }

    @Override
    public boolean completeDelivery(String shipId) {

            if(goDelMapper.updateOrderStatus(shipId, "배송완료") > 0) {
            return true;
        }
        
        return false;
    }

}