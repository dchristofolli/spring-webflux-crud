package com.dchristofolli.webfluxessentials.repository;

import com.dchristofolli.webfluxessentials.domain.User;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface UserRepository extends ReactiveCrudRepository<User, Integer> {
    Mono<User> findByUsername(String name);
}
