package t03.web.controller;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import t03.datasource.mapper.DataMapperComponent;
import t03.domain.DomainInterface;
import t03.domain.model.Game;
import t03.domain.model.MoveResult;
import t03.web.mapper.WebMapperComponent;
import t03.web.model.api.*;
import t03.web.model.HashHuman;
import t03.web.service.AuthService;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api")
@AllArgsConstructor
public class APIController {

    private static final Logger logger = LoggerFactory.getLogger(APIController.class.getName());

    private final AuthService authService;
    private final DomainInterface domain;
    private final WebMapperComponent webMapper;
    private final DataMapperComponent dataMapper;

    @PostMapping("/register")
    public ResponseEntity<MR<Boolean>> register(@RequestBody SignUpRequest request) {
        return authService.register(request).response();
    }

    @PostMapping("/login")
    public ResponseEntity<MR<UUID>> login(@RequestHeader("Authorization") String authHeader) {
        Optional<UUID> userId = authService.authenticate(authHeader).map(HashHuman::getId);
        MR<UUID> mr = new MR<>(userId.orElse(null));
        if (userId.isEmpty()) {
            mr.status(HttpStatus.UNAUTHORIZED);
            if (!authHeader.startsWith("Basic ")) mr.error("Неизвестный тип авторизации");
            else mr.error("Логин или пароль неверны");
        }
        return mr.response();
    }

    @PostMapping("/human/drop/force")
    public ResponseEntity<MR<Boolean>> dropHumanForce(@RequestAttribute("hashhuman") HashHuman hashHuman) {
        domain.removeGame(hashHuman);
        return new MR<>(domain.dropHuman(hashHuman)).response();
    }

    @PostMapping("/human/drop")
    public ResponseEntity<MR<Boolean>> dropHuman(@RequestAttribute("hashhuman") HashHuman hashHuman) {
        if (domain.DTOgameByHH(hashHuman).isPresent())
            return new MR<>(false)
                    .error("Удалите игру, либо запросите через /api/human/drop/force")
                    .status(HttpStatus.CONFLICT)
                    .response();
        return new MR<>(domain.dropHuman(hashHuman)).response();
    }

    @GetMapping("/opponent/list")
    public ResponseEntity<MR<List<DTOHuman>>> getOpponents(@RequestAttribute("hashhuman") HashHuman hashHuman) {
        List<HashHuman> opponents = domain.getOpponentList(hashHuman);
        if (opponents == null) return new MR<List<DTOHuman>>(List.of())
                .error("Ошибка получения списка соперников")
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .response();
        return new MR<>(opponents.stream().map(webMapper::hhToDTO).toList())
                .response();
    }

    @PostMapping("/game/new")
    public ResponseEntity<MR<Boolean>> gameNew(@RequestAttribute("hashhuman") HashHuman hashHuman,
                                           @RequestBody RequireGame requireGame) {
        if (requireGame.getId() != null && domain.getGameByHid(requireGame.getId()).isPresent())
            return new MR<>(false).status(HttpStatus.BAD_REQUEST)
                    .error("Соперник уже играет. Две игры одновременно запрещены.")
                    .response();
        if (requireGame.getId() != null && requireGame.getId().equals(hashHuman.getId()))
            return new MR<>(false).status(HttpStatus.BAD_REQUEST)
                    .error("Играть самому с собой - это не интересно")
                    .response();
        if (requireGame.getId() != null && domain.hhFindId(requireGame.getId()).isEmpty())
            return new MR<>(false).status(HttpStatus.BAD_REQUEST)
                    .error("Соперник с таким id не существует")
                    .response();
        domain.newGame(hashHuman.getId(), requireGame.getId());
        return new MR<>(true).response();
    }
    @PostMapping("/game/step/{position}")
    public ResponseEntity<MR<DTOGame>> gameStep(@RequestAttribute("hashhuman") HashHuman hashHuman,
                                                @PathVariable int position) {
        Optional<Game> oGame = domain.getGameByHid(hashHuman.getId());
        if (oGame.isEmpty()) return new MR<DTOGame>(null).status(HttpStatus.NOT_FOUND)
                .error("Игра отсутствует для авторизованного пользователя")
                .response();
        Game game = oGame.get();
        MoveResult moveResult = domain.makeMove(game, position, hashHuman.getId());
        if (moveResult.isValidMove()) {
            return new MR<>(webMapper.geToDTO(dataMapper.toEntity(moveResult.getGame())))
                    .response();
        }
        return new MR<>(webMapper.geToDTO(dataMapper.toEntity(moveResult.getGame())))
                .error(moveResult.getMessage())
                .response();
    }

    @GetMapping("/game/state")
    public ResponseEntity<MR<DTOGame>> getGameState(@RequestAttribute("hashhuman") HashHuman hashHuman){
        Optional<DTOGame> optGame = domain.DTOgameByHH(hashHuman);
        return new MR<>(optGame.orElse(null)).response();
    }

}
