package com.marketinganalytics.platform.service.impl;

import com.marketinganalytics.platform.dto.comment.ThemeSummary;
import com.marketinganalytics.platform.entity.AudienceComment;

import java.text.Normalizer;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Extracts the most-mentioned words across a set of comments — a simple,
 * transparent stand-in for topic modelling: frequency counting over
 * normalized tokens with common Spanish stopwords removed. Good enough to
 * turn "what are people actually talking about" into a short list a human
 * (or the content generator) can act on, without an external NLP service.
 */
public final class ThemeAnalyzer {

    private static final Pattern WORD_SPLIT = Pattern.compile("[^\\p{L}0-9]+");
    private static final int MIN_WORD_LENGTH = 4;

    private static final Set<String> STOPWORDS = Set.of(
            "para", "esto", "esta", "este", "esos", "esas", "pero", "porque", "como", "cuando", "donde",
            "muy", "mas", "menos", "todo", "toda", "todos", "todas", "nada", "algo", "algun", "alguna",
            "algunos", "algunas", "otro", "otra", "otros", "otras", "sobre", "entre", "desde", "hasta",
            "hacia", "tambien", "solo", "sido", "estan", "estar", "estoy", "estamos", "tiene", "tienen",
            "tengo", "hace", "hacer", "puede", "pueden", "podria", "seria", "seran", "estaba",
            "quiero", "queria", "gracias", "hola", "buenas", "buenos", "dias", "les", "las", "los",
            "una", "uno", "unos", "unas", "que", "con", "sin", "por", "del"
    );

    private ThemeAnalyzer() {
    }

    public static List<ThemeSummary> topThemes(List<AudienceComment> comments, int limit) {
        Map<String, Integer> counts = new LinkedHashMap<>();
        Map<String, String> sampleQuotes = new LinkedHashMap<>();

        for (AudienceComment comment : comments) {
            Set<String> wordsInThisComment = new LinkedHashSet<>(tokenize(comment.getText()));
            for (String word : wordsInThisComment) {
                counts.merge(word, 1, Integer::sum);
                sampleQuotes.putIfAbsent(word, comment.getText());
            }
        }

        return counts.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(limit)
                .map(e -> new ThemeSummary(e.getKey(), e.getValue(), sampleQuotes.get(e.getKey())))
                .toList();
    }

    private static List<String> tokenize(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }
        String normalized = Normalizer.normalize(text.toLowerCase(), Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        List<String> words = new ArrayList<>();
        for (String word : WORD_SPLIT.split(normalized)) {
            if (word.length() >= MIN_WORD_LENGTH && !STOPWORDS.contains(word) && !word.chars().allMatch(Character::isDigit)) {
                words.add(word);
            }
        }
        return words;
    }
}
