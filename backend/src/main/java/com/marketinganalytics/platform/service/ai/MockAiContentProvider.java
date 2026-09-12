package com.marketinganalytics.platform.service.ai;

import com.marketinganalytics.platform.dto.content.GeneratedContentVariant;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Template-based stand-in for a real generative-AI provider. It produces
 * believable, tone-aware copy deterministically (no external calls, no API
 * key), so the product works end-to-end today. Replace with a real
 * {@link AiContentProvider} implementation when an LLM API key is available —
 * see the interface javadoc for how to wire it in.
 */
@Service
public class MockAiContentProvider implements AiContentProvider {

    /** Must stay under content_ideas.generated_title's VARCHAR(200) — titles are
     * built from user-supplied product/audience text of unbounded length. */
    private static final int TITLE_MAX_LENGTH = 200;

    private static final List<String> HOOKS_ENERGETIC = List.of(
            "¡%s ya llegó y va a cambiar tu forma de ver %s!",
            "Esto es lo que estabas esperando: %s para %s.",
            "%s: la forma más rápida de conseguir resultados en %s."
    );
    private static final List<String> HOOKS_PROFESSIONAL = List.of(
            "Presentamos %s, pensado para %s.",
            "%s ayuda a %s a alcanzar mejores resultados.",
            "Descubrí cómo %s puede transformar la forma en que %s trabaja."
    );
    private static final List<String> HOOKS_CASUAL = List.of(
            "¿Ya conocés %s? Ideal para %s.",
            "%s llegó para hacerle la vida más fácil a %s.",
            "Che, esto le va a encantar a %s: %s."
    );
    private static final List<String> HOOKS_INSPIRATIONAL = List.of(
            "Cada gran paso empieza con la decisión correcta: %s para %s.",
            "%s: porque %s merece llegar más lejos.",
            "Transformá la manera en que %s alcanza sus metas con %s."
    );
    private static final List<String> HOOKS_HUMOROUS = List.of(
            "%s: porque %s también se merece algo bueno.",
            "Sin vueltas: %s es justo lo que %s necesitaba.",
            "%s llegó, y %s ya no tiene excusas."
    );
    private static final List<String> HOOKS_URGENT = List.of(
            "Por tiempo limitado: %s para %s.",
            "No te quedes afuera: %s ya está disponible para %s.",
            "Última oportunidad de sumar %s para %s."
    );

    @Override
    public List<GeneratedContentVariant> generate(AiGenerationContext context) {
        List<String> hooks = hooksFor(context.tone());
        int count = context.variantCount() > 0 ? Math.min(context.variantCount(), hooks.size()) : hooks.size();

        List<GeneratedContentVariant> variants = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            String hook = String.format(hooks.get(i), context.productOrService(), context.targetAudience());
            String title = truncateTitle(capitalize(hook.split("[.!:]")[0]));
            String copy = buildCopy(context, hook);
            String cta = context.callToAction() != null && !context.callToAction().isBlank()
                    ? context.callToAction()
                    : defaultCtaFor(context);
            variants.add(new GeneratedContentVariant(title, copy, cta, hashtagsFor(context)));
        }
        return variants;
    }

    private String buildCopy(AiGenerationContext context, String hook) {
        String objectiveLine = switch (context.objective()) {
            case AWARENESS -> "Conocé todo lo que " + context.productOrService() + " tiene para ofrecerte.";
            case TRAFFIC -> "Visitá nuestro sitio y descubrí más detalles.";
            case LEADS -> "Dejanos tus datos y te contamos cómo empezar.";
            case CONVERSIONS -> "Es el momento de dar el siguiente paso.";
            case SALES -> "Aprovechá esta propuesta pensada para vos.";
            case ENGAGEMENT -> "Contanos qué te parece en los comentarios.";
        };
        return hook + " " + objectiveLine + " Pensado especialmente para " + context.targetAudience() + ".";
    }

    private String defaultCtaFor(AiGenerationContext context) {
        return switch (context.objective()) {
            case AWARENESS -> "Conocé más";
            case TRAFFIC -> "Visitá el sitio";
            case LEADS -> "Quiero más información";
            case CONVERSIONS -> "Empezar ahora";
            case SALES -> "Comprar ahora";
            case ENGAGEMENT -> "Contanos tu opinión";
        };
    }

    private List<String> hashtagsFor(AiGenerationContext context) {
        String productTag = "#" + slugify(context.productOrService());
        String platformTag = "#" + slugify(context.platformName());
        String objectiveTag = "#" + context.objective().name().toLowerCase(Locale.ROOT);
        return List.of(productTag, platformTag, objectiveTag, "#marketing");
    }

    private List<String> hooksFor(com.marketinganalytics.platform.entity.enums.ContentTone tone) {
        return switch (tone) {
            case ENERGETIC -> HOOKS_ENERGETIC;
            case PROFESSIONAL -> HOOKS_PROFESSIONAL;
            case CASUAL -> HOOKS_CASUAL;
            case INSPIRATIONAL -> HOOKS_INSPIRATIONAL;
            case HUMOROUS -> HOOKS_HUMOROUS;
            case URGENT -> HOOKS_URGENT;
        };
    }

    private String slugify(String value) {
        return value.trim()
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9áéíóúñ]+", "")
                .replaceAll("^$", "producto");
    }

    private String truncateTitle(String title) {
        if (title.length() <= TITLE_MAX_LENGTH) {
            return title;
        }
        return title.substring(0, TITLE_MAX_LENGTH - 3).stripTrailing() + "...";
    }

    private String capitalize(String value) {
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            return trimmed;
        }
        return Character.toUpperCase(trimmed.charAt(0)) + trimmed.substring(1);
    }
}
