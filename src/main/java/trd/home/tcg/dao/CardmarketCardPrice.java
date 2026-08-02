package trd.home.tcg.dao;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;
import trd.home.common.dao.AuditedEntity;

@Entity
@Table(name = "cardmarket_card_price")
@Getter
@Setter
public class CardmarketCardPrice extends AuditedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "card_id", nullable = false)
    private CardmarketCard card;

    @Column(precision = 10, scale = 4)
    private BigDecimal fromInEuro;

    @Column(precision = 10, scale = 4)
    private BigDecimal trendInEuro;
}
