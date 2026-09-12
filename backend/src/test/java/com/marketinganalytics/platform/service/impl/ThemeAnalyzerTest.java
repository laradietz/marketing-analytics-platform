package com.marketinganalytics.platform.service.impl;

import com.marketinganalytics.platform.entity.AudienceComment;
import com.marketinganalytics.platform.entity.enums.CommentSentiment;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ThemeAnalyzerTest {

    private AudienceComment comment(String text) {
        return AudienceComment.builder().text(text).sentiment(CommentSentiment.NEUTRAL).build();
    }

    @Test
    void ranksTheMostFrequentlyMentionedWordFirst() {
        List<AudienceComment> comments = List.of(
                comment("El envío fue muy rápido, excelente envío"),
                comment("El envío llegó tarde esta vez"),
                comment("La calidad del producto es excelente")
        );

        var themes = ThemeAnalyzer.topThemes(comments, 5);

        assertThat(themes).isNotEmpty();
        assertThat(themes.get(0).keyword()).isEqualTo("envio");
        // Mentioned in 2 distinct comments (once per comment, not per raw occurrence).
        assertThat(themes.get(0).mentions()).isEqualTo(2);
    }

    @Test
    void excludesShortWordsAndStopwords() {
        List<AudienceComment> comments = List.of(comment("para que con los las una del por que como muy"));

        var themes = ThemeAnalyzer.topThemes(comments, 10);

        assertThat(themes).isEmpty();
    }

    @Test
    void includesASampleQuoteForEachTheme() {
        List<AudienceComment> comments = List.of(comment("El soporte técnico fue excelente"));

        var themes = ThemeAnalyzer.topThemes(comments, 5);

        assertThat(themes).anySatisfy(t -> {
            assertThat(t.keyword()).isEqualTo("soporte");
            assertThat(t.sampleQuote()).isEqualTo("El soporte técnico fue excelente");
        });
    }

    @Test
    void respectsTheLimitParameter() {
        List<AudienceComment> comments = List.of(
                comment("precio calidad envio soporte atencion demora entrega paquete")
        );

        var themes = ThemeAnalyzer.topThemes(comments, 3);

        assertThat(themes).hasSize(3);
    }

    @Test
    void returnsEmptyListForNoComments() {
        assertThat(ThemeAnalyzer.topThemes(List.of(), 5)).isEmpty();
    }
}
