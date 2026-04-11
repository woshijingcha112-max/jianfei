package com.dietrecord.backend.config;

import org.springframework.util.StringUtils;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;

@Configuration
public class WebMvcConfiguration implements WebMvcConfigurer {

    private final AppProperties appProperties;

    public WebMvcConfiguration(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String accessPrefix = normalizeAccessPrefix(appProperties.getStorage().getAccessPrefix());
        String location = Path.of(appProperties.getStorage().getUploadRoot()).toUri().toString();
        registry.addResourceHandler(accessPrefix + "/**")
                .addResourceLocations(location);
    }

    private String normalizeAccessPrefix(String accessPrefix) {
        if (!StringUtils.hasText(accessPrefix)) {
            return "/uploads";
        }
        return accessPrefix.startsWith("/") ? accessPrefix : "/" + accessPrefix;
    }
}
