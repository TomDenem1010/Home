package trd.home.tcg.dao;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import trd.home.common.dao.AuditedEntity;
import trd.home.tcg.constant.CardFoilType;

@Entity
@Table(name = "cardmarket_card")
@Getter
@Setter
public class CardmarketCard extends AuditedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @NonNull
    private String id;

    @NonNull
    private String link;

    @Enumerated(EnumType.STRING)
    @NonNull
    private CardFoilType foilType = CardFoilType.NO;
}
