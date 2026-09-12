package com.marketinganalytics.platform.controller;

import com.marketinganalytics.platform.dto.comment.*;
import com.marketinganalytics.platform.dto.common.PageResponse;
import com.marketinganalytics.platform.dto.content.ContentIdeaResponse;
import com.marketinganalytics.platform.entity.enums.CommentSentiment;
import com.marketinganalytics.platform.service.AudienceCommentService;
import com.marketinganalytics.platform.service.impl.CommentCsvParser;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
@Tag(name = "Comentarios de audiencia")
@SecurityRequirement(name = "bearerAuth")
public class AudienceCommentController {

    private final AudienceCommentService audienceCommentService;

    @GetMapping
    public PageResponse<CommentResponse> list(
            @RequestParam(required = false) Long platformId,
            @RequestParam(required = false) CommentSentiment sentiment,
            @RequestParam(required = false) String search,
            @PageableDefault(size = 15, sort = "createdAt") Pageable pageable) {
        return audienceCommentService.list(new CommentFilter(platformId, sentiment, search), pageable);
    }

    @PostMapping
    public ResponseEntity<CommentResponse> create(@Valid @RequestBody CommentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(audienceCommentService.create(request));
    }

    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public CommentImportResult importCsv(@RequestParam("file") MultipartFile file) {
        return audienceCommentService.importCsv(file);
    }

    @GetMapping("/import/template")
    public ResponseEntity<byte[]> downloadTemplate() {
        byte[] csv = CommentCsvParser.template().getBytes(StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("text/csv"))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename("plantilla-comentarios.csv").build().toString())
                .body(csv);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        audienceCommentService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/summary")
    public CommentPlatformSummary summary(@RequestParam Long platformId) {
        return audienceCommentService.summarize(platformId);
    }

    @PostMapping("/generate-ideas")
    public List<ContentIdeaResponse> generateIdeas(@Valid @RequestBody GenerateIdeasFromCommentsRequest request) {
        return audienceCommentService.generateIdeasFromComments(request);
    }
}
