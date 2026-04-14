package t03.datasource.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "human")
public class HumanEntity {

    @Id
    private UUID id;

    @Column(name = "password",
            length = 60, nullable = false)
    private String password;

    @Column(name = "name",
            length = 100, nullable = false)
    private String name;

    @OneToMany(mappedBy = "human", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<SessionEntity> sessions;

    public HumanEntity(UUID id, String password, String name) {
        this.id = id;
        this.password = password;
        this.name = name;
    }

}
