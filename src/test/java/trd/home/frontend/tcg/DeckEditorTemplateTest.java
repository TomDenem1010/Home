package trd.home.frontend.tcg;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.thymeleaf.context.Context;
import org.thymeleaf.linkbuilder.ILinkBuilder;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import trd.home.tcg.constant.CardFoilType;
import trd.home.tcg.dto.DeckEditor;

class DeckEditorTemplateTest {
    @Test
    void rendersCardNamesSelectionAndAddForm() {
        String link = "https://www.cardmarket.com/en/Magic/Products/Singles/Test/Example-Card";
        String html = render(List.of(new DeckEditor.Card("card-id", link, CardFoilType.FOIL, 3)));
        assertTrue(html.contains(">Example-Card</a>"));
        assertTrue(html.contains("Select Example-Card FOIL"));
        assertTrue(html.contains("data-sort-value=\"Example-Card\""));
        assertTrue(html.contains("value=\"card-id\""));
        assertTrue(html.contains("href=\"" + link + "\""));
        assertTrue(html.contains("name=\"quantity\""));
        assertTrue(html.contains("name=\"foilType\""));
    }

    @Test
    void rendersEmptyDeckWithDisabledRemoval() {
        String html = render(List.of());
        assertTrue(html.contains("This deck contains no cards."));
        assertTrue(html.contains("disabled=\"disabled\""));
        assertTrue(html.contains("Add card"));
    }

    private String render(List<DeckEditor.Card> cards) {
        var resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("templates/");
        resolver.setSuffix(".html");
        var engine = new SpringTemplateEngine();
        engine.setTemplateResolver(resolver);
        var links = mock(ILinkBuilder.class);
        when(links.getName()).thenReturn("test-links");
        when(links.buildLink(any(), anyString(), any())).thenAnswer(call -> call.getArgument(1));
        engine.setLinkBuilder(links);
        var context = new Context();
        context.setVariable("deck", new DeckEditor("deck-id", "Deck", "version-id", "v1", cards));
        context.setVariable("foilTypes", CardFoilType.values());
        return engine.process("tcg/edit-deck", context);
    }
}
