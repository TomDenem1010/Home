package trd.home.tcg.specification;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import trd.home.tcg.constant.CardFoilType;
import trd.home.tcg.constant.CardGameType;
import trd.home.tcg.dao.CardmarketCard;
import trd.home.tcg.dao.CardmarketDeckCard;
import trd.home.tcg.dao.CardmarketDeckVersion;
import trd.home.tcg.dto.CardSearchFilter;

class CardSearchSpecificationTest {
    @Test
    void utilityClassHasOnlyAPrivateConstructor() throws Exception {
        var constructor = CardSearchSpecification.class.getDeclaredConstructor();
        org.junit.jupiter.api.Assertions.assertTrue(java.lang.reflect.Modifier.isPrivate(constructor.getModifiers()));
        constructor.setAccessible(true);
        org.junit.jupiter.api.Assertions.assertNotNull(constructor.newInstance());
    }

    @Test
    @SuppressWarnings("unchecked")
    void combinesVersionFoilGameAndEveryWordWithLiteralWildcardEscaping() {
        Root<CardmarketDeckCard> root = mock(Root.class);
        Path<CardmarketDeckVersion> version = mock(Path.class);
        Path<String> versionId = mock(Path.class);
        Join<CardmarketDeckCard, CardmarketCard> card = mock(Join.class);
        Path<String> link = mock(Path.class);
        Path<CardFoilType> foil = mock(Path.class);
        var builder = mock(CriteriaBuilder.class);
        var predicate = mock(Predicate.class);
        when(root.<CardmarketDeckVersion>get("deckVersion")).thenReturn(version);
        when(version.<String>get("id")).thenReturn(versionId);
        when(root.<CardmarketDeckCard, CardmarketCard>join("card")).thenReturn(card);
        when(card.<String>get("link")).thenReturn(link);
        when(card.<CardFoilType>get("foilType")).thenReturn(foil);
        when(builder.lower(link)).thenReturn(link);
        when(builder.and(any(Predicate[].class))).thenReturn(predicate);

        var filter = new CardSearchFilter(
                CardFoilType.FOIL, " High-lord 100%_! ", CardGameType.MAGIC_THE_GATHERING, null, null, null);
        assertSame(
                predicate,
                CardSearchSpecification.matching(List.of("v1", "v2"), filter)
                        .toPredicate(root, mock(CriteriaQuery.class), builder));

        verify(versionId).in(List.of("v1", "v2"));
        verify(builder).equal(foil, CardFoilType.FOIL);
        verify(builder).like(link, "%/Magic/%", '!');
        verify(builder).like(link, "%high%", '!');
        verify(builder).like(link, "%lord%", '!');
        verify(builder).like(link, "%100!%!_!!%", '!');
        verify(builder).like(link, "%!%%", '!');
        var combined = ArgumentCaptor.forClass(Predicate[].class);
        verify(builder).and(combined.capture());
        assertEquals(6, combined.getValue().length);
    }
}
