package trd.home.tcg.repository;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.hibernate.cfg.Configuration;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactory;
import trd.home.tcg.dao.CardmarketCard;
import trd.home.tcg.dao.CardmarketCardPrice;

class CardmarketCardPriceRepositoryTest {
    @Test
    void createsDerivedQueriesAndPriceProjectionWithOracleMappings() {
        // Validate Spring Data's derived query creation without opening a database connection.
        var configuration = new Configuration()
                .addAnnotatedClass(CardmarketCard.class)
                .addAnnotatedClass(CardmarketCardPrice.class)
                .setProperty("hibernate.dialect", "org.hibernate.dialect.OracleDialect")
                .setProperty("hibernate.boot.allow_jdbc_metadata_access", "false");
        try (var factory = configuration.buildSessionFactory();
                var entityManager = factory.createEntityManager()) {
            assertNotNull(new JpaRepositoryFactory(entityManager).getRepository(CardmarketCardPriceRepository.class));
        }
    }
}
