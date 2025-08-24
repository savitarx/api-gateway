package com.github.savitarx.api_gateway.config;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.savitarx.api_gateway.dto.ErrorResponse;
import com.github.savitarx.api_gateway.feign.AuthServiceClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import java.io.IOException;
import java.util.Collections;



@Component
public class JwtRequestFilter implements WebFilter {

    private final AuthServiceClient authServiceClient;
    private final ObjectMapper objectMapper;
    private final String secretKey = "your-secret-key"; // Use a secure key in production

    @Autowired
    public JwtRequestFilter(AuthServiceClient authServiceClient, ObjectMapper objectMapper) {
        this.authServiceClient = authServiceClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {


        String requestTokenHeader = exchange.getRequest().getHeaders().getFirst("Authorization");
        if (requestTokenHeader != null && requestTokenHeader.startsWith("Bearer ")) {
            String jwtToken = requestTokenHeader.substring(7);

            return Mono.fromCallable(() -> authServiceClient.validateToken("Bearer " + jwtToken))
                    .flatMap(validationResponse -> {

                        SimpleGrantedAuthority authority = mapRoleToAuthority(validationResponse.role());
                        return chain.filter(exchange)
                                .contextWrite(ReactiveSecurityContextHolder.withAuthentication(
                                        new UsernamePasswordAuthenticationToken(
                                                validationResponse.username(),
                                                null,
                                                Collections.singleton(authority)
                                        )
                                ));
                    })
                    .onErrorResume(e -> {

                        e.printStackTrace();
                        return createErrorResponse(exchange, "Unauthorized: Invalid or expired token", HttpStatus.UNAUTHORIZED);
                    });
        }

        return chain.filter(exchange);
    }

    private SimpleGrantedAuthority mapRoleToAuthority(String role) {
        switch (role) {
            case "USER":
                return new SimpleGrantedAuthority("ROLE_USER");
            default:
                return new SimpleGrantedAuthority("ROLE_UNKNOWN");
        }
    }
    private Mono<Void> createErrorResponse(ServerWebExchange exchange, String message, HttpStatus status) {
        ErrorResponse errorResponse = new ErrorResponse(message, status.value());

        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        try {
            byte[] bytes = objectMapper.writeValueAsBytes(errorResponse);
            return exchange.getResponse().writeWith(Mono.just(exchange.getResponse().bufferFactory().wrap(bytes)));
        } catch (IOException e) {
            return Mono.error(e);
        }
    }
}
