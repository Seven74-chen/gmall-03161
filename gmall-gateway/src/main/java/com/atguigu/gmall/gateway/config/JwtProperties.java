package com.atguigu.gmall.auth.config;

import com.atguigu.gmall.common.exception.UserException;
import com.atguigu.gmall.common.utils.RsaUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.annotation.PostConstruct;
import java.io.File;
import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * @author Baozhong Chen
 * @version 1.0
 * @date 2020/9/6 22:52
 */

@Data
@Slf4j
@ConfigurationProperties(prefix = "auth.jwt")
public class JwtProperties {
    private String pubKeyPath;
    private String priKeyPath;
    private String secret;
    private String cookieName;
    private Integer expire;
    private String unick;

    private PublicKey publicKey;
    private PrivateKey privateKey;

    /**
     * 该方法在构造方法之前执行
     */
    @PostConstruct
    public void init(){
        //根据路径读取公钥和私钥
        try {
            File pubFile = new File(pubKeyPath);
            File priFile = new File(priKeyPath);
            //如果不存在，重新生成
            if (!priFile.exists()||!pubFile.exists()){
                RsaUtils.generateKey(pubKeyPath,priKeyPath,secret);
            }
            this.publicKey = RsaUtils.getPublicKey(pubKeyPath);
            this.privateKey = RsaUtils.getPrivateKey(priKeyPath);
        } catch (Exception e) {
            e.printStackTrace();
            throw new UserException("生成公钥和私钥出错" + e.getMessage());
        }


    }


}
