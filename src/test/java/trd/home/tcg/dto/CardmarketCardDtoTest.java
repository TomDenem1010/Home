package trd.home.tcg.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import trd.home.tcg.constant.CardGameType;
import trd.home.tcg.constant.CardLanguage;

class CardmarketCardDtoTest {

    @ParameterizedTest
    @MethodSource("cardmarketUrls")
    void extractsCardmarketValuesFromUrl(
            String link,
            CardGameType expectedCardGameType,
            String expectedExpansion,
            String expectedName,
            CardLanguage expectedCardLanguage) {
        CardmarketCardDto dto = new CardmarketCardDto("card-id", link);

        assertEquals("card-id", dto.getId());
        assertEquals(link, dto.getLink());
        assertEquals(expectedCardGameType, dto.getCardGameType());
        assertEquals(expectedExpansion, dto.getExpansion());
        assertEquals(expectedName, dto.getName());
        assertEquals(expectedCardLanguage, dto.getCardLanguage());
    }

    private static Stream<Arguments> cardmarketUrls() {
        return Stream.of(
                Arguments.of(
                        "https://www.cardmarket.com/en/FleshAndBlood/Products/Singles/The-Hunted/Perforate-Regular?language=1",
                        CardGameType.FLESH_AND_BLOOD,
                        "The-Hunted",
                        "Perforate-Regular",
                        CardLanguage.ENGLISH),
                Arguments.of(
                        "https://www.cardmarket.com/en/Magic/Products/Singles/Ravnica-Remastered-Extras/Godless-Shrine-V2?language=1",
                        CardGameType.MAGIC_THE_GATHERING,
                        "Ravnica-Remastered-Extras",
                        "Godless-Shrine-V2",
                        CardLanguage.ENGLISH));
    }
}
