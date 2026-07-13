package com.uniplore.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * CORS 跨域配置
 * <p>
 * 允许前端开发服务器（http://localhost:5173）跨域访问后端接口。
 * 生产环境请将 allowedOrigins 替换为实际前端域名。
 * </p>
 *
 * @author yf
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Bean
    public CorsFilter corsFilter() {
        // CORS 配置
        CorsConfiguration config = new CorsConfiguration();
        // 允许前端地址（Vite 默认端口 5173）
        config.addAllowedOrigin("http://localhost:5173");
        // 允许携带 Cookie（Sa-Token 需要）
        config.setAllowCredentials(true);
        // 允许所有请求头
        config.addAllowedHeader("*");
        // 允许所有请求方法（GET、POST、PUT、DELETE 等）
        config.addAllowedMethod("*");

        // 对所有接口生效
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return new CorsFilter(source);
    }
}
