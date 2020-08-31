package com.atguigu.gmall.sms.api;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.sms.vo.SkuSaleVo;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @author Baozhong Chen
 * @version 1.0
 * @date 2020/8/25 13:48
 */

public interface GmallSmsApi {
    @PostMapping("/sms/skubounds/skusale/save")
    public ResponseVo<Object> saveSkuSaleInfo(@RequestBody SkuSaleVo skuSaleVo);

}
