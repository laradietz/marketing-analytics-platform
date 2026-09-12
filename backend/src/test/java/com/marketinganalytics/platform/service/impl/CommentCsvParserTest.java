package com.marketinganalytics.platform.service.impl;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class CommentCsvParserTest {

    @Test
    void parsesQuotedTextContainingCommasCorrectly() {
        String csv = """
                platform,author,text,postedAt
                instagram,Camila R.,"Me encanta este producto, la calidad es excelente",2026-01-01
                """;

        var outcome = CommentCsvParser.parse(csv);

        assertThat(outcome.errors()).isEmpty();
        assertThat(outcome.rows()).hasSize(1);
        var row = outcome.rows().get(0);
        assertThat(row.platformSlug()).isEqualTo("instagram");
        assertThat(row.author()).isEqualTo("Camila R.");
        assertThat(row.text()).isEqualTo("Me encanta este producto, la calidad es excelente");
        assertThat(row.postedAt()).isEqualTo(LocalDate.of(2026, 1, 1));
    }

    @Test
    void handlesEscapedQuotesInsideAQuotedField() {
        String csv = """
                platform,text
                facebook,"Dijo ""genial"" y compró de nuevo"
                """;

        var outcome = CommentCsvParser.parse(csv);

        assertThat(outcome.errors()).isEmpty();
        assertThat(outcome.rows().get(0).text()).isEqualTo("Dijo \"genial\" y compró de nuevo");
    }

    @Test
    void authorAndPostedAtAreOptional() {
        String csv = """
                platform,text
                tiktok,Buen contenido
                """;

        var outcome = CommentCsvParser.parse(csv);

        assertThat(outcome.errors()).isEmpty();
        var row = outcome.rows().get(0);
        assertThat(row.author()).isNull();
        assertThat(row.postedAt()).isNull();
    }

    @Test
    void reportsMissingRequiredColumns() {
        String csv = """
                author,postedAt
                Camila,2026-01-01
                """;

        var outcome = CommentCsvParser.parse(csv);

        assertThat(outcome.rows()).isEmpty();
        assertThat(outcome.errors()).hasSize(1);
        assertThat(outcome.errors().get(0).message()).contains("platform", "text");
    }

    @Test
    void reportsARowMissingRequiredDataButKeepsParsingTheRest() {
        String csv = """
                platform,text
                instagram,
                facebook,Todo perfecto
                """;

        var outcome = CommentCsvParser.parse(csv);

        assertThat(outcome.totalRows()).isEqualTo(2);
        assertThat(outcome.rows()).hasSize(1);
        assertThat(outcome.errors()).hasSize(1);
    }

    @Test
    void templateProducesAParsableCsv() {
        var outcome = CommentCsvParser.parse(CommentCsvParser.template());

        assertThat(outcome.errors()).isEmpty();
        assertThat(outcome.rows()).hasSize(2);
    }
}
