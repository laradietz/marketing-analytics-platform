package com.marketinganalytics.platform.controller;

import com.marketinganalytics.platform.dto.integration.ManualConnectRequest;
import com.marketinganalytics.platform.dto.integration.PlatformConnectionResponse;
import com.marketinganalytics.platform.dto.integration.SyncRequest;
import com.marketinganalytics.platform.dto.integration.SyncResult;
import com.marketinganalytics.platform.service.PlatformConnectionService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/integrations")
@RequiredArgsConstructor
@Tag(name = "Integraciones")
public class IntegrationController {

    private final PlatformConnectionService platformConnectionService;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @GetMapping
    @SecurityRequirement(name = "bearerAuth")
    public List<PlatformConnectionResponse> list() {
        return platformConnectionService.listConnections();
    }

    @GetMapping("/{platformId}/authorize-url")
    @SecurityRequirement(name = "bearerAuth")
    public Map<String, String> authorizeUrl(@PathVariable Long platformId) {
        return Map.of("url", platformConnectionService.getAuthorizationUrl(platformId));
    }

    /**
     * Reached by the browser being redirected from the ad platform's own
     * consent screen — never called by our frontend directly, so it cannot
     * require a bearer token (see SecurityConfig permitAll for this path).
     */
    @GetMapping("/callback")
    public ResponseEntity<Void> callback(@RequestParam String state, @RequestParam(required = false) String code,
                                          @RequestParam(required = false) String error) {
        String redirectTarget = frontendUrl + "/settings/integrations";
        try {
            if (error != null) {
                redirectTarget += "?error=" + encode(error);
            } else {
                platformConnectionService.handleOAuthCallback(state, code);
                redirectTarget += "?connected=true";
            }
        } catch (Exception ex) {
            redirectTarget += "?error=" + encode(ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(redirectTarget)).build();
    }

    private String encode(String value) {
        return java.net.URLEncoder.encode(value != null ? value : "unknown_error", java.nio.charset.StandardCharsets.UTF_8);
    }

    @PostMapping("/{platformId}/connect-manual")
    @SecurityRequirement(name = "bearerAuth")
    public PlatformConnectionResponse connectManually(@PathVariable Long platformId, @Valid @RequestBody ManualConnectRequest request) {
        return platformConnectionService.connectManually(platformId, request);
    }

    @PatchMapping("/{platformId}/external-account-id")
    @SecurityRequirement(name = "bearerAuth")
    public PlatformConnectionResponse updateExternalAccountId(@PathVariable Long platformId, @RequestBody Map<String, String> body) {
        return platformConnectionService.updateExternalAccountId(platformId, body.get("externalAccountId"));
    }

    @PostMapping("/{platformId}/sync")
    @SecurityRequirement(name = "bearerAuth")
    public SyncResult sync(@PathVariable Long platformId, @Valid @RequestBody SyncRequest request) {
        return platformConnectionService.sync(platformId, request.from(), request.to());
    }

    @DeleteMapping("/{platformId}")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Void> disconnect(@PathVariable Long platformId) {
        platformConnectionService.disconnect(platformId);
        return ResponseEntity.noContent().build();
    }
}
