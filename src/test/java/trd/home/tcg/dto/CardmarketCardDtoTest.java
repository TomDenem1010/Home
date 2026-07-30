package trd.home.tcg.dto;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import trd.home.tcg.constant.CardGameType;
import trd.home.tcg.constant.CardLanguage;

class CardmarketCardDtoTest {

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("cardmarketLinks")
    void extractsCardmarketValuesFromLink(
            String link,
            CardGameType expectedCardGameType,
            String expectedExpansion,
            String expectedName,
            CardLanguage expectedCardLanguage) {
        CardmarketCardDto dto = new CardmarketCardDto("card-id", link);

        assertAll(
                () -> assertEquals("card-id", dto.getId()),
                () -> assertEquals(link, dto.getLink()),
                () -> assertEquals(expectedCardGameType, dto.getCardGameType()),
                () -> assertEquals(expectedExpansion, dto.getExpansion()),
                () -> assertEquals(expectedName, dto.getName()),
                () -> assertEquals(expectedCardLanguage, dto.getCardLanguage()));
    }

    @ParameterizedTest
    @MethodSource("invalidLinks")
    void throwsExceptionForInvalidUri(String link) {
        assertThrows(IllegalArgumentException.class, () -> new CardmarketCardDto("card-id", link));
    }

    private static Stream<Arguments> cardmarketLinks() {
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
                        CardLanguage.ENGLISH),
                Arguments.of(
                        "https://www.cardmarket.com/en/Magic/Products/Singles/Ravnica-Remastered-Extras/Godless-Shrine-V2?foo=bar&language=1&sort=price",
                        CardGameType.MAGIC_THE_GATHERING,
                        "Ravnica-Remastered-Extras",
                        "Godless-Shrine-V2",
                        CardLanguage.ENGLISH),
                Arguments.of(
                        "https://www.cardmarket.com/en/Magic/Products",
                        CardGameType.MAGIC_THE_GATHERING,
                        null,
                        null,
                        null),
                Arguments.of(
                        "https://www.cardmarket.com/en/Magic/Products/Singles",
                        CardGameType.MAGIC_THE_GATHERING,
                        null,
                        null,
                        null),
                Arguments.of(
                        "https://www.cardmarket.com/en/Magic/Products/Singles/Ravnica-Remastered-Extras",
                        CardGameType.MAGIC_THE_GATHERING,
                        "Ravnica-Remastered-Extras",
                        null,
                        null),
                Arguments.of(
                        "https://www.cardmarket.com/en/UnknownGame/Products/Singles/Set/Card?language=2",
                        null,
                        "Set",
                        "Card",
                        null),
                Arguments.of(
                        "https://www.cardmarket.com/en/Magic/Products/Singles/Set/Card?language=abc",
                        CardGameType.MAGIC_THE_GATHERING,
                        "Set",
                        "Card",
                        null),
                Arguments.of(
                        "https://www.cardmarket.com/en/Magic/Products/Singles/Set/Card?language=",
                        CardGameType.MAGIC_THE_GATHERING,
                        "Set",
                        "Card",
                        null),
                Arguments.of(
                        "https://www.cardmarket.com/en/Magic/Products/Singles/Set/Card?Language=1",
                        CardGameType.MAGIC_THE_GATHERING,
                        "Set",
                        "Card",
                        null),
                Arguments.of(
                        "https://www.cardmarket.com/en/Magic/Products/Singles/Set/Card?language",
                        CardGameType.MAGIC_THE_GATHERING,
                        "Set",
                        "Card",
                        null),
                Arguments.of("   ", null, null, null, null),
                Arguments.of("", null, null, null, null),
                Arguments.of((String) null, null, null, null, null));
    }

    private static Stream<String> invalidLinks() {
        return Stream.of("not a valid URI", "https://www.cardmarket.com/en/Magic/Products/Singles/Set/Card?language=1%ZZ");
    }
}
