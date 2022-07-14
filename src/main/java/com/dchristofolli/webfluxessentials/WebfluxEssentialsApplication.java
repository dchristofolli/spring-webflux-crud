package com.dchristofolli.webfluxessentials;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import reactor.blockhound.BlockHound;

@SpringBootApplication
public class WebfluxEssentialsApplication {
    static {
        BlockHound.install();
    }
    public static void main(String[] args) {
        System.out.println(PasswordEncoderFactories.createDelegatingPasswordEncoder().encode("game"));//NOSONAR
        SpringApplication.run(WebfluxEssentialsApplication.class, args);
    }
}
