package com.wedu.proposal.controller;

import com.wedu.global.response.ApiResponse;
import com.wedu.proposal.domain.ProposalItemCategory;
import com.wedu.proposal.dto.ProposalOptionRequest;
import com.wedu.proposal.dto.ProposalResponse;
import com.wedu.proposal.service.ProposalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 인증 사용자의 "나만의 프로포즈 만들기" HTTP 요청을 처리한다. */
@Tag(name = "Proposal", description = "나만의 프로포즈 만들기")
@RestController
@RequestMapping("/api/proposals")
@RequiredArgsConstructor
public class ProposalController {

    private final ProposalService proposalService;

    /** 카테고리 옵션을 선택하거나 교체한다. */
    @Operation(summary = "프로포즈 옵션 선택/변경")
    @PostMapping("/options")
    public ApiResponse<ProposalResponse> selectOption(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody ProposalOptionRequest request) {
        return ApiResponse.ok(proposalService.selectOption(userId, request));
    }

    /** 선택했던 카테고리 옵션을 취소한다. */
    @Operation(summary = "프로포즈 옵션 선택 취소")
    @DeleteMapping("/options/{category}")
    public ApiResponse<ProposalResponse> removeOption(
            @AuthenticationPrincipal Long userId, @PathVariable ProposalItemCategory category) {
        return ApiResponse.ok(proposalService.removeOption(userId, category));
    }

    /** 내가 구성 중인 프로포즈의 선택 현황과 예상 견적 합계를 조회한다. */
    @Operation(summary = "내 프로포즈 선택 현황/견적 조회")
    @GetMapping("/me")
    public ApiResponse<ProposalResponse> getMyProposal(@AuthenticationPrincipal Long userId) {
        return ApiResponse.ok(proposalService.getMyProposal(userId));
    }
}
