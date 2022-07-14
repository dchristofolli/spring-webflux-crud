package com.dchristofolli.webfluxessentials.config;

import com.dchristofolli.webfluxessentials.service.UserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UserDetailsRepositoryReactiveAuthenticationManager;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@EnableWebFluxSecurity
public class SecurityConfig {
    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        String baseUrl = "/games/**";
        String admin = "ADMIN";
        return http
            .csrf().disable()
            .authorizeExchange()
            .pathMatchers(HttpMethod.POST, baseUrl).hasRole(admin)
            .pathMatchers(HttpMethod.PUT, baseUrl).hasRole(admin)
            .pathMatchers(HttpMethod.DELETE, baseUrl).hasRole(admin)
            .pathMatchers(HttpMethod.GET, baseUrl).hasRole("USER")
            .anyExchange().authenticated()
            .and().formLogin()
            .and().httpBasic().and().build();
    }

    @Bean
    public ReactiveAuthenticationManager authenticationManager(UserDetailsService userDetailsService){
        return new UserDetailsRepositoryReactiveAuthenticationManager(userDetailsService);
    }
}
