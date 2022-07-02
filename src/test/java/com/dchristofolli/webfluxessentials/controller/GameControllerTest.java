package com.dchristofolli.webfluxessentials.controller;

import com.dchristofolli.webfluxessentials.domain.Game;
import com.dchristofolli.webfluxessentials.service.GameService;
import com.dchristofolli.webfluxessentials.util.GameCreator;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.server.ResponseStatusException;
import reactor.blockhound.BlockHound;
import reactor.blockhound.BlockingOperationError;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

@ExtendWith(SpringExtension.class)
class GameControllerTest {
    @InjectMocks
    private GameController gameController;

    @Mock
    private GameService gameService;

    private final Game game = GameCreator.createValidGame();

    @BeforeAll
    static void blockHoundSetup() {
        BlockHound.install();
    }

    @BeforeEach
    void setup() {
        BDDMockito.when(gameService.findAll())
            .thenReturn(Flux.just(game));

        BDDMockito.when(gameService.findById(ArgumentMatchers.anyInt()))
            .thenReturn(Mono.just(game));

        BDDMockito.when(gameService.save(GameCreator.createGameToBeSaved()))
            .thenReturn(Mono.just(game));

        BDDMockito.when(gameService.delete(ArgumentMatchers.anyInt()))
            .thenReturn(Mono.empty());

        BDDMockito.when(gameService.save(GameCreator.createValidGame()))
            .thenReturn(Mono.empty());
    }

    @Test
    void blockHoundWorks() {
        try {
            FutureTask<?> task = new FutureTask<>(() -> {
                Thread.sleep(0);
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
        StepVerifier.create(gameController.listAll())
            .expectSubscription()
            .expectNext(game)
            .verifyComplete();
    }

    @Test
    @DisplayName("findById returns a mono with game when it exists")
    void findById_ReturnMonoGame_WhenSuccessful() {
        StepVerifier.create(gameController.findById(1))
            .expectSubscription()
            .expectNext(game)
            .verifyComplete();
    }

    @Test
    @DisplayName("save creates a game when successful")
    void save_CreatesGame_WhenSuccessful() {
        Game gameToBeSaved = GameCreator.createGameToBeSaved();
        StepVerifier.create(gameController.save(gameToBeSaved))
            .expectSubscription()
            .expectNext(game)
            .verifyComplete();
    }

    @Test
    @DisplayName("removes the game when successful")
    void delete_removesGame_WhenSuccessful() {
        StepVerifier.create(gameController.delete(1))
            .expectSubscription()
            .verifyComplete();
    }

    @Test
    @DisplayName("delete return mono error when the game does not exists")
    void delete_ReturnMonoError_WhenEmptyMonoIsReturned() {
        BDDMockito.when(gameService.findById(ArgumentMatchers.anyInt()))
            .thenReturn(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Game not found")));
        StepVerifier.create(gameController.delete(1))
            .expectError()
            .verify();
    }

    @Test
    @DisplayName("update save updated game and returns empty mono when successful")
    void update_SaveUpdatedGame_WhenSuccessful() {
        Game validGame = GameCreator.createValidGame();
        StepVerifier.create(gameController.update(1, validGame))
            .expectSubscription()
            .verifyComplete();
    }

    @Test
    @DisplayName("update return mono error when anime does not exists")
    void update_ReturnMonoError_WhenEmptyMonoIsReturned() {
        BDDMockito.when(gameService.findById(ArgumentMatchers.anyInt()))
            .thenReturn(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Game not found")));

        Game validGame = GameCreator.createValidGame();
        StepVerifier.create(gameController.update(1, validGame))
            .expectSubscription()
            .expectError(ResponseStatusException.class)
            .verify();
    }
}