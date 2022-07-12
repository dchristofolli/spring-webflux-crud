package com.dchristofolli.webfluxessentials.service;

import com.dchristofolli.webfluxessentials.domain.Game;
import com.dchristofolli.webfluxessentials.repository.GameRepository;
import io.netty.util.internal.StringUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

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
        return gameRepository.save(game);
    }

    public Mono<Void> update(Game game) {
        return findById(game.getId())
            .flatMap(gameRepository::save)
            .then();
    }

    public Mono<Void> delete(int id) {
        return findById(id)
            .flatMap(gameRepository::delete);
    }

    @Transactional
    public Flux<Game> saveAll(List<Game> games) {
        return gameRepository.saveAll(games)
            .doOnNext(this::throwResponseStatusExceptionWhenEmptyName);
    }

    private void throwResponseStatusExceptionWhenEmptyName(Game game) {
        if (StringUtil.isNullOrEmpty(game.getName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Game name is required");
        }
    }

    private <T> Mono<T> monoResponseStatusNotFoundException() {
        return Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Game not found"));
    }
}
