package com.wedu.proposal.repository;

import com.wedu.proposal.domain.Proposal;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProposalRepository extends JpaRepository<Proposal, Long> {

    Optional<Proposal> findByUserId(Long userId);
}
