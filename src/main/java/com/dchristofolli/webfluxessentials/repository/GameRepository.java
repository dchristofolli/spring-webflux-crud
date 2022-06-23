package com.dchristofolli.webfluxessentials.repository;

import com.dchristofolli.webfluxessentials.domain.Game;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface GameRepository extends ReactiveCrudRepository<Game, Integer> {
}
