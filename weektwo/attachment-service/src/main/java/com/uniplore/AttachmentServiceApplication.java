package com.uniplore;

import cn.dev33.satoken.SaManager;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 *
 *
 * @author yf
 */
@SpringBootApplication
@MapperScan("com.uniplore.mapper")
public class AttachmentServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AttachmentServiceApplication.class, args);

        System.out.println("启动成功，Sa-Token 配置如下：" + SaManager.getConfig());
    }

}
