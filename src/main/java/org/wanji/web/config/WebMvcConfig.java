package org.wanji.web.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 *
 * @author yezhihao
 * https://gitee.com/yezhihao/jt808-server
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    /* 自定义资源处理器 */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
    }

    /* 配置拦截器 */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
    }

    /* 配置CORS，允许跨域请求， */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**").combine(corsConfig());
    }

    /* 配置一个CORS过滤器，应用上面的CORS */
    @Bean
    public FilterRegistrationBean<CorsFilter> corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig());

        FilterRegistrationBean<CorsFilter> bean = new FilterRegistrationBean<>(new CorsFilter(source));
        bean.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return bean;
    }

    /* 定义CORS的各种规则 */
    @Bean
    public CorsConfiguration corsConfig() {
        CorsConfiguration config = new CorsConfiguration();
        config.addAllowedOriginPattern(CorsConfiguration.ALL);
        config.addAllowedMethod(CorsConfiguration.ALL);
        config.addAllowedHeader(CorsConfiguration.ALL);
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);
        return config;
    }
}