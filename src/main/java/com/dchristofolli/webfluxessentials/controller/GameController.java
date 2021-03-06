package com.dchristofolli.webfluxessentials.controller;

import com.dchristofolli.webfluxessentials.domain.Game;
import com.dchristofolli.webfluxessentials.service.GameService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.Valid;
import java.util.List;

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

    @GetMapping(path = "{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<Game> findById(@PathVariable int id) {
        return gameService.findById(id);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<Game> save(@Valid @RequestBody Game game) {
        return gameService.save(game);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(path = "batch",
        consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Flux<Game> saveBatch(@RequestBody List<Game> games) {
        return gameService.saveAll(games);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PutMapping(path = "{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<Void> update(@PathVariable int id,
                             @Valid @RequestBody Game game) {
        return gameService.update(game.withId(id));
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping(path = "{id}")
    public Mono<Void> delete(@PathVariable int id) {
        return gameService.delete(id);
    }
}
