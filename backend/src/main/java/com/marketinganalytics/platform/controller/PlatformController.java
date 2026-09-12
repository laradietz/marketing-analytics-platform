package com.marketinganalytics.platform.controller;

import com.marketinganalytics.platform.dto.platform.PlatformResponse;
import com.marketinganalytics.platform.mapper.PlatformMapper;
import com.marketinganalytics.platform.repository.PlatformRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/platforms")
@RequiredArgsConstructor
@Tag(name = "Plataformas")
public class PlatformController {

    private final PlatformRepository platformRepository;

    @GetMapping
    public List<PlatformResponse> list() {
        return platformRepository.findByActiveTrueOrderByNameAsc().stream()
                .map(PlatformMapper::toResponse)
                .toList();
    }
}
