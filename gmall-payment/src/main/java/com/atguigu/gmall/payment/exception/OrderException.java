package com.atguigu.gmall.oms.exception;

/**
 * @author Baozhong Chen
 * @version 1.0
 * @date 2020/9/11 20:35
 */
public class OrderException extends RuntimeException {

    public OrderException() {
        super();
    }

    public OrderException(String message) {
        super(message);
    }
}
