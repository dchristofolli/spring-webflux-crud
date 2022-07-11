package com.dchristofolli.webfluxessentials.integration;

import com.dchristofolli.webfluxessentials.domain.Game;
import com.dchristofolli.webfluxessentials.exception.CustomAttributes;
import com.dchristofolli.webfluxessentials.repository.GameRepository;
import com.dchristofolli.webfluxessentials.service.GameService;
import com.dchristofolli.webfluxessentials.util.GameCreator;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.blockhound.BlockHound;
import reactor.blockhound.BlockingOperationError;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

@ExtendWith(SpringExtension.class)
@WebFluxTest
@Import({GameService.class, CustomAttributes.class})
class GameControllerIT {
    @MockBean
    private GameRepository gameRepository;

    @MockBean
    private WebProperties.Resources resources;
    @Autowired
    private WebTestClient testClient;

    private final Game game = GameCreator.createValidGame();

    @BeforeAll
    static void blockHoundSetup() {
        BlockHound.install();
    }

    @BeforeEach
    void setup() {
        BDDMockito.when(gameRepository.findAll())
            .thenReturn(Flux.just(game));
        BDDMockito.when(gameRepository.findById(ArgumentMatchers.anyInt()))
            .thenReturn(Mono.just(game));
    }

    @Test
    void blockHoundWorks() {
        try {
            FutureTask<?> task = new FutureTask<>(() -> {
                Thread.sleep(0); //NOSONAR
                return "";
            });
            Schedulers.parallel().schedule(task);

            task.get(10, TimeUnit.SECONDS);
            Assertions.fail("should fail");
        } catch (Exception e) {
            Assertions.assertTrue(e.getCause() instanceof BlockingOperationError);
        }
    }

    @Test
    @DisplayName("findAll returns a flux of game")
    void findAll_ReturnFluxOfGame_WhenSuccessful() {
        testClient
            .get()
            .uri("/games")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.[0].id").isEqualTo(game.getId())
            .jsonPath("$.[0].name").isEqualTo(game.getName());
    }

    @Test
    @DisplayName("findById returns a mono with game when it exists")
    void findById_ReturnMonoGame_WhenSuccessful() {
        testClient
            .get()
            .uri("/games/{id}", 1)
            .exchange()
            .expectStatus().isOk()
            .expectBody(Game.class)
            .isEqualTo(game);
    }

    @Test
    @DisplayName("findById throws exception when game does not exist")
    void findById_ThrowsException_WhenEmptyMonoIsReturned() {
        BDDMockito.when(gameRepository.findById(ArgumentMatchers.anyInt()))
            .thenReturn(Mono.empty());
        testClient
            .get()
            .uri("/games/{id}", 1)
            .exchange()
            .expectStatus().isNotFound();
    }
}
