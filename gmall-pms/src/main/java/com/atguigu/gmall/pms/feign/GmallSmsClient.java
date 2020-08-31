package com.atguigu.gmall.pms.feign;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.sms.api.GmallSmsApi;
import com.atguigu.gmall.sms.vo.SkuSaleVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @author Baozhong Chen
 * @version 1.0
 * @date 2020/8/24 20:42
 */

@FeignClient("sms-service")
public interface GmallSmsClient extends GmallSmsApi {
}
