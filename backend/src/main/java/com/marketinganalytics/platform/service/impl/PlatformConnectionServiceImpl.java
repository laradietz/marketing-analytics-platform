package com.marketinganalytics.platform.service.impl;

import com.marketinganalytics.platform.dto.integration.ManualConnectRequest;
import com.marketinganalytics.platform.dto.integration.PlatformConnectionResponse;
import com.marketinganalytics.platform.dto.integration.SyncResult;
import com.marketinganalytics.platform.dto.metric.CampaignMetricRequest;
import com.marketinganalytics.platform.entity.Campaign;
import com.marketinganalytics.platform.entity.Platform;
import com.marketinganalytics.platform.entity.PlatformConnection;
import com.marketinganalytics.platform.entity.enums.ConnectionStatus;
import com.marketinganalytics.platform.entity.enums.IntegrationProvider;
import com.marketinganalytics.platform.exception.BadRequestException;
import com.marketinganalytics.platform.exception.ResourceNotFoundException;
import com.marketinganalytics.platform.repository.CampaignRepository;
import com.marketinganalytics.platform.repository.PlatformConnectionRepository;
import com.marketinganalytics.platform.repository.PlatformRepository;
import com.marketinganalytics.platform.security.CurrentUserProvider;
import com.marketinganalytics.platform.service.ActivityLogService;
import com.marketinganalytics.platform.service.CampaignMetricService;
import com.marketinganalytics.platform.service.PlatformConnectionService;
import com.marketinganalytics.platform.service.integration.CredentialCipher;
import com.marketinganalytics.platform.service.integration.ExternalInsight;
import com.marketinganalytics.platform.service.integration.OAuthStateStore;
import com.marketinganalytics.platform.service.integration.OAuthTokenResult;
import com.marketinganalytics.platform.service.integration.PlatformSyncProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PlatformConnectionServiceImpl implements PlatformConnectionService {

    /**
     * Which real ad platform each of our seeded {@link Platform} rows maps to.
     * LinkedIn has no adapter yet — see {@link PlatformSyncProvider} javadoc
     * for how to add one; until then it simply doesn't appear as connectable.
     */
    private static final Map<String, IntegrationProvider> PLATFORM_PROVIDERS = Map.of(
            "facebook", IntegrationProvider.META_ADS,
            "instagram", IntegrationProvider.META_ADS,
            "google-ads", IntegrationProvider.GOOGLE_ADS,
            "tiktok", IntegrationProvider.TIKTOK_ADS
    );

    private final PlatformRepository platformRepository;
    private final PlatformConnectionRepository platformConnectionRepository;
    private final CampaignRepository campaignRepository;
    private final CampaignMetricService campaignMetricService;
    private final CredentialCipher credentialCipher;
    private final OAuthStateStore oAuthStateStore;
    private final ActivityLogService activityLogService;
    private final CurrentUserProvider currentUserProvider;
    private final List<PlatformSyncProvider> syncProviders;

    private Map<IntegrationProvider, PlatformSyncProvider> providersByType;

    private Map<IntegrationProvider, PlatformSyncProvider> providers() {
        if (providersByType == null) {
            providersByType = new EnumMap<>(IntegrationProvider.class);
            syncProviders.forEach(p -> providersByType.put(p.getProvider(), p));
        }
        return providersByType;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PlatformConnectionResponse> listConnections() {
        List<PlatformConnectionResponse> result = new ArrayList<>();
        for (Platform platform : platformRepository.findByActiveTrueOrderByNameAsc()) {
            IntegrationProvider provider = PLATFORM_PROVIDERS.get(platform.getSlug());
            if (provider == null) {
                continue;
            }
            PlatformConnection connection = platformConnectionRepository.findByPlatformId(platform.getId()).orElse(null);
            boolean configured = providers().get(provider) != null && providers().get(provider).isConfigured();

            result.add(new PlatformConnectionResponse(
                    platform.getId(), platform.getName(), platform.getColorHex(), provider,
                    connection != null ? connection.getStatus() : ConnectionStatus.DISCONNECTED,
                    connection != null ? connection.getExternalAccountId() : null,
                    connection != null ? connection.getLastSyncedAt() : null,
                    connection != null ? connection.getLastSyncMessage() : null,
                    configured
            ));
        }
        return result;
    }

    @Override
    public String getAuthorizationUrl(Long platformId) {
        PlatformSyncProvider provider = resolveProvider(platformId);
        String state = oAuthStateStore.createState(platformId);
        return provider.buildAuthorizationUrl(state, provider.getRedirectUri());
    }

    @Override
    @Transactional
    public void handleOAuthCallback(String state, String code) {
        Long platformId = oAuthStateStore.consume(state);
        if (platformId == null) {
            throw new BadRequestException("La solicitud de conexión expiró o no es válida. Intentá de nuevo.");
        }

        Platform platform = platformRepository.findById(platformId)
                .orElseThrow(() -> ResourceNotFoundException.of("Plataforma", platformId));
        PlatformSyncProvider provider = resolveProvider(platformId);

        OAuthTokenResult tokens = provider.exchangeCodeForToken(code, provider.getRedirectUri());

        PlatformConnection connection = platformConnectionRepository.findByPlatformId(platformId)
                .orElseGet(() -> PlatformConnection.builder().platform(platform).provider(provider.getProvider()).build());

        connection.setProvider(provider.getProvider());
        connection.setStatus(ConnectionStatus.CONNECTED);
        connection.setAccessTokenEncrypted(credentialCipher.encrypt(tokens.accessToken()));
        if (tokens.refreshToken() != null) {
            connection.setRefreshTokenEncrypted(credentialCipher.encrypt(tokens.refreshToken()));
        }
        connection.setTokenExpiresAt(tokens.expiresInSeconds() != null ? Instant.now().plusSeconds(tokens.expiresInSeconds()) : null);
        if (tokens.externalAccountId() != null) {
            connection.setExternalAccountId(tokens.externalAccountId());
        }

        platformConnectionRepository.save(connection);

        // The OAuth provider redirects the browser here directly, with no
        // Authorization header, so there is no authenticated user in context.
        activityLogService.log(null, "PLATFORM_CONNECTED", "Platform", platformId,
                "Se conectó " + platform.getName() + " (" + provider.getProvider() + ")");
    }

    @Override
    @Transactional
    public PlatformConnectionResponse connectManually(Long platformId, ManualConnectRequest request) {
        Platform platform = platformRepository.findById(platformId)
                .orElseThrow(() -> ResourceNotFoundException.of("Plataforma", platformId));
        IntegrationProvider provider = requireProvider(platform);

        PlatformConnection connection = platformConnectionRepository.findByPlatformId(platformId)
                .orElseGet(() -> PlatformConnection.builder().platform(platform).provider(provider).build());

        connection.setProvider(provider);
        connection.setStatus(ConnectionStatus.CONNECTED);
        connection.setAccessTokenEncrypted(credentialCipher.encrypt(request.accessToken()));
        connection.setRefreshTokenEncrypted(request.refreshToken() != null ? credentialCipher.encrypt(request.refreshToken()) : null);
        connection.setExternalAccountId(request.externalAccountId());
        connection.setTokenExpiresAt(null);

        connection = platformConnectionRepository.save(connection);

        activityLogService.log(currentUserProvider.getCurrentUser(), "PLATFORM_CONNECTED", "Platform", platformId,
                "Se conectó " + platform.getName() + " manualmente (" + provider + ")");

        return toResponse(platform, connection, provider);
    }

    @Override
    @Transactional
    public PlatformConnectionResponse updateExternalAccountId(Long platformId, String externalAccountId) {
        PlatformConnection connection = platformConnectionRepository.findByPlatformId(platformId)
                .orElseThrow(() -> ResourceNotFoundException.of("Conexión", platformId));
        connection.setExternalAccountId(externalAccountId);
        connection = platformConnectionRepository.save(connection);
        return toResponse(connection.getPlatform(), connection, connection.getProvider());
    }

    @Override
    @Transactional
    public void disconnect(Long platformId) {
        PlatformConnection connection = platformConnectionRepository.findByPlatformId(platformId).orElse(null);
        if (connection == null) {
            return;
        }
        connection.setStatus(ConnectionStatus.DISCONNECTED);
        connection.setAccessTokenEncrypted(null);
        connection.setRefreshTokenEncrypted(null);
        connection.setTokenExpiresAt(null);
        platformConnectionRepository.save(connection);

        activityLogService.log(currentUserProvider.getCurrentUser(), "PLATFORM_DISCONNECTED", "Platform", platformId,
                "Se desconectó " + connection.getPlatform().getName());
    }

    @Override
    @Transactional
    public SyncResult sync(Long platformId, LocalDate from, LocalDate to) {
        PlatformConnection connection = platformConnectionRepository.findByPlatformId(platformId)
                .orElseThrow(() -> new BadRequestException("Esta plataforma todavía no está conectada"));
        if (connection.getStatus() != ConnectionStatus.CONNECTED) {
            throw new BadRequestException("Esta plataforma todavía no está conectada");
        }

        PlatformSyncProvider provider = providers().get(connection.getProvider());
        String accessToken = credentialCipher.decrypt(connection.getAccessTokenEncrypted());

        List<Campaign> linkedCampaigns = campaignRepository.findByPlatformIdAndExternalCampaignIdIsNotNull(platformId);
        List<String> errors = new ArrayList<>();
        int metricsImported = 0;
        int campaignsSynced = 0;

        for (Campaign campaign : linkedCampaigns) {
            try {
                List<ExternalInsight> insights = provider.fetchInsights(
                        accessToken, connection.getExternalAccountId(), campaign.getExternalCampaignId(), from, to);
                for (ExternalInsight insight : insights) {
                    campaignMetricService.record(campaign.getId(), toMetricRequest(insight));
                    metricsImported++;
                }
                campaignsSynced++;
            } catch (Exception ex) {
                errors.add(campaign.getName() + ": " + ex.getMessage());
            }
        }

        connection.setLastSyncedAt(Instant.now());
        connection.setLastSyncMessage(errors.isEmpty()
                ? "Sincronizadas " + campaignsSynced + " campañas (" + metricsImported + " métricas)"
                : "Completado con errores: " + errors.size() + " campaña(s) fallaron");
        connection.setStatus(errors.size() == linkedCampaigns.size() && !linkedCampaigns.isEmpty()
                ? ConnectionStatus.ERROR : ConnectionStatus.CONNECTED);
        platformConnectionRepository.save(connection);

        activityLogService.log(currentUserProvider.getCurrentUser(), "PLATFORM_SYNCED", "Platform", platformId,
                connection.getLastSyncMessage());

        return new SyncResult(campaignsSynced, metricsImported, errors);
    }

    private CampaignMetricRequest toMetricRequest(ExternalInsight insight) {
        return new CampaignMetricRequest(
                insight.date(), insight.impressions(), 0L, insight.clicks(), insight.conversions(),
                insight.spend(), insight.revenue(), 0L, 0L, 0L, 0L);
    }

    private PlatformSyncProvider resolveProvider(Long platformId) {
        Platform platform = platformRepository.findById(platformId)
                .orElseThrow(() -> ResourceNotFoundException.of("Plataforma", platformId));
        IntegrationProvider providerType = requireProvider(platform);
        PlatformSyncProvider provider = providers().get(providerType);
        if (provider == null || !provider.isConfigured()) {
            throw new BadRequestException(
                    platform.getName() + " no tiene credenciales configuradas todavía. Definí las variables de entorno correspondientes.");
        }
        return provider;
    }

    private IntegrationProvider requireProvider(Platform platform) {
        IntegrationProvider provider = PLATFORM_PROVIDERS.get(platform.getSlug());
        if (provider == null) {
            throw new BadRequestException(platform.getName() + " todavía no tiene una integración disponible");
        }
        return provider;
    }

    private PlatformConnectionResponse toResponse(Platform platform, PlatformConnection connection, IntegrationProvider provider) {
        boolean configured = providers().get(provider) != null && providers().get(provider).isConfigured();
        return new PlatformConnectionResponse(
                platform.getId(), platform.getName(), platform.getColorHex(), provider,
                connection.getStatus(), connection.getExternalAccountId(),
                connection.getLastSyncedAt(), connection.getLastSyncMessage(), configured);
    }
}
