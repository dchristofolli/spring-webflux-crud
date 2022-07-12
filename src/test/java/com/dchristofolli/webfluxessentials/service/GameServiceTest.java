package com.dchristofolli.webfluxessentials.service;

import com.dchristofolli.webfluxessentials.domain.Game;
import com.dchristofolli.webfluxessentials.repository.GameRepository;
import com.dchristofolli.webfluxessentials.util.GameCreator;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.server.ResponseStatusException;
import reactor.blockhound.BlockHound;
import reactor.blockhound.BlockingOperationError;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

@ExtendWith(SpringExtension.class)
class GameServiceTest {
    @InjectMocks
    private GameService gameService;

    @Mock
    private GameRepository gameRepository;

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
        BDDMockito.when(gameRepository.save(GameCreator.createGameToBeSaved()))
            .thenReturn(Mono.just(game));
        BDDMockito.when(gameRepository.saveAll(List.of(GameCreator.createGameToBeSaved(),
                GameCreator.createGameToBeSaved())))
            .thenReturn(Flux.just(game, game));
        BDDMockito.when(gameRepository.delete(ArgumentMatchers.any(Game.class)))
            .thenReturn(Mono.empty());
        BDDMockito.when(gameRepository.save(GameCreator.createValidGame()))
            .thenReturn(Mono.empty());
    }

    @Test
    void blockHoundWorks() {
        try {
            FutureTask<?> task = new FutureTask<>(() -> {
                Thread.sleep(0);//NOSONAR
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
        StepVerifier.create(gameService.findAll())
            .expectSubscription()
            .expectNext(game)
            .verifyComplete();
    }

    @Test
    @DisplayName("findById returns a mono with game when it exists")
    void findById_ReturnMonoGame_WhenSuccessful() {
        StepVerifier.create(gameService.findById(1))
            .expectSubscription()
            .expectNext(game)
            .verifyComplete();
    }

    @Test
    @DisplayName("findById returns a mono error when game does not exist")
    void findById_ReturnMonoError_WhenEmptyMonoIsReturned() {
        BDDMockito.when(gameRepository.findById(ArgumentMatchers.anyInt()))
            .thenReturn(Mono.empty());

        StepVerifier.create(gameService.findById(1))
            .expectSubscription()
            .expectError(ResponseStatusException.class)
            .verify();
    }

    @Test
    @DisplayName("save creates a game when successful")
    void save_CreatesGame_WhenSuccessful() {
        Game gameToBeSaved = GameCreator.createGameToBeSaved();
        StepVerifier.create(gameService.save(gameToBeSaved))
            .expectSubscription()
            .expectNext(game)
            .verifyComplete();
    }

    @Test
    @DisplayName("saveAll creates a list of game when successful")
    void saveAll_CreatesListOfGame_WhenSuccessful() {
        Game gameToBeSaved = GameCreator.createGameToBeSaved();
        StepVerifier.create(gameService.saveAll(List.of(gameToBeSaved, gameToBeSaved)))
            .expectSubscription()
            .expectNext(game, game)
            .verifyComplete();
    }

    @Test
    @DisplayName("saveAll returns mono error when one of games in the list contains null or empty name")
    void saveAll_ReturnsMonoError_WhenContainsInvalidName() {
        Game gameToBeSaved = GameCreator.createGameToBeSaved();
        BDDMockito.when(gameRepository.saveAll(List.of(gameToBeSaved, gameToBeSaved.withName(""))))
            .thenReturn(Flux.just(game, game.withName("")));
        StepVerifier.create(gameService.saveAll(List.of(gameToBeSaved, gameToBeSaved.withName(""))))
            .expectSubscription()
            .expectNext(game)
            .expectError(ResponseStatusException.class)
            .verify();
    }

    @Test
    @DisplayName("removes the game when successful")
    void delete_removesGame_WhenSuccessful() {
        StepVerifier.create(gameService.delete(1))
            .expectSubscription()
            .verifyComplete();
    }

    @Test
    @DisplayName("delete return mono error when the game does not exists")
    void delete_ReturnMonoError_WhenEmptyMonoIsReturned() {
        BDDMockito.when(gameRepository.findById(ArgumentMatchers.anyInt()))
            .thenReturn(Mono.empty());
        StepVerifier.create(gameService.delete(1))
            .expectError(ResponseStatusException.class)
            .verify();
    }

    @Test
    @DisplayName("update save updated game and returns empty mono when successful")
    void update_SaveUpdatedGame_WhenSuccessful() {
        Game validGame = GameCreator.createValidGame();
        StepVerifier.create(gameService.update(validGame))
            .expectSubscription()
            .verifyComplete();
    }

    @Test
    @DisplayName("update return mono error when anime does not exists")
    void update_ReturnMonoError_WhenEmptyMonoIsReturned() {
        BDDMockito.when(gameRepository.findById(ArgumentMatchers.anyInt()))
            .thenReturn(Mono.empty());

        Game validGame = GameCreator.createValidGame();
        StepVerifier.create(gameService.update(validGame))
            .expectSubscription()
            .expectError(ResponseStatusException.class)
            .verify();
    }
}