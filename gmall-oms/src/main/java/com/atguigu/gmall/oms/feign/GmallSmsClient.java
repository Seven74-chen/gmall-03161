package com.atguigu.gmall.order.feign;

import com.atguigu.gmall.sms.api.GmallSmsApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @author Baozhong Chen
 * @version 1.0
 * @date 2020/9/4 20:10
 */
@FeignClient("sms-service")
public interface GmallSmsClient extends GmallSmsApi {
}
