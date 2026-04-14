package t03.web.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data @AllArgsConstructor
public class HashHuman {

    private UUID id;
    private String name;
    private String hash;

}
