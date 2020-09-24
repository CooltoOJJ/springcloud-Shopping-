package com.ojj.upload.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * 解决跨域问题
 */
@Configuration
public class LeyouCorsConfiguration {

    @Bean
    public CorsFilter corsFilter(){

        //初始化CORS配置对象
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.addAllowedOrigin("http://manage.leyou.com");//允许跨域的域名，如果带Cookie，不能写成“*”：代表所有域名都能访问
        corsConfiguration.setAllowCredentials(true);//允许携带Cookie
        corsConfiguration.addAllowedMethod("*");//允许所有方法
        corsConfiguration.addAllowedHeader("*");//允许携带任何请求头

        //初始化cors配置源对象
        UrlBasedCorsConfigurationSource corsConfigurationSource =new UrlBasedCorsConfigurationSource();
        //所有路径都验证跨域
        corsConfigurationSource.registerCorsConfiguration("/**",corsConfiguration);

        return new CorsFilter(corsConfigurationSource);
    }
}
