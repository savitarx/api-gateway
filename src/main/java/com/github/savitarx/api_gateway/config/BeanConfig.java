package com.github.savitarx.api_gateway.config;


import feign.codec.Decoder;
import feign.jackson.JacksonDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanConfig {

    @Bean
    public Decoder decoder(){
        return new JacksonDecoder();
    }
}
