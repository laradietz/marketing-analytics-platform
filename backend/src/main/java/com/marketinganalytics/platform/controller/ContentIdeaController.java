package com.marketinganalytics.platform.controller;

import com.marketinganalytics.platform.dto.common.PageResponse;
import com.marketinganalytics.platform.dto.content.ContentGenerationRequest;
import com.marketinganalytics.platform.dto.content.ContentIdeaResponse;
import com.marketinganalytics.platform.dto.content.ContentIdeaUpdateRequest;
import com.marketinganalytics.platform.service.ContentIdeaService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/content-ideas")
@RequiredArgsConstructor
@Tag(name = "Generador de contenido (IA)")
@SecurityRequirement(name = "bearerAuth")
public class ContentIdeaController {

    private final ContentIdeaService contentIdeaService;

    @PostMapping("/generate")
    public ResponseEntity<List<ContentIdeaResponse>> generate(@Valid @RequestBody ContentGenerationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(contentIdeaService.generate(request));
    }

    @GetMapping
    public PageResponse<ContentIdeaResponse> list(@PageableDefault(size = 12, sort = "createdAt") Pageable pageable) {
        return contentIdeaService.list(pageable);
    }

    @PutMapping("/{id}")
    public ContentIdeaResponse update(@PathVariable Long id, @Valid @RequestBody ContentIdeaUpdateRequest request) {
        return contentIdeaService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        contentIdeaService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
