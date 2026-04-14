package t03.web.model.api;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data @AllArgsConstructor
public class DTOGame {

    private UUID id;
    private String board;
    private DTOHuman x;
    private DTOHuman o;
    private Boolean xturn;
    private Character winner;

}
