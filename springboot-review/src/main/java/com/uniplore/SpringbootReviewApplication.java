package com.uniplore;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.uniplore.mapper")
public class SpringbootReviewApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringbootReviewApplication.class, args);
    }

}
