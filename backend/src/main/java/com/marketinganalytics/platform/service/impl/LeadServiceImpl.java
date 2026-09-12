package com.marketinganalytics.platform.service.impl;

import com.marketinganalytics.platform.dto.common.PageResponse;
import com.marketinganalytics.platform.dto.lead.LeadFilter;
import com.marketinganalytics.platform.dto.lead.LeadRequest;
import com.marketinganalytics.platform.dto.lead.LeadResponse;
import com.marketinganalytics.platform.dto.lead.LeadStatusUpdateRequest;
import com.marketinganalytics.platform.entity.Campaign;
import com.marketinganalytics.platform.entity.Lead;
import com.marketinganalytics.platform.entity.User;
import com.marketinganalytics.platform.exception.ResourceNotFoundException;
import com.marketinganalytics.platform.mapper.LeadMapper;
import com.marketinganalytics.platform.repository.CampaignRepository;
import com.marketinganalytics.platform.repository.LeadRepository;
import com.marketinganalytics.platform.repository.UserRepository;
import com.marketinganalytics.platform.repository.spec.LeadSpecifications;
import com.marketinganalytics.platform.security.CurrentUserProvider;
import com.marketinganalytics.platform.service.ActivityLogService;
import com.marketinganalytics.platform.service.LeadScoringService;
import com.marketinganalytics.platform.service.LeadService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LeadServiceImpl implements LeadService {

    private final LeadRepository leadRepository;
    private final CampaignRepository campaignRepository;
    private final UserRepository userRepository;
    private final ActivityLogService activityLogService;
    private final CurrentUserProvider currentUserProvider;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<LeadResponse> list(LeadFilter filter, Pageable pageable) {
        return PageResponse.from(leadRepository.findAll(LeadSpecifications.fromFilter(filter), pageable)
                .map(LeadMapper::toResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public LeadResponse getById(Long id) {
        return LeadMapper.toResponse(findOrThrow(id));
    }

    @Override
    @Transactional
    public LeadResponse create(LeadRequest request) {
        Lead lead = new Lead();
        applyRequest(lead, request);
        lead.setScore(LeadScoringService.computeScore(lead));
        lead = leadRepository.save(lead);

        activityLogService.log(currentUserProvider.getCurrentUser(), "LEAD_CREATED", "Lead", lead.getId(),
                "Nuevo lead: " + lead.getName());

        return LeadMapper.toResponse(lead);
    }

    @Override
    @Transactional
    public LeadResponse update(Long id, LeadRequest request) {
        Lead lead = findOrThrow(id);
        applyRequest(lead, request);
        lead.setScore(LeadScoringService.computeScore(lead));
        return LeadMapper.toResponse(leadRepository.save(lead));
    }

    @Override
    @Transactional
    public LeadResponse updateStatus(Long id, LeadStatusUpdateRequest request) {
        Lead lead = findOrThrow(id);
        lead.setStatus(request.status());
        lead.setScore(LeadScoringService.computeScore(lead));
        lead = leadRepository.save(lead);

        activityLogService.log(currentUserProvider.getCurrentUser(), "LEAD_STATUS_CHANGED", "Lead", lead.getId(),
                lead.getName() + " pasó a estado " + request.status());

        return LeadMapper.toResponse(lead);
    }

    @Override
    @Transactional
    public LeadResponse registerContact(Long id) {
        Lead lead = findOrThrow(id);
        lead.setContactCount(lead.getContactCount() + 1);
        lead.setScore(LeadScoringService.computeScore(lead));
        return LeadMapper.toResponse(leadRepository.save(lead));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Lead lead = findOrThrow(id);
        leadRepository.delete(lead);
        activityLogService.log(currentUserProvider.getCurrentUser(), "LEAD_DELETED", "Lead", id,
                "Se eliminó el lead " + lead.getName());
    }

    @Override
    @Transactional(readOnly = true)
    public List<LeadResponse> getRecent() {
        return leadRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(0, 5, Sort.by("createdAt").descending()))
                .stream()
                .map(LeadMapper::toResponse)
                .toList();
    }

    private void applyRequest(Lead lead, LeadRequest request) {
        lead.setName(request.name());
        lead.setEmail(request.email());
        lead.setPhone(request.phone());
        lead.setCompany(request.company());
        lead.setSource(request.source());
        lead.setNotes(request.notes());

        if (lead.getStatus() == null) {
            lead.setStatus(com.marketinganalytics.platform.entity.enums.LeadStatus.NEW);
        }

        if (request.campaignId() != null) {
            Campaign campaign = campaignRepository.findById(request.campaignId())
                    .orElseThrow(() -> ResourceNotFoundException.of("Campaña", request.campaignId()));
            lead.setCampaign(campaign);
        } else {
            lead.setCampaign(null);
        }

        if (request.assignedToId() != null) {
            User user = userRepository.findById(request.assignedToId())
                    .orElseThrow(() -> ResourceNotFoundException.of("Usuario", request.assignedToId()));
            lead.setAssignedTo(user);
        } else {
            lead.setAssignedTo(null);
        }
    }

    private Lead findOrThrow(Long id) {
        return leadRepository.findById(id).orElseThrow(() -> ResourceNotFoundException.of("Lead", id));
    }
}
