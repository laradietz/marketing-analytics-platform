package com.marketinganalytics.platform.service.impl;

import com.marketinganalytics.platform.dto.campaign.CampaignRequest;
import com.marketinganalytics.platform.entity.Campaign;
import com.marketinganalytics.platform.entity.Platform;
import com.marketinganalytics.platform.entity.User;
import com.marketinganalytics.platform.entity.enums.CampaignObjective;
import com.marketinganalytics.platform.entity.enums.CampaignStatus;
import com.marketinganalytics.platform.exception.BadRequestException;
import com.marketinganalytics.platform.exception.ResourceNotFoundException;
import com.marketinganalytics.platform.repository.CampaignMetricRepository;
import com.marketinganalytics.platform.repository.CampaignRepository;
import com.marketinganalytics.platform.repository.PlatformRepository;
import com.marketinganalytics.platform.repository.UserRepository;
import com.marketinganalytics.platform.security.CurrentUserProvider;
import com.marketinganalytics.platform.service.ActivityLogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CampaignServiceImplTest {

    @Mock
    private CampaignRepository campaignRepository;
    @Mock
    private PlatformRepository platformRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private CampaignMetricRepository campaignMetricRepository;
    @Mock
    private ActivityLogService activityLogService;
    @Mock
    private CurrentUserProvider currentUserProvider;

    @InjectMocks
    private CampaignServiceImpl campaignService;

    private Platform platform;
    private User user;

    @BeforeEach
    void setUp() {
        platform = Platform.builder().id(1L).name("Instagram").slug("instagram").build();
        user = User.builder().id(1L).name("Lara").build();
    }

    @Test
    void createsACampaignInDraftStatus() {
        when(platformRepository.findById(1L)).thenReturn(Optional.of(platform));
        when(currentUserProvider.getCurrentUser()).thenReturn(user);
        when(campaignRepository.save(any(Campaign.class))).thenAnswer(inv -> {
            Campaign c = inv.getArgument(0);
            c.setId(100L);
            return c;
        });
        when(campaignMetricRepository.findByCampaignIdOrderByRecordedDateAsc(100L)).thenReturn(List.of());

        var request = new CampaignRequest("Lanzamiento", "desc", 1L, CampaignObjective.SALES,
                new BigDecimal("1000"), LocalDate.now(), null, "Audiencia", null, null, null);

        var response = campaignService.create(request);

        assertThat(response.status()).isEqualTo(CampaignStatus.DRAFT);
        assertThat(response.name()).isEqualTo("Lanzamiento");
        verify(activityLogService).log(eq(user), eq("CAMPAIGN_CREATED"), eq("Campaign"), eq(100L), anyString());
    }

    @Test
    void rejectsEndDateBeforeStartDate() {
        when(platformRepository.findById(1L)).thenReturn(Optional.of(platform));

        var request = new CampaignRequest("X", null, 1L, CampaignObjective.TRAFFIC, BigDecimal.TEN,
                LocalDate.now(), LocalDate.now().minusDays(1), null, null, null, null);

        assertThatThrownBy(() -> campaignService.create(request))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void activatesADraftCampaign() {
        Campaign campaign = Campaign.builder().id(5L).name("C").platform(platform).status(CampaignStatus.DRAFT).build();
        when(campaignRepository.findById(5L)).thenReturn(Optional.of(campaign));
        when(campaignRepository.save(any(Campaign.class))).thenAnswer(inv -> inv.getArgument(0));
        when(campaignMetricRepository.findByCampaignIdOrderByRecordedDateAsc(5L)).thenReturn(List.of());
        when(currentUserProvider.getCurrentUser()).thenReturn(user);

        var response = campaignService.activate(5L);

        assertThat(response.status()).isEqualTo(CampaignStatus.ACTIVE);
    }

    @Test
    void cannotActivateACompletedCampaign() {
        Campaign campaign = Campaign.builder().id(6L).name("C").platform(platform).status(CampaignStatus.COMPLETED).build();
        when(campaignRepository.findById(6L)).thenReturn(Optional.of(campaign));

        assertThatThrownBy(() -> campaignService.activate(6L))
                .isInstanceOf(BadRequestException.class);

        verify(campaignRepository, never()).save(any());
    }

    @Test
    void throwsNotFoundForMissingCampaign() {
        when(campaignRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> campaignService.getById(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
