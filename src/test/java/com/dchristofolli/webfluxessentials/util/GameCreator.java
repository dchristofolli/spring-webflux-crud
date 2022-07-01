package com.dchristofolli.webfluxessentials.util;

import com.dchristofolli.webfluxessentials.domain.Game;

public class GameCreator {
    public static Game createGameToBeSaved() {
        return Game.builder()
            .name("The Last of Us")
            .build();
    }

    public static Game createValidGame() {
        return Game.builder()
            .id(1)
            .name("The Last of Us")
            .build();
    }

    public static Game createValidUpdatedGame() {
        return Game.builder()
            .id(1)
            .name("The Last of Us - Part 2")
            .build();
    }
}
