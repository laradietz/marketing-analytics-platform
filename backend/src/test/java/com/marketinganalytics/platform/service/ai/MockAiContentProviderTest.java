package com.marketinganalytics.platform.service.ai;

import com.marketinganalytics.platform.entity.enums.CampaignObjective;
import com.marketinganalytics.platform.entity.enums.ContentTone;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MockAiContentProviderTest {

    private final MockAiContentProvider provider = new MockAiContentProvider();

    @Test
    void generatesTheRequestedNumberOfVariants() {
        var context = new AiGenerationContext(
                "curso de marketing", "emprendedores", "Instagram", CampaignObjective.LEADS, ContentTone.PROFESSIONAL, null, 3);

        var variants = provider.generate(context);

        assertThat(variants).hasSize(3);
        variants.forEach(v -> {
            assertThat(v.title()).isNotBlank();
            assertThat(v.copy()).isNotBlank();
            assertThat(v.callToAction()).isNotBlank();
            assertThat(v.hashtags()).isNotEmpty();
        });
    }

    @Test
    void neverProducesATitleLongerThanTheDatabaseColumnLimit() {
        // A long, unbounded targetAudience (e.g. one synthesized from many
        // audience-comment themes) must never blow past generated_title's
        // VARCHAR(200) and crash the insert.
        String veryLongAudience = "Personas que comentan sobre " + "tema, ".repeat(40) + "y muchos temas más";
        var context = new AiGenerationContext(
                "zapatillas running", veryLongAudience, "Instagram", CampaignObjective.ENGAGEMENT, ContentTone.CASUAL, null, 3);

        var variants = provider.generate(context);

        variants.forEach(v -> assertThat(v.title().length()).isLessThanOrEqualTo(200));
    }

    @Test
    void usesTheProvidedCallToActionWhenGiven() {
        var context = new AiGenerationContext(
                "producto", "audiencia", "Facebook", CampaignObjective.SALES, ContentTone.URGENT, "Comprá ya", 1);

        var variants = provider.generate(context);

        assertThat(variants.get(0).callToAction()).isEqualTo("Comprá ya");
    }
}
