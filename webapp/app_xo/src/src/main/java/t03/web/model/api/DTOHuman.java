package t03.web.model.api;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data @AllArgsConstructor
public class DTOHuman {

    private UUID id;
    private String name;

}