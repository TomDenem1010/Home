package trd.home.tcg.dao;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "cardmarket_card")
@Data
public class CardmarketCard {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String link;
}
