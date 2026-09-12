package com.marketinganalytics.platform.service.sentiment;

import com.marketinganalytics.platform.entity.enums.CommentSentiment;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Rule-based sentiment classifier for Spanish-language comments: counts
 * matches against curated positive/negative word lists (diacritics-insensitive)
 * and classifies by which side wins. Simple on purpose — see
 * {@link SentimentClassifier} for how to replace it with a real model.
 */
@Service
public class KeywordSentimentClassifier implements SentimentClassifier {

    private static final Pattern WORD_SPLIT = Pattern.compile("[^\\p{L}0-9]+");

    private static final Set<String> POSITIVE_WORDS = Set.of(
            "genial", "excelente", "increible", "perfecto", "perfecta", "buenisimo", "buenisima", "recomiendo",
            "amo", "encanta", "hermoso", "hermosa", "gracias", "espectacular", "buenazo", "buenaza", "top",
            "maravilloso", "maravillosa", "feliz", "contento", "contenta", "rapido", "rapida", "calidad",
            "confiable", "bueno", "buena", "mejor", "fantastico", "fantastica", "util", "vale", "satisfecho",
            "satisfecha", "impecable"
    );

    // Note: intentionally excludes standalone words that are ambiguous in Spanish
    // (e.g. "tarde" alone means "late" but also just "afternoon") — those are only
    // captured via the unambiguous multi-word phrases below.
    private static final Set<String> NEGATIVE_WORDS = Set.of(
            "malo", "mala", "pesimo", "pesima", "horrible", "terrible", "odio", "caro", "cara", "carisimo",
            "carisima", "lento", "lenta", "nunca", "estafa", "decepcion", "decepcionado", "decepcionada",
            "roto", "rota", "defectuoso", "defectuosa", "queja", "problema", "demora", "feo", "fea", "peor",
            "basura", "fraude", "cancelar", "devolucion", "reembolso", "desastre",
            "llego tarde", "muy tarde", "no funciona", "no sirve"
    );

    @Override
    public CommentSentiment classify(String text) {
        if (text == null || text.isBlank()) {
            return CommentSentiment.NEUTRAL;
        }
        String normalized = normalize(text);

        int positiveScore = countMatches(normalized, POSITIVE_WORDS);
        int negativeScore = countMatches(normalized, NEGATIVE_WORDS);

        if (positiveScore > negativeScore) {
            return CommentSentiment.POSITIVE;
        }
        if (negativeScore > positiveScore) {
            return CommentSentiment.NEGATIVE;
        }
        return CommentSentiment.NEUTRAL;
    }

    private int countMatches(String normalizedText, Set<String> vocabulary) {
        int count = 0;
        for (String phrase : vocabulary) {
            if (phrase.contains(" ")) {
                if (normalizedText.contains(phrase)) {
                    count++;
                }
            } else {
                for (String word : WORD_SPLIT.split(normalizedText)) {
                    if (word.equals(phrase)) {
                        count++;
                    }
                }
            }
        }
        return count;
    }

    private String normalize(String text) {
        String withoutAccents = Normalizer.normalize(text.toLowerCase(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return withoutAccents;
    }
}
