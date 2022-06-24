package com.dchristofolli.webfluxessentials.controller;

import com.dchristofolli.webfluxessentials.domain.Game;
import com.dchristofolli.webfluxessentials.service.GameService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@RestController
@RequestMapping("games")
@Slf4j
public class GameController {
    private final GameService gameService;

    @GetMapping
    public Flux<Game> listAll() {
        return gameService.findAll();
    }

    @GetMapping(path = "{id}")
    public Mono<Game> findById(@PathVariable int id) {
        return gameService.findById(id);
    }
}
