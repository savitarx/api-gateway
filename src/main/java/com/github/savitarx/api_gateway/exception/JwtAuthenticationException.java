package com.github.savitarx.api_gateway.exception;

public class JwtAuthenticationException extends Exception {
    public JwtAuthenticationException(String message) {
        super(message);
    }
}