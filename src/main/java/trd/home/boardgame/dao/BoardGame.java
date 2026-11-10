package trd.home.boardgame.dao;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import trd.home.boardgame.constant.BoardGameState;
import trd.home.boardgame.constant.BoardGameType;
import trd.home.common.dao.AuditedEntity;

@Entity
@Table(
        name = "board_game",
        check = {
            @CheckConstraint(name = "ck_board_game_state", constraint = "state IN ('OWN', 'SOLD')"),
            @CheckConstraint(
                    name = "ck_board_game_type",
                    constraint = "type IN ('BASE', 'EXPANSION', 'BASE_AND_EXPANSION')")
        })
@Getter
@Setter
public class BoardGame extends AuditedEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(name = "bgg_link", length = 1000)
    private String bggLink;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private BoardGameState state;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private BoardGameType type;
}
