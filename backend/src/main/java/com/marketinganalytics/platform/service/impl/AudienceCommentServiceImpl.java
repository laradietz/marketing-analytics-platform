package com.marketinganalytics.platform.service.impl;

import com.marketinganalytics.platform.dto.comment.*;
import com.marketinganalytics.platform.dto.common.PageResponse;
import com.marketinganalytics.platform.dto.content.ContentGenerationRequest;
import com.marketinganalytics.platform.dto.content.ContentIdeaResponse;
import com.marketinganalytics.platform.entity.AudienceComment;
import com.marketinganalytics.platform.entity.Content;
import com.marketinganalytics.platform.entity.Platform;
import com.marketinganalytics.platform.entity.enums.CommentSentiment;
import com.marketinganalytics.platform.entity.enums.CommentSource;
import com.marketinganalytics.platform.exception.BadRequestException;
import com.marketinganalytics.platform.exception.ResourceNotFoundException;
import com.marketinganalytics.platform.mapper.AudienceCommentMapper;
import com.marketinganalytics.platform.repository.AudienceCommentRepository;
import com.marketinganalytics.platform.repository.ContentRepository;
import com.marketinganalytics.platform.repository.PlatformRepository;
import com.marketinganalytics.platform.repository.spec.AudienceCommentSpecifications;
import com.marketinganalytics.platform.security.CurrentUserProvider;
import com.marketinganalytics.platform.service.ActivityLogService;
import com.marketinganalytics.platform.service.AudienceCommentService;
import com.marketinganalytics.platform.service.ContentIdeaService;
import com.marketinganalytics.platform.service.sentiment.SentimentClassifier;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AudienceCommentServiceImpl implements AudienceCommentService {

    private static final int TOP_THEMES_LIMIT = 8;

    private final AudienceCommentRepository audienceCommentRepository;
    private final PlatformRepository platformRepository;
    private final ContentRepository contentRepository;
    private final SentimentClassifier sentimentClassifier;
    private final ContentIdeaService contentIdeaService;
    private final ActivityLogService activityLogService;
    private final CurrentUserProvider currentUserProvider;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CommentResponse> list(CommentFilter filter, Pageable pageable) {
        return PageResponse.from(audienceCommentRepository.findAll(AudienceCommentSpecifications.fromFilter(filter), pageable)
                .map(AudienceCommentMapper::toResponse));
    }

    @Override
    @Transactional
    public CommentResponse create(CommentRequest request) {
        Platform platform = platformRepository.findById(request.platformId())
                .orElseThrow(() -> ResourceNotFoundException.of("Plataforma", request.platformId()));

        Content content = null;
        if (request.contentId() != null) {
            content = contentRepository.findById(request.contentId())
                    .orElseThrow(() -> ResourceNotFoundException.of("Contenido", request.contentId()));
        }

        AudienceComment comment = AudienceComment.builder()
                .platform(platform)
                .content(content)
                .authorName(request.authorName())
                .text(request.text())
                .sentiment(sentimentClassifier.classify(request.text()))
                .source(CommentSource.MANUAL)
                .postedAt(request.postedAt())
                .build();

        comment = audienceCommentRepository.save(comment);

        activityLogService.log(currentUserProvider.getCurrentUser(), "COMMENT_ADDED", "AudienceComment", comment.getId(),
                "Se registró un comentario en " + platform.getName());

        return AudienceCommentMapper.toResponse(comment);
    }

    @Override
    @Transactional
    public CommentImportResult importCsv(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("El archivo está vacío");
        }

        String content;
        try {
            content = new String(file.getBytes(), StandardCharsets.UTF_8);
        } catch (IOException ex) {
            throw new BadRequestException("No se pudo leer el archivo");
        }

        CommentCsvParser.ParseOutcome outcome = CommentCsvParser.parse(content);
        List<CommentImportResult.RowError> errors = new ArrayList<>(outcome.errors());

        Map<String, Platform> platformBySlug = platformRepository.findAll().stream()
                .collect(java.util.stream.Collectors.toMap(Platform::getSlug, p -> p));

        int imported = 0;
        int rowNumber = 1;
        for (CommentCsvParser.ParsedRow row : outcome.rows()) {
            rowNumber++;
            Platform platform = platformBySlug.get(row.platformSlug());
            if (platform == null) {
                errors.add(new CommentImportResult.RowError(rowNumber, "Plataforma desconocida: \"" + row.platformSlug() + "\""));
                continue;
            }

            AudienceComment comment = AudienceComment.builder()
                    .platform(platform)
                    .authorName(row.author())
                    .text(row.text())
                    .sentiment(sentimentClassifier.classify(row.text()))
                    .source(CommentSource.IMPORTED)
                    .postedAt(row.postedAt())
                    .build();
            audienceCommentRepository.save(comment);
            imported++;
        }

        activityLogService.log(currentUserProvider.getCurrentUser(), "COMMENTS_IMPORTED", "AudienceComment", null,
                "Se importaron " + imported + " comentarios desde CSV");

        return new CommentImportResult(outcome.totalRows(), imported, errors);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!audienceCommentRepository.existsById(id)) {
            throw ResourceNotFoundException.of("Comentario", id);
        }
        audienceCommentRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public CommentPlatformSummary summarize(Long platformId) {
        List<AudienceComment> comments = audienceCommentRepository.findByPlatformIdOrderByCreatedAtDesc(platformId);

        long positive = comments.stream().filter(c -> c.getSentiment() == CommentSentiment.POSITIVE).count();
        long negative = comments.stream().filter(c -> c.getSentiment() == CommentSentiment.NEGATIVE).count();
        long neutral = comments.size() - positive - negative;

        List<ThemeSummary> themes = ThemeAnalyzer.topThemes(comments, TOP_THEMES_LIMIT);

        return new CommentPlatformSummary(comments.size(), positive, neutral, negative, themes);
    }

    @Override
    @Transactional
    public List<ContentIdeaResponse> generateIdeasFromComments(GenerateIdeasFromCommentsRequest request) {
        Platform platform = platformRepository.findById(request.platformId())
                .orElseThrow(() -> ResourceNotFoundException.of("Plataforma", request.platformId()));

        List<AudienceComment> comments = audienceCommentRepository.findByPlatformIdOrderByCreatedAtDesc(request.platformId());
        if (comments.isEmpty()) {
            throw new BadRequestException("Todavía no hay comentarios cargados para " + platform.getName());
        }

        List<ThemeSummary> themes = ThemeAnalyzer.topThemes(comments, 5);
        if (themes.isEmpty()) {
            throw new BadRequestException("No se encontraron temas recurrentes en los comentarios de " + platform.getName());
        }

        long positive = comments.stream().filter(c -> c.getSentiment() == CommentSentiment.POSITIVE).count();
        long negative = comments.stream().filter(c -> c.getSentiment() == CommentSentiment.NEGATIVE).count();

        String keywordList = themes.stream().map(ThemeSummary::keyword).reduce((a, b) -> a + ", " + b).orElse("");
        String sentimentNote = negative > positive
                ? "sobre todo reclamos y puntos de fricción que conviene abordar directamente en el mensaje"
                : "en su mayoría comentarios favorables que conviene reforzar y agradecer";
        String syntheticAudience = truncate(
                "Personas que comentan en " + platform.getName() + " sobre " + keywordList + " — " + sentimentNote, 300);

        ContentGenerationRequest generationRequest = new ContentGenerationRequest(
                request.productOrService(),
                syntheticAudience,
                request.platformId(),
                request.objective(),
                request.tone(),
                null,
                request.variantCount()
        );

        activityLogService.log(currentUserProvider.getCurrentUser(), "IDEAS_FROM_COMMENTS_GENERATED", "Platform", request.platformId(),
                "Se generaron ideas de contenido a partir de comentarios de " + platform.getName());

        return contentIdeaService.generate(generationRequest);
    }

    private String truncate(String value, int maxLength) {
        if (value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength - 3).stripTrailing() + "...";
    }
}
