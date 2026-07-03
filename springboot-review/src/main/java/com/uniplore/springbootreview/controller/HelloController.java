package com.uniplore.springbootreview.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "hello", description = "hello controller")
@RestController
public class HelloController {

    @GetMapping("/hello")
    @Operation(summary = "hello", description = "这是一个测试接口")
    public String hello() {
        return "<h1>hello world</h1>";
    }



}
