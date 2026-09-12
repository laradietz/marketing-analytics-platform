package com.marketinganalytics.platform.service.sentiment;

import com.marketinganalytics.platform.entity.enums.CommentSentiment;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class KeywordSentimentClassifierTest {

    private final KeywordSentimentClassifier classifier = new KeywordSentimentClassifier();

    @Test
    void classifiesClearlyPositiveTextAsPositive() {
        assertThat(classifier.classify("Excelente producto, me encanta la calidad, muy recomendable"))
                .isEqualTo(CommentSentiment.POSITIVE);
    }

    @Test
    void classifiesClearlyNegativeTextAsNegative() {
        assertThat(classifier.classify("Pésimo servicio, el envío llegó tarde y todo llegó roto"))
                .isEqualTo(CommentSentiment.NEGATIVE);
    }

    @Test
    void classifiesNeutralTextWithNoSentimentWordsAsNeutral() {
        assertThat(classifier.classify("Llegó el pedido ayer a la tarde"))
                .isEqualTo(CommentSentiment.NEUTRAL);
    }

    @Test
    void isCaseAndAccentInsensitive() {
        assertThat(classifier.classify("EXCELENTE, INCREÍBLE, LO RECOMIENDO"))
                .isEqualTo(CommentSentiment.POSITIVE);
    }

    @Test
    void tiedSentimentCountsResolveToNeutral() {
        assertThat(classifier.classify("bueno pero malo"))
                .isEqualTo(CommentSentiment.NEUTRAL);
    }

    @Test
    void blankTextIsNeutral() {
        assertThat(classifier.classify("")).isEqualTo(CommentSentiment.NEUTRAL);
        assertThat(classifier.classify(null)).isEqualTo(CommentSentiment.NEUTRAL);
    }

    @Test
    void matchesFeminineAdjectiveFormsNotJustTheMasculineDictionaryForm() {
        // Spanish adjectives agree in gender with the noun they modify ("pésima
        // experiencia" vs "pésimo servicio") — the word list must cover both.
        assertThat(classifier.classify("Pesima experiencia, el pedido llego incompleto y nadie me responde"))
                .isEqualTo(CommentSentiment.NEGATIVE);
    }

    @Test
    void doesNotMatchWordsThatMerelyContainAKeywordAsASubstring() {
        // "malopolitano" is not the word "malo" — word-boundary matching should not flag it.
        assertThat(classifier.classify("Soy de malopolitano y hoy hace calor"))
                .isEqualTo(CommentSentiment.NEUTRAL);
    }
}
