package t03.web.mapper;

import org.springframework.stereotype.Component;
import t03.datasource.model.GameEntity;
import t03.datasource.model.HumanEntity;
import t03.domain.model.Game;
import t03.domain.model.Cell;
import t03.web.model.api.DTOGame;
import t03.web.model.api.DTOHuman;
import t03.web.model.HashHuman;
import t03.web.model.WebBord;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

@Component
public class WebMapperComponent {

    public WebBord gameToWebBoard(Game game) {
        String mess = "";
        if (game.isGameOver()) {
            if (game.getWinner() != null && game.getWinner().getSymbol() != ' ') {
                mess = "Игра окончена! Победил " + game.getWinner() + "!";
            } else {
                mess = "Игра окончена! Ничья!";
            }
        }
        return new WebBord(
                Arrays.stream(game.getBoard().getCells()).map(Cell::getSymbol).toList(),
                game.getId(),
                game.getNameX(),
                game.getNameO(),
                game.isGameOver(),
                mess,
                game.isXPlay()
        );
    }

    private String urlNameDecode(String urlName) {
        String decodedName = URLDecoder.decode(urlName, StandardCharsets.UTF_8);
        String trimmedName = decodedName.trim();
        if (trimmedName.isEmpty()) throw new IllegalArgumentException("Имя игрока не может быть пустым!");
        return trimmedName;
    }

    public HashHuman heTohh(HumanEntity entity){
        return new HashHuman(entity.getId(),entity.getName(),entity.getPassword());
    }

    //API

    public DTOHuman hhToDTO(HashHuman hashHuman) {
        if (hashHuman == null) return null;
        return new DTOHuman(hashHuman.getId(), hashHuman.getName());
    }
    public DTOHuman heToDTO(HumanEntity human) {
        if (human == null) return null;
        return new DTOHuman(human.getId(), human.getName());
    }

    public DTOGame geToDTO(GameEntity ge) {
        if (ge == null) return null;
        return new DTOGame(
                ge.getId(),
                ge.getBoardstate(),
                heToDTO(ge.getHumanX()),
                heToDTO(ge.getHumanO()),
                ge.isXplay(),
                ge.getWinner()
        );
    }

}
