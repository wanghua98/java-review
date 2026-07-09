package com.uniplore;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


/**
 * 启动类
 *
 * @author 杨锋
 */
@SpringBootApplication
@MapperScan("com.uniplore.mapper")
public class SpringbootReviewApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringbootReviewApplication.class, args);
    }

}
