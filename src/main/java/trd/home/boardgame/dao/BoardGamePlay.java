package trd.home.boardgame.dao;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import trd.home.common.dao.AuditedEntity;

@Entity
@Table(
        name = "board_game_play",
        check = @CheckConstraint(name = "ck_board_game_play_rating", constraint = "rating BETWEEN 1 AND 5"))
@Getter
@Setter
public class BoardGamePlay extends AuditedEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "board_game_id", nullable = false)
    private BoardGame boardGame;

    @Column(name = "played_at", nullable = false)
    private LocalDateTime time;

    @Column(nullable = false)
    private int rating;
}
