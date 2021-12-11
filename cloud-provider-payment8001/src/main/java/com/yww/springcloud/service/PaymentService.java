package com.yww.springcloud.service;

import com.yww.springcloud.entity.Payment;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 *     服务类
 * </p>
 *
 * @author yww
 * @version 1.0
 * @date 2021/12/10 23:56
 **/
public interface PaymentService {


    /**
     * 新增数据
     * @param payment 实体类
     * @return 数据ID
     */
    int save(Payment payment);

    /**
     * 根据ID获取数据
     * @param id    数据ID
     * @return      数据实体
     */
    Payment getById(@Param("id") Long id);

}
