package com.dchristofolli.webfluxessentials.service;

import com.dchristofolli.webfluxessentials.domain.Game;
import com.dchristofolli.webfluxessentials.repository.GameRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class GameService {
    private final GameRepository gameRepository;

    public Flux<Game> findAll() {
        return gameRepository.findAll();
    }

    public Mono<Game> findById(int id) {
        return gameRepository.findById(id)
            .switchIfEmpty(monoResponseStatusNotFoundException());
    }

    public Mono<Game> save(Game game) {
        return gameRepository.save(game)
            .log();
    }

    public <T> Mono<T> monoResponseStatusNotFoundException() {
        return Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Game not found"));
    }

    public Mono<Void> update(Game game) {
        return gameRepository.findById(game.getId())
            .map(foundGame -> game)
            .flatMap(gameRepository::save)
            .log()
            .then();
    }

    public Mono<Void> delete(int id) {
        return findById(id)
            .flatMap(gameRepository::delete);
    }
}
