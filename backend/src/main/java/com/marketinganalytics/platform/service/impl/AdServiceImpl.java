package com.marketinganalytics.platform.service.impl;

import com.marketinganalytics.platform.dto.ad.AdRequest;
import com.marketinganalytics.platform.dto.ad.AdResponse;
import com.marketinganalytics.platform.entity.Ad;
import com.marketinganalytics.platform.entity.Campaign;
import com.marketinganalytics.platform.entity.enums.AdStatus;
import com.marketinganalytics.platform.exception.BadRequestException;
import com.marketinganalytics.platform.exception.ResourceNotFoundException;
import com.marketinganalytics.platform.mapper.AdMapper;
import com.marketinganalytics.platform.repository.AdRepository;
import com.marketinganalytics.platform.repository.CampaignRepository;
import com.marketinganalytics.platform.service.AdService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdServiceImpl implements AdService {

    private final AdRepository adRepository;
    private final CampaignRepository campaignRepository;

    @Override
    @Transactional(readOnly = true)
    public List<AdResponse> listByCampaign(Long campaignId) {
        return adRepository.findByCampaignIdOrderByCreatedAtDesc(campaignId).stream()
                .map(AdMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public AdResponse create(Long campaignId, AdRequest request) {
        Campaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> ResourceNotFoundException.of("Campaña", campaignId));

        Ad ad = Ad.builder()
                .campaign(campaign)
                .name(request.name())
                .format(request.format())
                .headline(request.headline())
                .body(request.body())
                .ctaLabel(request.ctaLabel())
                .status(request.status() != null ? request.status() : AdStatus.ACTIVE)
                .build();

        return AdMapper.toResponse(adRepository.save(ad));
    }

    @Override
    @Transactional
    public AdResponse update(Long campaignId, Long adId, AdRequest request) {
        Ad ad = findOrThrow(campaignId, adId);
        ad.setName(request.name());
        ad.setFormat(request.format());
        ad.setHeadline(request.headline());
        ad.setBody(request.body());
        ad.setCtaLabel(request.ctaLabel());
        if (request.status() != null) {
            ad.setStatus(request.status());
        }
        return AdMapper.toResponse(adRepository.save(ad));
    }

    @Override
    @Transactional
    public void delete(Long campaignId, Long adId) {
        Ad ad = findOrThrow(campaignId, adId);
        adRepository.delete(ad);
    }

    private Ad findOrThrow(Long campaignId, Long adId) {
        Ad ad = adRepository.findById(adId).orElseThrow(() -> ResourceNotFoundException.of("Ad", adId));
        if (!ad.getCampaign().getId().equals(campaignId)) {
            throw new BadRequestException("El aviso no pertenece a la campaña indicada");
        }
        return ad;
    }
}
