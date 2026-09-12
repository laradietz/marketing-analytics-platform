package com.marketinganalytics.platform.service.impl;

import com.marketinganalytics.platform.dto.comment.CommentImportResult;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Parses a CSV export of comments (platform slug, author, text, optional
 * date) into rows ready to classify and persist — same hand-rolled,
 * dependency-free approach as {@link MetricCsvParser}.
 * <p>
 * Expected header (case-insensitive, any order): {@code platform, text},
 * with {@code author} and {@code postedAt} optional.
 */
public final class CommentCsvParser {

    private static final List<String> REQUIRED_COLUMNS = List.of("platform", "text");

    private CommentCsvParser() {
    }

    public record ParsedRow(String platformSlug, String author, String text, LocalDate postedAt) {
    }

    public record ParseOutcome(List<ParsedRow> rows, List<CommentImportResult.RowError> errors, int totalRows) {
    }

    public static ParseOutcome parse(String csvContent) {
        List<ParsedRow> rows = new ArrayList<>();
        List<CommentImportResult.RowError> errors = new ArrayList<>();

        String[] lines = csvContent.replace("\r\n", "\n").replace("\r", "\n").split("\n");
        if (lines.length == 0 || lines[0].isBlank()) {
            errors.add(new CommentImportResult.RowError(0, "El archivo está vacío"));
            return new ParseOutcome(rows, errors, 0);
        }

        Map<String, Integer> columnIndex = indexHeader(splitCsvLine(lines[0]));
        List<String> missing = REQUIRED_COLUMNS.stream().filter(c -> !columnIndex.containsKey(c)).toList();
        if (!missing.isEmpty()) {
            errors.add(new CommentImportResult.RowError(0, "Faltan columnas obligatorias: " + String.join(", ", missing)));
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
            String[] cells = splitCsvLine(line);

            String platformSlug = cell(cells, columnIndex, "platform");
            String text = cell(cells, columnIndex, "text");
            String author = cell(cells, columnIndex, "author");
            String postedAtRaw = cell(cells, columnIndex, "postedat");

            if (platformSlug.isBlank() || text.isBlank()) {
                errors.add(new CommentImportResult.RowError(rowNumber, "Faltan datos obligatorios (platform/text)"));
                continue;
            }

            LocalDate postedAt = null;
            if (!postedAtRaw.isBlank()) {
                try {
                    postedAt = LocalDate.parse(postedAtRaw);
                } catch (DateTimeParseException ex) {
                    errors.add(new CommentImportResult.RowError(rowNumber, "Fecha inválida \"" + postedAtRaw + "\" (usá AAAA-MM-DD)"));
                    continue;
                }
            }

            rows.add(new ParsedRow(platformSlug.toLowerCase(Locale.ROOT).trim(), author.isBlank() ? null : author, text, postedAt));
        }

        return new ParseOutcome(rows, errors, totalRows);
    }

    public static String template() {
        return "platform,author,text,postedAt\n"
                + "instagram,Camila R.,\"Me encanta este producto, la calidad es excelente\"," + LocalDate.now().minusDays(2) + "\n"
                + "facebook,Bruno C.,\"El envío llegó tarde y el precio me pareció caro\"," + LocalDate.now().minusDays(1) + "\n";
    }

    private static Map<String, Integer> indexHeader(String[] headers) {
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

    /**
     * Splits one CSV line into cells, honouring double-quoted fields (which
     * may contain commas or escaped {@code ""} quotes) — comment text is
     * free-form and routinely contains commas, unlike the numeric-only rows
     * {@link MetricCsvParser} deals with.
     */
    private static String[] splitCsvLine(String line) {
        List<String> cells = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean insideQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (insideQuotes) {
                if (c == '"') {
                    if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
                        current.append('"');
                        i++;
                    } else {
                        insideQuotes = false;
                    }
                } else {
                    current.append(c);
                }
            } else if (c == '"') {
                insideQuotes = true;
            } else if (c == ',') {
                cells.add(current.toString());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }
        cells.add(current.toString());
        return cells.toArray(new String[0]);
    }
}
