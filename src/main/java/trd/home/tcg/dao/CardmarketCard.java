package trd.home.tcg.dao;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import trd.home.common.dao.AuditedEntity;
import trd.home.tcg.constant.CardFoilType;

@Entity
@Table(
        name = "cardmarket_card",
        uniqueConstraints =
                @UniqueConstraint(
                        name = "ix_card_link_foil",
                        columnNames = {"link", "foil_type"}))
@Getter
@Setter
public class CardmarketCard extends AuditedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @NonNull
    private String id;

    @Column(nullable = false)
    @NonNull
    private String link;

    @Enumerated(EnumType.STRING)
    @Column(name = "foil_type", nullable = false, length = 64)
    @NonNull
    private CardFoilType foilType = CardFoilType.NO;
}
