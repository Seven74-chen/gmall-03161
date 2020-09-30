package com.atguigu.gmall.cart.interceptor;

import com.atguigu.gmall.cart.bean.UserInfo;
import com.atguigu.gmall.cart.config.JwtProperties;
import com.atguigu.gmall.common.utils.CookieUtils;
import com.atguigu.gmall.common.utils.JwtUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.UUID;

/**
 * @author Baozhong Chen
 * @version 1.0
 * @date 2020/9/7 22:31
 */

/**
 * 配置拦截器
 */
@Component
@EnableConfigurationProperties({JwtProperties.class})
public class LoginInterceptor implements HandlerInterceptor {
    @Autowired
    private JwtProperties jwtProperties;
    //声明线程局部变量。
    private static final ThreadLocal<UserInfo> THREAD_LOCAL = new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //获取请求头
        String userKey = CookieUtils.getCookieValue(request, jwtProperties.getUserKey());
        if (StringUtils.isBlank(userKey)){
            //设置userKey
            userKey = UUID.randomUUID().toString();
            //生成cookie
            CookieUtils.setCookie(request,response,jwtProperties.getUserKey(),userKey,jwtProperties.getExpireTime());
        }
        //userInfo为有效载荷
        UserInfo userInfo = new UserInfo();
        //设置userKey
        userInfo.setUserKey(userKey);

        //获取用户的登录请求
        String token = CookieUtils.getCookieValue(request, jwtProperties.getCookieName());
        if (StringUtils.isNotBlank(token)){
            try {
                //解析jwt
                Map<String, Object> map = JwtUtils.getInfoFromToken(token, jwtProperties.getPublicKey());
                Long userId = Long.valueOf(map.get("userId").toString());
                userInfo.setUserId(userId);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        THREAD_LOCAL.set(userInfo);
        return true;
    }

    public static UserInfo getUserInfo(){
        return THREAD_LOCAL.get();
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {

    }
}
