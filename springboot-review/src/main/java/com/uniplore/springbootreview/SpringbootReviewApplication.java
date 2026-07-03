package com.uniplore.springbootreview;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.uniplore.springbootreview.mapper")
public class SpringbootReviewApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringbootReviewApplication.class, args);
    }

}
