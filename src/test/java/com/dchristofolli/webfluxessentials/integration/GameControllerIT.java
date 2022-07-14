package com.dchristofolli.webfluxessentials.integration;

import com.dchristofolli.webfluxessentials.domain.Game;
import com.dchristofolli.webfluxessentials.domain.User;
import com.dchristofolli.webfluxessentials.repository.GameRepository;
import com.dchristofolli.webfluxessentials.service.UserDetailsService;
import com.dchristofolli.webfluxessentials.util.GameCreator;
import com.dchristofolli.webfluxessentials.util.WebTestClientUtil;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.blockhound.BlockHound;
import reactor.blockhound.BlockingOperationError;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class GameControllerIT {
    @Autowired
    private WebTestClientUtil webTestClientUtil;
    @MockBean
    private GameRepository gameRepository;

    @MockBean
    private WebProperties.Resources resources;
    private WebTestClient testClientUser;
    private WebTestClient testClientAdmin;
    private WebTestClient testClientInvalid;

    private final Game game = GameCreator.createValidGame();

    @BeforeAll
    static void blockHoundSetup() {
        BlockHound.install();
    }

    @BeforeEach
    void setup() {
        testClientUser = webTestClientUtil.authenticateClient("user", "game");
        testClientAdmin = webTestClientUtil.authenticateClient("admin", "game");
        testClientUser = webTestClientUtil.authenticateClient("x", "x");
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
        testClientUser
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
        testClientUser
            .get()
            .uri("/games/{id}", 1)
            .exchange()
            .expectStatus().isOk()
            .expectBody(Game.class)
            .isEqualTo(game);
    }

    @Test
    @DisplayName("findById throws exception when game does not exist")
    void findById_ThrowsException_WhenGameDoesNotExist() {
        BDDMockito.when(gameRepository.findById(ArgumentMatchers.anyInt()))
            .thenReturn(Mono.empty());
        testClientUser
            .get()
            .uri("/games/{id}", 1)
            .exchange()
            .expectStatus().isNotFound()
            .expectBody()
            .jsonPath("$.status").isEqualTo(404)
            .jsonPath("$.error").isEqualTo("Not Found")
            .jsonPath("$.message").isEqualTo("404 NOT_FOUND \"Game not found\"")
            .jsonPath("$.developerMessage").isEqualTo("A ResponseStatusException happened");
    }

    @Test
    @DisplayName("save creates a game when successful")
    void save_CreatesGame_WhenSuccessful() {
        var gameToBeSaved = GameCreator.createGameToBeSaved();
        testClientUser
            .post()
            .uri("/games")
            .contentType(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromValue(gameToBeSaved))
            .exchange()
            .expectStatus().isCreated()
            .expectBody(Game.class)
            .isEqualTo(game);
    }

    @Test
    @DisplayName("saveBatch creates a list of game when successful")
    void saveBatch_CreatesListOfGame_WhenSuccessful() {
        var gameToBeSaved = GameCreator.createGameToBeSaved();
        testClientUser
            .post()
            .uri("/games/batch")
            .contentType(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromValue(List.of(gameToBeSaved, gameToBeSaved)))
            .exchange()
            .expectStatus().isCreated()
            .expectBodyList(Game.class)
            .hasSize(2)
            .contains(game);
    }

    @Test
    @DisplayName("saveBatch returns mono error when one of games in the list contains null or empty name")
    void saveBatch_ReturnsMonoError_WhenContainsInvalidName() {
        var gameToBeSaved = GameCreator.createGameToBeSaved();
        BDDMockito.when(gameRepository.saveAll(List.of(gameToBeSaved,
                gameToBeSaved.withName(""))))
            .thenReturn(Flux.just(game, game.withName("")));
        testClientUser
            .post()
            .uri("/games/batch")
            .contentType(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromValue(List.of(gameToBeSaved, gameToBeSaved.withName(""))))
            .exchange()
            .expectStatus().isBadRequest()
            .expectBody()
            .jsonPath("$.status").isEqualTo(400);
    }

    @Test
    @DisplayName("save returns mono error with bad request when name is empty")
    void save_ReturnsError_WhenNameIsEmpty() {
        var gameToBeSaved = GameCreator.createGameToBeSaved().withName("");
        testClientUser
            .post()
            .uri("/games")
            .contentType(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromValue(gameToBeSaved))
            .exchange()
            .expectStatus().isBadRequest()
            .expectBody()
            .jsonPath("$.status").isEqualTo(400)
            .jsonPath("$.developerMessage").isEqualTo("A ResponseStatusException happened");
    }

    @Test
    @DisplayName("removes the game when successful")
    void delete_removesGame_WhenSuccessful() {
        testClientUser
            .delete()
            .uri("/games/{id}", 1)
            .exchange()
            .expectStatus().isNoContent();
    }

    @Test
    @DisplayName("delete return mono error when the game does not exists")
    void delete_ReturnMonoError_WhenEmptyMonoIsReturned() {
        BDDMockito.when(gameRepository.findById(ArgumentMatchers.anyInt()))
            .thenReturn(Mono.empty());
        testClientUser
            .delete()
            .uri("/games/{id}", 1)
            .exchange()
            .expectStatus().isNotFound()
            .expectBody()
            .jsonPath("$.status").isEqualTo(404)
            .jsonPath("$.developerMessage").isEqualTo("A ResponseStatusException happened");
    }

    @Test
    @DisplayName("update save updated game and returns empty mono when successful")
    void update_SaveUpdatedGame_WhenSuccessful() {
        testClientUser
            .put()
            .uri("/games/{id}", 1)
            .contentType(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromValue(game))
            .exchange()
            .expectStatus().isNoContent();
    }

    @Test
    @DisplayName("update returns mono error when anime does not exists")
    void update_ReturnMonoError_WhenEmptyMonoIsReturned() {
        BDDMockito.when(gameRepository.findById(ArgumentMatchers.anyInt()))
            .thenReturn(Mono.empty());

        testClientUser
            .put()
            .uri("/games/{id}", 1)
            .contentType(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromValue(game))
            .exchange()
            .expectStatus().isNotFound()
            .expectBody()
            .jsonPath("$.status").isEqualTo(404)
            .jsonPath("$.developerMessage").isEqualTo("A ResponseStatusException happened");
    }
}
