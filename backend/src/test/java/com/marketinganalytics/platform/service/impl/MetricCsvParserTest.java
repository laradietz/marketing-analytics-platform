package com.marketinganalytics.platform.service.impl;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class MetricCsvParserTest {

    @Test
    void parsesAValidCsvWithAllColumns() {
        String csv = """
                recordedDate,impressions,reach,clicks,conversions,spend,revenue,likes,comments,shares,saves
                2026-01-01,10000,8000,250,12,150.00,480.00,90,12,5,20
                2026-01-02,12000,9000,300,15,180.00,600.00,100,15,6,25
                """;

        var outcome = MetricCsvParser.parse(csv);

        assertThat(outcome.errors()).isEmpty();
        assertThat(outcome.totalRows()).isEqualTo(2);
        assertThat(outcome.rows()).hasSize(2);

        var first = outcome.rows().get(0);
        assertThat(first.recordedDate()).isEqualTo(LocalDate.of(2026, 1, 1));
        assertThat(first.impressions()).isEqualTo(10000);
        assertThat(first.spend()).isEqualByComparingTo("150.00");
        assertThat(first.likes()).isEqualTo(90L);
    }

    @Test
    void defaultsOptionalColumnsToZeroWhenMissingEntirely() {
        String csv = """
                recordedDate,impressions,clicks,conversions,spend,revenue
                2026-01-01,10000,250,12,150.00,480.00
                """;

        var outcome = MetricCsvParser.parse(csv);

        assertThat(outcome.errors()).isEmpty();
        var row = outcome.rows().get(0);
        assertThat(row.reach()).isEqualTo(0L);
        assertThat(row.likes()).isEqualTo(0L);
        assertThat(row.saves()).isEqualTo(0L);
    }

    @Test
    void reportsMissingRequiredColumns() {
        String csv = """
                recordedDate,impressions,clicks
                2026-01-01,10000,250
                """;

        var outcome = MetricCsvParser.parse(csv);

        assertThat(outcome.rows()).isEmpty();
        assertThat(outcome.errors()).hasSize(1);
        assertThat(outcome.errors().get(0).message()).contains("conversions", "spend", "revenue");
    }

    @Test
    void reportsAnInvalidRowButKeepsParsingTheRest() {
        String csv = """
                recordedDate,impressions,clicks,conversions,spend,revenue
                2026-01-01,not-a-number,250,12,150.00,480.00
                2026-01-02,12000,300,15,180.00,600.00
                """;

        var outcome = MetricCsvParser.parse(csv);

        assertThat(outcome.totalRows()).isEqualTo(2);
        assertThat(outcome.rows()).hasSize(1);
        assertThat(outcome.errors()).hasSize(1);
        assertThat(outcome.errors().get(0).row()).isEqualTo(2);
    }

    @Test
    void rejectsAnInvalidDateFormat() {
        String csv = """
                recordedDate,impressions,clicks,conversions,spend,revenue
                01/01/2026,10000,250,12,150.00,480.00
                """;

        var outcome = MetricCsvParser.parse(csv);

        assertThat(outcome.rows()).isEmpty();
        assertThat(outcome.errors()).hasSize(1);
        assertThat(outcome.errors().get(0).message()).contains("Fecha inválida");
    }

    @Test
    void ignoresBlankLines() {
        String csv = """
                recordedDate,impressions,clicks,conversions,spend,revenue
                2026-01-01,10000,250,12,150.00,480.00

                2026-01-02,12000,300,15,180.00,600.00
                """;

        var outcome = MetricCsvParser.parse(csv);

        assertThat(outcome.totalRows()).isEqualTo(2);
        assertThat(outcome.rows()).hasSize(2);
    }

    @Test
    void headerMatchingIsCaseInsensitiveAndOrderIndependent() {
        String csv = """
                Spend,Revenue,RecordedDate,Clicks,Conversions,Impressions
                150.00,480.00,2026-01-01,250,12,10000
                """;

        var outcome = MetricCsvParser.parse(csv);

        assertThat(outcome.errors()).isEmpty();
        assertThat(outcome.rows().get(0).impressions()).isEqualTo(10000);
        assertThat(outcome.rows().get(0).spend()).isEqualByComparingTo("150.00");
    }

    @Test
    void templateProducesAParsableCsv() {
        var outcome = MetricCsvParser.parse(MetricCsvParser.template());

        assertThat(outcome.errors()).isEmpty();
        assertThat(outcome.rows()).hasSize(1);
    }
}
