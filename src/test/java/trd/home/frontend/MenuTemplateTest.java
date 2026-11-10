package trd.home.frontend;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import org.jsoup.Jsoup;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.thymeleaf.context.Context;
import org.thymeleaf.linkbuilder.ILinkBuilder;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

class MenuTemplateTest {
    @ParameterizedTest
    @CsvSource({
        "/boardgame/games, BOARDGAME",
        "/boardgame/games/new, BOARDGAME",
        "/boardgame/plays, BOARDGAME",
        "/boardgame/plays/new, BOARDGAME",
        "/tcg/statistics, TCG",
        "/auth/users, AUTH",
        "/media/by-actor, MEDIA",
        "/helper/chrome, HELPER"
    })
    void keepsOnlyActiveModuleOpenAfterNavigation(String activePath, String expectedMenu) {
        var resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("templates/");
        resolver.setSuffix(".html");
        var engine = new SpringTemplateEngine();
        engine.setTemplateResolver(resolver);
        var links = mock(ILinkBuilder.class);
        when(links.getName()).thenReturn("test-links");
        when(links.buildLink(any(), anyString(), any())).thenAnswer(call -> call.getArgument(1));
        engine.setLinkBuilder(links);
        var authentication = new UsernamePasswordAuthenticationToken(
                "user",
                null,
                List.of("ADMIN", "TCG", "MEDIA", "BOARDGAME").stream()
                        .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                        .toList());
        var context = new Context();
        context.setVariable("menuItems", new MenuModelAdvice().menuItems(authentication));
        context.setVariable("activePath", activePath);

        var document = Jsoup.parse(engine.process("menu", context));
        var openGroups = document.select("details[open]");
        assertEquals(1, openGroups.size());
        assertEquals(
                expectedMenu, openGroups.getFirst().selectFirst("summary span").text());
        var activeLinks = document.select("a[aria-current=page]");
        assertEquals(1, activeLinks.size());
        assertEquals(activePath, activeLinks.getFirst().attr("href"));
        assertTrue(activeLinks.getFirst().hasClass("active"));
    }
}
