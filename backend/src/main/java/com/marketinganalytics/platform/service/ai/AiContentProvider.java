package com.marketinganalytics.platform.service.ai;

import com.marketinganalytics.platform.dto.content.GeneratedContentVariant;

import java.util.List;

/**
 * Abstraction over whatever generates marketing copy. Swapping the mock
 * implementation for a real LLM provider (OpenAI, Anthropic, ...) later only
 * requires a new {@code @Service} implementing this interface — nothing
 * upstream (ContentIdeaService, controllers, DTOs) needs to change.
 * <p>
 * To wire in a real provider: implement this interface in a new class
 * annotated {@code @Service} and reading its API key from an environment
 * variable (e.g. {@code AI_PROVIDER_API_KEY}), then mark it
 * {@code @Primary} so it takes precedence over {@link MockAiContentProvider}.
 */
public interface AiContentProvider {
    List<GeneratedContentVariant> generate(AiGenerationContext context);
}
