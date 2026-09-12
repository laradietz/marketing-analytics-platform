package com.marketinganalytics.platform.controller;

import com.marketinganalytics.platform.dto.common.PageResponse;
import com.marketinganalytics.platform.dto.report.ReportGenerateRequest;
import com.marketinganalytics.platform.dto.report.ReportResponse;
import com.marketinganalytics.platform.service.ReportService;
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

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@Tag(name = "Reportes")
@SecurityRequirement(name = "bearerAuth")
public class ReportController {

    private final ReportService reportService;

    @PostMapping
    public ResponseEntity<ReportResponse> generate(@Valid @RequestBody ReportGenerateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(reportService.generate(request));
    }

    @GetMapping
    public PageResponse<ReportResponse> list(@PageableDefault(size = 10, sort = "createdAt") Pageable pageable) {
        return reportService.list(pageable);
    }

    @GetMapping("/{id}")
    public ReportResponse getById(@PathVariable Long id) {
        return reportService.getById(id);
    }

    @GetMapping("/{id}/export")
    public ResponseEntity<byte[]> exportCsv(@PathVariable Long id) {
        byte[] csv = reportService.exportCsv(id);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("text/csv"))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename("reporte-" + id + ".csv").build().toString())
                .body(csv);
    }
}
