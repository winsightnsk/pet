package t03.web.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data @AllArgsConstructor
public class WebBord {

    private List<Character> cells;
    private UUID gameId;
    private String xName;
    private String oName;
    private boolean gameOver;
    private String message;
    private boolean xTurn;

}
