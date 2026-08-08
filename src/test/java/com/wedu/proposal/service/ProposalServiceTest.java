package com.wedu.proposal.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.wedu.global.error.BusinessException;
import com.wedu.global.error.ErrorCode;
import com.wedu.product.dto.ProductDetailResponse;
import com.wedu.product.service.ProductService;
import com.wedu.proposal.domain.Proposal;
import com.wedu.proposal.domain.ProposalItemCategory;
import com.wedu.proposal.dto.ProposalOptionRequest;
import com.wedu.proposal.dto.ProposalResponse;
import com.wedu.proposal.repository.ProposalRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProposalServiceTest {

    @Mock
    private ProposalRepository proposalRepository;

    @Mock
    private ProductService productService;

    private ProposalService proposalService;

    @BeforeEach
    void setUp() {
        proposalService = new ProposalService(proposalRepository, productService);
    }

    @Test
    @DisplayName("진행 중인 프로포즈가 없으면 새로 시작하며 상품 상세 정보로 옵션을 선택한다")
    void selectOptionStartsNewProposal() {
        when(productService.getDetail(10L))
                .thenReturn(new ProductDetailResponse(10L, "커플링", null, 150_000, "업체", null, null));
        when(proposalRepository.findByUserId(1L)).thenReturn(Optional.empty());
        when(proposalRepository.saveAndFlush(any(Proposal.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        ProposalOptionRequest request = new ProposalOptionRequest(ProposalItemCategory.RING, 10L);

        ProposalResponse response = proposalService.selectOption(1L, request);

        assertThat(response.items()).hasSize(1);
        assertThat(response.items().get(0).name()).isEqualTo("커플링");
        assertThat(response.totalPrice()).isEqualTo(150_000);
    }

    @Test
    @DisplayName("이미 진행 중인 프로포즈에 옵션을 추가한다")
    void selectOptionOnExistingProposal() {
        Proposal proposal = Proposal.start(1L);
        when(productService.getDetail(20L))
                .thenReturn(new ProductDetailResponse(20L, "스냅 촬영", null, 300_000, "업체", null, null));
        when(proposalRepository.findByUserId(1L)).thenReturn(Optional.of(proposal));
        ProposalOptionRequest request = new ProposalOptionRequest(ProposalItemCategory.PHOTO, 20L);

        ProposalResponse response = proposalService.selectOption(1L, request);

        assertThat(response.items()).hasSize(1);
        assertThat(response.totalPrice()).isEqualTo(300_000);
    }

    @Test
    @DisplayName("같은 카테고리를 다시 선택하면 최신 상품 정보로 교체된다")
    void selectOptionReplacesSameCategory() {
        Proposal proposal = Proposal.start(1L);
        when(productService.getDetail(10L))
                .thenReturn(new ProductDetailResponse(10L, "커플링", null, 150_000, "업체", null, null));
        when(productService.getDetail(11L))
                .thenReturn(new ProductDetailResponse(11L, "다이아 반지", null, 500_000, "업체", null, null));
        when(proposalRepository.findByUserId(1L)).thenReturn(Optional.of(proposal));
        proposalService.selectOption(1L, new ProposalOptionRequest(ProposalItemCategory.RING, 10L));

        ProposalResponse response =
                proposalService.selectOption(1L, new ProposalOptionRequest(ProposalItemCategory.RING, 11L));

        assertThat(response.items()).hasSize(1);
        assertThat(response.items().get(0).productId()).isEqualTo(11L);
        assertThat(response.items().get(0).name()).isEqualTo("다이아 반지");
        assertThat(response.totalPrice()).isEqualTo(500_000);
    }

    @Test
    @DisplayName("내 프로포즈가 없으면 조회 시 예외가 발생한다")
    void rejectGetMyProposalWhenMissing() {
        when(proposalRepository.findByUserId(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> proposalService.getMyProposal(1L))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.PROPOSAL_NOT_FOUND));
    }

    @Test
    @DisplayName("선택한 옵션을 취소한다")
    void removeOption() {
        Proposal proposal = Proposal.start(1L);
        proposal.selectOption(ProposalItemCategory.RING, 10L, "커플링", 150_000);
        when(proposalRepository.findByUserId(1L)).thenReturn(Optional.of(proposal));

        ProposalResponse response = proposalService.removeOption(1L, ProposalItemCategory.RING);

        assertThat(response.items()).isEmpty();
        assertThat(response.totalPrice()).isZero();
    }
}
