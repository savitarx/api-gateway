package com.github.savitarx.api_gateway.exception;

import com.github.savitarx.api_gateway.dto.ErrorResponse;
import feign.FeignException;
import org.springframework.core.Ordered;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebExceptionHandler;
import reactor.core.publisher.Mono;
import org.springframework.http.HttpStatus;
import java.security.SignatureException;


@Component
public class CustomGlobalErrorWebExceptionHandler implements WebExceptionHandler, Ordered {

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        HttpStatus status;
        String message;

        if (ex instanceof JwtException) {
            status = HttpStatus.UNAUTHORIZED;
            message = "JWT error: " + ex.getMessage();
        } else if (ex instanceof SignatureException) {  // Replace with your actual service exception
            status = HttpStatus.BAD_REQUEST;
            message = "Service error: " + ex.getMessage();
        }else if(ex instanceof FeignException.Unauthorized){
            status = HttpStatus.UNAUTHORIZED;
            message = "Service error: " + ex.getMessage();
        }
        else {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
            message = "An unexpected error occurred: " + ex.getMessage();
        }

        ErrorResponse errorResponse = new ErrorResponse(message, status.value());

        return exchange.getResponse().writeWith(Mono.just(
                exchange.getResponse()
                        .bufferFactory()
                        .wrap(errorResponse.toString().getBytes())
        ));
    }

    @Override
    public int getOrder() {
        return -1; // Make sure it runs before default error handling
    }
}