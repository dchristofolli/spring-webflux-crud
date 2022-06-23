package com.dchristofolli.webfluxessentials.controller;

import com.dchristofolli.webfluxessentials.domain.Game;
import com.dchristofolli.webfluxessentials.repository.GameRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RequiredArgsConstructor
@RestController
@RequestMapping("games")
@Slf4j
public class GameController {
    private final GameRepository gameRepository;

    @GetMapping
    public Flux<Game> listAll(){
        return gameRepository.findAll();
    }
}
