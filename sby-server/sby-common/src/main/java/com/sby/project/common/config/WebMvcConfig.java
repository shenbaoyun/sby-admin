package com.sby.project.common.config;

import com.alibaba.fastjson2.support.config.FastJsonConfig;
import com.alibaba.fastjson2.support.spring6.http.converter.FastJsonHttpMessageConverter;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        // 1. 创建 Fastjson 转换器
        FastJsonHttpMessageConverter converter = new FastJsonHttpMessageConverter();

        // 2. 自定义配置（如日期格式、空值处理）
        FastJsonConfig config = new FastJsonConfig();
        config.setDateFormat("yyyy-MM-dd HH:mm:ss");
        // 这里的 Feature 是 Fastjson 2 的核心配置点
        config.setWriterFeatures(
                com.alibaba.fastjson2.JSONWriter.Feature.WriteMapNullValue, // 序列化 null 字段
                com.alibaba.fastjson2.JSONWriter.Feature.PrettyFormat,      // 格式化输出
                com.alibaba.fastjson2.JSONWriter.Feature.WriteLongAsString // 将所有 Long 转为 String 输出
        );

        converter.setFastJsonConfig(config);

        // 3. 将 Fastjson 转换器添加到最前面，使其优先级高于 Jackson
        converters.addFirst(converter);
    }
}