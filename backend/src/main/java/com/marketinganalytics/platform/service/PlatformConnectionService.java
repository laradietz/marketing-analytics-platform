package com.marketinganalytics.platform.service;

import com.marketinganalytics.platform.dto.integration.ManualConnectRequest;
import com.marketinganalytics.platform.dto.integration.PlatformConnectionResponse;
import com.marketinganalytics.platform.dto.integration.SyncResult;

import java.time.LocalDate;
import java.util.List;

public interface PlatformConnectionService {
    List<PlatformConnectionResponse> listConnections();
    String getAuthorizationUrl(Long platformId);
    void handleOAuthCallback(String state, String code);
    PlatformConnectionResponse connectManually(Long platformId, ManualConnectRequest request);
    PlatformConnectionResponse updateExternalAccountId(Long platformId, String externalAccountId);
    void disconnect(Long platformId);
    SyncResult sync(Long platformId, LocalDate from, LocalDate to);
}
