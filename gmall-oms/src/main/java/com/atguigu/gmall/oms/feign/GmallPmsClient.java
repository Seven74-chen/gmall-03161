package com.atguigu.gmall.order.feign;

import com.atguigu.gmall.pms.api.GmallPmsApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @author Baozhong Chen
 * @version 1.0
 * @date 2020/9/4 20:09
 */

@FeignClient("pms-service")
public interface GmallPmsClient extends GmallPmsApi {
}
