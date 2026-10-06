package trd.home.tcg.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import trd.home.tcg.dao.CardmarketDeckVersion;

public interface CardmarketDeckVersionRepository extends JpaRepository<CardmarketDeckVersion, String> {}
