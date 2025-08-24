package com.github.savitarx.api_gateway.feign;


import com.github.savitarx.api_gateway.dto.ValidationResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "authentication-service", url = "http://localhost:8081")
public interface AuthServiceClient {

    @GetMapping(value = "/api/auth/validate",produces = MediaType.APPLICATION_JSON_VALUE)
    ValidationResponse validateToken(@RequestHeader("Authorization") String authorizationHeader);
}