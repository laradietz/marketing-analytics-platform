package com.marketinganalytics.platform.service.impl;

import com.marketinganalytics.platform.dto.common.PageResponse;
import com.marketinganalytics.platform.dto.content.ContentGenerationRequest;
import com.marketinganalytics.platform.dto.content.ContentIdeaResponse;
import com.marketinganalytics.platform.dto.content.ContentIdeaUpdateRequest;
import com.marketinganalytics.platform.dto.content.GeneratedContentVariant;
import com.marketinganalytics.platform.entity.ContentIdea;
import com.marketinganalytics.platform.entity.Platform;
import com.marketinganalytics.platform.exception.ResourceNotFoundException;
import com.marketinganalytics.platform.mapper.ContentIdeaMapper;
import com.marketinganalytics.platform.repository.ContentIdeaRepository;
import com.marketinganalytics.platform.repository.PlatformRepository;
import com.marketinganalytics.platform.security.CurrentUserProvider;
import com.marketinganalytics.platform.service.ActivityLogService;
import com.marketinganalytics.platform.service.ContentIdeaService;
import com.marketinganalytics.platform.service.ai.AiContentProvider;
import com.marketinganalytics.platform.service.ai.AiGenerationContext;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ContentIdeaServiceImpl implements ContentIdeaService {

    private final ContentIdeaRepository contentIdeaRepository;
    private final PlatformRepository platformRepository;
    private final AiContentProvider aiContentProvider;
    private final ActivityLogService activityLogService;
    private final CurrentUserProvider currentUserProvider;

    @Override
    @Transactional
    public List<ContentIdeaResponse> generate(ContentGenerationRequest request) {
        Platform platform = platformRepository.findById(request.platformId())
                .orElseThrow(() -> ResourceNotFoundException.of("Plataforma", request.platformId()));

        AiGenerationContext context = new AiGenerationContext(
                request.productOrService(),
                request.targetAudience(),
                platform.getName(),
                request.objective(),
                request.tone(),
                request.callToAction(),
                request.variantCount() != null ? request.variantCount() : 3
        );

        List<GeneratedContentVariant> variants = aiContentProvider.generate(context);
        var user = currentUserProvider.getCurrentUser();

        List<ContentIdea> ideas = variants.stream().map(variant -> ContentIdea.builder()
                .productOrService(request.productOrService())
                .targetAudience(request.targetAudience())
                .platform(platform)
                .objective(request.objective())
                .tone(request.tone())
                .generatedTitle(variant.title())
                .generatedCopy(variant.copy())
                .generatedCta(variant.callToAction())
                .generatedHashtags(String.join(" ", variant.hashtags()))
                .createdBy(user)
                .build()
        ).toList();

        List<ContentIdea> saved = contentIdeaRepository.saveAll(ideas);

        activityLogService.log(user, "CONTENT_IDEAS_GENERATED", "ContentIdea", null,
                "Se generaron " + saved.size() + " variantes de contenido para \"" + request.productOrService() + "\"");

        return saved.stream().map(ContentIdeaMapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ContentIdeaResponse> list(Pageable pageable) {
        return PageResponse.from(contentIdeaRepository.findAllByOrderByCreatedAtDesc(pageable)
                .map(ContentIdeaMapper::toResponse));
    }

    @Override
    @Transactional
    public ContentIdeaResponse update(Long id, ContentIdeaUpdateRequest request) {
        ContentIdea idea = contentIdeaRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Idea de contenido", id));

        idea.setGeneratedTitle(request.generatedTitle());
        idea.setGeneratedCopy(request.generatedCopy());
        idea.setGeneratedCta(request.generatedCta());
        idea.setGeneratedHashtags(request.generatedHashtags());
        if (request.status() != null) {
            idea.setStatus(request.status());
        }

        return ContentIdeaMapper.toResponse(contentIdeaRepository.save(idea));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!contentIdeaRepository.existsById(id)) {
            throw ResourceNotFoundException.of("Idea de contenido", id);
        }
        contentIdeaRepository.deleteById(id);
    }
}
