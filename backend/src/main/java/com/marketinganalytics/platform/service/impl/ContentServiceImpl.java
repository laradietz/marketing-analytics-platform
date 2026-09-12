package com.marketinganalytics.platform.service.impl;

import com.marketinganalytics.platform.dto.common.PageResponse;
import com.marketinganalytics.platform.dto.content.ContentFilter;
import com.marketinganalytics.platform.dto.content.ContentRequest;
import com.marketinganalytics.platform.dto.content.ContentResponse;
import com.marketinganalytics.platform.entity.Campaign;
import com.marketinganalytics.platform.entity.Content;
import com.marketinganalytics.platform.entity.Platform;
import com.marketinganalytics.platform.entity.enums.ContentStatus;
import com.marketinganalytics.platform.exception.ResourceNotFoundException;
import com.marketinganalytics.platform.mapper.ContentMapper;
import com.marketinganalytics.platform.repository.CampaignRepository;
import com.marketinganalytics.platform.repository.ContentRepository;
import com.marketinganalytics.platform.repository.PlatformRepository;
import com.marketinganalytics.platform.repository.spec.ContentSpecifications;
import com.marketinganalytics.platform.security.CurrentUserProvider;
import com.marketinganalytics.platform.service.ActivityLogService;
import com.marketinganalytics.platform.service.ContentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class ContentServiceImpl implements ContentService {

    private final ContentRepository contentRepository;
    private final PlatformRepository platformRepository;
    private final CampaignRepository campaignRepository;
    private final ActivityLogService activityLogService;
    private final CurrentUserProvider currentUserProvider;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ContentResponse> list(ContentFilter filter, Pageable pageable) {
        return PageResponse.from(contentRepository.findAll(ContentSpecifications.fromFilter(filter), pageable)
                .map(ContentMapper::toResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public ContentResponse getById(Long id) {
        return ContentMapper.toResponse(findOrThrow(id));
    }

    @Override
    @Transactional
    public ContentResponse create(ContentRequest request) {
        Content content = new Content();
        content.setCreatedBy(currentUserProvider.getCurrentUser());
        applyRequest(content, request);
        content = contentRepository.save(content);

        activityLogService.log(currentUserProvider.getCurrentUser(), "CONTENT_CREATED", "Content", content.getId(),
                "Se creó el contenido \"" + content.getTitle() + "\"");

        return ContentMapper.toResponse(content);
    }

    @Override
    @Transactional
    public ContentResponse update(Long id, ContentRequest request) {
        Content content = findOrThrow(id);
        applyRequest(content, request);
        return ContentMapper.toResponse(contentRepository.save(content));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Content content = findOrThrow(id);
        contentRepository.delete(content);
        activityLogService.log(currentUserProvider.getCurrentUser(), "CONTENT_DELETED", "Content", id,
                "Se eliminó el contenido \"" + content.getTitle() + "\"");
    }

    private void applyRequest(Content content, ContentRequest request) {
        Platform platform = platformRepository.findById(request.platformId())
                .orElseThrow(() -> ResourceNotFoundException.of("Plataforma", request.platformId()));

        content.setTitle(request.title());
        content.setCopyText(request.copyText());
        content.setPlatform(platform);
        content.setObjective(request.objective());
        content.setStatus(request.status());
        content.setScheduledDate(request.scheduledDate());
        content.setHashtags(request.hashtags());
        content.setCtaText(request.ctaText());

        if (request.status() == ContentStatus.PUBLISHED && content.getPublishedDate() == null) {
            content.setPublishedDate(LocalDate.now());
        }

        if (request.campaignId() != null) {
            Campaign campaign = campaignRepository.findById(request.campaignId())
                    .orElseThrow(() -> ResourceNotFoundException.of("Campaña", request.campaignId()));
            content.setCampaign(campaign);
        } else {
            content.setCampaign(null);
        }
    }

    private Content findOrThrow(Long id) {
        return contentRepository.findById(id).orElseThrow(() -> ResourceNotFoundException.of("Contenido", id));
    }
}
