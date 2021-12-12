package com.yww.springcloud.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * <p>
 *     订单实体类
 * </p>
 *
 * @author yww
 * @version 1.0
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Payment implements Serializable {

    /**
     * 数据ID
     */
    private Long id;

    /**
     * 订单流水号
     */
    private String serial;

}
