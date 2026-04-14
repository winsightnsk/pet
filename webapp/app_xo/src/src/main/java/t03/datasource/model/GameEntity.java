package t03.datasource.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@ToString
@NoArgsConstructor @AllArgsConstructor
@Entity @Table(name = "gameentity")
public class GameEntity {

    @Id
    @Column(name = "id", nullable = false,
            updatable = false)
    private UUID id;

    @Column(name = "boardstate", nullable = false,
            length = 10)
    private String boardstate;

    @Column(name = "winner", nullable = false)
    private Character winner;

    @OneToOne
    @JoinColumn(name = "humanx", nullable = false, unique = true)
    private HumanEntity humanX;

    @OneToOne
    @JoinColumn(name = "humano", unique = true)
    private HumanEntity humanO;

    @Column(name = "xplay", nullable = false,
            columnDefinition = "BOOLEAN DEFAULT true")
    private boolean xplay = true;

}
