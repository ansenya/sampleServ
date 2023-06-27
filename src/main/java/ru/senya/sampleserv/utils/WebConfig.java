package ru.senya.sampleserv.utils;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry
                .addResourceHandler("/static/**")  // Путь к статическим ресурсам
                .addResourceLocations("classpath:/static/")  // Местоположение папки статических ресурсов
                .setCacheControl(CacheControl.noStore())    // Установка заголовков кэширования для запрета кэширования
                .setCacheControl(CacheControl.noCache())
                .setCachePeriod(0);
    }


}