package com.marketinganalytics.platform.service.impl;

import com.marketinganalytics.platform.dto.metric.CampaignMetricRequest;
import com.marketinganalytics.platform.dto.metric.MetricImportResult;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Parses the CSV export a user downloads from Meta/Google/TikTok Ads Manager
 * (or fills in by hand) into {@link CampaignMetricRequest} rows, without a
 * third-party CSV library — the format is a flat, unquoted comma-separated
 * table, so a hand-rolled parser is simpler than a new dependency.
 * <p>
 * Expected header (case-insensitive, any order): {@code recordedDate,
 * impressions, reach, clicks, conversions, spend, revenue, likes, comments,
 * shares, saves}. {@code reach, likes, comments, shares, saves} are optional
 * and default to 0. Dates must be ISO ({@code yyyy-MM-dd}).
 */
public final class MetricCsvParser {

    private static final List<String> REQUIRED_COLUMNS = List.of("recordeddate", "impressions", "clicks", "conversions", "spend", "revenue");

    private MetricCsvParser() {
    }

    public record ParseOutcome(List<CampaignMetricRequest> rows, List<MetricImportResult.RowError> errors, int totalRows) {
    }

    public static ParseOutcome parse(String csvContent) {
        List<CampaignMetricRequest> rows = new ArrayList<>();
        List<MetricImportResult.RowError> errors = new ArrayList<>();

        String[] lines = csvContent.replace("\r\n", "\n").replace("\r", "\n").split("\n");
        if (lines.length == 0 || lines[0].isBlank()) {
            errors.add(new MetricImportResult.RowError(0, "El archivo está vacío"));
            return new ParseOutcome(rows, errors, 0);
        }

        Map<String, Integer> columnIndex = indexHeader(lines[0]);
        List<String> missing = REQUIRED_COLUMNS.stream().filter(c -> !columnIndex.containsKey(c)).toList();
        if (!missing.isEmpty()) {
            errors.add(new MetricImportResult.RowError(0, "Faltan columnas obligatorias: " + String.join(", ", missing)));
            return new ParseOutcome(rows, errors, 0);
        }

        int totalRows = 0;
        for (int i = 1; i < lines.length; i++) {
            String line = lines[i];
            if (line.isBlank()) {
                continue;
            }
            totalRows++;
            int rowNumber = i + 1;
            String[] cells = line.split(",", -1);

            try {
                rows.add(new CampaignMetricRequest(
                        parseDate(cell(cells, columnIndex, "recordeddate")),
                        parseLong(cell(cells, columnIndex, "impressions"), "impressions"),
                        parseOptionalLong(cell(cells, columnIndex, "reach")),
                        parseLong(cell(cells, columnIndex, "clicks"), "clicks"),
                        parseLong(cell(cells, columnIndex, "conversions"), "conversions"),
                        parseDecimal(cell(cells, columnIndex, "spend"), "spend"),
                        parseDecimal(cell(cells, columnIndex, "revenue"), "revenue"),
                        parseOptionalLong(cell(cells, columnIndex, "likes")),
                        parseOptionalLong(cell(cells, columnIndex, "comments")),
                        parseOptionalLong(cell(cells, columnIndex, "shares")),
                        parseOptionalLong(cell(cells, columnIndex, "saves"))
                ));
            } catch (RowParseException ex) {
                errors.add(new MetricImportResult.RowError(rowNumber, ex.getMessage()));
            }
        }

        return new ParseOutcome(rows, errors, totalRows);
    }

    public static String template() {
        return "recordedDate,impressions,reach,clicks,conversions,spend,revenue,likes,comments,shares,saves\n"
                + LocalDate.now().minusDays(1) + ",10000,8000,250,12,150.00,480.00,90,12,5,20\n";
    }

    private static Map<String, Integer> indexHeader(String headerLine) {
        String[] headers = headerLine.split(",", -1);
        Map<String, Integer> index = new HashMap<>();
        for (int i = 0; i < headers.length; i++) {
            index.put(headers[i].trim().toLowerCase(Locale.ROOT), i);
        }
        return index;
    }

    private static String cell(String[] cells, Map<String, Integer> columnIndex, String column) {
        Integer idx = columnIndex.get(column);
        if (idx == null || idx >= cells.length) {
            return "";
        }
        return cells[idx].trim();
    }

    private static LocalDate parseDate(String value) {
        try {
            return LocalDate.parse(value);
        } catch (DateTimeParseException ex) {
            throw new RowParseException("Fecha inválida \"" + value + "\" (usá formato AAAA-MM-DD)");
        }
    }

    private static long parseLong(String value, String field) {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException ex) {
            throw new RowParseException("Valor inválido para \"" + field + "\": \"" + value + "\"");
        }
    }

    private static Long parseOptionalLong(String value) {
        if (value == null || value.isBlank()) {
            return 0L;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException ex) {
            return 0L;
        }
    }

    private static BigDecimal parseDecimal(String value, String field) {
        try {
            return new BigDecimal(value);
        } catch (NumberFormatException ex) {
            throw new RowParseException("Valor inválido para \"" + field + "\": \"" + value + "\"");
        }
    }

    private static final class RowParseException extends RuntimeException {
        RowParseException(String message) {
            super(message);
        }
    }
}
