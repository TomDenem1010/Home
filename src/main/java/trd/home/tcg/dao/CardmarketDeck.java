package trd.home.tcg.dao;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import trd.home.common.dao.AuditedEntity;
import trd.home.tcg.constant.DeckStatus;

@Entity
@Table(name = "cardmarket_deck")
@Getter
@Setter
public class CardmarketDeck extends AuditedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String name;

    @Enumerated(EnumType.STRING)
    private DeckStatus status = DeckStatus.ACTIVE;

    private Instant deletedAt;

    @OneToOne
    @JoinColumn(name = "current_version_id")
    private CardmarketDeckVersion currentVersion;

    @OneToMany(mappedBy = "deck", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CardmarketDeckVersion> versions = new ArrayList<>();

    public void addVersion(CardmarketDeckVersion version) {
        versions.add(version);
        version.setDeck(this);
        currentVersion = version;
    }

    public void delete(Instant deletedAt) {
        status = DeckStatus.DELETED;
        this.deletedAt = deletedAt;
    }
}
