package com.wedu.proposal.service;

import com.wedu.global.error.BusinessException;
import com.wedu.global.error.ErrorCode;
import com.wedu.product.dto.ProductDetailResponse;
import com.wedu.product.service.ProductService;
import com.wedu.proposal.domain.Proposal;
import com.wedu.proposal.domain.ProposalItemCategory;
import com.wedu.proposal.dto.ProposalOptionRequest;
import com.wedu.proposal.dto.ProposalResponse;
import com.wedu.proposal.repository.ProposalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 나만의 프로포즈(옵션 선택·견적 계산) 유스케이스를 처리한다. */
@Service
@RequiredArgsConstructor
public class ProposalService {

    private final ProposalRepository proposalRepository;
    private final ProductService productService;

    /** 카테고리 옵션을 선택한다. 진행 중인 프로포즈가 없으면 새로 시작한다. 표시용 이름/가격은 상품 상세 조회 결과를 그대로 스냅샷한다. */
    @Transactional
    public ProposalResponse selectOption(Long userId, ProposalOptionRequest request) {
        ProductDetailResponse product = productService.getDetail(request.productId());
        Proposal proposal = findOrCreate(userId);
        proposal.selectOption(request.category(), product.id(), product.name(), product.price());
        return ProposalResponse.from(proposal);
    }

    /** 선택했던 카테고리 옵션을 취소한다. */
    @Transactional
    public ProposalResponse removeOption(Long userId, ProposalItemCategory category) {
        Proposal proposal = findByUserIdOrThrow(userId);
        proposal.removeOption(category);
        return ProposalResponse.from(proposal);
    }

    /** 내가 구성 중인 프로포즈의 선택 현황과 예상 견적 합계를 조회한다. */
    @Transactional(readOnly = true)
    public ProposalResponse getMyProposal(Long userId) {
        return ProposalResponse.from(findByUserIdOrThrow(userId));
    }

    private Proposal findOrCreate(Long userId) {
        return proposalRepository.findByUserId(userId).orElseGet(() -> createNew(userId));
    }

    private Proposal createNew(Long userId) {
        try {
            return proposalRepository.saveAndFlush(Proposal.start(userId));
        } catch (DataIntegrityViolationException e) {
            return findByUserIdOrThrow(userId);
        }
    }

    private Proposal findByUserIdOrThrow(Long userId) {
        return proposalRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROPOSAL_NOT_FOUND));
    }
}
