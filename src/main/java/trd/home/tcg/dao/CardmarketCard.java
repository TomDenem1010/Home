package trd.home.tcg.dao;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import trd.home.common.dao.AuditedEntity;

@Entity
@Table(name = "cardmarket_card")
@Getter
@Setter
public class CardmarketCard extends AuditedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String link;
}
