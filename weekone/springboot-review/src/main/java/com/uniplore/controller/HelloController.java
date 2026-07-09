package com.uniplore.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 测试类，用于测试SpringBoot是否正常运行，以及Swagger是否正常运行
 *
 * @author 杨锋
 * @date 2026/7/6
 */
@Tag(name = "hello", description = "hello controller")
@RestController
public class HelloController {

    /**
     * 测试接口
     *
     * @return 测试字符串
     */
    @GetMapping("/hello")
    @Operation(summary = "hello", description = "这是一个测试接口")
    public String hello() {
        return "<h1>hello world</h1>";
    }


}
