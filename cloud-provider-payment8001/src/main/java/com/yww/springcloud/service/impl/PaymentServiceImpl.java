package com.yww.springcloud.service.impl;

import com.yww.springcloud.dao.PaymentDAO;
import com.yww.springcloud.entity.Payment;
import com.yww.springcloud.service.PaymentService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * <p>
 *     服务实现类
 * </p>
 *
 * @author yww
 * @version 1.0
 * @date 2021/12/10 23:57
 **/
@Service
public class PaymentServiceImpl implements PaymentService {
    @Resource
    private PaymentDAO paymentDAO;

    @Override
    public int save(Payment payment) {
        return paymentDAO.save(payment);
    }

    @Override
    public Payment getById(Long id) {
        return paymentDAO.getById(id);
    }

}
